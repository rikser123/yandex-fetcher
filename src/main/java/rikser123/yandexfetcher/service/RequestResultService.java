package rikser123.yandexfetcher.service;

import rikser123.yandexfetcher.dto.response.YandexResponseXMLData;
import rikser123.yandexfetcher.repository.entity.Request;
import rikser123.yandexfetcher.repository.entity.RequestResult;
import rikser123.yandexfetcher.repository.entity.RequestResultError;
import rikser123.yandexfetcher.repository.entity.RequestResultStatus;

import java.util.List;
import java.util.UUID;

/**
 * Сервис для управления результатами запросов
 */
public interface RequestResultService {

  /**
   * Сохранить результаты запроса
   *
   * @param docs    документы из ответа Яндекс
   * @param request запрос
   * @return сохраненные результаты
   */
  List<RequestResult> saveRequestResults(List<YandexResponseXMLData.Doc> docs, Request request);

  /**
   * Изменить статус результата
   *
   * @param requestResult результат запроса
   * @param status        новый статус
   * @return обновленный результат
   */
  RequestResult changeStatus(RequestResult requestResult, RequestResultStatus status);

  /**
   * Найти сущность по айди
   *
   * @param id айди записи
   * @return найденный результат
   */
  RequestResult findById(UUID id);

  /**
   * Сохранить ошибку
   *
   * @param error ошибка
   * @return Сохраненная ошибка
   */
  RequestResultError saveRequestResultError(RequestResultError error);
}