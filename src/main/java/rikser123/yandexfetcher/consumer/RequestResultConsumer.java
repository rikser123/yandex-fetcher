package rikser123.yandexfetcher.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import rikser123.bundle.component.ConstraintValidator;
import rikser123.yandexfetcher.dto.KafkaMessageRequestResultStatusDto;
import rikser123.yandexfetcher.repository.entity.RequestResultError;
import rikser123.yandexfetcher.service.RequestResultService;

import java.util.Objects;

@Component
@RequiredArgsConstructor
@Slf4j
public class RequestResultConsumer {
  private static final String REQUEST_RESULT_TOPIC = "REQUEST_RESULT";

  private final ObjectMapper objectMapper;
  private final ConstraintValidator validator;
  private final RequestResultService requestResultService;

  @KafkaListener(topics = { REQUEST_RESULT_TOPIC }, groupId = "fetcher")
  public void requestListener(String message) {

    try {
      var data = objectMapper.readValue(message, KafkaMessageRequestResultStatusDto.class);
      validator.validate(data);
      var status = data.getStatus();
      var id = data.getRequestResultId();
      var error = data.getError();

      var currentResult = requestResultService.findById(id);

      if (!Objects.isNull(error)) {
        var requestError = new RequestResultError();
        requestError.setCode(error.getCode());
        requestError.setMessage(error.getMessage());;
        requestError.setRequestResult(currentResult);
        requestResultService.saveRequestResultError(requestError);
      }

      requestResultService.changeStatus(currentResult, status);

    } catch (Exception e) {
      log.warn("error handling request result", e);
    }
  }
}
