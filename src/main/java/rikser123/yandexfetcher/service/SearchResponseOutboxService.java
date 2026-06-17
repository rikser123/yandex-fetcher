package rikser123.yandexfetcher.service;

import org.springframework.stereotype.Service;
import rikser123.bundle.repository.entity.OutboxMessageStatus;
import rikser123.bundle.service.OutboxMessageService;
import rikser123.bundle.service.StatusMatrix;
import rikser123.yandexfetcher.repository.SearchResponseMessageRepository;
import rikser123.yandexfetcher.repository.entity.SearchResponseMessage;

@Service
public class SearchResponseOutboxService extends OutboxMessageService<SearchResponseMessage> {
  public SearchResponseOutboxService(
    SearchResponseMessageRepository kafkaRequestMessageRepository,
    StatusMatrix<OutboxMessageStatus> outboxStatusMatrix)
  {
    super(kafkaRequestMessageRepository, outboxStatusMatrix);
  }
}
