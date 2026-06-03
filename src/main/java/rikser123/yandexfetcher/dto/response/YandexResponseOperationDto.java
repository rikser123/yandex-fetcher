package rikser123.yandexfetcher.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import rikser123.yandexfetcher.dto.YandexError;
import rikser123.yandexfetcher.dto.YandexResponse;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class YandexResponseOperationDto {
  private boolean done;
  private Object metadata;
  private YandexError error;
  private YandexResponse response;
  private String id;
  private String description;
  private String createdAt;
  private String createdBy;
  private String modifiedAt;
}
