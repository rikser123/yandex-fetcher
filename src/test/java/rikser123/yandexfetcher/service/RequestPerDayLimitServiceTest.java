package rikser123.yandexfetcher.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import rikser123.yandexfetcher.repository.RequestPerDayLimitRepository;
import rikser123.yandexfetcher.repository.entity.RequestPerDayLimit;
import rikser123.yandexfetcher.service.impl.RequestPerDayLimitServiceImpl;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
public class RequestPerDayLimitServiceTest {
  @Mock
  private RequestPerDayLimitRepository requestPerDayLimitRepository;

  private RequestPerDayLimitService requestPerDayLimitService;

  @BeforeEach
  void init() {
    requestPerDayLimitService = new RequestPerDayLimitServiceImpl(requestPerDayLimitRepository);
  }

  @Test
  void createNewLimit() {
    when(requestPerDayLimitRepository.findByUserId(any())).thenReturn(Optional.empty());

    requestPerDayLimitService.checkLimit(UUID.randomUUID(), 50);
    verify(requestPerDayLimitRepository, times(1)).save(argThat(arg -> {
      assertThat(arg.getRequestCount()).isEqualTo(1);
      assertThat(arg.getRequestLimit()).isEqualTo(50);
      return true;
    }));
  }

  @Test
  void changeExistedLimit() {
    var limit = createLimit();
    when(requestPerDayLimitRepository.findByUserId(any())).thenReturn(Optional.of(limit));

    requestPerDayLimitService.checkLimit(UUID.randomUUID(), 50);
    verify(requestPerDayLimitRepository, times(1)).save(argThat(arg -> {
      assertThat(arg.getRequestCount()).isEqualTo(2);
      assertThat(arg.getRequestLimit()).isEqualTo(50);
      return true;
    }));
  }

  @Test
  void shouldCatchLimitExcess () {
    var limit = createLimit();
    limit.setRequestLimit(1);
    when(requestPerDayLimitRepository.findByUserId(any())).thenReturn(Optional.of(limit));

    assertThatThrownBy(() -> requestPerDayLimitService.checkLimit(UUID.randomUUID(), 50)).
      isInstanceOf(IllegalStateException.class);
  }

  private static RequestPerDayLimit createLimit() {
    var limit = new RequestPerDayLimit();
    limit.setId(UUID.randomUUID());
    limit.setUserId(UUID.randomUUID());
    limit.setRequestCount(1);
    limit.setRequestLimit(50);
    limit.setStartTime(Instant.now());
    limit.setEndTime(Instant.now());
    return limit;
  }
}
