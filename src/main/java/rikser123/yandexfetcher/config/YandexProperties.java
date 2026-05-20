package rikser123.yandexfetcher.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "yandex")
@Data
public class YandexProperties {
  private String token;
  private int maxAttempts;
  private int delay;
}
