package rikser123.yandexfetcher.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import rikser123.yandexfetcher.repository.entity.RequestResultStatus;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KafkaMessageRequestResultStatusDto {
  @NotNull(message = "Параметр requestResultId должен присутствовать!")
  private UUID requestResultId;

  @NotNull(message = "Параметр status должен присутствовать!")
  private RequestResultStatus status;
  private KafkaMessageError error;

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class KafkaMessageError {
    private String message;
    private String code;
  }
}
