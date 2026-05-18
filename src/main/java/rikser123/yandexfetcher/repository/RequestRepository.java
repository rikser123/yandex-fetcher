package rikser123.yandexfetcher.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rikser123.yandexfetcher.repository.entity.Request;

import java.util.UUID;

public interface RequestRepository extends JpaRepository<Request, UUID> {
}
