package rikser123.yandexfetcher.repository;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import rikser123.yandexfetcher.repository.entity.QueryAnalysisStatus;
import rikser123.yandexfetcher.repository.entity.UserSearchQuery;
import rikser123.yandexfetcher.repository.entity.UserSearchQueryStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserSearchQueryRepository extends JpaRepository<UserSearchQuery, UUID>,
  JpaSpecificationExecutor<UserSearchQuery> {


  Optional<UserSearchQuery> findByUserIdAndQueryTextAndStatusIsIn(UUID userId, String queryText, List<UserSearchQueryStatus> status);

  List<UserSearchQuery> findAllByStatusOrderByCreated(UserSearchQueryStatus status, PageRequest size);

  @Query("""
    SELECT q FROM UserSearchQuery q
    LEFT JOIN FETCH q.analyses a
    WHERE q.queryText =:queryText
    AND q.status =: queryStatus
    AND a.status =:analysisStatus
  """)
  Optional<UserSearchQuery> findByQueryTextAndStatus(String queryText, UserSearchQueryStatus queryStatus, QueryAnalysisStatus analysisStatus);
}
