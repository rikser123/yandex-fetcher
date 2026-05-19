package rikser123.yandexfetcher.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rikser123.bundle.dto.User;
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
import rikser123.yandexfetcher.service.RequestService;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class RequestServiceImpl implements RequestService {
  private final RequestRepository requestRepository;
  private final UserDetailService userDetailService;
  private final RequestResultMapper requestResultMapper;
  private final RequestResultRepository requestResultRepository;

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
}
