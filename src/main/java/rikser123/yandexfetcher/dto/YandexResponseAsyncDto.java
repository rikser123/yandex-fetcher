package rikser123.yandexfetcher.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class YandexResponseAsyncDto {
  private String id;
  private String description;
  private String createdAt;
  private String createdBy;
  private String modifiedAt;
  private boolean done;
  private Object metadata;
  private YandexError error;
  private YandexResponse response;
}
