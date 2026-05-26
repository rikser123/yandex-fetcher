package rikser123.yandexfetcher.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class YandexSpellerResponseItemDto {
  private String word;
  private Integer pos;
  private Integer len;
  private List<String> s;
}
