package rikser123.yandexfetcher.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rikser123.yandexfetcher.repository.entity.RequestResult;

import java.util.UUID;

@Repository
public interface RequestResultRepository extends JpaRepository<RequestResult, UUID> {
}
