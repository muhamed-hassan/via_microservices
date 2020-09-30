package com.practice.services;

import static com.practice.utils.Constants.ISK;
import static com.practice.utils.Mappings.COUNTRIES_JSON;
import static com.practice.utils.Mappings.COUNTRIES_WITH_INVALID_CODE_JSON;
import static com.practice.utils.Mappings.COUNTRIES_WITH_INVALID_COUNTRY_NAME_AND_CURRENCIES_AND_CODE_JSON;
import static com.practice.utils.Mappings.COUNTRIES_WITH_INVALID_COUNTRY_NAME_AND_CURRENCIES_JSON;
import static com.practice.utils.Mappings.COUNTRIES_WITH_INVALID_COUNTRY_NAME_JSON;
import static com.practice.utils.Mappings.COUNTRIES_WITH_INVALID_CURRENCIES_AND_CODE_JSON;
import static com.practice.utils.Mappings.COUNTRIES_WITH_INVALID_CURRENCIES_JSON;
import static com.practice.utils.Mappings.COUNTRY_OF_ISK_JSON;
import static com.practice.utils.Mappings.LATEST_RATES_OF_ISK_JSON;
import static com.practice.utils.Mappings.LATEST_RATES_OF_ISK_JSON_WITH_INVALID_RATES;
import static com.practice.utils.Mappings.LOWEST_AND_HIGHEST_RATES_OF_ISK_JSON;
import static com.practice.utils.MappingsCache.getMappingFromExternalApi;
import static com.practice.utils.MappingsCache.getMappingFromInternalApi;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.practice.application.CurrencyConversionService;
import com.practice.application.ServiceNotAvailableException;
import com.practice.infrastructure.integration.CountryProvider;
import com.practice.infrastructure.integration.RateProvider;
import com.practice.application.CurrencyConversionServiceImpl;

import feign.FeignException;

@ExtendWith(MockitoExtension.class)
class CurrencyConversionServiceTest {

    private static ObjectMapper objectMapper;

    private ObjectMapper mockedObjectMapper;

    private CountryProvider countryProvider;

    private RateProvider rateProvider;

    private CurrencyConversionService conversionService;

    @BeforeAll
    static void initObjectMapper() {
        objectMapper = new ObjectMapper();
    }

    @BeforeEach
    void injectRefs() {
        mockedObjectMapper = mock(ObjectMapper.class);
        countryProvider = mock(CountryProvider.class);
        rateProvider = spy(RateProvider.class);
        conversionService = new CurrencyConversionServiceImpl(mockedObjectMapper, countryProvider, rateProvider);
    }

    @Test
    void testGetCountriesWithTheirCurrencyCodes_WhenExternalApiAvailableAndItsResponseIsValid_ThenReturnProcessedData()
            throws Exception {
        String rawResponse = getMappingFromExternalApi(COUNTRIES_JSON);
        when(countryProvider.getCountriesWithTheirCurrencyCodes())
            .thenReturn(rawResponse);
        JsonNode parsedResponse = objectMapper.readTree(rawResponse);
        when(mockedObjectMapper.readTree(anyString()))
            .thenReturn(parsedResponse);
        Map<String, String> processedResponse = objectMapper.readValue(getMappingFromInternalApi(COUNTRIES_JSON), Map.class);

        Map<String, String> actualResult = conversionService.getCountriesWithTheirCurrencyCodes();

        assertThat(actualResult, is(processedResponse));
    }

    @Test
    void testGetCountriesWithTheirCurrencyCodes_WhenExternalApiNotAvailable_ThenThrowServiceNotAvailableException() {
        doThrow(FeignException.ServiceUnavailable.class)
            .when(countryProvider).getCountriesWithTheirCurrencyCodes();

        assertThrows(ServiceNotAvailableException.class,
            () -> conversionService.getCountriesWithTheirCurrencyCodes());
    }

    @ParameterizedTest
    @MethodSource("provideArgsForTestGetCountriesWithTheirCurrencyCodesWhenExternalApiAvailableAndItsResponseIsInvalid")
    void testGetCountriesWithTheirCurrencyCodes_WhenExternalApiAvailableAndItsResponseIsInvalid_ThenThrowRuntimeException(String responseFile)
            throws Exception {
        String rawResponse = getMappingFromExternalApi(responseFile);
        when(countryProvider.getCountriesWithTheirCurrencyCodes())
            .thenReturn(rawResponse);
        JsonNode parsedResponse = objectMapper.readTree(rawResponse);
        when(mockedObjectMapper.readTree(anyString()))
            .thenReturn(parsedResponse);

        assertThrows(RuntimeException.class,
            () -> conversionService.getCountriesWithTheirCurrencyCodes());
    }

    private static Stream<Arguments> provideArgsForTestGetCountriesWithTheirCurrencyCodesWhenExternalApiAvailableAndItsResponseIsInvalid() {
        return Stream.of(
            Arguments.of(COUNTRIES_WITH_INVALID_COUNTRY_NAME_JSON),
            Arguments.of(COUNTRIES_WITH_INVALID_CURRENCIES_JSON),
            Arguments.of(COUNTRIES_WITH_INVALID_CODE_JSON),
            Arguments.of(COUNTRIES_WITH_INVALID_COUNTRY_NAME_AND_CURRENCIES_AND_CODE_JSON),
            Arguments.of(COUNTRIES_WITH_INVALID_COUNTRY_NAME_AND_CURRENCIES_JSON),
            Arguments.of(COUNTRIES_WITH_INVALID_CURRENCIES_AND_CODE_JSON)
        );
    }

    @Test
    void testGetCountriesByCurrencyCode_WhenExternalApiAvailableAndItsResponseIsValid_ThenReturnProcessedData()
            throws Exception {
        String rawResponse = getMappingFromExternalApi(COUNTRY_OF_ISK_JSON);
        when(countryProvider.getCountriesByCurrencyCode(anyString()))
            .thenReturn(rawResponse);
        JsonNode parsedResponse = objectMapper.readTree(rawResponse);
        when(mockedObjectMapper.readTree(anyString()))
            .thenReturn(parsedResponse);
        List<String> processedResponse = objectMapper.readValue(getMappingFromInternalApi(COUNTRY_OF_ISK_JSON), List.class);

        List<String> actualResult = conversionService.getCountriesByCurrencyCode(ISK);

        assertThat(actualResult, is(processedResponse));
    }

    @Test
    void testGetCountriesByCurrencyCode_WhenExternalApiNotAvailable_ThenThrowServiceNotAvailableException() {
        doThrow(FeignException.ServiceUnavailable.class)
            .when(countryProvider).getCountriesByCurrencyCode(anyString());

        assertThrows(ServiceNotAvailableException.class,
            () -> conversionService.getCountriesByCurrencyCode(ISK));
    }

    @Test
    void testGetCountriesByCurrencyCode_WhenExternalApiAvailableAndItsResponseIsInvalid_ThenThrowRuntimeException()
            throws Exception {
        String rawResponse = getMappingFromExternalApi(COUNTRIES_WITH_INVALID_COUNTRY_NAME_JSON);
        when(countryProvider.getCountriesByCurrencyCode(anyString()))
            .thenReturn(rawResponse);
        JsonNode parsedResponse = objectMapper.readTree(rawResponse);
        when(mockedObjectMapper.readTree(anyString()))
            .thenReturn(parsedResponse);

        assertThrows(RuntimeException.class,
            () -> conversionService.getCountriesByCurrencyCode(ISK));
    }

    @Test
    void testGetHighestAndLowestRatesByBase_WhenExternalApiAvailableAndItsResponseIsValid_ThenReturnProcessedData()
            throws Exception {
        String rawResponse = getMappingFromExternalApi(LATEST_RATES_OF_ISK_JSON);
        doReturn(rawResponse)
            .when(rateProvider).getLatestRatesByBase(anyString());
        JsonNode parsedResponse = objectMapper.readTree(rawResponse);
        when(mockedObjectMapper.readTree(anyString()))
            .thenReturn(parsedResponse);
        Map<String, Double> processedResponse = objectMapper.readValue(getMappingFromInternalApi(LOWEST_AND_HIGHEST_RATES_OF_ISK_JSON), Map.class);

        Map<String, Double> actualResult = conversionService.getHighestAndLowestRatesByBase(ISK);

        assertThat(actualResult, is(processedResponse));

    }

    @Test
    void testGetHighestAndLowestRatesByBase_WhenExternalApiNotAvailable_ThenThrowServiceNotAvailableException() {
        doThrow(FeignException.ServiceUnavailable.class)
            .when(rateProvider).getLatestRatesByBase(anyString());

        assertThrows(ServiceNotAvailableException.class,
            () -> conversionService.getHighestAndLowestRatesByBase(ISK));
    }

    @Test
    void testGetHighestAndLowestRatesByBase_WhenExternalApiAvailableAndItsResponseIsInvalid_ThenThrowRuntimeException()
            throws Exception {
        String rawResponse = getMappingFromExternalApi(LATEST_RATES_OF_ISK_JSON_WITH_INVALID_RATES);
        doReturn(rawResponse)
            .when(rateProvider).getLatestRatesByBase(anyString());
        JsonNode parsedResponse = objectMapper.readTree(rawResponse);
        when(mockedObjectMapper.readTree(anyString()))
            .thenReturn(parsedResponse);

        assertThrows(RuntimeException.class,
            () -> conversionService.getHighestAndLowestRatesByBase(ISK));
    }

    @Test
    void testGetLatestRatesByBase_WhenExternalApiAvailableAndItsResponseIsValid_ThenReturnProcessedData()
            throws Exception {
        String rawResponse = getMappingFromExternalApi(LATEST_RATES_OF_ISK_JSON);
        when(rateProvider.getLatestRatesByBase(anyString()))
            .thenReturn(rawResponse);
        JsonNode parsedResponse = objectMapper.readTree(rawResponse);
        when(mockedObjectMapper.readTree(anyString()))
            .thenReturn(parsedResponse);
        Map<String, Double> processedResponse = objectMapper.readValue(getMappingFromInternalApi(LATEST_RATES_OF_ISK_JSON), Map.class);

        Map<String, Double> actualResult = conversionService.getLatestRatesByBase(ISK);

        assertThat(actualResult, is(processedResponse));
    }

    @Test
    void testGetLatestRatesByBase_WhenExternalApiNotAvailable_ThenThrowServiceNotAvailableException() {
        doThrow(FeignException.ServiceUnavailable.class)
            .when(rateProvider).getLatestRatesByBase(anyString());

        assertThrows(ServiceNotAvailableException.class,
            () -> conversionService.getLatestRatesByBase(ISK));
    }

    @Test
    void testGetLatestRatesByBase_WhenExternalApiAvailableAndItsResponseIsInvalid_ThenThrowRuntimeException()
            throws Exception {
        String rawResponse = getMappingFromExternalApi(LATEST_RATES_OF_ISK_JSON_WITH_INVALID_RATES);
        when(rateProvider.getLatestRatesByBase(anyString()))
            .thenReturn(rawResponse);
        JsonNode parsedResponse = objectMapper.readTree(rawResponse);
        when(mockedObjectMapper.readTree(anyString()))
            .thenReturn(parsedResponse);

        assertThrows(RuntimeException.class,
            () -> conversionService.getLatestRatesByBase(ISK));
    }

}
