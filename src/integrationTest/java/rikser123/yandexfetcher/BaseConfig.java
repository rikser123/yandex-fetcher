package rikser123.yandexfetcher;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.mockserver.integration.ClientAndServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@ActiveProfiles("integration-test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc(addFilters = false)
@WithMockUser(authorities = {"CHECK_SPELLS", "CREATE_REQUEST"})
@Testcontainers
public abstract class BaseConfig {
  protected static ClientAndServer mockServer;

  @Container
  @ServiceConnection
  private static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16-alpine");

  @Autowired
  protected MockMvc client;

  @BeforeAll
  static void initMock() {
    mockServer = startClientAndServer(8081);
  }

  @AfterAll
  static void stopMock() {
    mockServer.stop();
  }

  protected void getYandexSearch() {
    mockServer
      .when(
        request()
          .withMethod("POST")
          .withPath("/v2/web/searchAsync")
      )
      .respond(
        response()
          .withStatusCode(200)
          .withHeader("Content-Type", "application/json")
          .withBody("{\"id\":\"id\",\"done\":false}")
      );
  }

  protected void getOperationSearch() throws IOException {
    var rawFile = BaseConfig.class.getResourceAsStream("/yandex-response.txt");
    var rawContent = new String(rawFile.readAllBytes(), StandardCharsets.UTF_8);
    mockServer
      .when(
        request()
          .withMethod("GET")
          .withPath("/operations/id")
      )
      .respond(
        response()
          .withStatusCode(200)
          .withHeader("Content-Type", "application/json")
          .withBody("{\"done\":true,\"response\":{\"rawData\":\"" + rawContent + "\"}}")
      );
  }
}
