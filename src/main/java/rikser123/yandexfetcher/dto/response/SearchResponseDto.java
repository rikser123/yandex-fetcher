package rikser123.yandexfetcher.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import rikser123.yandexfetcher.repository.entity.SearchResponseStatus;

import java.time.Instant;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class SearchResponseDto {
  private UUID id;
  private SearchResponseStatus status;
  private String savedCopyUrl;
  private String url;
  private String domain;
  private String title;
  private Instant modTime;
  private Long size;
  private String charset;
  private String mimeType;
  private String passages;
  private Instant created;
  private Instant updated;
}
