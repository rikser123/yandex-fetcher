package rikser123.yandexfetcher.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import rikser123.yandexfetcher.repository.entity.SearchResponseStatus;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageQueryResultDto {
  @NotNull(message = "Параметр searchResponseId должен присутствовать!")
  private UUID searchResponseId;

  @NotNull(message = "Параметр status должен присутствовать!")
  private SearchResponseStatus status;
  private KafkaMessageError error;

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class KafkaMessageError {
    private String message;
    private String code;
  }
}
