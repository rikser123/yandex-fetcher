package rikser123.yandexfetcher.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import rikser123.bundle.validation.CheckSqlInjection;
import rikser123.yandexfetcher.repository.entity.FamilyMode;
import rikser123.yandexfetcher.repository.entity.GroupsOnPage;
import rikser123.yandexfetcher.repository.entity.RequestStatus;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Параметры для получения списка запросов пользователя")
public class YandexRequestListDto {
  @Size(max = 400, message = "Параметр queryText не должен быть больше 400 символов")
  @Schema(description = "Запрос в яндекс", example = "Запрос")
  @CheckSqlInjection
  private String queryText;

  private FamilyMode familyMode;
  private GroupsOnPage groupsOnPage;
  private RequestStatus status;
  private Instant dateFrom;
  private Instant dateTo;
  private Integer pageNumber = 0;
  private Integer itemsOnPage = 25;
  private String sortBy;
}
