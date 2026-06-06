package rikser123.yandexfetcher.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import rikser123.bundle.dto.response.RikserResponseItem;
import rikser123.yandexfetcher.dto.response.UserResponseTarifDto;

import java.util.UUID;

@FeignClient(
  name = "security-client",
  url = "${bundle.security.service.url}"
)
public interface SecurityClient {
  @GetMapping("/api/v1/user/get/{id}/tarif")
  RikserResponseItem<UserResponseTarifDto> getUserWithTarif(@PathVariable UUID id);
}
