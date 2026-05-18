package rikser123.yandexfetcher.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rikser123.yandexfetcher.repository.entity.RequestResult;

import java.util.UUID;

public interface RequestResultRepository extends JpaRepository<RequestResult, UUID> {
}
