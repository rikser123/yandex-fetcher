package rikser123.yandexfetcher.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import rikser123.yandexfetcher.repository.entity.QueryPerDayLimit;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface QueryPerDayLimitRepository extends JpaRepository<QueryPerDayLimit, UUID> {
  @Query("""
    SELECT r FROM QueryPerDayLimit r WHERE r.userId = :userId 
    AND CURRENT_TIMESTAMP BETWEEN r.startTime AND r.endTime ORDER BY r.created LIMIT 1
    """)
  Optional<QueryPerDayLimit> findByUserId(UUID userId);
}
