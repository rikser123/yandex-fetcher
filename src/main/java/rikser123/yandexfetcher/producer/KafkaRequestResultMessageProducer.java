package rikser123.yandexfetcher.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import rikser123.bundle.repository.entity.OutboxMessageStatus;
import rikser123.yandexfetcher.repository.entity.KafkaRequestMessage;
import rikser123.yandexfetcher.service.RequestOutboxMessageService;

import static rikser123.yandexfetcher.config.KafkaTopicConfig.REQUEST_TOPIC;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaRequestResultMessageProducer {
  private final KafkaTemplate<String, String> kafkaTemplate;
  private final ObjectMapper objectMapper;
  private final RequestOutboxMessageService requestOutboxMessageService;

  @SneakyThrows
  public CompletableFuture<SendResult<String, String>> send(KafkaRequestMessage kafkaRequestMessage) {
    var dto = kafkaRequestMessage.getDto();
    var message = objectMapper.writeValueAsString(dto);

    return kafkaTemplate.send(REQUEST_TOPIC, message).whenComplete((result, error) -> {
      if (!Objects.isNull(result)) {
        log.info("message successfully send {}", kafkaRequestMessage.getId());
        requestOutboxMessageService.changeStatus(kafkaRequestMessage, OutboxMessageStatus.SENT);
      } else if (!Objects.isNull(error)) {
        log.warn("message fail send {}", kafkaRequestMessage.getId());
      }
    });
  }
}
