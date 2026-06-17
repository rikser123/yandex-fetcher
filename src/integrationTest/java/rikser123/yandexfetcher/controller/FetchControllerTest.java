package rikser123.yandexfetcher.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import rikser123.bundle.component.KafkaProducerInterceptor;
import rikser123.bundle.dto.User;
import rikser123.bundle.service.UserDetailService;
import rikser123.yandexfetcher.BaseConfig;
import rikser123.yandexfetcher.IntegrationUtils;
import rikser123.yandexfetcher.dto.request.YandexSearchQueryDto;
import rikser123.yandexfetcher.repository.UserSearchQueryRepository;
import rikser123.yandexfetcher.repository.SearchResponseRepository;
import rikser123.yandexfetcher.repository.entity.UserSearchQuery;
import rikser123.yandexfetcher.repository.entity.SearchResponse;
import rikser123.yandexfetcher.repository.entity.SearchResponseStatus;
import rikser123.yandexfetcher.repository.entity.UserSearchQueryStatus;

import java.time.Duration;
import java.util.Set;
import java.util.UUID;

import static org.awaitility.Awaitility.await;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class FetchControllerTest extends BaseConfig {
  @Autowired
  private UserSearchQueryRepository userSearchQueryRepository;

  @Autowired
  private SearchResponseRepository searchResponseRepository;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private UserDetailService userDetailService;

  @MockitoBean
  private KafkaProducerInterceptor kafkaProducerInterceptor;

  @BeforeEach
  void cleanUp() {
    userSearchQueryRepository.deleteAllInBatch();
    searchResponseRepository.deleteAllInBatch();
  }


  @Test
  void fetchData() throws Exception {
   var searchDto = new YandexSearchQueryDto();
   searchDto.setQueryText("text");
   var user = new User();
   user.setPrivileges(Set.of("CREATE_REQUEST", "CHECK_SPELLS"));
   user.setId(UUID.randomUUID());

   getYandexSearch();
   getOperationSearch();
   getSecurityClientUserWithTarif(user.getId());

   when(userDetailService.getCurrentUser()).thenReturn(user);

  client.perform(post("/api/v1/yandex-fetcher/search")
      .contentType(MediaType.APPLICATION_JSON)
      .content(objectMapper.writeValueAsString(IntegrationUtils.buildRequest(searchDto))))
    .andExpect(status().isOk())
    .andExpect(jsonPath("$.result").value(true))
    .andExpect(jsonPath("$.data.queryId").isNotEmpty());

    await()
      .atMost(Duration.ofSeconds(5))
      .pollInterval(Duration.ofMillis(500))
      .untilAsserted(() -> {
        assertThat(searchResponseRepository.findAll().size()).isEqualTo(10);
        var allRequests = userSearchQueryRepository.findAll();
        assertThat(allRequests.getFirst().getStatus()).isEqualTo(UserSearchQueryStatus.IN_PROCESSING);
      });
  }

  @Test
  void findQueries() throws Exception {
    var user = new User();
    user.setPrivileges(Set.of("CREATE_REQUEST", "CHECK_SPELLS", "VIEW_REQUEST"));
    user.setId(UUID.randomUUID());
    var request = createRequest(user.getId());

    when(userDetailService.getCurrentUser()).thenReturn(user);

    client.perform(get("/api/v1/requests")
        .contentType(MediaType.APPLICATION_JSON)
        .queryParam("status", "CREATED")
        .queryParam("queryText", "queryText")
       )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.result").value(true))
      .andExpect(jsonPath("$.data.searchQueries[0].id").value(request.getId().toString()))
      .andExpect(jsonPath("$.data.searchQueries[0].userId").value(user.getId().toString()))
      .andExpect(jsonPath("$.data.searchQueries[0].responses").isNotEmpty());
  }

  private UserSearchQuery createRequest(UUID userId) {
    var request = new UserSearchQuery();
    request.setUserId(userId);
    request.setQueryText("queryText");
    request.setStatus(UserSearchQueryStatus.CREATED);

    var requestResult = new SearchResponse();
    requestResult.setStatus(SearchResponseStatus.CREATED);
    requestResult.setUrl("url");
    requestResult.setDomain("domain");
    requestResult.setTitle("title");
    request.getResponses().add(requestResult);
    requestResult.setUserSearchQuery(request);

    return userSearchQueryRepository.save(request);
  }
}
