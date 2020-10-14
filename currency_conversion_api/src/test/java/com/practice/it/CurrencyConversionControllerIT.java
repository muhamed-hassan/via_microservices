package com.practice.it;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.practice.it.helpers.ExternalApiResponseHeaders.COUNTRIES_API_RESPONSE_HEADERS;
import static com.practice.it.helpers.ExternalApiResponseHeaders.RATES_API_RESPONSE_HEADERS;
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
import static com.practice.utils.Constants.ISK;
import static com.practice.utils.ErrorKeys.INVALID_VALUE_CURRENCY_CODE;
import static com.practice.utils.ErrorKeys.MISSING_VALUE_CURRENCY_CODE;
import static com.practice.utils.ErrorMsgsCache.getMessage;
import static com.practice.utils.Mappings.COUNTRIES_JSON;
import static com.practice.utils.Mappings.COUNTRY_OF_ISK_JSON;
import static com.practice.utils.Mappings.LATEST_RATES_OF_ISK_JSON;
import static com.practice.utils.Mappings.LOWEST_AND_HIGHEST_RATES_OF_ISK_JSON;
import static com.practice.utils.MappingsCache.getMappingFromInternalApi;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.text.MessageFormat;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.http.HttpHeaders;
import com.practice.it.models.ResponseFromMockServer;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureWireMock(port = 5555)
@ActiveProfiles("test")
class CurrencyConversionControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testGetCountriesWithTheirCurrencyCodesWhen3rdPartyApiIsAvailableThenReturn200WithData()
            throws Exception {
        var responseFromMockServer = new ResponseFromMockServer()
                                                                .withStatus(OK.value())
                                                                .withHeaders(COUNTRIES_API_RESPONSE_HEADERS)
                                                                .withBodyFile(COUNTRIES_JSON);
        prepareStubServer(ALL_COUNTRIES_EXTERNAL, responseFromMockServer);

        var resultActions = mockMvc.perform(
                                                    get(ALL_COUNTRIES_INTERNAL)
                                                        .accept(MediaType.APPLICATION_JSON));

        var expectedProcessedResponse = getMappingFromInternalApi(COUNTRIES_JSON);
        resultActions.andExpect(status().isOk())
                        .andExpect(content().json(expectedProcessedResponse, true));
    }

    @Test
    void testGetCountriesWithTheirCurrencyCodes_When3rdPartyApiIsDown_ThenReturn503WithErrorMsg()
            throws Exception {
        var responseFromMockServer = new ResponseFromMockServer()
                                                                .withStatus(SERVICE_UNAVAILABLE.value());
        prepareStubServer(ALL_COUNTRIES_EXTERNAL, responseFromMockServer);

        var resultActions = mockMvc.perform(
                                                    get(ALL_COUNTRIES_INTERNAL)
                                                        .accept(MediaType.APPLICATION_JSON));

        var expectedResponse = "[]";
        resultActions.andExpect(status().isOk())
                        .andExpect(content().json(expectedResponse, true));
    }

    @Test
    void testGetCountriesByCurrencyCodeWhen3rdPartyApiIsAvailableThenReturn200WithData()
            throws Exception {
        var responseFromMockServer = new ResponseFromMockServer()
                                                                .withStatus(OK.value())
                                                                .withHeaders(COUNTRIES_API_RESPONSE_HEADERS)
                                                                .withBodyFile(COUNTRY_OF_ISK_JSON);
        prepareStubServer(MessageFormat.format(COUNTRIES_BY_BASE_EXTERNAL, ISK), responseFromMockServer);

        var resultActions = mockMvc.perform(
                                                    get(MessageFormat.format(COUNTRIES_BY_BASE_INTERNAL, ISK))
                                                        .accept(MediaType.APPLICATION_JSON));

        var expectedProcessedResponse = getMappingFromInternalApi(COUNTRY_OF_ISK_JSON);
        resultActions.andExpect(status().isOk())
                        .andExpect(content().json(expectedProcessedResponse, true));
    }

    @Test
    void testGetCountriesByCurrencyCode_When3rdPartyApiIsDown_ThenReturn503WithErrorMsg()
            throws Exception {
        var responseFromMockServer = new ResponseFromMockServer()
                                                                .withStatus(SERVICE_UNAVAILABLE.value());
        prepareStubServer(MessageFormat.format(COUNTRIES_BY_BASE_EXTERNAL, ISK), responseFromMockServer);

        var resultActions = mockMvc.perform(
                                                    get(MessageFormat.format(COUNTRIES_BY_BASE_INTERNAL, ISK))
                                                        .accept(MediaType.APPLICATION_JSON));

        var expectedResponse = "[]";
        resultActions.andExpect(status().isOk())
                        .andExpect(content().json(expectedResponse, true));
    }

    @Test
    void testGetCountriesByCurrencyCodeWhenInternalApiUriHasInvalidCurrencyCodeThenReturn400WithErrorMsg()
            throws Exception {

        var resultActions = mockMvc.perform(
                                                    get(COUNTRIES_BY_BASE_INTERNAL_WITH_INVALID_CURRENCY_CODE)
                                                        .accept(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.error").value(getMessage(INVALID_VALUE_CURRENCY_CODE)));
    }

    @Test
    void testGetHighestAndLowestRatesByBaseWhen3rdPartyApiIsAvailableThenReturn200WithData()
            throws Exception {
        var responseFromMockServer = new ResponseFromMockServer()
                                                                .withStatus(OK.value())
                                                                .withHeaders(RATES_API_RESPONSE_HEADERS)
                                                                .withBodyFile(LATEST_RATES_OF_ISK_JSON);
        prepareStubServer(MessageFormat.format(LATEST_RATES_EXTERNAL, ISK), responseFromMockServer);

        var resultActions = mockMvc.perform(
                                                    get(MessageFormat.format(LOWEST_AND_HIGHEST_RATE_INTERNAL, ISK))
                                                        .accept(MediaType.APPLICATION_JSON));

        var expectedProcessedResponse = getMappingFromInternalApi(LOWEST_AND_HIGHEST_RATES_OF_ISK_JSON);
        resultActions.andExpect(status().isOk())
                        .andExpect(content().json(expectedProcessedResponse, true));
    }

    @Test
    void testGetHighestAndLowestRatesByBase_When3rdPartyApiIsDown_ThenReturn503WithErrorMsg()
            throws Exception {
        var responseFromMockServer = new ResponseFromMockServer()
                                                                .withStatus(SERVICE_UNAVAILABLE.value());
        prepareStubServer(MessageFormat.format(LATEST_RATES_EXTERNAL, ISK), responseFromMockServer);

        var resultActions = mockMvc.perform(
                                                    get(MessageFormat.format(LOWEST_AND_HIGHEST_RATE_INTERNAL, ISK))
                                                        .accept(MediaType.APPLICATION_JSON));

        var expectedResponse = "{}";
        resultActions.andExpect(status().isOk())
                        .andExpect(content().json(expectedResponse, true));
    }

    @ParameterizedTest
    @MethodSource("provideArgumentsForTestGetHighestAndLowestRatesByBaseWhenInternalApiUriHasInvalidCurrencyCode")
    void testGetHighestAndLowestRatesByBaseWhenInternalApiUriHasInvalidCurrencyCodeThenReturn400WithErrorMsg(String requestUri, String errorMsg)
            throws Exception {

        var resultActions = mockMvc.perform(
                                                    get(requestUri)
                                                        .accept(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.error").value(errorMsg));
    }

    private static Stream<Arguments> provideArgumentsForTestGetHighestAndLowestRatesByBaseWhenInternalApiUriHasInvalidCurrencyCode() {
        return Stream.of(
            Arguments.of(LOWEST_AND_HIGHEST_RATE_INTERNAL_WITH_INVALID_CURRENCY_CODE, getMessage(INVALID_VALUE_CURRENCY_CODE)),
            Arguments.of(MessageFormat.format(LOWEST_AND_HIGHEST_RATE_INTERNAL_MALFORMED, ISK), getMessage(MISSING_VALUE_CURRENCY_CODE))
        );
    }

    @Test
    void testGetLatestRatesByBaseWhen3rdPartyApiIsAvailableThenReturn200WithData()
            throws Exception {
        var responseFromMockServer = new ResponseFromMockServer()
                                                                .withStatus(OK.value())
                                                                .withHeaders(RATES_API_RESPONSE_HEADERS)
                                                                .withBodyFile(LATEST_RATES_OF_ISK_JSON);
        prepareStubServer(MessageFormat.format(LATEST_RATES_EXTERNAL, ISK), responseFromMockServer);

        var resultActions = mockMvc.perform(
                                                    get(MessageFormat.format(LATEST_RATES_INTERNAL, ISK))
                                                        .accept(MediaType.APPLICATION_JSON));

        var expectedProcessedResponse = getMappingFromInternalApi(LATEST_RATES_OF_ISK_JSON);
        resultActions.andExpect(status().isOk())
                        .andExpect(content().json(expectedProcessedResponse, true));
    }

    @Test
    void testGetLatestRatesByBase_When3rdPartyApiIsDown_ThenReturn503WithErrorMsg()
            throws Exception {
        var responseFromMockServer = new ResponseFromMockServer()
                                                                .withStatus(SERVICE_UNAVAILABLE.value());
        prepareStubServer(MessageFormat.format(LATEST_RATES_EXTERNAL, ISK), responseFromMockServer);

        var resultActions = mockMvc.perform(
                                                    get(MessageFormat.format(LATEST_RATES_INTERNAL, ISK))
                                                        .accept(MediaType.APPLICATION_JSON));

        var expectedResponse = "[]";
        resultActions.andExpect(status().isOk())
                        .andExpect(content().json(expectedResponse, true));
    }

    @ParameterizedTest
    @MethodSource("provideArgumentsForTestGetLatestRatesByBaseWhenInternalApiUriHasInvalidCurrencyCode")
    void testGetLatestRatesByBaseWhenInternalApiUriHasInvalidCurrencyCodeThenReturn400WithErrorMsg(String requestUri, String errorMsg)
            throws Exception {

        var resultActions = mockMvc.perform(
                                                    get(requestUri)
                                                        .accept(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.error").value(errorMsg));
    }

    private static Stream<Arguments> provideArgumentsForTestGetLatestRatesByBaseWhenInternalApiUriHasInvalidCurrencyCode() {
        return Stream.of(
            Arguments.of(LATEST_RATES_INTERNAL_WITH_INVALID_CURRENCY_CODE, getMessage(INVALID_VALUE_CURRENCY_CODE)),
            Arguments.of(MessageFormat.format(LATEST_RATES_INTERNAL_MALFORMED, ISK), getMessage(MISSING_VALUE_CURRENCY_CODE))
        );
    }

    private void prepareStubServer(String requestPath, ResponseFromMockServer response) {
        var mappingBuilder = WireMock.get(urlEqualTo(requestPath));
        var responseDefinitionBuilder = aResponse().withStatus(response.getStatus());
        if (response.getBodyFile() != null) {
            responseDefinitionBuilder.withBodyFile(response.getBodyFile());
        }
        if (response.getHeaders() != null) {
            var responseHeaders = response.getHeaders()
                                                        .entrySet()
                                                        .stream()
                                                        .map(entry -> new HttpHeader(entry.getKey(), entry.getValue()))
                                                        .collect(Collectors.toList());
            responseDefinitionBuilder.withHeaders(new HttpHeaders(responseHeaders));
        }
        stubFor(mappingBuilder.willReturn(responseDefinitionBuilder));
    }

}
