package rikser123.yandexfetcher.controller.impl;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import rikser123.bundle.dto.response.RikserResponseItem;
import rikser123.yandexfetcher.controller.UserApi;


@RestController
@RequestMapping("/api/v1/yandex-fetcher")
@RequiredArgsConstructor
@Slf4j
@Tag(
    name = "Сервис для работы с пользователями",
    description = "Сервис для работы с пользтвателями")
public class UserController implements UserApi {

  @Override
  public Mono<RikserResponseItem<Object>> get() {
    return Mono.just(new RikserResponseItem<>());
  }
}
