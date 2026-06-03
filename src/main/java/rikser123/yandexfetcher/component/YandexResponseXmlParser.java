package rikser123.yandexfetcher.component;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import rikser123.yandexfetcher.dto.response.YandexResponseXMLData;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class YandexResponseXmlParser {
  XmlMapper xmlMapper = XmlMapper.builder()
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    .build();

  @SneakyThrows
  public YandexResponseXMLData parseRawResponse(String rawResponse) {
    var encodedBytes = Base64.getDecoder().decode(rawResponse.getBytes(StandardCharsets.UTF_8));
    var encodedString = new String(encodedBytes, StandardCharsets.UTF_8);

    var yandexResponse = xmlMapper.readValue(encodedString, YandexResponseXMLData.class);

    return yandexResponse;
  }
}
