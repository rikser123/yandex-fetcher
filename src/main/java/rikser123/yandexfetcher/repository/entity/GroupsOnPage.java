package rikser123.yandexfetcher.repository.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum GroupsOnPage {
  FIVE("5"),
  TEN("10"),
  FITHTEEN("15"),
  TWENTY("20"),
  THIRTY("30");

  private String count;
}
