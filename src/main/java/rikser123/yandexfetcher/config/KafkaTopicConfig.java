package rikser123.yandexfetcher.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaTopicConfig {
  public static final String QUERY_TOPIC = "QUERY";

  @Bean
  public NewTopic request() {
    return new NewTopic(QUERY_TOPIC, 3, (short) 2);
  }
}
