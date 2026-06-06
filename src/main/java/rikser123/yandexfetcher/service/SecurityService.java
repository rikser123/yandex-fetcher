package rikser123.yandexfetcher.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import rikser123.yandexfetcher.dto.response.UserResponseTarifDto;
import rikser123.yandexfetcher.feign.SecurityClient;

import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SecurityService {
  private final SecurityClient securityClient;

  public UserResponseTarifDto.UserTarifResponseDto getUserTarif(UUID userId) {
    var tarifData = securityClient.getUserWithTarif(userId);

    if (Objects.isNull(tarifData.getData())) {
      throw new IllegalStateException("Не удалось получить данные по тарифу пользователя!");
    }

    return tarifData.getData().getTarif();
  }
}
