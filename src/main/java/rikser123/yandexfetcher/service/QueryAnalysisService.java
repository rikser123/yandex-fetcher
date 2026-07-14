package rikser123.yandexfetcher.service;

import rikser123.yandexfetcher.dto.QueryAnalysisDto;
import rikser123.yandexfetcher.repository.entity.QueryAnalysis;
import rikser123.yandexfetcher.repository.entity.UserSearchQuery;

/**
 * Сервис для работы с анализами поисковых запросов
 */
public interface QueryAnalysisService {

  /**
   * Сохраняет анализ поискового запроса
   *
   * @param dto   DTO с данными анализа
   * @param query сущность поискового запроса, к которому привязывается анализ
   * @return сохраненная сущность анализа
   */
  QueryAnalysis save(QueryAnalysisDto dto, UserSearchQuery query);
}