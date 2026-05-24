package rikser123.yandexfetcher.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rikser123.bundle.dto.User;
import rikser123.bundle.exception.StatusChangeException;
import rikser123.bundle.service.StatusMatrix;
import rikser123.bundle.service.UserDetailService;
import rikser123.yandexfetcher.dto.YandexResponseXMLData;
import rikser123.yandexfetcher.dto.YandexSearchRequestDto;
import rikser123.yandexfetcher.mapper.RequestResultMapper;
import rikser123.yandexfetcher.repository.RequestRepository;
import rikser123.yandexfetcher.repository.RequestResultRepository;
import rikser123.yandexfetcher.repository.entity.FamilyMode;
import rikser123.yandexfetcher.repository.entity.GroupsOnPage;
import rikser123.yandexfetcher.repository.entity.Request;
import rikser123.yandexfetcher.repository.entity.RequestResult;
import rikser123.yandexfetcher.repository.entity.RequestStatus;
import rikser123.yandexfetcher.service.RequestService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class RequestServiceImpl implements RequestService {
  private final RequestRepository requestRepository;
  private final UserDetailService userDetailService;
  private final RequestResultMapper requestResultMapper;
  private final RequestResultRepository requestResultRepository;
  private final StatusMatrix<RequestStatus> requestStatusMatrix;

  private final static String PUNCTUATION_REGEX = "[!\"#$%&'()*+,\\-./:;<=>?@\\[\\]^_`{|}~…—«»]";
  private final static int UNIQ_RATE = 10;

  @Transactional
  @Override
  public Request save(Request request) {
    return requestRepository.save(request);
  }

  public Request saveByYandexRequest(YandexSearchRequestDto dto) {
    var user = (User) userDetailService.getCurrentUser();

    var request = new Request();
    request.setFamilyMode(Objects.isNull(dto.getFamilyMode()) ? FamilyMode.FAMILY_MODE_MODERATE : dto.getFamilyMode());
    request.setGroupsOnPage(Objects.isNull(dto.getGroupsOnPage()) ? GroupsOnPage.TEN : dto.getGroupsOnPage());
    request.setQueryText(dto.getQueryText());
    request.setUserId(user.getId());
    request.setStatus(RequestStatus.CREATED);

    return save(request);
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

    return requestResultRepository.saveAll(uniqResults);
  }

  @Override
  @Transactional
  public Request changeStatus(Request request, RequestStatus status) {
    if (request.getStatus() == status || !requestStatusMatrix.isAvailable(request.getStatus(), status)) {
      log.warn(
        "ERROR: while checkStatusMovement for request: {} from: {} to: {}",
        request.getId(),
        request.getStatus(),
        status);
      throw new StatusChangeException();
    }

    request.setStatus(status);
    requestRepository.save(request);
    return request;
  }

  @Override
  public Optional<Request> findProcessingRequest(UUID userId, String queryText) {
    return requestRepository.findByUserIdAndQueryTextAndStatusIsIn(
      userId,
      queryText,
      List.of(RequestStatus.IN_PROCESSING, RequestStatus.CREATED)
    );
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
