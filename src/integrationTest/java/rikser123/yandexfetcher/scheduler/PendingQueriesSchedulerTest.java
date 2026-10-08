package rikser123.yandexfetcher.scheduler;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import rikser123.bundle.service.UserDetailService;
import rikser123.yandexfetcher.BaseConfig;
import rikser123.yandexfetcher.dto.request.YandexQueryDto;
import rikser123.yandexfetcher.repository.UserSearchQueryRepository;
import rikser123.yandexfetcher.repository.entity.SearchResponse;
import rikser123.yandexfetcher.repository.entity.SearchResponseStatus;
import rikser123.yandexfetcher.repository.entity.UserSearchQuery;
import rikser123.yandexfetcher.repository.entity.UserSearchQueryStatus;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

public class PendingQueriesSchedulerTest extends BaseConfig {

  @Autowired
  private UserSearchQueryRepository userSearchQueryRepository;

  @Autowired
  private PendingQueriesScheduler scheduler;

  @MockitoBean
  private UserDetailService userDetailService;

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @Autowired
  private EntityManager entityManager;

  @Test
  void shouldChangePendingStatuses() {
    var processingQuery = createSearchQuery(UserSearchQueryStatus.IN_PROCESSING);
    var pendingQuery = createSearchQuery(UserSearchQueryStatus.IN_PROCESSING_RETRY);

    scheduler.schedule();

    await()
      .atMost(Duration.ofSeconds(5))
      .pollInterval(Duration.ofMillis(500))
      .untilAsserted(() -> {
        assertThat(userSearchQueryRepository.findAll().size()).isEqualTo(2);
        assertThat(userSearchQueryRepository
          .findById(processingQuery.getId()).get().getStatus())
          .isEqualTo(UserSearchQueryStatus.IN_PROCESSING_RETRY);
        assertThat(userSearchQueryRepository
          .findById(pendingQuery.getId()).get().getStatus())
          .isEqualTo(UserSearchQueryStatus.FAILED);
      });

  }

  private UserSearchQuery createSearchQuery(UserSearchQueryStatus status) {
    var query = new UserSearchQuery();
    query.setUserId(UUID.randomUUID());
    query.setQueryText("query text");
    query.setStatus(status);
    query.setUserRequest(new YandexQueryDto());

    var searchResponse = new SearchResponse();
    searchResponse.setStatus(SearchResponseStatus.IN_PROCESSING);
    searchResponse.setUrl("url");
    searchResponse.setDomain("domain");
    searchResponse.setTitle("title");
    searchResponse.setUserSearchQuery(query);

    query.getResponses().add(searchResponse);

    var saved = userSearchQueryRepository.saveAndFlush(query);

    jdbcTemplate.update(
      "UPDATE user_search_query SET created = ? WHERE id = ?",
      Timestamp.from(Instant.now().plus(100, ChronoUnit.DAYS)),
      saved.getId()
    );

    entityManager.clear();

    return userSearchQueryRepository.findById(saved.getId()).get();
  }
}
