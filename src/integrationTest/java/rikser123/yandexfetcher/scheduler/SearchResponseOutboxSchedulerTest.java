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
import rikser123.yandexfetcher.repository.SearchResponseRepository;
import rikser123.yandexfetcher.repository.UserSearchQueryRepository;
import rikser123.yandexfetcher.repository.entity.SearchResponse;
import rikser123.yandexfetcher.repository.entity.SearchResponseMessage;
import rikser123.yandexfetcher.repository.entity.SearchResponseStatus;
import rikser123.yandexfetcher.repository.entity.UserSearchQuery;
import rikser123.yandexfetcher.repository.entity.UserSearchQueryStatus;

import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

public class SearchResponseOutboxSchedulerTest extends BaseConfig {
  @MockitoBean
  private UserDetailService userDetailService;

  @Autowired
  private SearchResponseMessageRepository requestMessageRepository;

  @Autowired
  private UserSearchQueryRepository userSearchQueryRepository;

  @Autowired
  private SearchResponseRepository searchResponseRepository;

  @AfterEach
  void cleanup() {
    requestMessageRepository.deleteAllInBatch();
  }

  @Test
  void shouldSendKafkaMessage() {
    var kafkaMessage = createKafkaMessage();

    await()
      .atMost(Duration.ofSeconds(10))
      .pollInterval(Duration.ofMillis(500))
      .untilAsserted(() -> {
        var firstMessage = requestMessageRepository.findAll().getFirst();
        assertThat(firstMessage.getStatus()).isEqualTo(OutboxMessageStatus.SENT);

        var userQuery = userSearchQueryRepository.findById(kafkaMessage.getDto().getSearchQueryId());
        assertThat(userQuery.get().getStatus()).isEqualTo(UserSearchQueryStatus.IN_PROCESSING);

        var searchResponse = searchResponseRepository
          .findById(kafkaMessage.getDto().getSearchResponses().getFirst().getSearchResponseId());
        assertThat(searchResponse.get().getStatus()).isEqualTo(SearchResponseStatus.IN_PROCESSING);
      });
  }

  private SearchResponseMessage createKafkaMessage() {
    var searchQuery = new UserSearchQuery();
    searchQuery.setUserId(UUID.randomUUID());
    searchQuery.setQueryText("text");
    searchQuery.setStatus(UserSearchQueryStatus.CREATED);
    var query = userSearchQueryRepository.save(searchQuery);

    var searchResponseEntity = new SearchResponse();
    searchResponseEntity.setStatus(SearchResponseStatus.CREATED);
    searchResponseEntity.setUrl("url");
    searchResponseEntity.setDomain("domain");
    searchResponseEntity.setTitle(("title"));
    var response = searchResponseRepository.save(searchResponseEntity);

    var kafkaMessage = new SearchResponseMessage();
    kafkaMessage.setStatus(OutboxMessageStatus.CREATED);

    var dto = new MessageSearchResponseDto();
    dto.setSearchQueryId(query.getId());
    dto.setUserId(UUID.randomUUID());

    var searchResponse = new MessageSearchResponseDto.SearchResponse();
    searchResponse.setUrl("url");
    searchResponse.setDomain("domain");
    searchResponse.setSearchResponseId(response.getId());
    dto.getSearchResponses().add(searchResponse);
    kafkaMessage.setDto(dto);
    return requestMessageRepository.save(kafkaMessage);
  }
}
