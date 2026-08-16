package rikser123.yandexfetcher.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rikser123.yandexfetcher.repository.entity.QueryAnalysis;
import rikser123.yandexfetcher.repository.entity.QueryAnalysisStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface QueryAnalysisRepository extends JpaRepository<QueryAnalysis, UUID> {
  List<QueryAnalysis> findAllByCreatedLessThanAndStatus(Instant date, QueryAnalysisStatus status);
}
