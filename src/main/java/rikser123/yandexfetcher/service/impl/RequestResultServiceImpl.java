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
import rikser123.yandexfetcher.dto.request.KafkaMessageRequestResultDto;
import rikser123.yandexfetcher.dto.response.YandexResponseXMLData;
import rikser123.yandexfetcher.mapper.RequestResultMapper;
import rikser123.yandexfetcher.repository.RequestResultErrorRepository;
import rikser123.yandexfetcher.repository.RequestResultRepository;
import rikser123.yandexfetcher.repository.entity.KafkaRequestMessage;
import rikser123.yandexfetcher.repository.entity.Request;
import rikser123.yandexfetcher.repository.entity.RequestResult;
import rikser123.yandexfetcher.repository.entity.RequestResultError;
import rikser123.yandexfetcher.repository.entity.RequestResultStatus;
import rikser123.yandexfetcher.service.RequestOutboxMessageService;
import rikser123.yandexfetcher.service.RequestResultService;

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
public class RequestResultServiceImpl implements RequestResultService {
  private final RequestResultMapper requestResultMapper;
  private final RequestResultRepository requestResultRepository;
  private final StatusMatrix<RequestResultStatus> requestResultStatusMatrix;
  private final RequestOutboxMessageService requestOutboxMessageService;
  private final RequestResultErrorRepository requestResultErrorRepository;

  private final static String PUNCTUATION_REGEX = "[!\"#$%&'()*+,\\-./:;<=>?@\\[\\]^_`{|}~…—«»]";
  private final static int UNIQ_RATE = 10;

  @Override
  public RequestResultError saveRequestResultError(RequestResultError error) {
    return requestResultErrorRepository.save(error);
  }

  @Override
  public RequestResult findById(UUID id) {
    return requestResultRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Результат запроса не найден!"));
  }

  @Transactional
  @Override
  public List<RequestResult> saveRequestResults(List<YandexResponseXMLData.Doc> docs, Request request) {
    var results = docs.stream().map(doc -> {
      var entity = requestResultMapper.mapFromDto(doc);
      entity.setRequest(request);
      return entity;
    }).toList();
    var uniqResults = getOnlyUniqueResults(results);

    var savedResults = requestResultRepository.saveAll(uniqResults);

    var kafkaMessages = savedResults.stream().map(result -> {
      var kafkaDto = new KafkaMessageRequestResultDto();
      kafkaDto.setUserId(request.getUserId());
      kafkaDto.setDomain(result.getDomain());
      kafkaDto.setRequestResultId(request.getId());
      kafkaDto.setUrl(result.getUrl());

      var kafkaRequestMessage = new KafkaRequestMessage();
      kafkaRequestMessage.setDto(kafkaDto);
      kafkaRequestMessage.setStatus(OutboxMessageStatus.CREATED);
      return kafkaRequestMessage;
    }).toList();
    requestOutboxMessageService.saveAll(kafkaMessages);

    return savedResults;
  }

  @Override
  @Transactional
  public RequestResult changeStatus(RequestResult requestResult, RequestResultStatus status) {
    if (requestResult.getStatus() == status || !requestResultStatusMatrix.isAvailable(requestResult.getStatus(), status)) {
      log.warn(
        "ERROR: while checkStatusMovement for requestResult: {} from: {} to: {}",
        requestResult.getId(),
        requestResult.getStatus(),
        requestResult);
      throw new StatusChangeException();
    }

    requestResult.setStatus(status);
    requestResultRepository.save(requestResult);
    return requestResult;
  }

  private List<RequestResult> getOnlyUniqueResults(List<RequestResult> docs) {
    var docsList = new ArrayList<RequestResult>();
    var docMaps = new HashMap<RequestResult, Map<String, Integer>>();

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
    RequestResult doc1,
    RequestResult doc2 ,
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
