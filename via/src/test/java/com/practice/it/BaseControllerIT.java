package com.practice.it;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.http.HttpHeaders;
import com.practice.it.configs.WireMockServerConfig;
import com.practice.it.helpers.models.RequestToMockServer;
import com.practice.it.helpers.models.ResponseFromMockServer;

@SpringBootTest
@AutoConfigureMockMvc
public class BaseControllerIT {

    protected static final String EXTERNAL_API_MAPPINGS_DIR = "external/";
    protected static final String EXPECTED_FROM_CURRENT_API_MAPPINGS_DIR = "expected/";

    private static ObjectMapper objectMapper = new ObjectMapper();

    private static final String BASE_MAPPINGS_DIR = "__files/";

    @Autowired
    private WireMockServerConfig wireMockServerConfig;

    @Autowired
    private MockMvc mockMvc;

    protected static ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    protected static String readJsonFrom(final String responseLocation) throws Exception {
        String content = Files.readAllLines(Paths.get(ClassLoader.getSystemResource(BASE_MAPPINGS_DIR + responseLocation).toURI()),
                                            Charset.forName(StandardCharsets.UTF_8.name()))
                            .stream()
                            .collect(Collectors.joining());
        return objectMapper.readTree(content).toString();
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
        wireMockServerConfig.wireMockServer().stubFor(mappingBuilder.willReturn(responseDefinitionBuilder));
    }

    protected MockHttpServletResponse fireRequest(final RequestBuilder requestBuilder, final boolean encodeResponseWithUTF8) throws Exception {
        MockHttpServletResponse response = mockMvc.perform(requestBuilder).andReturn().getResponse();
        if (encodeResponseWithUTF8) {
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        }
        return response;
    }

    protected MockMvc getMockMvc() {
        return mockMvc;
    }

}
