package rikser123.yandexfetcher.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rikser123.yandexfetcher.repository.RequestPerDayLimitRepository;
import rikser123.yandexfetcher.repository.entity.RequestPerDayLimit;
import rikser123.yandexfetcher.service.RequestPerDayLimitService;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RequestPerDayLimitServiceImpl implements RequestPerDayLimitService {
  private final RequestPerDayLimitRepository requestPerDayLimitRepository;

  @Override
  @Transactional
  public RequestPerDayLimit checkLimit(UUID userId, Integer requestLimit) {
    var limit = requestPerDayLimitRepository.findByUserId(userId).orElse(null);
    if (!Objects.isNull(limit)) {
      var currentCount = limit.getRequestCount();
      if (currentCount + 1 > limit.getRequestLimit()) {
        throw new IllegalStateException("Количество допустимых запросов превышает лимит тарифа!");
      }
      limit.setRequestCount(limit.getRequestCount() + 1);
    } else {
      limit = createLimit(userId, requestLimit);
      limit.setRequestCount(limit.getRequestCount() + 1);
    }
    return requestPerDayLimitRepository.save(limit);
  }

  private RequestPerDayLimit createLimit(UUID userId, Integer requestLimit) {
    var requestPerDayLimit = new RequestPerDayLimit();
    requestPerDayLimit.setUserId(userId);
    requestPerDayLimit.setRequestLimit(requestLimit);
    requestPerDayLimit.setRequestCount(0);

    var moscowStartDay = LocalDate.now(ZoneId.of("+03:00"))
      .atStartOfDay(ZoneId.of("+03:00"))
      .toInstant();

    var moscowEndDate = LocalDate.now(ZoneId.of("+03:00"))
      .plusDays(1)
      .atStartOfDay()
      .atZone(ZoneId.of("+03:00"))
      .toInstant();
    requestPerDayLimit.setStartTime(moscowStartDay);
    requestPerDayLimit.setEndTime(moscowEndDate);
    return requestPerDayLimit;
  }
}
