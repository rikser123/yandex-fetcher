package rikser123.yandexfetcher.service;

import rikser123.bundle.dto.response.RikserResponseItem;
import rikser123.yandexfetcher.dto.YandexSpellerResponseDto;

/**
 * Сервис исправления орфографии через Яндекс.Спеллер.
 * <p>
 * Разбивает текст на слова, заменяет ошибочные слова первым вариантом исправления,
 * собирает обратно в строку.
 * </p>
 */
public interface YandexSpellerService {

  /**
   * Исправляет орфографические ошибки в тексте.
   *
   * @param text исходный текст для проверки
   * @return ответ с исправленным текстом
   */
  RikserResponseItem<YandexSpellerResponseDto> getSpellCorrection(String text);
}