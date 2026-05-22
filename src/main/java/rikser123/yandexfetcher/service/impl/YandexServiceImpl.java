package rikser123.yandexfetcher.service.impl;

import com.optimaize.langdetect.LanguageDetector;
import com.optimaize.langdetect.i18n.LdLocale;
import com.optimaize.langdetect.text.CommonTextObjectFactories;
import com.optimaize.langdetect.text.TextObjectFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import rikser123.bundle.dto.User;
import rikser123.bundle.dto.response.RikserResponseItem;
import rikser123.bundle.service.RedisCacheService;
import rikser123.bundle.service.UserDetailService;
import rikser123.bundle.utils.RikserResponseUtils;
import rikser123.yandexfetcher.component.YandexResponseXmlParser;
import rikser123.yandexfetcher.config.YandexProperties;
import rikser123.yandexfetcher.dto.YandexRequestDto;
import rikser123.yandexfetcher.dto.YandexResponseOperationDto;
import rikser123.yandexfetcher.dto.YandexResponseXMLData;
import rikser123.yandexfetcher.dto.YandexSearchRequestDto;
import rikser123.yandexfetcher.dto.YandexSearchResponseDto;
import rikser123.yandexfetcher.feign.YandexOperationClient;
import rikser123.yandexfetcher.feign.YandexSearchClient;
import rikser123.yandexfetcher.mapper.YandexMapper;
import rikser123.yandexfetcher.repository.entity.Request;
import rikser123.yandexfetcher.repository.entity.RequestStatus;
import rikser123.yandexfetcher.service.RequestService;
import rikser123.yandexfetcher.service.YandexService;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@RequiredArgsConstructor
@Slf4j
public class YandexServiceImpl implements YandexService {
  private final YandexSearchClient yandexClient;
  private final YandexOperationClient yandexOperationClient;
  private final YandexResponseXmlParser xmlParser;
  private final YandexProperties yandexProperties;
  private final RequestService requestService;
  private final RedisCacheService redisCacheService;
  private final UserDetailService userDetailService;
  private final YandexMapper yandexMapper;
  private final LanguageDetector languageDetector;

  private static final TextObjectFactory textFactory = CommonTextObjectFactories.forDetectingShortCleanText();

  private static final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
  private static final String API_KEY = "Api-Key";
  private static final String DEFAULT_LANGUAGE = "ru";

  @Override
  public RikserResponseItem<YandexSearchResponseDto> search(YandexSearchRequestDto searchDto) {
    var currentUser = (User) userDetailService.getCurrentUser();
    var existedRequestOpt = requestService.findProcessingRequest(currentUser.getId(), searchDto.getQueryText());

    if (existedRequestOpt.isPresent()) {
      return createSearchResponse(existedRequestOpt.get());
    }

    Optional<Request> existedQueryOpt = redisCacheService.get(searchDto.getQueryText(), Request.class);
    if (existedQueryOpt.isPresent()) {
      var request = existedQueryOpt.get();
      return createSearchResponse(request);
    }

    var requestDto = yandexMapper.mapToRequestDto(searchDto);
    var localization = getLocalization(searchDto.getQueryText());
    requestDto.setL10n(localization);
    var authHeader = API_KEY + " " + yandexProperties.getToken();
    var request = requestService.saveByYandexRequest(searchDto);
    var currentAttempts = 0;

    CompletableFuture
      .supplyAsync(() -> getOperationId(requestDto, authHeader, currentAttempts), executor)
      .thenApply(operationId -> getSearchResults(operationId, authHeader, currentAttempts))
      .thenApply(result -> {
        var data = result.getResponse().getRawData();
        var parsedResponse = xmlParser.parseRawResponse(data);
        var docs = getDocs(parsedResponse);
        return requestService.saveRequestResults(docs, request);
      })
      .whenComplete((result, error) -> {
        if (!Objects.isNull(result)) {
          log.info("successfully saved {}", result);
          requestService.changeStatus(request, RequestStatus.IN_PROCESSING);
          redisCacheService.put(searchDto.getQueryText(), request);
        } else if (!Objects.isNull(error)) {
          log.warn("error get operation with query {} {}", searchDto.getQueryText(), error);
          requestService.changeStatus(request, RequestStatus.FAILED);
        }
      });

   return createSearchResponse(request);
  }

  private String getOperationId(YandexRequestDto searchDto, String authHeader, int attempts) {
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
      .toList();
  }

  private RikserResponseItem<YandexSearchResponseDto> createSearchResponse(Request request) {
    var responseDto = new YandexSearchResponseDto();
    responseDto.setRequestId(request.getId());
    return RikserResponseUtils.createResponse(responseDto);
  }

  private YandexRequestDto.Localization getLocalization(String text) {
    var textObject = textFactory.forText(text);
    var langOpt = Optional.ofNullable(languageDetector.detect(textObject))
      .map(opt -> opt.isPresent() ? opt.get() : null)
      .map(LdLocale::getLanguage)
      .orElse(DEFAULT_LANGUAGE);

    return switch (langOpt) {
      case "uk" -> YandexRequestDto.Localization.LOCALIZATION_UK;
      case "be" -> YandexRequestDto.Localization.LOCALIZATION_BE;
      case "kk" -> YandexRequestDto.Localization.LOCALIZATION_KK;
      case "tr" -> YandexRequestDto.Localization.LOCALIZATION_TR;
      case "en" -> YandexRequestDto.Localization.LOCALIZATION_EN;
      default -> YandexRequestDto.Localization.LOCALIZATION_RU;
    };
  }
}
