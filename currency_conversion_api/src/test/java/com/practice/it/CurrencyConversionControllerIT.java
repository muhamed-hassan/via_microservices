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
import static java.text.MessageFormat.format;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;

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

    @ParameterizedTest
    @MethodSource("provideArsWhenThe3rdPartyApiIsAvailable")
    void shouldReturnStatus200WithDataWhenThe3rdPartyApiIsAvailable(Map<String, String> headers, String bodyFile, String expectedProcessedResponse,
                                                                    String requestUriToCurrencyConversionApi, String requestUriTo3rdPartyApi)
            throws Exception {
        var responseFromMockServer = new ResponseFromMockServer()
                                                                .withStatus(OK.value())
                                                                .withHeaders(headers)
                                                                .withBodyFile(bodyFile);
        prepareStubServer(requestUriTo3rdPartyApi, responseFromMockServer);

        var resultActions = mockMvc.perform(get(requestUriToCurrencyConversionApi).accept(MediaType.APPLICATION_JSON));

        expect(resultActions, status().isOk(), content().json(expectedProcessedResponse, true));
    }

    private static Stream<Arguments> provideArsWhenThe3rdPartyApiIsAvailable() {
        return Stream.of(
            Arguments.of(COUNTRIES_API_RESPONSE_HEADERS, COUNTRIES_JSON, getMappingFromInternalApi(COUNTRIES_JSON), ALL_COUNTRIES_INTERNAL,
                         ALL_COUNTRIES_EXTERNAL),
            Arguments.of(COUNTRIES_API_RESPONSE_HEADERS, COUNTRY_OF_ISK_JSON, getMappingFromInternalApi(COUNTRY_OF_ISK_JSON), format(COUNTRIES_BY_BASE_INTERNAL, ISK),
                         format(COUNTRIES_BY_BASE_EXTERNAL, ISK)),
            Arguments.of(RATES_API_RESPONSE_HEADERS, LATEST_RATES_OF_ISK_JSON, getMappingFromInternalApi(LOWEST_AND_HIGHEST_RATES_OF_ISK_JSON),
                         format(LOWEST_AND_HIGHEST_RATE_INTERNAL, ISK), format(LATEST_RATES_EXTERNAL, ISK)),
            Arguments.of(RATES_API_RESPONSE_HEADERS, LATEST_RATES_OF_ISK_JSON, getMappingFromInternalApi(LATEST_RATES_OF_ISK_JSON), format(LATEST_RATES_INTERNAL, ISK),
                         format(LATEST_RATES_EXTERNAL, ISK))
        );
    }

    @ParameterizedTest
    @MethodSource("provideArsWhenTheRequestUriHasInvalidParams")
    void shouldReturnStatus400WithErrMsgWhenTheRequestUriHasInvalidParams(String requestUri, String errorMsg)
            throws Exception {

        var resultActions = mockMvc.perform(get(requestUri).accept(MediaType.APPLICATION_JSON));

        expect(resultActions, status().isBadRequest(), jsonPath("$.error").value(errorMsg));
    }

    private static Stream<Arguments> provideArsWhenTheRequestUriHasInvalidParams() {
        return Stream.of(
            Arguments.of(COUNTRIES_BY_BASE_INTERNAL_WITH_INVALID_CURRENCY_CODE, getMessage(INVALID_VALUE_CURRENCY_CODE)),
            Arguments.of(LOWEST_AND_HIGHEST_RATE_INTERNAL_WITH_INVALID_CURRENCY_CODE, getMessage(INVALID_VALUE_CURRENCY_CODE)),
            Arguments.of(format(LOWEST_AND_HIGHEST_RATE_INTERNAL_MALFORMED, ISK), getMessage(MISSING_VALUE_CURRENCY_CODE)),
            Arguments.of(LATEST_RATES_INTERNAL_WITH_INVALID_CURRENCY_CODE, getMessage(INVALID_VALUE_CURRENCY_CODE)),
            Arguments.of(format(LATEST_RATES_INTERNAL_MALFORMED, ISK), getMessage(MISSING_VALUE_CURRENCY_CODE))
        );
    }

    @ParameterizedTest
    @MethodSource("provideArsWhenThe3rdPartyApiIsDown")
    void shouldReturnStatus503WithErrMsgWhenThe3rdPartyApiIsDown(String expectedResponse, String requestUriToCurrencyConversionApi, String requestUriTo3rdPartyApiString)
            throws Exception {
        var responseFromMockServer = new ResponseFromMockServer()
                                                                .withStatus(SERVICE_UNAVAILABLE.value());
        prepareStubServer(requestUriTo3rdPartyApiString, responseFromMockServer);

        var resultActions = mockMvc.perform(get(requestUriToCurrencyConversionApi).accept(MediaType.APPLICATION_JSON));

        expect(resultActions, status().isOk(), content().json(expectedResponse, true));
    }

    private static Stream<Arguments> provideArsWhenThe3rdPartyApiIsDown() {
        return Stream.of(
            Arguments.of("[]", ALL_COUNTRIES_INTERNAL, ALL_COUNTRIES_EXTERNAL),
            Arguments.of("[]", format(COUNTRIES_BY_BASE_INTERNAL, ISK), format(COUNTRIES_BY_BASE_EXTERNAL, ISK)),
            Arguments.of("{}", format(LOWEST_AND_HIGHEST_RATE_INTERNAL, ISK), format(LATEST_RATES_EXTERNAL, ISK)),
            Arguments.of("[]", format(LATEST_RATES_INTERNAL, ISK), format(LATEST_RATES_EXTERNAL, ISK))
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

    private void expect(ResultActions resultActions, ResultMatcher... resultMatchers)
            throws Exception {
        for (var resultMatcher : resultMatchers) {
            resultActions.andExpect(resultMatcher);
        }
    }

}
