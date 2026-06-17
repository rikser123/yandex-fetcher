package rikser123.yandexfetcher.service;

import org.springframework.data.domain.Page;
import rikser123.bundle.exception.StatusChangeException;
import rikser123.yandexfetcher.dto.request.YandexQueryListDto;
import rikser123.yandexfetcher.dto.request.YandexSearchQueryDto;
import rikser123.yandexfetcher.dto.response.UserSearchQueryDto;
import rikser123.yandexfetcher.repository.entity.UserSearchQuery;
import rikser123.yandexfetcher.repository.entity.UserSearchQueryStatus;

import java.util.Optional;
import java.util.UUID;

/**
 * Сервис для управления запросами и результатами поиска.
 */
public interface UserSearchQueryService {

  /**
   * Сохраняет запрос в БД.
   *
   * @param userSearchQuery сущность запроса
   * @return сохранённый запрос с присвоенным ID
   */
  UserSearchQuery save(UserSearchQuery userSearchQuery);

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
  UserSearchQuery saveByYandexRequest(YandexSearchQueryDto dto);


  /**
   * Изменение статуса запроса>
   *
   * @param userSearchQuery    запрос к яндексу
   * @param status новый статус запроса
   * @return новый запрос
   * @throws StatusChangeException если статус невозможно изменить
   */
  UserSearchQuery changeStatus(UserSearchQuery userSearchQuery, UserSearchQueryStatus status);

  /**
   * Найти текущий запрос в обработке для пользователя
   *
   * @param userId    Id пользователя
   * @param queryText запрос пользователя
   * @return новый запрос
   */
  Optional<UserSearchQuery> findProcessingQuery(UUID userId, String queryText);

  /**
   * Найти все записи пользователя
   *
   * @param filter    Фильтры на запросы пользователя
   * @return Список запросов пользователя в яндекс
   */
  Page<UserSearchQueryDto> findAll(YandexQueryListDto filter);
}
