package rikser123.yandexfetcher.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import rikser123.yandexfetcher.repository.UserSearchQueryRepository;
import rikser123.yandexfetcher.repository.entity.SearchResponseStatus;
import rikser123.yandexfetcher.repository.entity.UserSearchQueryStatus;
import rikser123.yandexfetcher.service.SearchResponseOutboxService;
import rikser123.yandexfetcher.service.SearchResponseService;
import rikser123.yandexfetcher.service.UserSearchQueryService;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class PendingQueriesScheduler {
  private final UserSearchQueryService searchQueryService;
  private final SearchResponseOutboxService searchResponseOutboxService;
  private final SearchResponseService searchResponseService;
  private final UserSearchQueryRepository uuu;

  @Scheduled(fixedDelayString = "${schedulers.pending-queries.delay}")
  @SchedulerLock(name = "PendingQueriesScheduler", lockAtLeastFor = "3s", lockAtMostFor = "10s")
  public void schedule() {
    var failedPendingQueries = searchQueryService.findFailedPendingQueries(Instant.now().plus(30, ChronoUnit.MINUTES));
    failedPendingQueries.forEach(query -> {
      searchQueryService.setFailed(query);
    });

    var all = uuu.findAll();
    log.warn("uuu {}", all);

    var pendingQueries = searchQueryService.findPendingQueries(Instant.now().plus(15, ChronoUnit.MINUTES));

    pendingQueries.forEach(query -> {
      searchResponseOutboxService.createSearchResponseMessage(query, query.getResponses().stream().toList());
      searchQueryService.changeStatus(query, UserSearchQueryStatus.IN_PROCESSING_RETRY);
      query.getResponses().forEach(response -> {
        searchResponseService.changeStatus(response, SearchResponseStatus.CREATED);
      });
    });
  }
}
