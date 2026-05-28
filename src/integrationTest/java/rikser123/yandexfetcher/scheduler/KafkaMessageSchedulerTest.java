package rikser123.yandexfetcher.scheduler;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import rikser123.yandexfetcher.BaseConfig;
import rikser123.yandexfetcher.dto.KafkaMessageRequestResultDto;
import rikser123.yandexfetcher.repository.KafkaRequestMessageRepository;
import rikser123.yandexfetcher.repository.entity.KafkaEntityStatus;
import rikser123.yandexfetcher.repository.entity.KafkaRequestMessage;

import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

public class KafkaMessageSchedulerTest extends BaseConfig {

  @Autowired
  private KafkaRequestMessageRepository requestMessageRepository;

  @AfterEach
  void cleanup() {
    requestMessageRepository.deleteAllInBatch();
  }

  @Test
  void shouldSendKafkaMessage() {
    createKafkaMessage();

    await()
      .atMost(Duration.ofSeconds(10))
      .pollInterval(Duration.ofMillis(500))
      .untilAsserted(() -> {
        var firstMessage = requestMessageRepository.findAll().getFirst();
        assertThat(firstMessage.getStatus()).isEqualTo(KafkaEntityStatus.SENT);
      });
  }

  private KafkaRequestMessage createKafkaMessage() {
    var kafkaMessage = new KafkaRequestMessage();
    kafkaMessage.setStatus(KafkaEntityStatus.CREATED);

    var dto = new KafkaMessageRequestResultDto();
    dto.setUrl("url");
    dto.setDomain("domain");
    dto.setUserId(UUID.randomUUID());
    dto.setRequestResultId(UUID.randomUUID());
    kafkaMessage.setDto(dto);
    return requestMessageRepository.save(kafkaMessage);
  }
}
