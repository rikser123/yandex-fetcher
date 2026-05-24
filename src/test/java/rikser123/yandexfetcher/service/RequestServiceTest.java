package rikser123.yandexfetcher.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import rikser123.bundle.service.StatusMatrix;
import rikser123.bundle.service.UserDetailService;
import rikser123.bundle.service.impl.StatusMatrixImpl;
import rikser123.yandexfetcher.component.YandexResponseXmlParser;
import rikser123.yandexfetcher.dto.YandexResponseXMLData;
import rikser123.yandexfetcher.mapper.RequestResultMapper;
import rikser123.yandexfetcher.repository.RequestRepository;
import rikser123.yandexfetcher.repository.RequestResultRepository;
import rikser123.yandexfetcher.repository.entity.Request;
import rikser123.yandexfetcher.repository.entity.RequestStatus;
import rikser123.yandexfetcher.service.impl.RequestServiceImpl;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { YandexResponseXmlParser.class })
public class RequestServiceTest {
  private RequestService requestService;
  private List<YandexResponseXMLData.Doc> docs;

  @Mock
  private RequestRepository requestRepository;

  @Mock
  private UserDetailService userDetailService;

  private RequestResultMapper requestResultMapper = Mappers.getMapper(RequestResultMapper.class);

  @Mock
  private RequestResultRepository requestResultRepository;

  private StatusMatrix<RequestStatus> statusMatrix= new StatusMatrixImpl<>();

  @Autowired
  private YandexResponseXmlParser parser;

  @BeforeEach
  void init() throws IOException {
    requestService = new RequestServiceImpl(
      requestRepository,
      userDetailService,
      requestResultMapper,
      requestResultRepository,
      statusMatrix
    );

    var yandexResponse = RequestServiceTest.class.getResourceAsStream("/yandex-response.txt");
    var text = new String(yandexResponse.readAllBytes(), StandardCharsets.UTF_8);
    var dto = parser.parseRawResponse(text);

    docs = Optional.ofNullable(dto)
      .map(YandexResponseXMLData::getResponse)
      .map(YandexResponseXMLData.Response::getResults)
      .map(YandexResponseXMLData.Results::getGrouping)
      .map(YandexResponseXMLData.Grouping::getGroups)
      .stream()
      .flatMap(Collection::stream)
      .map(YandexResponseXMLData.Group::getDocs)
      .flatMap(Collection::stream)
      .toList();
  }

  @Test
  void shouldSaveAllDocs() {
    var request = new Request();
    requestService.saveRequestResults(docs, request);

    verify(requestResultRepository, times(1)).saveAll(argThat(arg -> {
      var argList = (List) arg;
      assertThat(argList.size()).isEqualTo(docs.size());
      return true;
    }));
  }

  @Test
  void shouldFindSameDocs() {
    var firstDoc = docs.getFirst();
    var secondDoc = docs.get(1);
    var request = new Request();

    secondDoc.setDomain(firstDoc.getDomain());
    secondDoc.setPassages(firstDoc.getPassages());

    requestService.saveRequestResults(docs, request);
    verify(requestResultRepository, times(1)).saveAll(argThat(arg -> {
      var argList = (List) arg;
      assertThat(argList.size()).isEqualTo(docs.size() - 1);
      return true;
    }));

  }
}
