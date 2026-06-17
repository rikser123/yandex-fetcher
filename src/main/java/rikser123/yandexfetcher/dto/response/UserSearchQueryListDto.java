package rikser123.yandexfetcher.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserSearchQueryListDto {
  private List<UserSearchQueryDto> searchQueries;
  private Integer pageNumber;
  private Long totalElements;
}
