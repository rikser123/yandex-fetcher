package rikser123.yandexfetcher.feign;

import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import rikser123.yandexfetcher.dto.request.YandexQueryDto;
import rikser123.yandexfetcher.dto.response.YandexResponseAsyncDto;

@FeignClient(
  name = "yandex-client",
  url = "${yandex.searchApiUrl}"
)
public interface YandexSearchClient {
  @PostMapping("/v2/web/searchAsync")
  YandexResponseAsyncDto search(
    @RequestBody @Valid YandexQueryDto searchDto,
    @RequestHeader("Authorization") String authorization
  );
}
