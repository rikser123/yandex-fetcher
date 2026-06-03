package rikser123.yandexfetcher.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import rikser123.yandexfetcher.dto.response.YandexSpellerResponseItemDto;
import rikser123.yandexfetcher.feign.YandexSpellerClient;
import rikser123.yandexfetcher.service.impl.YandexSpellerServiceImpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;

@ExtendWith(SpringExtension.class)
public class YandexSpellerServiceTest {
  private YandexSpellerService yandexSpellerService;

  @Mock
  private YandexSpellerClient yandexSpellerClient;

  @BeforeEach
  void init() {
    yandexSpellerService = new YandexSpellerServiceImpl(yandexSpellerClient);
  }

  @Test
  void shouldGetResultCorrection() {
    var clientResponseItem = new YandexSpellerResponseItemDto();
    clientResponseItem.setLen(14);
    clientResponseItem.setPos(0);
    clientResponseItem.setWord("синхрафазатрон");
    clientResponseItem.setS(List.of("синхрофазотрон"));

    var clientResponseItem2 = new YandexSpellerResponseItemDto();
    clientResponseItem2.setLen(4);
    clientResponseItem2.setPos(17);
    clientResponseItem2.setWord("дубн");
    clientResponseItem2.setS(List.of("дубне"));

    when(yandexSpellerClient.getSpells(any())).thenReturn(List.of(clientResponseItem, clientResponseItem2));

    var result = yandexSpellerService.getSpellCorrection("синхрафазатрон в дубн");
    assertThat(result.getData().getCorrection()).isEqualTo("синхрофазотрон в дубне");
  }
}
