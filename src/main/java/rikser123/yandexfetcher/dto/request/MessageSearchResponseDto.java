package rikser123.yandexfetcher.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageSearchResponseDto {
  private UUID searchQueryId;
  private List<SearchResponse> searchResponses = new ArrayList<>();
  private UUID userId;
  private String queryText;

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class SearchResponse {
    private UUID searchResponseId;
    private String url;
    private String domain;
  }
}
