package rikser123.yandexfetcher.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaTopicConfig {
  public static final String REQUEST_TOPIC = "REQUEST";

  @Bean
  public NewTopic request() {
    return new NewTopic(REQUEST_TOPIC, 3, (short) 1);
  }
}
