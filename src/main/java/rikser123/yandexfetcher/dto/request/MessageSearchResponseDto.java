package rikser123.yandexfetcher.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageSearchResponseDto {
  private UUID searchResponseId;
  private UUID userId;
  private String url;
  private String domain;
}
