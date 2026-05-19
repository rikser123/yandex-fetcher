package rikser123.yandexfetcher.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import rikser123.yandexfetcher.repository.entity.FamilyMode;
import rikser123.yandexfetcher.repository.entity.GroupsOnPage;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class YandexRequestDto {
  @Valid
  private Query query;

  private GroupSpec groupSpec;
  private SortSpec sortSpec;
  private String region;
  private ResponseFormat responseFormat;
  private Localization l10n;
  private String userAgent;
  private Period period;

  @AllArgsConstructor
  @NoArgsConstructor
  @Data
  public static class Query {
    private SearchType searchType;

    @NotBlank(message = "Параметр queryText не должен быть пустым")
    @Size(max = 400, message = "Параметр queryText не должен быть больше 400 символов!")

    private String queryText;
    private FamilyMode familyMode;
  }

  @AllArgsConstructor
  @NoArgsConstructor
  @Data
  public static class GroupSpec {
    private GroupsOnPage groupsOnPage;
  }

  @AllArgsConstructor
  @NoArgsConstructor
  @Data
  public static class SortSpec {
    private SortMode sortMode;
    private SortOrder sortOrder;
  }

  public enum SortMode {
    SORT_MODE_BY_RELEVANCE,
    SORT_MODE_BY_TIME
  }

  public enum SortOrder {
    SORT_ORDER_ASC,
    SORT_ORDER_DESC
  }

  public enum SearchType {
    SEARCH_TYPE_RU,
    SEARCH_TYPE_TR,
    SEARCH_TYPE_COM,
    SEARCH_TYPE_KK,
    SEARCH_TYPE_BE,
    SEARCH_TYPE_UZ
  }

  public enum ResponseFormat {
    FORMAT_XML,
    FORMAT_HTML
  }

  public enum Localization {
    LOCALIZATION_RU,
    LOCALIZATION_UK,
    LOCALIZATION_BE,
    LOCALIZATION_KK,
    LOCALIZATION_TR,
    LOCALIZATION_EN
  }

  public enum Period {
    PERIOD_ALL_TIME,
    PERIOD_DAY,
    PERIOD_2_WEEKS,
    PERIOD_MONTH
  }
}
