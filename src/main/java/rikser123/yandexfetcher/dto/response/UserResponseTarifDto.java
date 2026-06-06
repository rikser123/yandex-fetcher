package rikser123.yandexfetcher.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponseTarifDto {
  private UserTarifResponseDto tarif;

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class UserTarifResponseDto {
    private UUID id;
    private String name;
    private String description;
    private Integer requestPerDay;

  }
}