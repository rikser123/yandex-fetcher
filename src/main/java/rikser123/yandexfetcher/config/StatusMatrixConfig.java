package rikser123.yandexfetcher.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import rikser123.bundle.service.StatusMatrix;
import rikser123.bundle.service.impl.StatusMatrixImpl;
import rikser123.yandexfetcher.repository.entity.RequestResultStatus;
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

  @Bean
  public StatusMatrix<RequestResultStatus> requestResultStatusMatrix() {
    var statusMatrix = new StatusMatrixImpl<RequestResultStatus>();
    statusMatrix.addTransition(RequestResultStatus.CREATED, EnumSet.of(RequestResultStatus.IN_PROCESSING, RequestResultStatus.FAILED));
    statusMatrix.addTransition(RequestResultStatus.IN_PROCESSING, EnumSet.of(RequestResultStatus.PROCESSED, RequestResultStatus.FAILED));

    return statusMatrix;
  }
}
