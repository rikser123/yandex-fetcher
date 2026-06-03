package rikser123.yandexfetcher.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import rikser123.yandexfetcher.dto.YandexError;
import rikser123.yandexfetcher.dto.YandexResponse;

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
