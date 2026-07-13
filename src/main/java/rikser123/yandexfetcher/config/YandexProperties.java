package rikser123.yandexfetcher.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "yandex")
@Data
public class YandexProperties {
  private String token;
  private int maxAttempts;
  private int delay;
  private List<String> excludeDomains;
  private int poolSize;
  private int queueSize;
}
