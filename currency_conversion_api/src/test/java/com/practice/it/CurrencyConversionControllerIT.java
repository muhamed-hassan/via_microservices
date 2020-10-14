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
import static com.practice.utils.Constants.ISK;
import static com.practice.utils.ErrorKeys.INVALID_VALUE_CURRENCY_CODE;
import static com.practice.utils.ErrorKeys.MISSING_VALUE_CURRENCY_CODE;
import static com.practice.utils.ErrorKeys.SERVICE_NOT_AVAILABLE;
import static com.practice.utils.ErrorMsgsCache.getMessage;
import static com.practice.utils.Mappings.COUNTRIES_JSON;
import static com.practice.utils.Mappings.COUNTRY_OF_ISK_JSON;
import static com.practice.utils.Mappings.INVALID_CURRENCY_CODE_JSON;
import static com.practice.utils.Mappings.LATEST_RATES_OF_ISK_JSON;
import static com.practice.utils.Mappings.LOWEST_AND_HIGHEST_RATES_OF_ISK_JSON;
import static com.practice.utils.Mappings.MISSING_CURRENCY_CODE_JSON;
import static com.practice.utils.Mappings.SERVICE_NOT_AVAILABLE_JSON;
import static com.practice.utils.MappingsCache.getMappingFromExternalApi;
import static com.practice.utils.MappingsCache.getMappingFromInternalApi;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.text.MessageFormat;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.http.HttpHeaders;
import com.practice.it.configs.WireMockServerConfig;
import com.practice.it.models.ResponseFromMockServer;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
class CurrencyConversionControllerIT {

    @Autowired
    private WireMockServerConfig wireMockServerConfig;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testGetCountriesWithTheirCurrencyCodesWhen3rdPartyApiIsAvailableThenReturn200WithData()
            throws Exception {
        String rawResponse = getMappingFromExternalApi(COUNTRIES_JSON);
        ResponseFromMockServer responseFromMockServer = new ResponseFromMockServer(rawResponse, OK.value(), COUNTRIES_API);
        String expectedProcessedResponse = getMappingFromInternalApi(COUNTRIES_JSON);
        prepareStubServer(ALL_COUNTRIES_EXTERNAL, responseFromMockServer);

        ResultActions resultActions = mockMvc.perform(
                                                get(ALL_COUNTRIES_INTERNAL)
                                                .accept(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isOk())
                        .andExpect(content().json(expectedProcessedResponse, true));
    }

    // needs to be tested
//    @Test
//    void testGetCountriesWithTheirCurrencyCodes_When3rdPartyApiIsDown_ThenReturn503WithErrorMsg()
//            throws Exception {
//        String errorMsg = getMappingFromInternalApi(SERVICE_NOT_AVAILABLE_JSON);
//        ResponseFromMockServer responseFromMockServer = new ResponseFromMockServer(null, SERVICEUNAVAILABLE.value(), SERVICE_NOT_AVAILABLE_HEADERS);
//        prepareStubServer(ALL_COUNTRIES_EXTERNAL, responseFromMockServer);
//
//        ResultActions resultActions = mockMvc.perform(
//                                                get(ALL_COUNTRIES_INTERNAL)
//                                                .accept(MediaType.APPLICATION_JSON));
//
//        resultActions.andExpect(status().isServiceUnavailable())
//                        .andExpect(content().json("[]", true));
//    }

    @Test
    void testGetCountriesByCurrencyCodeWhen3rdPartyApiIsAvailableThenReturn200WithData()
            throws Exception {
        String rawResponse = getMappingFromExternalApi(COUNTRY_OF_ISK_JSON);
        ResponseFromMockServer responseFromMockServer = new ResponseFromMockServer(rawResponse, OK.value(), COUNTRIES_API);
        String expectedProcessedResponse = getMappingFromInternalApi(COUNTRY_OF_ISK_JSON);
        prepareStubServer(MessageFormat.format(COUNTRIES_BY_BASE_EXTERNAL, ISK), responseFromMockServer);

        ResultActions resultActions = mockMvc.perform(
                                                get(MessageFormat.format(COUNTRIES_BY_BASE_INTERNAL, ISK))
                                                .accept(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isOk())
                        .andExpect(content().json(expectedProcessedResponse, true));
    }

//    @Test
//    void testGetCountriesByCurrencyCode_When3rdPartyApiIsDown_ThenReturn503WithErrorMsg()
//            throws Exception {
//        String errorMsg = getMappingFromInternalApi(SERVICE_NOT_AVAILABLE_JSON);
//        ResponseFromMockServer responseFromMockServer = new ResponseFromMockServer(errorMsg, SERVICE_UNAVAILABLE.value(), SERVICE_NOT_AVAILABLE_HEADERS);
//        prepareStubServer(MessageFormat.format(COUNTRIES_BY_BASE_EXTERNAL, ISK), responseFromMockServer);
//
//        ResultActions resultActions = mockMvc.perform(
//                                                get(MessageFormat.format(COUNTRIES_BY_BASE_INTERNAL, ISK))
//                                                .accept(MediaType.APPLICATION_JSON));
//
//        resultActions.andExpect(status().isServiceUnavailable())
//                        .andExpect(jsonPath("$.error").value(getMessage(SERVICE_NOT_AVAILABLE)));
//    }

    @Test
    void testGetCountriesByCurrencyCodeWhenInternalApiUriHasInvalidCurrencyCodeThenReturn400WithErrorMsg()
            throws Exception {
//        String errorMsg = getMappingFromInternalApi(INVALID_CURRENCY_CODE_JSON);

        ResultActions resultActions = mockMvc.perform(
                                                get(COUNTRIES_BY_BASE_INTERNAL_WITH_INVALID_CURRENCY_CODE)
                                                .accept(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.error").value(getMessage(INVALID_VALUE_CURRENCY_CODE)));
    }

    @Test
    void testGetHighestAndLowestRatesByBaseWhen3rdPartyApiIsAvailableThenReturn200WithData()
            throws Exception {
        String rawResponse = getMappingFromExternalApi(LATEST_RATES_OF_ISK_JSON);
        ResponseFromMockServer responseFromMockServer = new ResponseFromMockServer(rawResponse, OK.value(), RATES_API);
        String expectedProcessedResponse = getMappingFromInternalApi(LOWEST_AND_HIGHEST_RATES_OF_ISK_JSON);
        prepareStubServer(MessageFormat.format(LATEST_RATES_EXTERNAL, ISK), responseFromMockServer);

        ResultActions resultActions = mockMvc.perform(
                                                get(MessageFormat.format(LOWEST_AND_HIGHEST_RATE_INTERNAL, ISK))
                                                .accept(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isOk())
                        .andExpect(content().json(expectedProcessedResponse, true));
    }

    // neeeds to be tested
//    @Test
//    void testGetHighestAndLowestRatesByBase_When3rdPartyApiIsDown_ThenReturn503WithErrorMsg()
//            throws Exception {
//        String errorMsg = getMappingFromInternalApi(SERVICE_NOT_AVAILABLE_JSON);
//        ResponseFromMockServer responseFromMockServer = new ResponseFromMockServer(errorMsg, SERVICE_UNAVAILABLE.value(), SERVICE_NOT_AVAILABLE_HEADERS);
//        prepareStubServer(MessageFormat.format(LATEST_RATES_EXTERNAL, ISK), responseFromMockServer);
//
//        ResultActions resultActions = mockMvc.perform(
//                                                get(MessageFormat.format(LOWEST_AND_HIGHEST_RATE_INTERNAL, ISK))
//                                                .accept(MediaType.APPLICATION_JSON));
//
//        resultActions.andExpect(status().isServiceUnavailable())
//                        .andExpect(jsonPath("$.error").value(getMessage(SERVICE_NOT_AVAILABLE)));
//    }

    @ParameterizedTest
    @MethodSource("provideArgumentsForTestGetHighestAndLowestRatesByBaseWhenInternalApiUriHasInvalidCurrencyCode")
    void testGetHighestAndLowestRatesByBaseWhenInternalApiUriHasInvalidCurrencyCodeThenReturn400WithErrorMsg(String requestUri, String errorMsg)
            throws Exception {

        ResultActions resultActions = mockMvc.perform(
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
        String rawResponse = getMappingFromExternalApi(LATEST_RATES_OF_ISK_JSON);
        ResponseFromMockServer responseFromMockServer = new ResponseFromMockServer(rawResponse, OK.value(), RATES_API);
        String expectedProcessedResponse = getMappingFromInternalApi(LATEST_RATES_OF_ISK_JSON);
        prepareStubServer(MessageFormat.format(LATEST_RATES_EXTERNAL, ISK), responseFromMockServer);

        ResultActions resultActions = mockMvc.perform(
                                                get(MessageFormat.format(LATEST_RATES_INTERNAL, ISK))
                                                .accept(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isOk())
                        .andExpect(content().json(expectedProcessedResponse, true));
    }

    // needds yto be tested
//    @Test
//    void testGetLatestRatesByBase_When3rdPartyApiIsDown_ThenReturn503WithErrorMsg()
//            throws Exception {
//        String errorMsg = getMappingFromInternalApi(SERVICE_NOT_AVAILABLE_JSON);
//        ResponseFromMockServer responseFromMockServer = new ResponseFromMockServer(errorMsg, SERVICE_UNAVAILABLE.value(), SERVICE_NOT_AVAILABLE_HEADERS);
//        prepareStubServer(MessageFormat.format(LATEST_RATES_EXTERNAL, ISK), responseFromMockServer);
//
//        ResultActions resultActions = mockMvc.perform(
//                                                get(MessageFormat.format(LATEST_RATES_INTERNAL, ISK))
//                                                .accept(MediaType.APPLICATION_JSON));
//
//        resultActions.andExpect(status().isServiceUnavailable())
//                        .andExpect(jsonPath("$.error").value(getMessage(SERVICE_NOT_AVAILABLE)));
//    }

    @ParameterizedTest
    @MethodSource("provideArgumentsForTestGetLatestRatesByBaseWhenInternalApiUriHasInvalidCurrencyCode")
    void testGetLatestRatesByBaseWhenInternalApiUriHasInvalidCurrencyCodeThenReturn400WithErrorMsg(String requestUri, String errorMsg)
            throws Exception {

        ResultActions resultActions = mockMvc.perform(
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
