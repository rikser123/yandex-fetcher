package rikser123.yandexfetcher.service;

import rikser123.yandexfetcher.dto.response.YandexResponseXMLData;
import rikser123.yandexfetcher.repository.entity.SearchResponse;
import rikser123.yandexfetcher.repository.entity.UserSearchQuery;
import rikser123.yandexfetcher.repository.entity.SearchResponseError;
import rikser123.yandexfetcher.repository.entity.SearchResponseStatus;

import java.util.List;
import java.util.UUID;

/**
 * Сервис для управления результатами запросов
 */
public interface SearchResponseService {
  /**
   * Найди всех по айди
   *
   * @param ids список айди запросов
   * @return сохраненные результаты
   */
  List<SearchResponse> findAllByIds(List<UUID> ids);

  /**
   * Сохранить результаты запроса
   *
   * @param docs    документы из ответа Яндекс
   * @param userSearchQuery запрос
   * @return сохраненные результаты
   */
  List<SearchResponse> saveSearchResponses(List<YandexResponseXMLData.Doc> docs, UserSearchQuery userSearchQuery);

  /**
   * Изменить статус результата
   *
   * @param searchResponse результат запроса
   * @param status        новый статус
   * @return обновленный результат
   */
  SearchResponse changeStatus(SearchResponse searchResponse, SearchResponseStatus status);

  /**
   * Найти сущность по айди
   *
   * @param id айди записи
   * @return найденный результат
   */
  SearchResponse findById(UUID id);

  /**
   * Сохранить ошибку
   *
   * @param error ошибка
   * @return Сохраненная ошибка
   */
  SearchResponseError saveSearchResponseError(SearchResponseError error);
}