package rikser123.yandexfetcher.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rikser123.yandexfetcher.repository.entity.UserQueryError;

import java.util.UUID;

@Repository
public interface UserQueryErrorRepository extends JpaRepository<UserQueryError, UUID> {
}
