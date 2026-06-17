package rikser123.yandexfetcher.repository;

import org.springframework.stereotype.Repository;
import rikser123.bundle.repository.OutboxMessageRepository;
import rikser123.yandexfetcher.repository.entity.SearchResponseMessage;

@Repository
public interface SearchResponseMessageRepository extends OutboxMessageRepository<SearchResponseMessage> {
}
