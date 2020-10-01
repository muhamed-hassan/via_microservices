package com.practice.it;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static com.practice.utils.MappingsCache.getMappingFromExternalApi;
import static com.practice.utils.MappingsCache.getMappingFromInternalApi;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.verify;
import static org.springframework.http.HttpStatus.OK;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.http.HttpHeaders;
import com.practice.it.helpers.models.ResponseFromMockServer;
import com.practice.application.ratealert.RateAlertServiceImpl;


//@SpringJUnitConfig(SchedularConfig.class)
//@TestPropertySource(properties = "via.scheduling.enable=false")
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = "eureka.client.enabled=false")
@ActiveProfiles("test")
//@ExtendWith(WireMockServerConfig.class)
public class AlertSchedularServiceIT {
    private static final String UPDATE_SCRIPTS_DIR = "db/scripts/";

    private static PostgreSQLContainer postgreSQLContainer;
    private WireMockServer wireMockServer;
//
    @SpyBean
    private RateAlertServiceImpl alertSchedularService;

    @BeforeAll
    public static void initWireMockServer() {
        if (postgreSQLContainer == null) {
            postgreSQLContainer = new PostgreSQLContainer("postgres:12")
                .withDatabaseName("integration-tests-db")
                .withUsername("username")
                .withPassword("password");
            postgreSQLContainer.start();
            System.setProperty("DB_URL", postgreSQLContainer.getJdbcUrl());
            System.setProperty("DB_USER", postgreSQLContainer.getUsername());
            System.setProperty("DB_PASSWORD", postgreSQLContainer.getPassword());
        }
//        if (wireMockServer == null) {
//            wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().port(5559));
//            wireMockServer.start();
//        }
    }

    @BeforeEach
    void configureSystemUnderTest() {
        wireMockServer = new WireMockServer(options()
            .port(5559)
        );
        wireMockServer.start();
    }

    @AfterEach
    void stopWireMockServer() {
        wireMockServer.stop();
    }

    @AfterAll
    public static void dispose() {
//        wireMockServer.stop();
        postgreSQLContainer.stop();
    }

    public void updateTestDB(String scriptName)
        throws SQLException, URISyntaxException, IOException {
        String script = Files.readAllLines(Paths.get(ClassLoader.getSystemResource(UPDATE_SCRIPTS_DIR + scriptName).toURI()))
            .stream()
            .collect(Collectors.joining());
        Connection connection = postgreSQLContainer.createConnection("");
        Statement statement = connection.createStatement();
        statement.execute(script);
    }

    private void prepareStubServer(String requestPath, ResponseFromMockServer response) {
        MappingBuilder mappingBuilder = WireMock.get(urlEqualTo(requestPath));
        ResponseDefinitionBuilder responseDefinitionBuilder = aResponse().withStatus(response.getHttpStatus());
        if (response.getResponseBody() != null) {
            responseDefinitionBuilder.withBody(response.getResponseBody());
        }
        if (response.getHeaders() != null) {
            List<HttpHeader> responseHeaders = response.getHeaders()
                .entrySet()
                .stream()
                .map(entry -> new HttpHeader(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
            responseDefinitionBuilder.withHeaders(new HttpHeaders(responseHeaders));
        }
        wireMockServer.stubFor(mappingBuilder.willReturn(responseDefinitionBuilder));
    }

    /*

:
:
:
:
:
    * */
    Map<String, String> currencyConversionApi = Map.of(
        "Content-Type", "application/json",
        "Transfer-Encoding", "chunked",
        "Date", "Sat, 08 Aug 2020 19:24:32 GMT",
        "Keep-Alive", "timeout=60",
        "Connection", "keep-alive"
    );
    @Test
    public void whenWaitOneSecond_thenScheduledIsCalledAtLeastTenTimes() throws SQLException, IOException, URISyntaxException {
        updateTestDB("new_rate_alert.sql");
        String rawResponse = getMappingFromExternalApi("latest-rates-of-isk.json");
        ResponseFromMockServer responseFromMockServer = new ResponseFromMockServer(rawResponse, OK.value(), currencyConversionApi);
        String expectedProcessedResponse = getMappingFromInternalApi("latest-rates-of-isk.json");
        prepareStubServer("/v1/rates", responseFromMockServer);
        await()
            .atMost(500, TimeUnit.SECONDS)
            .untilAsserted(() -> verify(alertSchedularService).sendScheduledMailAlert());
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        updateTestDB("reset_rate_alert_table.sql");
    }



}
