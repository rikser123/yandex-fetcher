package rikser123.yandexfetcher.controller.impl;

import io.swagger.v3.oas.annotations.tags.Tag;
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
import rikser123.yandexfetcher.dto.YandexSearchRequestDto;
import rikser123.yandexfetcher.dto.YandexSearchResponseDto;
import rikser123.yandexfetcher.service.YandexService;


@RestController
@RequestMapping("/api/v1/yandex-fetcher")
@RequiredArgsConstructor
@Slf4j
public class FetcherApiImpl implements FetcherApi {
  private final YandexService yandexService;

  @Override
  public RikserResponseItem<YandexSearchResponseDto> search(
    @RequestBody
    @Valid
    RikserRequestItem<YandexSearchRequestDto> requestDto,
    HttpServletRequest httpServletRequest
  ) {
    return yandexService.search(requestDto.getData(), httpServletRequest);
  }
}
