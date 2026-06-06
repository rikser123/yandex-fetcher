package rikser123.yandexfetcher.service;

import rikser123.yandexfetcher.repository.entity.RequestPerDayLimit;

import java.util.UUID;

public interface RequestPerDayLimitService {

  /**
   * Проверяет и обновляет лимит запросов пользователя
   * @param userId ID пользователя
   * @param requestLimit максимальное количество запросов
   * @return обновленный лимит
   * @throws IllegalStateException если превышен лимит
   */
  RequestPerDayLimit checkLimit(UUID userId, Integer requestLimit);
}
