package rikser123.yandexfetcher.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import rikser123.yandexfetcher.repository.entity.QueryAnalysisStatus;
import rikser123.yandexfetcher.service.QueryAnalysisService;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutdatedAnalysisScheduler {
  private final QueryAnalysisService queryAnalysisService;

  @Scheduled(fixedDelayString = "${schedulers.analysis.delay}")
  @SchedulerLock(name = "OutdatedAnalysisScheduler", lockAtLeastFor = "3s", lockAtMostFor = "10s")
  public void schedule() {
    log.info("OutdatedAnalysisScheduler started");

    var outdatedAnalysis = queryAnalysisService.findOutdatedAnalysis();
    outdatedAnalysis.forEach(analysis -> {
      queryAnalysisService.changeStatus(analysis, QueryAnalysisStatus.OUTDATED);
    });

    log.info("OutdatedAnalysisScheduler finished");
  }
}
