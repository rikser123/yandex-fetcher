package rikser123.yandexfetcher.service.impl;

import com.optimaize.langdetect.LanguageDetector;
import com.optimaize.langdetect.i18n.LdLocale;
import com.optimaize.langdetect.text.CommonTextObjectFactories;
import com.optimaize.langdetect.text.TextObjectFactory;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import rikser123.bundle.dto.User;
import rikser123.bundle.dto.response.RikserResponseItem;
import rikser123.bundle.service.RedisCacheService;
import rikser123.bundle.service.UserDetailService;
import rikser123.bundle.utils.RikserResponseUtils;
import rikser123.yandexfetcher.component.PrometheusMetrics;
import rikser123.yandexfetcher.component.YandexResponseXmlParser;
import rikser123.yandexfetcher.config.YandexProperties;
import rikser123.yandexfetcher.dto.request.YandexQueryDto;
import rikser123.yandexfetcher.dto.request.YandexQueryListDto;
import rikser123.yandexfetcher.dto.response.UserSearchQueryListDto;
import rikser123.yandexfetcher.dto.response.YandexResponseOperationDto;
import rikser123.yandexfetcher.dto.response.YandexResponseXMLData;
import rikser123.yandexfetcher.dto.request.YandexSearchQueryDto;
import rikser123.yandexfetcher.dto.response.YandexSearchResponseDto;
import rikser123.yandexfetcher.feign.YandexOperationClient;
import rikser123.yandexfetcher.feign.YandexSearchClient;
import rikser123.yandexfetcher.mapper.YandexMapper;
import rikser123.yandexfetcher.repository.entity.UserSearchQuery;
import rikser123.yandexfetcher.repository.entity.UserSearchQueryStatus;
import rikser123.yandexfetcher.service.Ip2RegionService;
import rikser123.yandexfetcher.service.QueryPerDayLimitService;
import rikser123.yandexfetcher.service.SearchResponseService;
import rikser123.yandexfetcher.service.UserSearchQueryService;
import rikser123.yandexfetcher.service.SecurityService;
import rikser123.yandexfetcher.service.YandexSearchService;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class YandexServiceImpl implements YandexSearchService {
  private final YandexSearchClient yandexClient;
  private final YandexOperationClient yandexOperationClient;
  private final YandexResponseXmlParser xmlParser;
  private final YandexProperties yandexProperties;
  private final UserSearchQueryService userSearchQueryService;
  private final RedisCacheService redisCacheService;
  private final UserDetailService userDetailService;
  private final YandexMapper yandexMapper;
  private final LanguageDetector languageDetector;
  private final Ip2RegionService ip2RegionService;
  private final QueryPerDayLimitService queryPerDayLimitService;
  private final SecurityService securityService;
  private final PrometheusMetrics prometheusMetrics;
  private final SearchResponseService searchResponseService;

  private static final TextObjectFactory textFactory = CommonTextObjectFactories.forDetectingShortCleanText();
  private static final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
  private static final String API_KEY = "Api-Key";
  private static final String DEFAULT_LANGUAGE = "ru";

  @Override
  public RikserResponseItem<YandexSearchResponseDto> search(
    YandexSearchQueryDto searchDto,
    HttpServletRequest servletRequest
  ) {
    var currentUser = (User) userDetailService.getCurrentUser();
    var userTarifInfo = securityService.getUserTarif(currentUser.getId());

    searchDto.setQueryText(searchDto.getQueryText().strip());
    var existedRequestOpt = userSearchQueryService.findProcessingQuery(currentUser.getId(), searchDto.getQueryText());

    if (existedRequestOpt.isPresent()) {
      return createSearchResponse(existedRequestOpt.get());
    }

    prometheusMetrics.incrementTotal();

    try {
      queryPerDayLimitService.checkLimit(currentUser.getId(), userTarifInfo.getRequestPerDay());
    } catch (IllegalArgumentException ex) {
      prometheusMetrics.incrementFail();
      log.warn("Превышен лимит запросов по данному тарифу!", ex);
      return RikserResponseUtils.createResponse("Превышен лимит запросов по данному тарифу!", HttpStatus.FORBIDDEN);
    }

    Optional<UserSearchQuery> existedQueryOpt = redisCacheService.get(searchDto.getQueryText(), UserSearchQuery.class);
    if (existedQueryOpt.isPresent()) {
      prometheusMetrics.incrementCache();
      var request = existedQueryOpt.get();
      return createSearchResponse(request);
    }

    var requestDto = yandexMapper.mapToRequestDto(searchDto);
    var localization = getLocalization(searchDto.getQueryText());
    requestDto.setL10n(localization);

    var clientIp = getClientIp(servletRequest);
    var searchType = getSearchType(clientIp);
    requestDto.getQuery().setSearchType(searchType);

    var userAgent = servletRequest.getHeader("User-Agent");
    requestDto.setUserAgent(userAgent);

    var authHeader = API_KEY + " " + yandexProperties.getToken();
    var request = userSearchQueryService.saveByYandexRequest(searchDto);
    var currentAttempts = 0;

    CompletableFuture
      .supplyAsync(() -> getOperationId(requestDto, authHeader, currentAttempts), executor)
      .thenApply(operationId -> getSearchResults(operationId, authHeader, currentAttempts))
      .thenApply(result -> {
        var data = result.getResponse().getRawData();
        var parsedResponse = xmlParser.parseRawResponse(data);
        var docs = getDocs(parsedResponse);
        return searchResponseService.saveSearchResponses(docs, request);
      })
      .whenComplete((result, error) -> {
        if (!Objects.isNull(result)) {
          log.info("successfully saved {}", result);
          userSearchQueryService.changeStatus(request, UserSearchQueryStatus.IN_PROCESSING);
          redisCacheService.put(searchDto.getQueryText(), request);
          prometheusMetrics.incrementSuccess();
        } else if (!Objects.isNull(error)) {
          log.warn("error get operation with query {} {}", searchDto.getQueryText(), error);
          userSearchQueryService.changeStatus(request, UserSearchQueryStatus.FAILED);
          prometheusMetrics.incrementFail();
        }
      });

   return createSearchResponse(request);
  }

  private String getOperationId(YandexQueryDto searchDto, String authHeader, int attempts) {
    if (attempts >= yandexProperties.getMaxAttempts()) {
      throw new IllegalStateException("Превышено количество попыток на скачивание!");
    }

    var operationResult = yandexClient.search(searchDto, authHeader);
    var operationId = operationResult.getId();

    if (StringUtils.isNotEmpty(operationId))  {
      return operationId;
    }

    if (StringUtils.isEmpty(operationId)) {
      try {
        Thread.sleep(yandexProperties.getDelay());
        return getOperationId(searchDto, authHeader, attempts + 1);
      } catch (InterruptedException ex) {
        throw new IllegalStateException("Не удалось получить айди операции яндекса!", ex);
      }
    }

    throw new IllegalStateException("Не удалось получить айди операции яндекса!");
  }

  private YandexResponseOperationDto getSearchResults(String operationId, String authHeader, int attempts) {
    if (attempts >= yandexProperties.getMaxAttempts()) {
      throw new IllegalStateException("Превышено количество попыток на скачивание!");
    }

    var yandexResult = yandexOperationClient.getSearchData(operationId, authHeader);
    var isDone = yandexResult.isDone();

    if (!isDone) {
     try {
       Thread.sleep(yandexProperties.getDelay());
       return getSearchResults(operationId, authHeader, attempts + 1);
     } catch (InterruptedException ex) {
       throw new IllegalStateException("Не удалось получить айди операции яндекса!", ex);
     }
    }

    return yandexResult;
  }

  private List<YandexResponseXMLData.Doc> getDocs(YandexResponseXMLData xmlData) {
    return Optional.ofNullable(xmlData)
      .map(YandexResponseXMLData::getResponse)
      .map(YandexResponseXMLData.Response::getResults)
      .map(YandexResponseXMLData.Results::getGrouping)
      .map(YandexResponseXMLData.Grouping::getGroups)
      .stream()
      .flatMap(Collection::stream)
      .map(YandexResponseXMLData.Group::getDocs)
      .flatMap(Collection::stream)
      .filter(doc -> !Optional.ofNullable(doc).
        map(YandexResponseXMLData.Doc::getPassages)
        .map(YandexResponseXMLData.Passages::getPassages)
        .orElse(Collections.emptyList()).isEmpty())
      .toList();
  }

  private RikserResponseItem<YandexSearchResponseDto> createSearchResponse(UserSearchQuery userSearchQuery) {
    var responseDto = new YandexSearchResponseDto();
    responseDto.setQueryId(userSearchQuery.getId());
    return RikserResponseUtils.createResponse(responseDto);
  }

  private YandexQueryDto.Localization getLocalization(String text) {
    var textObject = textFactory.forText(text);
    var langOpt = Optional.ofNullable(languageDetector.detect(textObject))
      .map(opt -> opt.isPresent() ? opt.get() : null)
      .map(LdLocale::getLanguage)
      .orElse(DEFAULT_LANGUAGE);

    return switch (langOpt) {
      case "uk" -> YandexQueryDto.Localization.LOCALIZATION_UK;
      case "be" -> YandexQueryDto.Localization.LOCALIZATION_BE;
      case "kk" -> YandexQueryDto.Localization.LOCALIZATION_KK;
      case "tr" -> YandexQueryDto.Localization.LOCALIZATION_TR;
      case "en" -> YandexQueryDto.Localization.LOCALIZATION_EN;
      default -> YandexQueryDto.Localization.LOCALIZATION_RU;
    };
  }

  private YandexQueryDto.SearchType getSearchType(String ip) {
    var codeCountry = ip2RegionService.getCountryCode(ip).toUpperCase();

    return switch (codeCountry) {
      case "TR" -> YandexQueryDto.SearchType.SEARCH_TYPE_TR;
      case "KZ" -> YandexQueryDto.SearchType.SEARCH_TYPE_KK;
      case "BY" -> YandexQueryDto.SearchType.SEARCH_TYPE_BE;
      case "UZ" -> YandexQueryDto.SearchType.SEARCH_TYPE_UZ;
      case "US", "CA", "DE", "GB", "FR", "IT", "ES", "AU", "NL", "CH"
        -> YandexQueryDto.SearchType.SEARCH_TYPE_COM;
      default -> YandexQueryDto.SearchType.SEARCH_TYPE_RU;
    };
  }

  private String getClientIp(HttpServletRequest request) {
    return Stream.of(
      request.getHeader("X-Forwarded-For"),
      request.getHeader("Proxy-Client-IP"),
      request.getHeader("WL-Proxy-Client-IP")
    ).filter(StringUtils::isNotEmpty)
      .map(address -> {
      if (address.contains(",")) {
        return address.split(",")[0].strip();
      }

      return address;
    }).findFirst().orElse(null);
  }

  @Override
  public RikserResponseItem<UserSearchQueryListDto> findAll(YandexQueryListDto dto) {
    var data = userSearchQueryService.findAll(dto);
    var response = new UserSearchQueryListDto();
    response.setSearchQueries(data.stream().toList());
    response.setPageNumber(dto.getPageNumber());
    response.setTotalElements(data.getTotalElements());

    return RikserResponseUtils.createResponse(response);
  }
}
