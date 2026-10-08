package rikser123.yandexfetcher.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import rikser123.yandexfetcher.repository.entity.UserSearchQueryStatus;
import rikser123.yandexfetcher.service.SearchResponseOutboxService;
import rikser123.yandexfetcher.service.UserSearchQueryService;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class PendingQueriesScheduler {
  private final UserSearchQueryService searchQueryService;
  private final SearchResponseOutboxService searchResponseOutboxService;

  @Scheduled(fixedDelayString = "${schedulers.pending-queries.delay}")
  @SchedulerLock(name = "PendingQueriesScheduler", lockAtLeastFor = "3s", lockAtMostFor = "10s")
  public void schedule() {
    var failedPendingQueries = searchQueryService.findFailedPendingQueries(Instant.now().plus(30, ChronoUnit.MINUTES));
    failedPendingQueries.forEach(query -> {
      searchQueryService.setFailed(query);
    });

    var pendingQueries = searchQueryService.findPendingQueries(Instant.now().plus(15, ChronoUnit.MINUTES));
    pendingQueries.forEach(query -> {
      searchResponseOutboxService.createSearchResponseMessage(query, query.getResponses().stream().toList());
      searchQueryService.changeStatus(query, UserSearchQueryStatus.IN_PROCESSING_RETRY);
    });
  }
}
