package rikser123.yandexfetcher.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import rikser123.bundle.component.ConstraintValidator;
import rikser123.yandexfetcher.dto.QueryAnalysisDto;
import rikser123.yandexfetcher.dto.UserQueryOutboxDto;
import rikser123.yandexfetcher.repository.entity.UserQueryError;
import rikser123.yandexfetcher.repository.entity.UserSearchQueryStatus;
import rikser123.yandexfetcher.service.QueryAnalysisService;
import rikser123.yandexfetcher.service.UserSearchQueryService;

import java.util.Objects;

@Component
@RequiredArgsConstructor
@Slf4j
public class QueryAnalysisConsumer {
  private static final String QUERY_ANALYSIS_TOPIC = "QUERY_ANALYSIS";

  private final ObjectMapper objectMapper;
  private final ConstraintValidator validator;
  private final UserSearchQueryService userSearchQueryService;
  private final QueryAnalysisService queryAnalysisService;


  @KafkaListener(topics = { QUERY_ANALYSIS_TOPIC }, groupId = "fetcher")
  public void requestListener(String message) {

    try {
      var data = objectMapper.readValue(message, UserQueryOutboxDto.class);
      validator.validate(data);
      var id = data.getSearchQueryId();
      var error = data.getError();

      var currentQuery = userSearchQueryService.findById(id);

      if (!Objects.isNull(error)) {
        var queryError = new UserQueryError();
        queryError.setCode(error.getCode());
        queryError.setMessage(error.getMessage());;
        queryError.setUserSearchQuery(currentQuery);
        userSearchQueryService.saveError(queryError);
        userSearchQueryService.changeStatus(currentQuery, UserSearchQueryStatus.FAILED);
        return;
      }

      var analysisDto = new QueryAnalysisDto();
      analysisDto.setAnalysis(data.getAnalysis());
      queryAnalysisService.save(analysisDto, currentQuery);
      userSearchQueryService.changeStatus(currentQuery, UserSearchQueryStatus.PROCESSED);
    } catch (Exception e) {
      log.warn("error handling query result ayanysys", e);
    }
  }
}
