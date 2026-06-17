package rikser123.yandexfetcher.controller.impl;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import rikser123.bundle.dto.request.RikserRequestItem;
import rikser123.bundle.dto.response.RikserResponseItem;
import rikser123.yandexfetcher.controller.FetcherApi;
import rikser123.yandexfetcher.dto.request.YandexSearchQueryDto;
import rikser123.yandexfetcher.dto.response.YandexSearchResponseDto;
import rikser123.yandexfetcher.service.YandexSearchService;


@RestController
@RequestMapping("/api/v1/yandex-fetcher")
@RequiredArgsConstructor
@Slf4j
public class FetcherApiImpl implements FetcherApi {
  private final YandexSearchService yandexService;

  @Override
  public RikserResponseItem<YandexSearchResponseDto> search(
    @RequestBody
    @Valid
    RikserRequestItem<YandexSearchQueryDto> requestDto,
    HttpServletRequest httpServletRequest
  ) {
    return yandexService.search(requestDto.getData(), httpServletRequest);
  }
}
