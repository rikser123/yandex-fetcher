package rikser123.yandexfetcher.service;

import com.optimaize.langdetect.LanguageDetector;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import rikser123.bundle.dto.User;
import rikser123.bundle.service.RedisCacheService;
import rikser123.bundle.service.UserDetailService;
import rikser123.yandexfetcher.component.PrometheusMetrics;
import rikser123.yandexfetcher.component.YandexResponseXmlParser;
import rikser123.yandexfetcher.config.YandexProperties;
import rikser123.yandexfetcher.dto.YandexResponse;
import rikser123.yandexfetcher.dto.request.YandexSearchQueryDto;
import rikser123.yandexfetcher.dto.response.UserResponseTarifDto;
import rikser123.yandexfetcher.dto.response.YandexResponseAsyncDto;
import rikser123.yandexfetcher.dto.response.YandexResponseOperationDto;
import rikser123.yandexfetcher.feign.YandexOperationClient;
import rikser123.yandexfetcher.feign.YandexSearchClient;
import rikser123.yandexfetcher.mapper.YandexMapper;
import rikser123.yandexfetcher.repository.entity.FamilyMode;
import rikser123.yandexfetcher.repository.entity.GroupsOnPage;
import rikser123.yandexfetcher.repository.entity.UserSearchQuery;
import rikser123.yandexfetcher.repository.entity.UserSearchQueryStatus;
import rikser123.yandexfetcher.service.impl.YandexServiceImpl;
import static org.awaitility.Awaitility.await;
import com.optimaize.langdetect.i18n.LdLocale;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
public class YandexServiceTest {
  private YandexSearchService yandexService;

  @Mock
  private YandexSearchClient yandexSearchClient;

  @Mock
  private YandexOperationClient yandexOperationClient;

  @Mock
  private UserSearchQueryService userSearchQueryService;

  @Mock
  private RedisCacheService redisCacheService;

  @Mock
  private UserDetailService userDetailService;

  @Mock
  private LanguageDetector languageDetector;

  @Mock
  private Ip2RegionService ip2RegionService;

  @Mock
  private QueryPerDayLimitService queryPerDayLimitService;

  @Mock
  private SecurityService securityService;

  @Mock
  private PrometheusMetrics prometheusMetrics;

  @Mock
  private SearchResponseService searchResponseService;

  private YandexMapper yandexMapper = Mappers.getMapper(YandexMapper.class);

  private static final MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();

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
      userSearchQueryService,
      redisCacheService,
      userDetailService,
      yandexMapper,
      languageDetector,
      ip2RegionService,
      queryPerDayLimitService,
      securityService,
      prometheusMetrics,
      searchResponseService
    );

    when(languageDetector.detect(any(CharSequence.class))).thenReturn(com.google.common.base.Optional.of((LdLocale.fromString("ru"))));
    when(ip2RegionService.getCountryCode(any())).thenReturn("ru");

    var tarifData = new UserResponseTarifDto();
    var tarifInfo = new UserResponseTarifDto.UserTarifResponseDto();
    tarifInfo.setRequestPerDay(50);
    tarifData.setTarif(tarifInfo);

    when(securityService.getUserTarif(any())).thenReturn(tarifData.getTarif());
  }

  @Test
  void shouldCatchErrorWhenGetOperationIdMaxAttempts() {
    var request = createRequest();
    var searchDto = new YandexSearchQueryDto();
    searchDto.setQueryText("queryText");

    when(userSearchQueryService.saveByYandexRequest(any())).thenReturn(request);
    when(yandexSearchClient.search(any(), any())).thenReturn(new YandexResponseAsyncDto());
    when(userDetailService.getCurrentUser()).thenReturn(new User());
    yandexService.search(searchDto, mockHttpServletRequest);

    await()
      .atMost(Duration.ofSeconds(5))
      .pollInterval(Duration.ofMillis(500))
      .untilAsserted(() -> {
        verify(yandexSearchClient, times(4)).search(any(), any());
        verify(userSearchQueryService, times(1)).changeStatus(any(), eq(UserSearchQueryStatus.FAILED));
      });
  }

  @Test
  void shouldCatchErrorWhenGetSearchResultsMaxAttempts() {
    var request = createRequest();
    var searchDto = new YandexSearchQueryDto();
    searchDto.setQueryText("queryText");

    var asyncDto = new YandexResponseAsyncDto();
    asyncDto.setId(UUID.randomUUID().toString());

    when(userSearchQueryService.saveByYandexRequest(any())).thenReturn(request);
    when(yandexSearchClient.search(any(), any())).thenReturn(asyncDto);
    when(yandexOperationClient.getSearchData(any(), any())).thenReturn(new YandexResponseOperationDto());
    when(userDetailService.getCurrentUser()).thenReturn(new User());
    yandexService.search(searchDto, mockHttpServletRequest);

    await()
      .atMost(Duration.ofSeconds(5))
      .pollInterval(Duration.ofMillis(500))
      .untilAsserted(() -> {
        verify(yandexOperationClient, times(4)).getSearchData(any(), any());
        verify(userSearchQueryService, times(1)).changeStatus(any(), eq(UserSearchQueryStatus.FAILED));
      });
  }

  @Test
  @SneakyThrows
  void shouldSavedDocs() {
    var request = createRequest();
    var searchDto = new YandexSearchQueryDto();
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

    when(userSearchQueryService.saveByYandexRequest(any())).thenReturn(request);
    when(yandexSearchClient.search(any(), any())).thenReturn(asyncDto);
    when(yandexOperationClient.getSearchData(any(), any())).thenReturn(operationDto);
    when(userDetailService.getCurrentUser()).thenReturn(new User());
    yandexService.search(searchDto, mockHttpServletRequest);

    await()
      .atMost(Duration.ofSeconds(5))
      .pollInterval(Duration.ofMillis(500))
      .untilAsserted(() -> {
        verify(searchResponseService, times(1)).saveSearchResponses(argThat(arg -> {
          assertThat(arg.size()).isEqualTo(10);
          return true;
        }), any());
        verify(userSearchQueryService, times(1)).changeStatus(any(), eq(UserSearchQueryStatus.IN_PROCESSING));
      });
  }

  @Test
  void shouldReturnExistedProcessingRequest() {
    var request = createRequest();
    request.setStatus(UserSearchQueryStatus.IN_PROCESSING);
    var user = new User();
    user.setId(UUID.randomUUID());
    var searchDto = new YandexSearchQueryDto();
    searchDto.setQueryText("queryText");

    when(userDetailService.getCurrentUser()).thenReturn(user);
    when(userSearchQueryService.findProcessingQuery(eq(user.getId()), eq(request.getQueryText()))).thenReturn(Optional.of(request));

    var result = yandexService.search(searchDto, mockHttpServletRequest);
    assertThat(result.getData().getQueryId()).isEqualTo(request.getId());
  }

  private static UserSearchQuery createRequest() {
    var request = new UserSearchQuery();
    request.setUserId(UUID.randomUUID());
    request.setId(UUID.randomUUID());
    request.setFamilyMode(FamilyMode.FAMILY_MODE_MODERATE);
    request.setGroupsOnPage(GroupsOnPage.TEN);
    request.setQueryText("queryText");
    return request;
  }
}
