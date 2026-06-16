package rikser123.yandexfetcher.service;

import org.springframework.data.domain.Page;
import rikser123.bundle.exception.StatusChangeException;
import rikser123.yandexfetcher.dto.request.YandexRequestListDto;
import rikser123.yandexfetcher.dto.response.RequestResponseDto;
import rikser123.yandexfetcher.dto.response.YandexResponseXMLData;
import rikser123.yandexfetcher.dto.request.YandexSearchRequestDto;
import rikser123.yandexfetcher.repository.entity.Request;
import rikser123.yandexfetcher.repository.entity.RequestResult;
import rikser123.yandexfetcher.repository.entity.RequestStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Сервис для управления запросами и результатами поиска.
 */
public interface RequestService {

  /**
   * Сохраняет запрос в БД.
   *
   * @param request сущность запроса
   * @return сохранённый запрос с присвоенным ID
   */
  Request save(Request request);

  /**
   * Создаёт и сохраняет запрос на основе DTO от Yandex API.
   * <p>
   * Извлекает текущего пользователя, устанавливает параметры поиска
   * со значениями по умолчанию и сохраняет запрос.
   * </p>
   *
   * @param dto DTO с параметрами поискового запроса
   * @return сохранённый запрос
   */
  Request saveByYandexRequest(YandexSearchRequestDto dto);


  /**
   * Изменение статуса запроса>
   *
   * @param request    запрос к яндексу
   * @param status новый статус запроса
   * @return новый запрос
   * @throws StatusChangeException если статус невозможно изменить
   */
  Request changeStatus(Request request, RequestStatus status);

  /**
   * Найти текущий запрос в обработке для пользователя
   *
   * @param userId    Id пользователя
   * @param queryText запрос пользователя
   * @return новый запрос
   */
  Optional<Request> findProcessingRequest(UUID userId, String queryText);

  /**
   * Найти все записи пользователя
   *
   * @param filter    Фильтры на запросы пользователя
   * @return Список запросов пользователя в яндекс
   */
  Page<RequestResponseDto> findAll(YandexRequestListDto filter);
}
