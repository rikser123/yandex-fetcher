package rikser123.yandexfetcher.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import rikser123.bundle.dto.response.RikserResponseItem;
import rikser123.yandexfetcher.dto.YandexSpellerResponseDto;

@Tag(name = "API для взаимодействия с сервисом проверки правописания")
@ApiResponses(
  value = {
    @ApiResponse(responseCode = "400", description = "Bad request"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
  })
@RequestMapping("/api/v1/speller")
public interface SpellerApi {
  @GetMapping()
  @Operation(description = "Получение скорректированной фразы")
  @PreAuthorize("hasAuthority('CHECK_SPELLS')")
  RikserResponseItem<YandexSpellerResponseDto> search(
    @Parameter(description = "Текст для проверки орфографии", example = "синхрафазатрон в дубнyy")
    @RequestParam String text);
}
