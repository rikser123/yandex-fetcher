package rikser123.yandexfetcher.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rikser123.bundle.exception.StatusChangeException;
import rikser123.bundle.service.StatusMatrix;
import rikser123.yandexfetcher.dto.QueryAnalysisDto;
import rikser123.yandexfetcher.mapper.QueryAnalysisMapper;
import rikser123.yandexfetcher.repository.QueryAnalysisRepository;
import rikser123.yandexfetcher.repository.entity.QueryAnalysis;
import rikser123.yandexfetcher.repository.entity.QueryAnalysisStatus;
import rikser123.yandexfetcher.repository.entity.UserSearchQuery;
import rikser123.yandexfetcher.service.QueryAnalysisService;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class QueryAnalysisServiceImpl implements QueryAnalysisService {
  private final QueryAnalysisRepository queryAnalysisRepository;
  private final QueryAnalysisMapper queryAnalysisMapper;
  private final StatusMatrix<QueryAnalysisStatus> queryAnalysisStatusStatusMatrix;

  @Transactional
  @Override
  public QueryAnalysis save(QueryAnalysisDto dto, UserSearchQuery query) {
    var analysis = queryAnalysisMapper.mapToEntity(dto);
    analysis.setUserSearchQuery(query);
    return queryAnalysisRepository.save(analysis);
  }

  @Override
  public List<QueryAnalysis> findOutdatedAnalysis() {
    var timeLimit = Instant.now().minus(7, ChronoUnit.DAYS);
    return queryAnalysisRepository.findAllByCreatedLessThanAndStatus(timeLimit, QueryAnalysisStatus.CREATED);
  }

  @Override
  @Transactional
  public QueryAnalysis changeStatus(QueryAnalysis queryAnalysis, QueryAnalysisStatus status) {
    if (queryAnalysis.getStatus() == status || !queryAnalysisStatusStatusMatrix.isAvailable(queryAnalysis.getStatus(), status)) {
      log.warn(
        "ERROR: while checkStatusMovement for query analysis: {} from: {} to: {}",
        queryAnalysis.getId(),
        queryAnalysis.getStatus(),
        status);
      throw new StatusChangeException();
    }

    queryAnalysis.setStatus(status);
    queryAnalysisRepository.save(queryAnalysis);
    return queryAnalysis;
  }
}
