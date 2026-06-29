package rikser123.yandexfetcher.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import rikser123.bundle.repository.entity.OutboxMessageStatus;
import rikser123.yandexfetcher.dto.request.MessageSearchResponseDto;
import rikser123.yandexfetcher.repository.entity.SearchResponseMessage;
import rikser123.yandexfetcher.repository.entity.SearchResponseStatus;
import rikser123.yandexfetcher.repository.entity.UserSearchQueryStatus;
import rikser123.yandexfetcher.service.SearchResponseOutboxService;
import rikser123.yandexfetcher.service.SearchResponseService;
import rikser123.yandexfetcher.service.UserSearchQueryService;

import static rikser123.yandexfetcher.config.KafkaTopicConfig.QUERY_TOPIC;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class QueryProducer {
  private final KafkaTemplate<String, String> kafkaTemplate;
  private final ObjectMapper objectMapper;
  private final SearchResponseOutboxService requestOutboxMessageService;
  private final SearchResponseService responseService;
  private final UserSearchQueryService userSearchQueryService;

  @SneakyThrows
  public CompletableFuture<SendResult<String, String>> send(SearchResponseMessage kafkaRequestMessage) {
    var dto = kafkaRequestMessage.getDto();
    var message = objectMapper.writeValueAsString(dto);

    return kafkaTemplate.send(QUERY_TOPIC, message).whenComplete((result, error) -> {
      if (!Objects.isNull(result)) {
        log.info("message successfully send {}", kafkaRequestMessage.getId());
        requestOutboxMessageService.changeStatus(kafkaRequestMessage, OutboxMessageStatus.SENT);
        var currentQuery = userSearchQueryService.findById(dto.getSearchQueryId());
        userSearchQueryService.changeStatus(currentQuery, UserSearchQueryStatus.IN_PROCESSING);

        var responseIds = dto.getSearchResponses()
          .stream()
          .map(MessageSearchResponseDto.SearchResponse::getSearchResponseId)
          .toList();
        var allResponses = responseService.findAllByIds(responseIds);
        allResponses.forEach(response -> {
          responseService.changeStatus(response, SearchResponseStatus.IN_PROCESSING);
        });
      } else if (!Objects.isNull(error)) {
        log.warn("message fail send {}", kafkaRequestMessage.getId());
      }
    });
  }
}
