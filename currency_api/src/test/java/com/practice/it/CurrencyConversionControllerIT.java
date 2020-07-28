package com.practice.it;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.practice.it.helpers.ExternalApiResponseHeaders.COUNTRIES_API;
import static com.practice.it.helpers.ExternalApiResponseHeaders.RATES_API;
import static com.practice.it.helpers.ExternalApiResponseHeaders.SERVICE_NOT_AVAILABLE_HEADERS;
import static com.practice.it.helpers.ExternalEndpoints.ALL_COUNTRIES_EXTERNAL;
import static com.practice.it.helpers.ExternalEndpoints.COUNTRIES_BY_BASE_EXTERNAL;
import static com.practice.it.helpers.ExternalEndpoints.LATEST_RATES_EXTERNAL;
import static com.practice.it.helpers.InternalEndpoints.ALL_COUNTRIES_INTERNAL;
import static com.practice.it.helpers.InternalEndpoints.COUNTRIES_BY_BASE_INTERNAL;
import static com.practice.it.helpers.InternalEndpoints.COUNTRIES_BY_BASE_INTERNAL_WITH_INVALID_CURRENCY_CODE;
import static com.practice.it.helpers.InternalEndpoints.LATEST_RATES_INTERNAL;
import static com.practice.it.helpers.InternalEndpoints.LATEST_RATES_INTERNAL_MALFORMED;
import static com.practice.it.helpers.InternalEndpoints.LATEST_RATES_INTERNAL_WITH_INVALID_CURRENCY_CODE;
import static com.practice.it.helpers.InternalEndpoints.LOWEST_AND_HIGHEST_RATE_INTERNAL;
import static com.practice.it.helpers.InternalEndpoints.LOWEST_AND_HIGHEST_RATE_INTERNAL_MALFORMED;
import static com.practice.it.helpers.InternalEndpoints.LOWEST_AND_HIGHEST_RATE_INTERNAL_WITH_INVALID_CURRENCY_CODE;
import static com.practice.utils.Constants.HUF;
import static com.practice.utils.HttpClient.doRequest;
import static com.practice.utils.Mappings.COUNTRIES_JSON;
import static com.practice.utils.Mappings.COUNTRY_OF_HUF_JSON;
import static com.practice.utils.Mappings.INVALID_CURRENCY_CODE_JSON;
import static com.practice.utils.Mappings.LATEST_RATES_OF_HUF_JSON;
import static com.practice.utils.Mappings.LOWEST_AND_HIGHEST_RATES_OF_HUF_JSON;
import static com.practice.utils.Mappings.MISSING_CURRENCY_CODE_JSON;
import static com.practice.utils.Mappings.SERVICE_NOT_AVAILABLE_JSON;
import static com.practice.utils.MappingsCache.getMappingFromExternalApi;
import static com.practice.utils.MappingsCache.getMappingFromInternalApi;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;

import java.text.MessageFormat;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.http.HttpHeaders;
import com.practice.it.configs.WireMockServerConfig;
import com.practice.it.models.ResponseFromMockServer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class CurrencyConversionControllerIT {

    @Autowired
    private WireMockServerConfig wireMockServerConfig;

    @Test
    public void testGetCountriesWithTheirCurrencyCodes_When3rdPartyApiIsAvailable_ThenReturn200WithData()
            throws Exception {
        String rawResponse = getMappingFromExternalApi(COUNTRIES_JSON);
        ResponseFromMockServer responseFromMockServer = new ResponseFromMockServer(rawResponse, OK.value(), COUNTRIES_API);
        String expectedProcessedResponse = getMappingFromInternalApi(COUNTRIES_JSON);
        prepareStubServer(ALL_COUNTRIES_EXTERNAL, responseFromMockServer);

        ResponseEntity<String> responseFromCurrencyApi = doRequest(ALL_COUNTRIES_INTERNAL);

        assertEquals(OK.value(), responseFromCurrencyApi.getStatusCode().value());
        JSONAssert.assertEquals(expectedProcessedResponse, responseFromCurrencyApi.getBody(), JSONCompareMode.NON_EXTENSIBLE);
    }

    @Test
    public void testGetCountriesWithTheirCurrencyCodes_When3rdPartyApiIsDown_ThenReturn503WithErrorMsg()
            throws Exception {
        String errorMsg = getMappingFromInternalApi(SERVICE_NOT_AVAILABLE_JSON);
        ResponseFromMockServer responseFromMockServer = new ResponseFromMockServer(errorMsg, SERVICE_UNAVAILABLE.value(), SERVICE_NOT_AVAILABLE_HEADERS);
        prepareStubServer(ALL_COUNTRIES_EXTERNAL, responseFromMockServer);

        ResponseEntity<String> actualProcessedResponse = doRequest(ALL_COUNTRIES_INTERNAL);

        assertEquals(SERVICE_UNAVAILABLE.value(), actualProcessedResponse.getStatusCode().value());
        JSONAssert.assertEquals(errorMsg, actualProcessedResponse.getBody(), JSONCompareMode.NON_EXTENSIBLE);
    }

    @Test
    public void testGetCountriesByCurrencyCode_When3rdPartyApiIsAvailable_ThenReturn200WithData()
            throws Exception {
        String rawResponse = getMappingFromExternalApi(COUNTRY_OF_HUF_JSON);
        ResponseFromMockServer responseFromMockServer = new ResponseFromMockServer(rawResponse, OK.value(), COUNTRIES_API);
        String expectedProcessedResponse = getMappingFromInternalApi(COUNTRY_OF_HUF_JSON);
        prepareStubServer(MessageFormat.format(COUNTRIES_BY_BASE_EXTERNAL, HUF), responseFromMockServer);

        ResponseEntity<String> actualProcessedResponse = doRequest(MessageFormat.format(COUNTRIES_BY_BASE_INTERNAL, HUF));

        assertEquals(OK.value(), actualProcessedResponse.getStatusCode().value());
        JSONAssert.assertEquals(expectedProcessedResponse, actualProcessedResponse.getBody(), JSONCompareMode.NON_EXTENSIBLE);
    }

    @Test
    public void testGetCountriesByCurrencyCode_When3rdPartyApiIsDown_ThenReturn503WithErrorMsg()
            throws Exception {
        String errorMsg = getMappingFromInternalApi(SERVICE_NOT_AVAILABLE_JSON);
        ResponseFromMockServer responseFromMockServer = new ResponseFromMockServer(errorMsg, SERVICE_UNAVAILABLE.value(), COUNTRIES_API);
        prepareStubServer(MessageFormat.format(COUNTRIES_BY_BASE_EXTERNAL, HUF), responseFromMockServer);

        ResponseEntity<String> actualProcessedResponse = doRequest(MessageFormat.format(COUNTRIES_BY_BASE_INTERNAL, HUF));

        assertEquals(SERVICE_UNAVAILABLE.value(), actualProcessedResponse.getStatusCode().value());
        JSONAssert.assertEquals(errorMsg, actualProcessedResponse.getBody(), JSONCompareMode.NON_EXTENSIBLE);
    }

    @Test
    public void testGetCountriesByCurrencyCode_WhenInternalApiUriHasInvalidCurrencyCode_ThenReturn400WithErrorMsg()
            throws Exception {
        String errorMsg = getMappingFromInternalApi(INVALID_CURRENCY_CODE_JSON);

        ResponseEntity<String> actualProcessedResponse = doRequest(COUNTRIES_BY_BASE_INTERNAL_WITH_INVALID_CURRENCY_CODE);

        assertEquals(BAD_REQUEST.value(), actualProcessedResponse.getStatusCode().value());
        JSONAssert.assertEquals(errorMsg, actualProcessedResponse.getBody(), JSONCompareMode.NON_EXTENSIBLE);
    }

    @Test
    public void testGetHighestAndLowestRatesByBase_When3rdPartyApiIsAvailable_ThenReturn200WithData()
            throws Exception {
        String rawResponse = getMappingFromExternalApi(LATEST_RATES_OF_HUF_JSON);
        ResponseFromMockServer responseFromMockServer = new ResponseFromMockServer(rawResponse, OK.value(), RATES_API);
        String expectedProcessedResponse = getMappingFromInternalApi(LOWEST_AND_HIGHEST_RATES_OF_HUF_JSON);
        prepareStubServer(MessageFormat.format(LATEST_RATES_EXTERNAL, HUF), responseFromMockServer);

        ResponseEntity<String> actualProcessedResponse = doRequest(MessageFormat.format(LOWEST_AND_HIGHEST_RATE_INTERNAL, HUF));

        assertEquals(OK.value(), actualProcessedResponse.getStatusCode().value());
        JSONAssert.assertEquals(expectedProcessedResponse, actualProcessedResponse.getBody(), JSONCompareMode.NON_EXTENSIBLE);
    }

    @Test
    public void testGetHighestAndLowestRatesByBase_When3rdPartyApiIsDown_ThenReturn503WithErrorMsg()
            throws Exception {
        String errorMsg = getMappingFromInternalApi(SERVICE_NOT_AVAILABLE_JSON);
        ResponseFromMockServer responseFromMockServer = new ResponseFromMockServer(errorMsg, SERVICE_UNAVAILABLE.value(), RATES_API);
        prepareStubServer(MessageFormat.format(LATEST_RATES_EXTERNAL, HUF), responseFromMockServer);

        ResponseEntity<String> actualProcessedResponse = doRequest(MessageFormat.format(LOWEST_AND_HIGHEST_RATE_INTERNAL, HUF));

        assertEquals(SERVICE_UNAVAILABLE.value(), actualProcessedResponse.getStatusCode().value());
        JSONAssert.assertEquals(errorMsg, actualProcessedResponse.getBody(), JSONCompareMode.NON_EXTENSIBLE);
    }

    @ParameterizedTest
    @MethodSource("provideArgumentsForTestGetHighestAndLowestRatesByBaseWhenInternalApiUriHasInvalidCurrencyCode")
    public void testGetHighestAndLowestRatesByBase_WhenInternalApiUriHasInvalidCurrencyCode_ThenReturn400WithErrorMsg(String requestUri, String errorMsg)
            throws Exception {

        ResponseEntity<String> actualProcessedResponse = doRequest(requestUri);

        assertEquals(BAD_REQUEST.value(), actualProcessedResponse.getStatusCode().value());
        JSONAssert.assertEquals(errorMsg, actualProcessedResponse.getBody(), JSONCompareMode.NON_EXTENSIBLE);
    }

    private static Stream<Arguments> provideArgumentsForTestGetHighestAndLowestRatesByBaseWhenInternalApiUriHasInvalidCurrencyCode() {
        String invalidCurrencyCodeMsg = getMappingFromInternalApi(INVALID_CURRENCY_CODE_JSON);
        String missingCurrencyCodeMsg = getMappingFromInternalApi(MISSING_CURRENCY_CODE_JSON);
        return Stream.of(
            Arguments.of(LOWEST_AND_HIGHEST_RATE_INTERNAL_WITH_INVALID_CURRENCY_CODE, invalidCurrencyCodeMsg),
            Arguments.of(MessageFormat.format(LOWEST_AND_HIGHEST_RATE_INTERNAL_MALFORMED, HUF), missingCurrencyCodeMsg)
        );
    }

    @Test
    public void testGetLatestRatesByBase_When3rdPartyApiIsAvailable_ThenReturn200WithData()
            throws Exception {
        String rawResponse = getMappingFromExternalApi(LATEST_RATES_OF_HUF_JSON);
        ResponseFromMockServer responseFromMockServer = new ResponseFromMockServer(rawResponse, OK.value(), RATES_API);
        String expectedProcessedResponse = getMappingFromInternalApi(LATEST_RATES_OF_HUF_JSON);
        prepareStubServer(MessageFormat.format(LATEST_RATES_EXTERNAL, HUF), responseFromMockServer);

        ResponseEntity<String> actualProcessedResponse = doRequest(MessageFormat.format(LATEST_RATES_INTERNAL, HUF));

        assertEquals(OK.value(), actualProcessedResponse.getStatusCode().value());
        JSONAssert.assertEquals(expectedProcessedResponse, actualProcessedResponse.getBody(), JSONCompareMode.NON_EXTENSIBLE);
    }

    @Test
    public void testGetLatestRatesByBase_When3rdPartyApiIsDown_ThenReturn503WithErrorMsg()
            throws Exception {
        String errorMsg = getMappingFromInternalApi(SERVICE_NOT_AVAILABLE_JSON);
        ResponseFromMockServer responseFromMockServer = new ResponseFromMockServer(errorMsg, SERVICE_UNAVAILABLE.value(), RATES_API);
        prepareStubServer(MessageFormat.format(LATEST_RATES_EXTERNAL, HUF), responseFromMockServer);

        ResponseEntity<String> actualProcessedResponse = doRequest(MessageFormat.format(LATEST_RATES_INTERNAL, HUF));

        assertEquals(SERVICE_UNAVAILABLE.value(), actualProcessedResponse.getStatusCode().value());
        JSONAssert.assertEquals(errorMsg, actualProcessedResponse.getBody(), JSONCompareMode.NON_EXTENSIBLE);
    }

    @ParameterizedTest
    @MethodSource("provideArgumentsForTestGetLatestRatesByBaseWhenInternalApiUriHasInvalidCurrencyCode")
    public void testGetLatestRatesByBase_WhenInternalApiUriHasInvalidCurrencyCode_ThenReturn400WithErrorMsg(String requestUri, String errorMsg)
            throws Exception {

        ResponseEntity<String> actualProcessedResponse = doRequest(requestUri);

        assertEquals(BAD_REQUEST.value(), actualProcessedResponse.getStatusCode().value());
        JSONAssert.assertEquals(errorMsg, actualProcessedResponse.getBody(), JSONCompareMode.NON_EXTENSIBLE);
    }

    private static Stream<Arguments> provideArgumentsForTestGetLatestRatesByBaseWhenInternalApiUriHasInvalidCurrencyCode() {
        String invalidCurrencyCodeMsg = getMappingFromInternalApi(INVALID_CURRENCY_CODE_JSON);
        String missingCurrencyCodeMsg = getMappingFromInternalApi(MISSING_CURRENCY_CODE_JSON);
        return Stream.of(
            Arguments.of(LATEST_RATES_INTERNAL_WITH_INVALID_CURRENCY_CODE, invalidCurrencyCodeMsg),
            Arguments.of(MessageFormat.format(LATEST_RATES_INTERNAL_MALFORMED, HUF), missingCurrencyCodeMsg)
        );
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

}