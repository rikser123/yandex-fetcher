package rikser123.yandexfetcher.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import rikser123.bundle.repository.entity.OutboxMessageStatus;
import rikser123.yandexfetcher.producer.KafkaRequestResultMessageProducer;
import rikser123.yandexfetcher.service.RequestOutboxMessageService;

import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaMessageScheduler {
  private final KafkaRequestResultMessageProducer kafkaProducer;
  private final RequestOutboxMessageService requestOutboxMessageService;

  @Scheduled(fixedDelayString = "${kafka.scheduler-delay}")
  @SchedulerLock(name = "KafkaMessageScheduler", lockAtLeastFor = "3s", lockAtMostFor = "10s")
  public void schedule() {
    log.info("KafkaMessageScheduler started");

    var createdMessages = requestOutboxMessageService.findAllByStatus(OutboxMessageStatus.CREATED);

    if (createdMessages.isEmpty()) {
      log.info("KafkaMessageScheduler finished, no messages");
      return;
    }

    var futures = createdMessages.stream().map(kafkaProducer::send).toList();
    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

    log.info("KafkaMessageScheduler finished");
  }
}
