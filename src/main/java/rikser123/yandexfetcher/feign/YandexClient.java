package rikser123.yandexfetcher.feign;

import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import rikser123.yandexfetcher.dto.YandexRequestDto;
import rikser123.yandexfetcher.dto.YandexResponseDto;

@FeignClient(
  name = "yandex-client",
  url = "https://searchapi.api.cloud.yandex.net"
)
public interface YandexClient {
  @PostMapping("/v2/web/searchAsync")
  YandexResponseDto search(
    @RequestBody @Valid YandexRequestDto searchDto,
    @RequestHeader("Authorization") String authorization
  );
}
