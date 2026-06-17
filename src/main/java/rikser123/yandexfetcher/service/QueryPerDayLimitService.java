package rikser123.yandexfetcher.service;

import rikser123.yandexfetcher.repository.entity.QueryPerDayLimit;

import java.util.UUID;

public interface QueryPerDayLimitService {

  /**
   * Проверяет и обновляет лимит запросов пользователя
   * @param userId ID пользователя
   * @param requestLimit максимальное количество запросов
   * @return обновленный лимит
   * @throws IllegalStateException если превышен лимит
   */
  QueryPerDayLimit checkLimit(UUID userId, Integer requestLimit);
}
