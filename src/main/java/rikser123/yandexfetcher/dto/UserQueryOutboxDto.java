package rikser123.yandexfetcher.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import rikser123.yandexfetcher.repository.entity.UserSearchQueryStatus;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserQueryOutboxDto {
  @NotNull(message = "Параметр searchQueryId должен быть заполнен")
  private UUID searchQueryId;

  @NotNull(message = "Параметр userId должен быть заполнен")
  private UUID userId;

  private String analysis;

  @NotNull(message = "Параметр status должен быть заполнен")
  private UserSearchQueryStatus status;
  private MessageError error;
}
