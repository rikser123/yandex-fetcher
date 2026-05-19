package rikser123.yandexfetcher;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "rikser123.yandexfetcher.feign")
public class RikserYandexFetcherApplication {
  public static void main(String[] args) {
    SpringApplication.run(RikserYandexFetcherApplication.class, args);
  }
}
