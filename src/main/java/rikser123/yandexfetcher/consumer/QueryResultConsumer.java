package rikser123.yandexfetcher.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import rikser123.bundle.component.ConstraintValidator;
import rikser123.yandexfetcher.dto.MessageQueryResultDto;
import rikser123.yandexfetcher.repository.entity.SearchResponseError;
import rikser123.yandexfetcher.service.SearchResponseService;

import java.util.Objects;

@Component
@RequiredArgsConstructor
@Slf4j
public class QueryResultConsumer {
  private static final String QUERY_RESULT_TOPIC = "QUERY_RESULT";

  private final ObjectMapper objectMapper;
  private final ConstraintValidator validator;
  private final SearchResponseService searchResponseService;

  @KafkaListener(topics = { QUERY_RESULT_TOPIC }, groupId = "fetcher")
  public void requestListener(String message) {

    try {
      var data = objectMapper.readValue(message, MessageQueryResultDto.class);
      validator.validate(data);
      var status = data.getStatus();
      var id = data.getSearchResponseId();
      var error = data.getError();

      var currentResult = searchResponseService.findById(id);

      if (!Objects.isNull(error)) {
        var requestError = new SearchResponseError();
        requestError.setCode(error.getCode());
        requestError.setMessage(error.getMessage());;
        requestError.setSearchResponse(currentResult);
        searchResponseService.saveSearchResponseError(requestError);
      }

      searchResponseService.changeStatus(currentResult, status);

    } catch (Exception e) {
      log.warn("error handling query result", e);
    }
  }
}
