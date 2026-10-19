package rikser123.yandexfetcher.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rikser123.bundle.exception.StatusChangeException;
import rikser123.bundle.service.StatusMatrix;
import rikser123.bundle.service.UserDetailService;
import rikser123.yandexfetcher.dto.request.YandexQueryDto;
import rikser123.yandexfetcher.dto.request.YandexQueryListDto;
import rikser123.yandexfetcher.dto.request.YandexSearchQueryDto;
import rikser123.yandexfetcher.dto.response.UserSearchQueryDto;
import rikser123.yandexfetcher.mapper.UserSearchQueryMapper;
import rikser123.yandexfetcher.repository.UserQueryErrorRepository;
import rikser123.yandexfetcher.repository.UserSearchQueryRepository;
import rikser123.yandexfetcher.repository.entity.QueryAnalysisStatus;
import rikser123.yandexfetcher.repository.entity.SearchResponseStatus;
import rikser123.yandexfetcher.repository.entity.UserQueryError;
import rikser123.yandexfetcher.repository.entity.UserSearchQuery;
import rikser123.yandexfetcher.repository.entity.UserSearchQueryStatus;
import rikser123.yandexfetcher.repository.spec.YandexQueryListSpec;
import rikser123.yandexfetcher.service.UserSearchQueryService;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserSearchQueryServiceImpl implements UserSearchQueryService {
  private final UserSearchQueryRepository userSearchQueryRepository;
  private final UserDetailService userDetailService;
  private final StatusMatrix<UserSearchQueryStatus> requestStatusMatrix;
  private final UserSearchQueryMapper requestMapper;
  private final UserQueryErrorRepository userQueryErrorRepository;

  public UserSearchQuery findById(UUID id) {
    return userSearchQueryRepository.findById(id)
      .orElseThrow(() -> new EntityNotFoundException("Не найден запрос пользователя с id " + id));
  }

  @Override
  public Page<UserSearchQueryDto> findAll(YandexQueryListDto filter) {
    var currentUser = userDetailService.getCurrentUser();

    var filterCriteria = new YandexQueryListSpec(filter, currentUser.getId());
    var pageRequest = PageRequest.of(
      filter.getPageNumber(),
      filter.getItemsOnPage(),
      Sort.by(Sort.Direction.ASC, StringUtils.defaultIfEmpty(filter.getSortBy(), "created"))
    );
    var result = userSearchQueryRepository.findAll(filterCriteria, pageRequest);
    var responses = result.get().map(requestMapper::mapToDto).toList();

    return new PageImpl<>(responses, pageRequest, result.getTotalElements());
  }

  @Transactional
  @Override
  public UserSearchQuery save(UserSearchQuery userSearchQuery) {
    return userSearchQueryRepository.save(userSearchQuery);
  }

  public UserSearchQuery saveByYandexRequest(YandexSearchQueryDto dto, YandexQueryDto userRequest, UserSearchQueryStatus status) {
    var user = userDetailService.getCurrentUser();

    var request = requestMapper.mapToEntity(dto);

    request.setUserId(user.getId());
    request.setStatus(status);
    request.setUserRequest(userRequest);

    return save(request);
  }


  @Override
  @Transactional
  public UserSearchQuery changeStatus(UserSearchQuery userSearchQuery, UserSearchQueryStatus status) {
    if (userSearchQuery.getStatus() == status || !requestStatusMatrix.isAvailable(userSearchQuery.getStatus(), status)) {
      log.warn(
        "ERROR: while checkStatusMovement for request: {} from: {} to: {}",
        userSearchQuery.getId(),
        userSearchQuery.getStatus(),
        status);
      throw new StatusChangeException();
    }

    userSearchQuery.setStatus(status);
    userSearchQueryRepository.save(userSearchQuery);
    return userSearchQuery;
  }

  @Override
  public Optional<UserSearchQuery> findProcessingQuery(UUID userId, String queryText) {
    return userSearchQueryRepository.findByUserIdAndQueryTextAndStatusIsIn(
      userId,
      queryText,
      List.of(UserSearchQueryStatus.IN_PROCESSING, UserSearchQueryStatus.CREATED)
    );
  }

  @Override
  public List<UserSearchQuery> findCreatedQueries(int limit) {
    var size = PageRequest.of(0, limit);
    return userSearchQueryRepository.findAllByStatusOrderByCreated(UserSearchQueryStatus.CREATED, size);
  }

  @Transactional
  @Override
  public UserQueryError saveError(UserQueryError error) {
    return userQueryErrorRepository.save(error);
  }

  @Transactional(readOnly = true)
  @Override
  public Optional<UserSearchQuery> findWithAnalysis(String queryText) {
    return userSearchQueryRepository.findByQueryTextAndStatus(queryText, UserSearchQueryStatus.PROCESSED, QueryAnalysisStatus.CREATED);
  }

  @Transactional(readOnly = true)
  @Override
  public List<UserSearchQuery> findPendingQueries(Instant time) {
    return userSearchQueryRepository.findByStatusAndCreatedIsGreaterThan(UserSearchQueryStatus.IN_PROCESSING, time);
  }

  @Transactional(readOnly = true)
  @Override
  public List<UserSearchQuery> findFailedPendingQueries(Instant time) {
    return userSearchQueryRepository.findByStatusAndCreatedIsGreaterThan(UserSearchQueryStatus.IN_PROCESSING_RETRY, time);
  }

  @Transactional
  @Override
  public UserSearchQuery setFailed(UserSearchQuery searchQuery) {
    changeStatus(searchQuery, UserSearchQueryStatus.FAILED);
    searchQuery.getResponses().forEach(response -> {
      response.setStatus(SearchResponseStatus.FAILED);
    });
    var error = new UserQueryError();
    error.setUserSearchQuery(searchQuery);
    error.setMessage("Не удалось получить анализ по запросу пользователя");
    userQueryErrorRepository.save(error);

    return save(searchQuery);
  }
}
