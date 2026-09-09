package rikser123.yandexfetcher.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import rikser123.bundle.validation.CheckSqlInjection;
import rikser123.yandexfetcher.repository.entity.FamilyMode;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Параметры создания запроса в яндекс")
public class YandexSearchQueryDto {
  @Schema(description = "Поисковая строка запроса")
  @NotBlank(message = "Параметр queryText должен быть заполнен!")
  @Size(max = 400, message = "Длина параметра queryText не должна превышать 400 символов!")
  @CheckSqlInjection
  private String queryText;

  @Schema(description = "Параметры семейного режима")
  private FamilyMode familyMode;

  @Schema(description = "Количество записей для обработки")
  @Pattern(regexp = "^([0-9]|[1-9][0-9]|100)$",
    message = "Значение должно быть числом от 0 до 100")
  @CheckSqlInjection
  private String groupsOnPage;

  @Schema(description = "Тип сортировки")
  private YandexQueryDto.SortMode sortMode;

  @Schema(description = "Направление сортировки")
  private YandexQueryDto.SortOrder sortOrder;

  @Schema(description = "Период запроса")
  private YandexQueryDto.Period period;
}
