package rikser123.yandexfetcher.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestResponseListDto {
  private List<RequestResponseDto> requestResponses;
  private Integer pageNumber;
  private Long totalElements;
}
