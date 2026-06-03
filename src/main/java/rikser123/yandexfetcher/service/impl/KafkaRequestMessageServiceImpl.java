package rikser123.yandexfetcher.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rikser123.bundle.exception.StatusChangeException;
import rikser123.bundle.service.StatusMatrix;
import rikser123.yandexfetcher.dto.request.KafkaMessageRequestResultDto;
import rikser123.yandexfetcher.repository.KafkaRequestMessageRepository;
import rikser123.yandexfetcher.repository.entity.KafkaEntityStatus;
import rikser123.yandexfetcher.repository.entity.KafkaRequestMessage;
import rikser123.yandexfetcher.service.KafkaRequestMessageService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaRequestMessageServiceImpl implements KafkaRequestMessageService {
  private final KafkaRequestMessageRepository kafkaRequestMessageRepository;
  private final StatusMatrix<KafkaEntityStatus> kafkaStatusMatrix;

  @Value("${kafka.max-fetch-limit}")
  private int maxFetchLimit;

  @Transactional
  @Override
  public List<KafkaRequestMessage> saveAll(List<KafkaMessageRequestResultDto> messages) {
    var entities = messages.stream().map(dto -> {
      var kafkaRequestMessage = new KafkaRequestMessage();
      kafkaRequestMessage.setStatus(KafkaEntityStatus.CREATED);
      kafkaRequestMessage.setDto(dto);
      return kafkaRequestMessage;
    }).toList();

    return kafkaRequestMessageRepository.saveAll(entities);
  }

  @Override
  public List<KafkaRequestMessage> findAllByStatus(KafkaEntityStatus status) {
    return kafkaRequestMessageRepository.findAllByStatusOrderByCreatedAsc(status, Limit.of(maxFetchLimit));
  }

  @Override
  @Transactional
  public KafkaRequestMessage changeStatus(KafkaRequestMessage kafkaMessage, KafkaEntityStatus status) {
    if (kafkaMessage.getStatus() == status || !kafkaStatusMatrix.isAvailable(kafkaMessage.getStatus(), status)) {
      log.warn(
        "ERROR: while checkStatusMovement for kafkaMessage: {} from: {} to: {}",
        kafkaMessage.getId(),
        kafkaMessage.getStatus(),
        status);
      throw new StatusChangeException();
    }

    kafkaMessage.setStatus(status);
    kafkaRequestMessageRepository.save(kafkaMessage);
    return kafkaMessage;
  }
}
