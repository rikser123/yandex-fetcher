package rikser123.yandexfetcher.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import rikser123.bundle.dto.response.RikserResponseItem;
import rikser123.yandexfetcher.dto.request.YandexRequestListDto;
import rikser123.yandexfetcher.dto.response.RequestResponseListDto;

@Tag(name = "API для взаимодействия с яндексом")
@ApiResponses(
  value = {
    @ApiResponse(responseCode = "400", description = "Bad request"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
  })
@RequestMapping("/api/v1/requests")
public interface RequestsApi {
  @GetMapping
  @Operation(description = "Получение списка запросов пользователя")
  @PreAuthorize("hasAuthority('VIEW_REQUEST')")
  RikserResponseItem<RequestResponseListDto> search(
    @ParameterObject()
    YandexRequestListDto requestDto
  );
}
