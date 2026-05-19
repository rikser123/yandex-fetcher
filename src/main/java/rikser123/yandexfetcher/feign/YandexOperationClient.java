package rikser123.yandexfetcher.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import rikser123.yandexfetcher.dto.YandexResponseOperationDto;

@FeignClient(
  name = "yandex-client",
  url = "${yandex.operationApiUrl}"
)
public interface YandexOperationClient {
  @GetMapping("/operations/{id}")
  YandexResponseOperationDto getSearchData(
    @PathVariable String id,
    @RequestHeader("Authorization") String authorization
  );
}
