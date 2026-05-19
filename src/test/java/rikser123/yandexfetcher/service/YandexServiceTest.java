package rikser123.yandexfetcher.service;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import rikser123.yandexfetcher.component.YandexResponseXmlParser;
import rikser123.yandexfetcher.configuration.YandexProperties;
import rikser123.yandexfetcher.dto.YandexResponse;
import rikser123.yandexfetcher.dto.YandexResponseAsyncDto;
import rikser123.yandexfetcher.dto.YandexResponseOperationDto;
import rikser123.yandexfetcher.dto.YandexSearchRequestDto;
import rikser123.yandexfetcher.feign.YandexOperationClient;
import rikser123.yandexfetcher.feign.YandexSearchClient;
import rikser123.yandexfetcher.repository.entity.FamilyMode;
import rikser123.yandexfetcher.repository.entity.GroupsOnPage;
import rikser123.yandexfetcher.repository.entity.Request;
import rikser123.yandexfetcher.service.impl.YandexServiceImpl;
import static org.awaitility.Awaitility.await;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
public class YandexServiceTest {
  private YandexService yandexService;

  @Mock
  private YandexSearchClient yandexSearchClient;

  @Mock
  private YandexOperationClient yandexOperationClient;

  @Mock
  private RequestService requestService;

  @BeforeEach
  void init() {
    var yandexProperties = new YandexProperties();
    yandexProperties.setDelay(500);
    yandexProperties.setToken("token");
    yandexProperties.setMaxAttempts(4);

    yandexService = new YandexServiceImpl(
      yandexSearchClient,
      yandexOperationClient,
      new YandexResponseXmlParser(),
      yandexProperties,
      requestService
    );
  }

  @Test
  void shouldCatchErrorWhenGetOperationIdMaxAttempts() {
    var request = createRequest();
    var searchDto = new YandexSearchRequestDto();
    searchDto.setQueryText("queryText");

    when(requestService.saveByYandexRequest(any())).thenReturn(request);
    when(yandexSearchClient.search(any(), any())).thenReturn(new YandexResponseAsyncDto());
    yandexService.search(searchDto);

    await()
      .atMost(Duration.ofSeconds(5))
      .pollInterval(Duration.ofMillis(500))
      .untilAsserted(() -> {
        verify(yandexSearchClient, times(4)).search(any(), any());
      });
  }

  @Test
  void shouldCatchErrorWhenGetSearchResultsMaxAttempts() {
    var request = createRequest();
    var searchDto = new YandexSearchRequestDto();
    searchDto.setQueryText("queryText");

    var asyncDto = new YandexResponseAsyncDto();
    asyncDto.setId(UUID.randomUUID().toString());

    when(requestService.saveByYandexRequest(any())).thenReturn(request);
    when(yandexSearchClient.search(any(), any())).thenReturn(asyncDto);
    when(yandexOperationClient.getSearchData(any(), any())).thenReturn(new YandexResponseOperationDto());
    yandexService.search(searchDto);

    await()
      .atMost(Duration.ofSeconds(5))
      .pollInterval(Duration.ofMillis(500))
      .untilAsserted(() -> {
        verify(yandexOperationClient, times(4)).getSearchData(any(), any());
      });
  }

  @Test
  @SneakyThrows
  void shouldSavedDocs() {
    var request = createRequest();
    var searchDto = new YandexSearchRequestDto();
    searchDto.setQueryText("queryText");

    var asyncDto = new YandexResponseAsyncDto();
    asyncDto.setId(UUID.randomUUID().toString());

    var operationDto = new YandexResponseOperationDto();
    operationDto.setDone(true);
    var yandexResponse = new YandexResponse();
    var encoded = YandexServiceTest.class.getResourceAsStream("/yandex-response.txt");
    var rawData = new String(encoded.readAllBytes(), StandardCharsets.UTF_8);
    yandexResponse.setRawData(rawData);
    operationDto.setResponse(yandexResponse);

    when(requestService.saveByYandexRequest(any())).thenReturn(request);
    when(yandexSearchClient.search(any(), any())).thenReturn(asyncDto);
    when(yandexOperationClient.getSearchData(any(), any())).thenReturn(operationDto);
    yandexService.search(searchDto);

    await()
      .atMost(Duration.ofSeconds(5))
      .pollInterval(Duration.ofMillis(500))
      .untilAsserted(() -> {
        verify(requestService, times(1)).saveRequestResults(argThat(arg -> {
          assertThat(arg.size()).isEqualTo(11);
          return true;
        }), any());
      });
  }

  private static Request createRequest() {
    var request = new Request();
    request.setUserId(UUID.randomUUID());
    request.setId(UUID.randomUUID());
    request.setFamilyMode(FamilyMode.FAMILY_MODE_MODERATE);
    request.setGroupsOnPage(GroupsOnPage.TEN);
    request.setQueryText("queryText");
    return request;
  }
}
