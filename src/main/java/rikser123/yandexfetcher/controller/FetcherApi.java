package rikser123.yandexfetcher.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import rikser123.bundle.dto.request.RikserRequestItem;
import rikser123.bundle.dto.response.RikserResponseItem;
import rikser123.yandexfetcher.dto.YandexSearchRequestDto;
import rikser123.yandexfetcher.dto.YandexSearchResponseDto;

@Tag(name = "API для взаимодействия с яндексом")
@ApiResponses(
    value = {
      @ApiResponse(responseCode = "400", description = "Bad request"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
    })
@RequestMapping("/api/v1/yandex-fetcher")
public interface FetcherApi {

  @PostMapping("/search")
  @Operation(description = "Получение результатов поиска от яндекса")
  RikserResponseItem<YandexSearchResponseDto> search(
    @Parameter(description = "Параметры для поиска", required = true)
    @RequestBody
    RikserRequestItem<YandexSearchRequestDto> requestDto,
    HttpServletRequest httpServletRequest
  );

}
