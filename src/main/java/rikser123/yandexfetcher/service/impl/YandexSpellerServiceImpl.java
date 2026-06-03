package rikser123.yandexfetcher.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import rikser123.bundle.dto.response.RikserResponseItem;
import rikser123.bundle.utils.RikserResponseUtils;
import rikser123.yandexfetcher.dto.response.YandexSpellerResponseDto;
import rikser123.yandexfetcher.feign.YandexSpellerClient;
import rikser123.yandexfetcher.service.YandexSpellerService;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class YandexSpellerServiceImpl implements YandexSpellerService {
  private final YandexSpellerClient yandexSpellerClient;

  @Override
  public RikserResponseItem<YandexSpellerResponseDto> getSpellCorrection(String text) {
    var result = yandexSpellerClient.getSpells(URLEncoder.encode(text, StandardCharsets.UTF_8));

    var correction = Arrays.stream(text.split(" ")).map(word ->
      result.stream()
        .filter(res -> res.getWord().equals(word))
        .map(res -> res.getS().getFirst())
        .findFirst()
        .orElse(word)
    ).collect(Collectors.joining(" "));

    var response = new YandexSpellerResponseDto();
    response.setCorrection(correction);

    return RikserResponseUtils.createResponse(response);
  }
}
