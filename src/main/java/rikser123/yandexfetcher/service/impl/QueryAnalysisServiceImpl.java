package rikser123.yandexfetcher.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rikser123.yandexfetcher.dto.QueryAnalysisDto;
import rikser123.yandexfetcher.mapper.QueryAnalysisMapper;
import rikser123.yandexfetcher.repository.QueryAnalysisRepository;
import rikser123.yandexfetcher.repository.entity.QueryAnalysis;
import rikser123.yandexfetcher.repository.entity.UserSearchQuery;
import rikser123.yandexfetcher.service.QueryAnalysisService;

@Service
@RequiredArgsConstructor
@Slf4j
public class QueryAnalysisServiceImpl implements QueryAnalysisService {
  private final QueryAnalysisRepository queryAnalysisRepository;
  private final QueryAnalysisMapper queryAnalysisMapper;

  @Transactional
  @Override
  public QueryAnalysis save(QueryAnalysisDto dto, UserSearchQuery query) {
    var analysis = queryAnalysisMapper.mapToEntity(dto);
    analysis.setUserSearchQuery(query);
    return queryAnalysisRepository.save(analysis);
  }
}
