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
import rikser123.yandexfetcher.dto.request.YandexSearchRequestDto;
import rikser123.yandexfetcher.repository.RequestRepository;
import rikser123.yandexfetcher.repository.RequestResultRepository;
import rikser123.yandexfetcher.repository.entity.Request;
import rikser123.yandexfetcher.repository.entity.RequestResult;
import rikser123.yandexfetcher.repository.entity.RequestResultStatus;
import rikser123.yandexfetcher.repository.entity.RequestStatus;

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
  private RequestRepository requestRepository;

  @Autowired
  private RequestResultRepository requestResultRepository;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private UserDetailService userDetailService;

  @MockitoBean
  private KafkaProducerInterceptor kafkaProducerInterceptor;

  @BeforeEach
  void cleanUp() {
    requestRepository.deleteAllInBatch();
    requestResultRepository.deleteAllInBatch();
  }


  @Test
  void fetchData() throws Exception {
   var searchDto = new YandexSearchRequestDto();
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
    .andExpect(jsonPath("$.data.requestId").isNotEmpty());

    await()
      .atMost(Duration.ofSeconds(5))
      .pollInterval(Duration.ofMillis(500))
      .untilAsserted(() -> {
        assertThat(requestResultRepository.findAll().size()).isEqualTo(10);
        var allRequests = requestRepository.findAll();
        assertThat(allRequests.getFirst().getStatus()).isEqualTo(RequestStatus.IN_PROCESSING);
      });
  }

  @Test
  void findRequests() throws Exception {
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
      .andExpect(jsonPath("$.data.requestResponses[0].id").value(request.getId().toString()))
      .andExpect(jsonPath("$.data.requestResponses[0].userId").value(user.getId().toString()))
      .andExpect(jsonPath("$.data.requestResponses[0].requestResults").isNotEmpty());
  }

  private Request createRequest(UUID userId) {
    var request = new Request();
    request.setUserId(userId);
    request.setQueryText("queryText");
    request.setStatus(RequestStatus.CREATED);

    var requestResult = new RequestResult();
    requestResult.setStatus(RequestResultStatus.CREATED);
    requestResult.setUrl("url");
    requestResult.setDomain("domain");
    requestResult.setTitle("title");
    request.getRequestResults().add(requestResult);
    requestResult.setRequest(request);

    return requestRepository.save(request);
  }
}
