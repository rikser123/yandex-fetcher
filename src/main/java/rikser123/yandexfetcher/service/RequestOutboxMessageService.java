package rikser123.yandexfetcher.service;

import org.springframework.stereotype.Service;
import rikser123.bundle.repository.entity.OutboxMessageStatus;
import rikser123.bundle.service.OutboxMessageService;
import rikser123.bundle.service.StatusMatrix;
import rikser123.yandexfetcher.repository.KafkaRequestMessageRepository;
import rikser123.yandexfetcher.repository.entity.KafkaRequestMessage;

@Service
public class RequestOutboxMessageService extends OutboxMessageService<KafkaRequestMessage> {
  public RequestOutboxMessageService(
    KafkaRequestMessageRepository kafkaRequestMessageRepository,
    StatusMatrix<OutboxMessageStatus> outboxStatusMatrix)
  {
    super(kafkaRequestMessageRepository, outboxStatusMatrix);
  }
}
