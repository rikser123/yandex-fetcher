package rikser123.yandexfetcher.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import rikser123.yandexfetcher.repository.entity.FamilyMode;
import rikser123.yandexfetcher.repository.entity.GroupsOnPage;
import rikser123.yandexfetcher.repository.entity.UserSearchQueryStatus;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserSearchQueryDto {
  private UUID id;
  private UUID userId;
  private String queryText;
  private FamilyMode familyMode;
  private GroupsOnPage groupsOnPage;
  private UserSearchQueryStatus status;
  private Set<SearchResponseDto> responses = new HashSet<>();
  private Instant updated;
  private Instant created;
}
