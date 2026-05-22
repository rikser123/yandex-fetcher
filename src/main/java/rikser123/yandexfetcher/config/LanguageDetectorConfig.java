package rikser123.yandexfetcher.config;

import com.optimaize.langdetect.LanguageDetector;
import com.optimaize.langdetect.LanguageDetectorBuilder;
import com.optimaize.langdetect.ngram.NgramExtractors;
import com.optimaize.langdetect.profiles.LanguageProfileReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class LanguageDetectorConfig {
  @Bean
  public LanguageDetector languageDetector() throws IOException {
      return LanguageDetectorBuilder
        .create(NgramExtractors.standard())
        .withProfiles(new LanguageProfileReader().readAllBuiltIn())
        .build();
  }
}
