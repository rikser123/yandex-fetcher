package rikser123.yandexfetcher.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;
import rikser123.bundle.service.RedisCacheService;
import rikser123.bundle.service.impl.RedisCacheServiceImpl;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnMissingBean(RedisCacheServiceImpl.class)
public class RedisCacheServiceMock implements RedisCacheService {
  @Override
  public <T> void put(String key, T value) {
    log.info("put: key={}, value={}", key, value);
  }

  @Override
  public <T> Optional<T> get(String key, Class<T> parsedClass) {
    log.info("get: key={}", key);
    return Optional.empty();
  }

  @Override
  public void delete(String key) {
    log.info("delete: key={}", key);
  }

  @Override
  public boolean contains(String key) {
    log.info("contains: key={}", key);
    return false;
  }
}
