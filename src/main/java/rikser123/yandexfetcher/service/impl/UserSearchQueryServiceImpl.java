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
import rikser123.bundle.dto.User;
import rikser123.bundle.exception.StatusChangeException;
import rikser123.bundle.service.StatusMatrix;
import rikser123.bundle.service.UserDetailService;
import rikser123.yandexfetcher.dto.request.YandexQueryListDto;
import rikser123.yandexfetcher.dto.request.YandexSearchQueryDto;
import rikser123.yandexfetcher.dto.response.UserSearchQueryDto;
import rikser123.yandexfetcher.mapper.UserSearchQueryMapper;
import rikser123.yandexfetcher.repository.UserSearchQueryRepository;
import rikser123.yandexfetcher.repository.entity.FamilyMode;
import rikser123.yandexfetcher.repository.entity.GroupsOnPage;
import rikser123.yandexfetcher.repository.entity.UserSearchQuery;
import rikser123.yandexfetcher.repository.entity.UserSearchQueryStatus;
import rikser123.yandexfetcher.repository.spec.YandexQueryListSpec;
import rikser123.yandexfetcher.service.UserSearchQueryService;

import java.util.List;
import java.util.Objects;
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

  @Transactional
  public UserSearchQuery findById(UUID id) {
    return userSearchQueryRepository.findById(id)
      .orElseThrow(() -> new EntityNotFoundException("Не найден запрос пользователя с id " + id));
  }

  @Override
  @Transactional
  public Page<UserSearchQueryDto> findAll(YandexQueryListDto filter) {
    var currentUser = (User) userDetailService.getCurrentUser();

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

  public UserSearchQuery saveByYandexRequest(YandexSearchQueryDto dto) {
    var user = (User) userDetailService.getCurrentUser();

    var request = new UserSearchQuery();
    request.setFamilyMode(Objects.isNull(dto.getFamilyMode()) ? FamilyMode.FAMILY_MODE_MODERATE : dto.getFamilyMode());
    request.setGroupsOnPage(Objects.isNull(dto.getGroupsOnPage()) ? GroupsOnPage.TEN : dto.getGroupsOnPage());
    request.setQueryText(dto.getQueryText());
    request.setUserId(user.getId());
    request.setStatus(UserSearchQueryStatus.CREATED);

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
}
