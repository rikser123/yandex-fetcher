package rikser123.yandexfetcher.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rikser123.yandexfetcher.repository.entity.QueryAnalysis;

import java.util.UUID;

public interface QueryAnalysisRepository extends JpaRepository<QueryAnalysis, UUID> {
}
