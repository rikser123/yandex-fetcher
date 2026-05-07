package rikser123.yandexfetcher.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import reactor.core.publisher.Mono;
import rikser123.bundle.dto.response.RikserResponseItem;


@Tag(name = "API для взаимодействия с пользователми")
@ApiResponses(
    value = {
      @ApiResponse(responseCode = "400", description = "Bad request"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
    })
@RequestMapping("/api/v1/yandex-fetcher")
public interface UserApi {

  @GetMapping("/get")
  @Operation(description = "Регистрация пользователя")
  Mono<RikserResponseItem<Object>> get();

}
