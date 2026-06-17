package rikser123.yandexfetcher.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import rikser123.bundle.repository.entity.OutboxMessageStatus;
import rikser123.yandexfetcher.producer.QueryProducer;
import rikser123.yandexfetcher.service.SearchResponseOutboxService;

import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class SearchResponseOutboxScheduler {
  private final QueryProducer queryProducer;
  private final SearchResponseOutboxService searchResponseOutboxService;

  @Scheduled(fixedDelayString = "${kafka.scheduler-delay}")
  @SchedulerLock(name = "SearchResponseOutboxScheduler", lockAtLeastFor = "3s", lockAtMostFor = "10s")
  public void schedule() {
    log.info("SearchResponseOutboxScheduler started");

    var createdMessages = searchResponseOutboxService.findAllByStatus(OutboxMessageStatus.CREATED);

    if (createdMessages.isEmpty()) {
      log.info("SearchResponseOutboxScheduler finished, no messages");
      return;
    }

    var futures = createdMessages.stream().map(queryProducer::send).toList();
    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

    log.info("SearchResponseOutboxScheduler finished");
  }
}
