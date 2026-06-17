package rikser123.yandexfetcher.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import rikser123.yandexfetcher.repository.QueryPerDayLimitRepository;
import rikser123.yandexfetcher.repository.entity.QueryPerDayLimit;
import rikser123.yandexfetcher.service.impl.QueryPerDayLimitServiceImpl;

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
public class QueryPerDayLimitServiceTest {
  @Mock
  private QueryPerDayLimitRepository queryPerDayLimitRepository;

  private QueryPerDayLimitService queryPerDayLimitService;

  @BeforeEach
  void init() {
    queryPerDayLimitService = new QueryPerDayLimitServiceImpl(queryPerDayLimitRepository);
  }

  @Test
  void createNewLimit() {
    when(queryPerDayLimitRepository.findByUserId(any())).thenReturn(Optional.empty());

    queryPerDayLimitService.checkLimit(UUID.randomUUID(), 50);
    verify(queryPerDayLimitRepository, times(1)).save(argThat(arg -> {
      assertThat(arg.getRequestCount()).isEqualTo(1);
      assertThat(arg.getRequestLimit()).isEqualTo(50);
      return true;
    }));
  }

  @Test
  void changeExistedLimit() {
    var limit = createLimit();
    when(queryPerDayLimitRepository.findByUserId(any())).thenReturn(Optional.of(limit));

    queryPerDayLimitService.checkLimit(UUID.randomUUID(), 50);
    verify(queryPerDayLimitRepository, times(1)).save(argThat(arg -> {
      assertThat(arg.getRequestCount()).isEqualTo(2);
      assertThat(arg.getRequestLimit()).isEqualTo(50);
      return true;
    }));
  }

  @Test
  void shouldCatchLimitExcess () {
    var limit = createLimit();
    limit.setRequestLimit(1);
    when(queryPerDayLimitRepository.findByUserId(any())).thenReturn(Optional.of(limit));

    assertThatThrownBy(() -> queryPerDayLimitService.checkLimit(UUID.randomUUID(), 50)).
      isInstanceOf(IllegalStateException.class);
  }

  private static QueryPerDayLimit createLimit() {
    var limit = new QueryPerDayLimit();
    limit.setId(UUID.randomUUID());
    limit.setUserId(UUID.randomUUID());
    limit.setRequestCount(1);
    limit.setRequestLimit(50);
    limit.setStartTime(Instant.now());
    limit.setEndTime(Instant.now());
    return limit;
  }
}
