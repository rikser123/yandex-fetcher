package rikser123.yandexfetcher.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RequestServiceImpl implements RequestService {
  private final RequestRepository requestRepository;
  private final UserDetailService userDetailService;
  private final RequestResultMapper requestResultMapper;
  private final RequestResultRepository requestResultRepository;
  private final StatusMatrix<RequestStatus> requestStatusMatrix;

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
    var requestResults = docs.stream().map(doc -> {
      var entity = requestResultMapper.mapFromDto(doc);
      entity.setRequest(request);
      return entity;
    }).toList();

    return requestResultRepository.saveAll(requestResults);
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
}
