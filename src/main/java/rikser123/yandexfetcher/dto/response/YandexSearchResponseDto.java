package rikser123.yandexfetcher.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Schema(description = "Параметры ответа на создание запроса")
public class YandexSearchResponseDto {
  @Schema(description = "Id запроса на поиск")
  private UUID queryId;
}
