package rikser123.yandexfetcher;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.security.reactive.ReactiveManagementWebSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactivefeign.spring.config.EnableReactiveFeignClients;

@SpringBootApplication(exclude = {
  ReactiveManagementWebSecurityAutoConfiguration.class
})
@EnableReactiveFeignClients(basePackages = "rikser123.yandexfetcher.controller.impl")
public class RikserYandexFetcherApplication {

  public static void main(String[] args) {
    SpringApplication.run(RikserYandexFetcherApplication.class, args);
  }
}
