package com.practice.it;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.practice.it.helpers.ExternalApiResponseHeaders.COUNTRIES_API;
import static com.practice.it.helpers.ExternalApiResponseHeaders.RATES_API;
import static com.practice.it.helpers.ExternalEndpoints.ALL_COUNTRIES_EXTERNAL;
import static com.practice.it.helpers.ExternalEndpoints.COUNTRIES_BY_BASE_EXTERNAL;
import static com.practice.it.helpers.ExternalEndpoints.LATEST_RATES_EXTERNAL;
import static com.practice.it.helpers.InternalEndpoints.ALL_COUNTRIES_INTERNAL;
import static com.practice.it.helpers.InternalEndpoints.COUNTRIES_BY_BASE_INTERNAL;
import static com.practice.it.helpers.InternalEndpoints.LATEST_RATES_INTERNAL;
import static com.practice.it.helpers.InternalEndpoints.LOWEST_AND_HIGHEST_RATE_INTERNAL;
import static com.practice.it.helpers.KeysOfHttpHeaders.ACCEPT_HEADER;
import static com.practice.it.helpers.Mappings.BASE_MAPPINGS_DIR;
import static com.practice.it.helpers.Mappings.COUNTRIES_JSON;
import static com.practice.it.helpers.Mappings.COUNTRY_OF_HUF;
import static com.practice.it.helpers.Mappings.ERRORS_DIR;
import static com.practice.it.helpers.Mappings.EXPECTED_RESPONSES_OF_INTERNAL_API_DIR;
import static com.practice.it.helpers.Mappings.LATEST_RATES_OF_HUF;
import static com.practice.it.helpers.Mappings.LOWEST_AND_HIGHEST_RATES_OF_HUF;
import static com.practice.it.helpers.Mappings.RESPONSES_OF_EXTERNAL_API_DIR;
import static com.practice.it.helpers.Mappings.SERVICE_NOT_AVAILABLE_JSON;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.test.context.ActiveProfiles;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.http.HttpHeaders;
import com.practice.it.configs.WireMockServerConfig;
import com.practice.it.models.ResponseFromMockServer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class CurrencyConversionControllerIT {

    private static final String BASE = "HUF";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private TestRestTemplate testRestTemplate;

    @Autowired
    private RestTemplateBuilder restTemplateBuilder;

    @Autowired
    private WireMockServerConfig wireMockServerConfig;

    @LocalServerPort
    private int port;

    @PostConstruct
    public void iniTestRestTemplate() {
        testRestTemplate = new TestRestTemplate(restTemplateBuilder.rootUri("http://localhost:" + port));
        testRestTemplate.getRestTemplate().setRequestFactory(new HttpComponentsClientHttpRequestFactory());
        testRestTemplate.getRestTemplate().getMessageConverters().add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));
    }

    @ParameterizedTest
    @MethodSource("provideArgumentsForTestGettersWhen3rdPartyApiIsAvailable")
    public void testGetters_When3rdPartyApiIsAvailable_ThenReturnCorrectResponse(String requestToMockServerUri,
                                                                                    ResponseFromMockServer responseFromMockServer,
                                                                                    String requestToCurrencyConversionApiUri,
                                                                                    String responseFromCurrencyConversionApi)
            throws Exception {
        prepareStubServer(requestToMockServerUri, responseFromMockServer);

        ResponseEntity<String> response = doRequest(requestToCurrencyConversionApiUri);

        assertEquals(OK.value(), response.getStatusCode().value());
        JSONAssert.assertEquals(responseFromCurrencyConversionApi, response.getBody(), JSONCompareMode.NON_EXTENSIBLE);
    }

    private static Stream<Arguments> provideArgumentsForTestGettersWhen3rdPartyApiIsAvailable()
            throws Exception {
        String allCountriesResponseExternal = readJsonFrom(RESPONSES_OF_EXTERNAL_API_DIR + COUNTRIES_JSON);
        ResponseFromMockServer allCountriesExternal = new ResponseFromMockServer(allCountriesResponseExternal, OK.value(), COUNTRIES_API);
        String allCountriesInternal = readJsonFrom(EXPECTED_RESPONSES_OF_INTERNAL_API_DIR + COUNTRIES_JSON);

        String countriesOfHufResponseExternal = readJsonFrom(RESPONSES_OF_EXTERNAL_API_DIR + COUNTRY_OF_HUF);
        ResponseFromMockServer countriesOfHufExternal = new ResponseFromMockServer(countriesOfHufResponseExternal, OK.value(), COUNTRIES_API);
        String countriesOfHufInternal = readJsonFrom(EXPECTED_RESPONSES_OF_INTERNAL_API_DIR + COUNTRY_OF_HUF);

        String lowestAndHighestRatesOfHufResponseExternal = readJsonFrom(RESPONSES_OF_EXTERNAL_API_DIR + LATEST_RATES_OF_HUF);
        ResponseFromMockServer lowestAndHighestRatesOfHufExternal = new ResponseFromMockServer(lowestAndHighestRatesOfHufResponseExternal, OK.value(), RATES_API);
        String lowestAndHighestRatesOfHufInternal = readJsonFrom(EXPECTED_RESPONSES_OF_INTERNAL_API_DIR + LOWEST_AND_HIGHEST_RATES_OF_HUF);

        String latestRatesOfHufResponseExternal = readJsonFrom(RESPONSES_OF_EXTERNAL_API_DIR + LATEST_RATES_OF_HUF);
        ResponseFromMockServer latestRatesOfHufExternal = new ResponseFromMockServer(latestRatesOfHufResponseExternal, OK.value(), RATES_API);
        String latestRatesOfHufInternal = readJsonFrom(EXPECTED_RESPONSES_OF_INTERNAL_API_DIR + LATEST_RATES_OF_HUF);

        return Stream.of(
            Arguments.of(ALL_COUNTRIES_EXTERNAL, allCountriesExternal, ALL_COUNTRIES_INTERNAL, allCountriesInternal),
            Arguments.of(MessageFormat.format(COUNTRIES_BY_BASE_EXTERNAL, BASE), countriesOfHufExternal,
                            MessageFormat.format(COUNTRIES_BY_BASE_INTERNAL, BASE), countriesOfHufInternal),
            Arguments.of(MessageFormat.format(LATEST_RATES_EXTERNAL, BASE), lowestAndHighestRatesOfHufExternal,
                            MessageFormat.format(LOWEST_AND_HIGHEST_RATE_INTERNAL, BASE), lowestAndHighestRatesOfHufInternal),
            Arguments.of(MessageFormat.format(LATEST_RATES_EXTERNAL, BASE), latestRatesOfHufExternal,
                            MessageFormat.format(LATEST_RATES_INTERNAL, BASE), latestRatesOfHufInternal)
        );
    }

    @ParameterizedTest
    @MethodSource("provideArgumentsForTestGettersWhen3rdPartyApiIsDown")
    public void testGetters_When3rdPartyApiIsDown_ThenReturn503WithErrorMsg(String requestToMockServerUri,
                                                                                ResponseFromMockServer responseFromMockServer,
                                                                                String requestToCurrencyConversionApiUri)
            throws Exception {
        prepareStubServer(requestToMockServerUri, responseFromMockServer);
        String expectedErrorMsg = readJsonFrom(ERRORS_DIR + SERVICE_NOT_AVAILABLE_JSON);

        ResponseEntity<String> response = doRequest(requestToCurrencyConversionApiUri);

        assertEquals(SERVICE_UNAVAILABLE.value(), response.getStatusCode().value());
        JSONAssert.assertEquals(expectedErrorMsg, response.getBody(), JSONCompareMode.NON_EXTENSIBLE);
    }

    private static Stream<Arguments> provideArgumentsForTestGettersWhen3rdPartyApiIsDown()
            throws Exception {
        String errorMsg = readJsonFrom(ERRORS_DIR + SERVICE_NOT_AVAILABLE_JSON);

        ResponseFromMockServer allCountries = new ResponseFromMockServer(errorMsg, SERVICE_UNAVAILABLE.value(), COUNTRIES_API);
        ResponseFromMockServer countriesOfHuf = new ResponseFromMockServer(errorMsg, SERVICE_UNAVAILABLE.value(), COUNTRIES_API);
        ResponseFromMockServer lowestAndHighestRatesOfHUF = new ResponseFromMockServer(errorMsg, SERVICE_UNAVAILABLE.value(), RATES_API);
        ResponseFromMockServer latestRatesOfHuf = new ResponseFromMockServer(errorMsg, SERVICE_UNAVAILABLE.value(), RATES_API);

        return Stream.of(
            Arguments.of(ALL_COUNTRIES_EXTERNAL, allCountries, ALL_COUNTRIES_INTERNAL),
            Arguments.of(MessageFormat.format(COUNTRIES_BY_BASE_EXTERNAL, BASE), countriesOfHuf,
                            MessageFormat.format(COUNTRIES_BY_BASE_INTERNAL, BASE)),
            Arguments.of(MessageFormat.format(LATEST_RATES_EXTERNAL, BASE), lowestAndHighestRatesOfHUF,
                            MessageFormat.format(LOWEST_AND_HIGHEST_RATE_INTERNAL, BASE)),
            Arguments.of(MessageFormat.format(LATEST_RATES_EXTERNAL, BASE), latestRatesOfHuf,
                            MessageFormat.format(LATEST_RATES_INTERNAL, BASE))
        );
    }

    private static String readJsonFrom(String responseLocation)
            throws Exception {
        String content = Files.readAllLines(Paths.get(ClassLoader.getSystemResource(BASE_MAPPINGS_DIR + responseLocation).toURI()),
                                                Charset.forName(StandardCharsets.UTF_8.name()))
                                .stream()
                                .collect(Collectors.joining());
        return OBJECT_MAPPER.readTree(content).toString();
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
        wireMockServerConfig.wireMockServer().stubFor(mappingBuilder.willReturn(responseDefinitionBuilder));
    }

    private ResponseEntity<String> doRequest(String requestUri) {
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.add(ACCEPT_HEADER, MediaType.APPLICATION_JSON_VALUE);
        HttpEntity requestEntity = new HttpEntity(headers);
        return testRestTemplate.exchange(requestUri, HttpMethod.GET, requestEntity, String.class);
    }

}
