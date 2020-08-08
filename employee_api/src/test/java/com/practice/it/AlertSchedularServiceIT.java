package com.practice.it;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.http.HttpHeaders;
import com.practice.it.helpers.models.RequestToMockServer;
import com.practice.it.helpers.models.ResponseFromMockServer;

@TestPropertySource(properties = "via.scheduling.enable=false")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
public class AlertSchedularServiceIT {
    private static final String UPDATE_SCRIPTS_DIR = "db/scripts/";

    private static PostgreSQLContainer postgreSQLContainer;
    private static WireMockServer wireMockServer;

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
        if (wireMockServer == null) {
            wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().port(5559));
            wireMockServer.start();
        }
    }

    @AfterAll
    public static void dispose() {
        wireMockServer.stop();
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

    protected void prepareStubServer(final RequestToMockServer request, final ResponseFromMockServer response) {
        MappingBuilder mappingBuilder = WireMock.get(urlEqualTo(request.getRequestPath()));
        ResponseDefinitionBuilder responseDefinitionBuilder = aResponse().withBody(response.getResponseBody()).withStatus(response.getHttpStatus());
        if (response.getHeaders() != null) {
            List<HttpHeader> responseHeaders = new LinkedList<>();
            for (Map.Entry<String, String> entry : response.getHeaders().entrySet()) {
                responseHeaders.add(new HttpHeader(entry.getKey(), entry.getValue()));
            }
            responseDefinitionBuilder.withHeaders(new HttpHeaders(responseHeaders));
        }
        wireMockServer.stubFor(mappingBuilder.willReturn(responseDefinitionBuilder));
    }


}
