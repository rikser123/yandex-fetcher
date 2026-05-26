package rikser123.yandexfetcher.service;

import jakarta.servlet.http.HttpServletRequest;
import rikser123.bundle.dto.response.RikserResponseItem;
import rikser123.yandexfetcher.dto.YandexSearchRequestDto;
import rikser123.yandexfetcher.dto.YandexSearchResponseDto;

/**
 * Сервис для асинхронного поиска через Yandex Search API.
 * <p>
 * При вызове метода
 * <ul>
 *   <li>Запускается фоновая задача в виртуальном потоке</li>
 *   <li>Метод сразу возвращает DTO с ID запроса (асинхронный режим)</li>
 * </ul>
 * Результат поиска обрабатывается асинхронно и сохраняется отдельно.
 */
public interface YandexSearchService {

  /**
   * Запускает асинхронный поиск.
   *
   * @param searchDto параметры поискового запроса (текст, фильтры, группировка)
   * @return объект с ID запроса для последующего отслеживания статуса
   */
  RikserResponseItem<YandexSearchResponseDto> search(
    YandexSearchRequestDto searchDto,
    HttpServletRequest servletRequest)
  ;
}