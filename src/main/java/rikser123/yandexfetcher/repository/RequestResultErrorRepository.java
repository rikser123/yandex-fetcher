package rikser123.yandexfetcher.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rikser123.yandexfetcher.repository.entity.RequestResultError;

import java.util.UUID;

@Repository
public interface RequestResultErrorRepository extends JpaRepository<RequestResultError, UUID> {
}
