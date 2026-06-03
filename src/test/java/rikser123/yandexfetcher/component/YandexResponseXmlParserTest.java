package rikser123.yandexfetcher.component;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rikser123.yandexfetcher.dto.response.YandexResponseXMLData;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;


public class YandexResponseXmlParserTest {
  private YandexResponseXmlParser parser;

  @BeforeEach
  void init() {
    parser = new YandexResponseXmlParser();
  }

  @SneakyThrows
  @Test
  void shouldTransformToResponse() {
    var encodedFile = YandexResponseXmlParserTest.class.getResourceAsStream("/yandex-response.txt");
    var text = new String(encodedFile.readAllBytes(), StandardCharsets.UTF_8);

    var result = parser.parseRawResponse(text);
    assertThat(result).isInstanceOf(YandexResponseXMLData.class);
  }
}
