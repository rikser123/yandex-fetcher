package rikser123.yandexfetcher.controller.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import rikser123.bundle.dto.response.RikserResponseItem;
import rikser123.yandexfetcher.controller.SpellerApi;
import rikser123.yandexfetcher.dto.response.YandexSpellerResponseDto;
import rikser123.yandexfetcher.service.YandexSpellerService;

@RestController
@RequiredArgsConstructor
public class SpellerApiImpl implements SpellerApi {
  private final YandexSpellerService yandexSpellerService;

  @Override
  public RikserResponseItem<YandexSpellerResponseDto> search(@RequestParam String text) {
    return yandexSpellerService.getSpellCorrection(text);
  }
}
