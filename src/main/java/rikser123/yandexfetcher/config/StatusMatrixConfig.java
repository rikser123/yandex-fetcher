package rikser123.yandexfetcher.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import rikser123.bundle.service.StatusMatrix;
import rikser123.bundle.service.impl.StatusMatrixImpl;
import rikser123.yandexfetcher.repository.entity.SearchResponseStatus;
import rikser123.yandexfetcher.repository.entity.UserSearchQueryStatus;

import java.util.EnumSet;

@Configuration
public class StatusMatrixConfig {

  @Bean
  public StatusMatrix<UserSearchQueryStatus> userQueryStatusMartrix() {
    var statusMatrix = new StatusMatrixImpl<UserSearchQueryStatus>();
    statusMatrix.addTransition(UserSearchQueryStatus.CREATED, EnumSet.of(UserSearchQueryStatus.IN_PROCESSING, UserSearchQueryStatus.FAILED));
    statusMatrix.addTransition(UserSearchQueryStatus.IN_PROCESSING, EnumSet.of(UserSearchQueryStatus.PROCESSED, UserSearchQueryStatus.FAILED));

    return statusMatrix;
  }

  @Bean
  public StatusMatrix<SearchResponseStatus> searchResponseStatusMatrix() {
    var statusMatrix = new StatusMatrixImpl<SearchResponseStatus>();
    statusMatrix.addTransition(SearchResponseStatus.CREATED, EnumSet.of(SearchResponseStatus.IN_PROCESSING, SearchResponseStatus.FAILED));
    statusMatrix.addTransition(SearchResponseStatus.IN_PROCESSING, EnumSet.of(SearchResponseStatus.PROCESSED, SearchResponseStatus.FAILED));

    return statusMatrix;
  }
}
