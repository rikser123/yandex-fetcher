package rikser123.yandexfetcher.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import rikser123.bundle.service.StatusMatrix;
import rikser123.bundle.service.impl.StatusMatrixImpl;
import rikser123.yandexfetcher.repository.entity.RequestStatus;

import java.util.EnumSet;

@Configuration
public class StatusMatrixConfig {

  @Bean
  public StatusMatrix<RequestStatus> requestStatusMatrix() {
    var statusMatrix = new StatusMatrixImpl<RequestStatus>();
    statusMatrix.addTransition(RequestStatus.CREATED, EnumSet.of(RequestStatus.IN_PROCESSING, RequestStatus.FAILED));
    statusMatrix.addTransition(RequestStatus.IN_PROCESSING, EnumSet.of(RequestStatus.PROCESSED, RequestStatus.FAILED));

    return statusMatrix;
  }
}
