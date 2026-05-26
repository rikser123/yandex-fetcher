package rikser123.yandexfetcher.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import rikser123.yandexfetcher.dto.YandexSpellerResponseItemDto;

import java.util.List;

@FeignClient(
  name = "yandex-speller-client",
  url = "${yandex.spellerApi}"
)
public interface YandexSpellerClient {
  @GetMapping("/services/spellservice.json/checkText")
  List<YandexSpellerResponseItemDto> getSpells(@RequestParam String text);
}
