package rikser123.yandexfetcher.repository;

import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rikser123.yandexfetcher.repository.entity.KafkaEntityStatus;
import rikser123.yandexfetcher.repository.entity.KafkaRequestMessage;

import java.util.List;
import java.util.UUID;

@Repository
public interface KafkaRequestMessageRepository extends JpaRepository<KafkaRequestMessage, UUID> {
  List<KafkaRequestMessage> findAllByStatusOrderByCreatedAsc(KafkaEntityStatus status, Limit limit);
}
