package rikser123.yandexfetcher.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import rikser123.yandexfetcher.repository.entity.FamilyMode;
import rikser123.yandexfetcher.repository.entity.GroupsOnPage;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Параметры создания запроса в яндекс")
public class YandexSearchRequestDto {
  @Schema(description = "Поисковая строка запроса")
  @NotBlank(message = "Параметр queryText должен быть заполнен!")
  @Size(max = 400, message = "Длина параметра queryText не должна превышать 400 символов!")
  private String queryText;

  @Schema(description = "Параметры семейного режима")
  private FamilyMode familyMode;

  @Schema(description = "Количество записей для обработки")
  private GroupsOnPage groupsOnPage;
}
