package rikser123.yandexfetcher.scheduler;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import rikser123.bundle.repository.entity.OutboxMessageStatus;
import rikser123.bundle.service.UserDetailService;
import rikser123.yandexfetcher.BaseConfig;
import rikser123.yandexfetcher.dto.request.MessageSearchResponseDto;
import rikser123.yandexfetcher.repository.SearchResponseMessageRepository;
import rikser123.yandexfetcher.repository.entity.SearchResponseMessage;

import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

public class SearchResponseOutboxSchedulerTest extends BaseConfig {
  @MockitoBean
  private UserDetailService userDetailService;

  @Autowired
  private SearchResponseMessageRepository requestMessageRepository;

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
        assertThat(firstMessage.getStatus()).isEqualTo(OutboxMessageStatus.SENT);
      });
  }

  private SearchResponseMessage createKafkaMessage() {
    var kafkaMessage = new SearchResponseMessage();
    kafkaMessage.setStatus(OutboxMessageStatus.CREATED);

    var dto = new MessageSearchResponseDto();
    dto.setUrl("url");
    dto.setDomain("domain");
    dto.setUserId(UUID.randomUUID());
    dto.setSearchResponseId(UUID.randomUUID());
    kafkaMessage.setDto(dto);
    return requestMessageRepository.save(kafkaMessage);
  }
}
