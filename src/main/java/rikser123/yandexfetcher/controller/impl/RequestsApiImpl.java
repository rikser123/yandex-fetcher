package rikser123.yandexfetcher.controller.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;
import rikser123.bundle.dto.response.RikserResponseItem;
import rikser123.yandexfetcher.controller.RequestsApi;
import rikser123.yandexfetcher.dto.request.YandexQueryListDto;
import rikser123.yandexfetcher.dto.response.UserSearchQueryListDto;
import rikser123.yandexfetcher.service.YandexSearchService;

@RestController
@RequiredArgsConstructor
@Slf4j
public class RequestsApiImpl implements RequestsApi {
  private final YandexSearchService yandexSearchService;

  @Override
  public RikserResponseItem<UserSearchQueryListDto> search(YandexQueryListDto requestDto) {
    return yandexSearchService.findAll(requestDto);
  }
}
