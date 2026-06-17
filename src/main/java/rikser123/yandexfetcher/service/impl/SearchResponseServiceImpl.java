package rikser123.yandexfetcher.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rikser123.bundle.exception.StatusChangeException;
import rikser123.bundle.repository.entity.OutboxMessageStatus;
import rikser123.bundle.service.StatusMatrix;
import rikser123.yandexfetcher.dto.request.MessageSearchResponseDto;
import rikser123.yandexfetcher.dto.response.YandexResponseXMLData;
import rikser123.yandexfetcher.mapper.SearchResponseMapper;
import rikser123.yandexfetcher.repository.SearchResponseErrorRepository;
import rikser123.yandexfetcher.repository.SearchResponseRepository;
import rikser123.yandexfetcher.repository.entity.SearchResponse;
import rikser123.yandexfetcher.repository.entity.SearchResponseMessage;
import rikser123.yandexfetcher.repository.entity.UserSearchQuery;
import rikser123.yandexfetcher.repository.entity.SearchResponseError;
import rikser123.yandexfetcher.repository.entity.SearchResponseStatus;
import rikser123.yandexfetcher.service.SearchResponseOutboxService;
import rikser123.yandexfetcher.service.SearchResponseService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchResponseServiceImpl implements SearchResponseService {
  private final SearchResponseMapper searchResponseMapper;
  private final SearchResponseRepository searchResponseRepository;
  private final StatusMatrix<SearchResponseStatus> searchResponseStatusMatrix;
  private final SearchResponseOutboxService searchResponseMessageService;
  private final SearchResponseErrorRepository searchResponseErrorRepository;

  private final static String PUNCTUATION_REGEX = "[!\"#$%&'()*+,\\-./:;<=>?@\\[\\]^_`{|}~…—«»]";
  private final static int UNIQ_RATE = 10;

  @Override
  public SearchResponseError saveSearchResponseError(SearchResponseError error) {
    return searchResponseErrorRepository.save(error);
  }

  @Override
  public SearchResponse findById(UUID id) {
    return searchResponseRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Результат запроса не найден!"));
  }

  @Transactional
  @Override
  public List<SearchResponse> saveSearchResponses(List<YandexResponseXMLData.Doc> docs, UserSearchQuery userSearchQuery) {
    var results = docs.stream().map(doc -> {
      var entity = searchResponseMapper.mapFromDto(doc);
      entity.setUserSearchQuery(userSearchQuery);
      return entity;
    }).toList();
    var uniqResults = getOnlyUniqueResults(results);

    var savedResults = searchResponseRepository.saveAll(uniqResults);

    var kafkaMessages = savedResults.stream().map(result -> {
      var kafkaDto = new MessageSearchResponseDto();
      kafkaDto.setUserId(userSearchQuery.getUserId());
      kafkaDto.setDomain(result.getDomain());
      kafkaDto.setSearchResponseId(result.getId());
      kafkaDto.setUrl(result.getUrl());

      var kafkaRequestMessage = new SearchResponseMessage();
      kafkaRequestMessage.setDto(kafkaDto);
      kafkaRequestMessage.setStatus(OutboxMessageStatus.CREATED);
      return kafkaRequestMessage;
    }).toList();
    searchResponseMessageService.saveAll(kafkaMessages);

    return savedResults;
  }

  @Override
  @Transactional
  public SearchResponse changeStatus(SearchResponse searchResponse, SearchResponseStatus status) {
    if (searchResponse.getStatus() == status || !searchResponseStatusMatrix.isAvailable(searchResponse.getStatus(), status)) {
      log.warn(
        "ERROR: while checkStatusMovement for searchResponse: {} from: {} to: {}",
        searchResponse.getId(),
        searchResponse.getStatus(),
        searchResponse);
      throw new StatusChangeException();
    }

    searchResponse.setStatus(status);
    searchResponseRepository.save(searchResponse);
    return searchResponse;
  }

  private List<SearchResponse> getOnlyUniqueResults(List<SearchResponse> docs) {
    var docsList = new ArrayList<SearchResponse>();
    var docMaps = new HashMap<SearchResponse, Map<String, Integer>>();

    docs.forEach(doc -> {
      var passagesText = Optional.ofNullable(doc.getPassages())
        .stream()
        .map(text -> Arrays.stream(text.replaceAll(PUNCTUATION_REGEX, "").split(" "))
          .filter(StringUtils::isNotBlank).toList())
        .flatMap(Collection::stream)
        .toList();

      var wordMap = new LinkedHashMap<String, Integer>();
      passagesText.forEach(word -> {
        wordMap.compute(word, (key, value) -> value == null ? 1 : value + 1);
      });
      if (docsList.stream().allMatch(savedDoc -> isResultUnique(savedDoc, doc, wordMap, docMaps.get(savedDoc)))) {
        docMaps.put(doc, wordMap);
        docsList.add(doc);
      }
    });

    return docsList;
  }

  private boolean isResultUnique(
    SearchResponse doc1,
    SearchResponse doc2 ,
    Map<String, Integer> doc1Map,
    Map<String, Integer> doc2Map) {
    if (!StringUtils.equals(doc1.getDomain(), doc2.getDomain())) {
      return true;
    }

    var equalScore = new AtomicInteger(0);

    var firstMapEntries = doc1Map.entrySet();
    firstMapEntries.forEach(entry -> {
      var key = entry.getKey();
      var value = entry.getValue();
      var secondMapValue = doc2Map.getOrDefault(key, 0);
      equalScore.addAndGet(Math.min(value, secondMapValue));
    });

    var firstMapSum = doc1Map.values().stream().reduce(Integer::sum).orElse(0);
    var secondMapSum = doc2Map.values().stream().reduce(Integer::sum).orElse(0);

    if (Stream.of(firstMapSum, secondMapSum, Integer.parseInt(String.valueOf(equalScore))).anyMatch(num -> num == 0)) {
      return true;
    }

    var uniqueFirstMapPercent = Integer.parseInt(String.valueOf(equalScore)) / firstMapSum * 100;
    var uniqueSecondMapPercent = Integer.parseInt(String.valueOf(equalScore)) / secondMapSum * 100;
    var commonUniqRate = (uniqueFirstMapPercent + uniqueSecondMapPercent) / 2;
    return  100 - commonUniqRate >= UNIQ_RATE;
  }
}
