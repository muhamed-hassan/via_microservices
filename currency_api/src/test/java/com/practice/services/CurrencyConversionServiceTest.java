package com.practice.services;

import static com.practice.utils.Constants.HUF;
import static com.practice.utils.ResponseHandler.RESPONSES_OF_EXTERNAL_API_DIR;
import static com.practice.utils.ResponseHandler.EXPECTED_RESPONSES_OF_INTERNAL_API_DIR;
import static com.practice.utils.ResponseHandler.COUNTRIES_JSON;
import static com.practice.utils.ResponseHandler.COUNTRIES_WITH_INVALID_COUNTRY_NAME_JSON;
import static com.practice.utils.ResponseHandler.COUNTRIES_WITH_INVALID_CURRENCIES_JSON;
import static com.practice.utils.ResponseHandler.COUNTRIES_WITH_INVALID_CODE_JSON;
import static com.practice.utils.ResponseHandler.COUNTRIES_WITH_INVALID_COUNTRY_NAME_AND_CURRENCIES_AND_CODE_JSON;
import static com.practice.utils.ResponseHandler.COUNTRIES_WITH_INVALID_COUNTRY_NAME_AND_CURRENCIES_JSON;
import static com.practice.utils.ResponseHandler.COUNTRIES_WITH_INVALID_CURRENCIES_AND_CODE_JSON;
import static com.practice.utils.ResponseHandler.COUNTRY_OF_HUF_JSON;
import static com.practice.utils.ResponseHandler.COUNTRY_OF_HUF_WITH_INVALID_COUNTRY_NAME_JSON;
import static com.practice.utils.ResponseHandler.LATEST_RATES_OF_HUF_JSON;
import static com.practice.utils.ResponseHandler.LATEST_RATES_OF_HUF_WITH_INVALID_RATES_JSON;
import static com.practice.utils.ResponseHandler.LOWEST_AND_HIGHEST_RATES_OF_HUF_JSON;
import static com.practice.utils.ResponseHandler.getObjectMapper;
import static com.practice.utils.ResponseHandler.readJsonFrom;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Spliterator;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.practice.exceptions.ServiceNotAvailableException;
import com.practice.integration.CountryProvider;
import com.practice.integration.RateProvider;
import com.practice.services.impl.CurrencyConversionServiceImpl;
import com.practice.transfomers.ResponseTransformer;

import feign.FeignException;

@ExtendWith(MockitoExtension.class)
public class CurrencyConversionServiceTest {

    @Mock
    private ObjectMapper mockedObjectMapper;

    @Mock
    private CountryProvider countryProvider;

    @Spy
    private RateProvider rateProvider;

    @Spy
    private ResponseTransformer responseTransformer;

    @InjectMocks
    private CurrencyConversionServiceImpl conversionService;

    @Test
    public void testGetCountriesWithTheirCurrencyCodes_WhenExternalApiAvailableAndItsResponseIsValid_ThenReturnProcessedData()
            throws Exception {
        String rawResponse = readJsonFrom(RESPONSES_OF_EXTERNAL_API_DIR + COUNTRIES_JSON);
        when(countryProvider.getCountriesWithTheirCurrencyCodes())
            .thenReturn(ResponseEntity.ok(rawResponse));
        JsonNode parsedResponse = getObjectMapper().readTree(rawResponse);
        when(mockedObjectMapper.readTree(anyString()))
            .thenReturn(parsedResponse);
        Map<String, String> processedResponse = getObjectMapper().readValue(
            readJsonFrom(EXPECTED_RESPONSES_OF_INTERNAL_API_DIR + COUNTRIES_JSON), Map.class);
        doReturn(processedResponse)
            .when(responseTransformer).transform(any(Spliterator.class), any(Function.class), any(Function.class));

        Map<String, String> actualResult = conversionService.getCountriesWithTheirCurrencyCodes();

        assertThat(actualResult, is(processedResponse));
    }

    @Test
    public void testGetCountriesWithTheirCurrencyCodes_WhenExternalApiNotAvailable_ThenThrowServiceNotAvailableException() {
        doThrow(FeignException.ServiceUnavailable.class)
            .when(countryProvider).getCountriesWithTheirCurrencyCodes();

        assertThrows(ServiceNotAvailableException.class,
            () -> conversionService.getCountriesWithTheirCurrencyCodes());
    }

    @ParameterizedTest
    @MethodSource("provideArgsForTestGetCountriesWithTheirCurrencyCodesWhenExternalApiAvailableAndItsResponseIsInvalid")
    public void testGetCountriesWithTheirCurrencyCodes_WhenExternalApiAvailableAndItsResponseIsInvalid_ThenThrowRuntimeException(String responseFile)
            throws Exception {
        String rawResponse = readJsonFrom(RESPONSES_OF_EXTERNAL_API_DIR + responseFile);
        when(countryProvider.getCountriesWithTheirCurrencyCodes())
            .thenReturn(ResponseEntity.ok(rawResponse));
        JsonNode parsedResponse = getObjectMapper().readTree(rawResponse);
        when(mockedObjectMapper.readTree(anyString()))
            .thenReturn(parsedResponse);
        doThrow(NullPointerException.class)
            .when(responseTransformer).transform(any(Spliterator.class), any(Function.class), any(Function.class));

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
    public void testGetCountriesByCurrencyCode_WhenExternalApiAvailableAndItsResponseIsValid_ThenReturnProcessedData()
            throws Exception {
        String rawResponse = readJsonFrom(RESPONSES_OF_EXTERNAL_API_DIR + COUNTRY_OF_HUF_JSON);
        when(countryProvider.getCountriesByCurrencyCode(anyString()))
            .thenReturn(ResponseEntity.ok(rawResponse));
        JsonNode parsedResponse = getObjectMapper().readTree(rawResponse);
        when(mockedObjectMapper.readTree(anyString()))
            .thenReturn(parsedResponse);
        List<String> processedResponse = getObjectMapper().readValue(
            readJsonFrom(EXPECTED_RESPONSES_OF_INTERNAL_API_DIR + COUNTRY_OF_HUF_JSON), List.class);
        doReturn(processedResponse)
            .when(responseTransformer).transform(any(Spliterator.class), any(Function.class), any(Collector.class));

        List<String> actualResult = conversionService.getCountriesByCurrencyCode(HUF);

        assertThat(actualResult, is(processedResponse));
    }

    @Test
    public void testGetCountriesByCurrencyCode_WhenExternalApiNotAvailable_ThenThrowServiceNotAvailableException() {
        doThrow(FeignException.ServiceUnavailable.class)
            .when(countryProvider).getCountriesByCurrencyCode(anyString());

        assertThrows(ServiceNotAvailableException.class,
            () -> conversionService.getCountriesByCurrencyCode(HUF));
    }

    @Test
    public void testGetCountriesByCurrencyCode_WhenExternalApiAvailableAndItsResponseIsInvalid_ThenThrowRuntimeException()
            throws Exception {
        String rawResponse = readJsonFrom(RESPONSES_OF_EXTERNAL_API_DIR + COUNTRY_OF_HUF_WITH_INVALID_COUNTRY_NAME_JSON);
        when(countryProvider.getCountriesByCurrencyCode(anyString()))
            .thenReturn(ResponseEntity.ok(rawResponse));
        JsonNode parsedResponse = getObjectMapper().readTree(rawResponse);
        when(mockedObjectMapper.readTree(anyString()))
            .thenReturn(parsedResponse);
        doThrow(NullPointerException.class)
            .when(responseTransformer).transform(any(Spliterator.class), any(Function.class), any(Function.class));

        assertThrows(RuntimeException.class,
            () -> conversionService.getCountriesByCurrencyCode(HUF));
    }

    @Test
    public void testGetHighestAndLowestRatesByBase_WhenExternalApiAvailableAndItsResponseIsValid_ThenReturnProcessedData()
            throws Exception {
        String rawResponse = readJsonFrom(RESPONSES_OF_EXTERNAL_API_DIR + LATEST_RATES_OF_HUF_JSON);
        doReturn(ResponseEntity.ok(rawResponse))
            .when(rateProvider).getLatestRatesByBase(anyString());
        JsonNode parsedResponse = getObjectMapper().readTree(rawResponse);
        when(mockedObjectMapper.readTree(anyString()))
            .thenReturn(parsedResponse);
        Map<String, Double> processedResponse = getObjectMapper().readValue(
            readJsonFrom(EXPECTED_RESPONSES_OF_INTERNAL_API_DIR + LOWEST_AND_HIGHEST_RATES_OF_HUF_JSON), Map.class);
        doReturn(processedResponse)
            .when(responseTransformer).transform(any(Spliterator.class), any(Function.class), any(Function.class));

        Map<String, Double> actualResult = conversionService.getHighestAndLowestRatesByBase(HUF);

        assertThat(actualResult, is(processedResponse));

    }

    @Test
    public void testGetHighestAndLowestRatesByBase_WhenExternalApiNotAvailable_ThenThrowServiceNotAvailableException() {
        doThrow(FeignException.ServiceUnavailable.class)
            .when(rateProvider).getLatestRatesByBase(anyString());

        assertThrows(ServiceNotAvailableException.class,
            () -> conversionService.getHighestAndLowestRatesByBase(HUF));
    }

    @Test
    public void testGetHighestAndLowestRatesByBase_WhenExternalApiAvailableAndItsResponseIsInvalid_ThenThrowRuntimeException()
            throws Exception {
        String rawResponse = readJsonFrom(RESPONSES_OF_EXTERNAL_API_DIR + LATEST_RATES_OF_HUF_WITH_INVALID_RATES_JSON);
        doReturn(ResponseEntity.ok(rawResponse))
            .when(rateProvider).getLatestRatesByBase(anyString());

        JsonNode parsedResponse = getObjectMapper().readTree(rawResponse);
        when(mockedObjectMapper.readTree(anyString()))
            .thenReturn(parsedResponse);

        assertThrows(RuntimeException.class,
            () -> conversionService.getHighestAndLowestRatesByBase(HUF));
    }

    @Test
    public void testGetLatestRatesByBase_WhenExternalApiAvailableAndItsResponseIsValid_ThenReturnProcessedData()
            throws Exception {
        String rawResponse = readJsonFrom(RESPONSES_OF_EXTERNAL_API_DIR + LATEST_RATES_OF_HUF_JSON);
        when(rateProvider.getLatestRatesByBase(anyString()))
            .thenReturn(ResponseEntity.ok(rawResponse));
        JsonNode parsedResponse = getObjectMapper().readTree(rawResponse);
        when(mockedObjectMapper.readTree(anyString()))
            .thenReturn(parsedResponse);
        Map<String, Double> processedResponse = getObjectMapper().readValue(
            readJsonFrom(EXPECTED_RESPONSES_OF_INTERNAL_API_DIR + LATEST_RATES_OF_HUF_JSON), Map.class);
        doReturn(processedResponse)
            .when(responseTransformer).transform(any(Spliterator.class), any(Function.class), any(Function.class));

        Map<String, Double> actualResult = conversionService.getLatestRatesByBase(HUF);

        assertThat(actualResult, is(processedResponse));
    }

    @Test
    public void testGetLatestRatesByBase_WhenExternalApiNotAvailable_ThenThrowServiceNotAvailableException() {
        doThrow(FeignException.ServiceUnavailable.class)
            .when(rateProvider).getLatestRatesByBase(anyString());

        assertThrows(ServiceNotAvailableException.class,
            () -> conversionService.getLatestRatesByBase(HUF));
    }

    @Test
    public void testGetLatestRatesByBase_WhenExternalApiAvailableAndItsResponseIsInvalid_ThenThrowRuntimeException()
            throws Exception {
        String rawResponse = readJsonFrom(RESPONSES_OF_EXTERNAL_API_DIR + LATEST_RATES_OF_HUF_WITH_INVALID_RATES_JSON);
        when(rateProvider.getLatestRatesByBase(anyString()))
            .thenReturn(ResponseEntity.ok(rawResponse));
        JsonNode parsedResponse = getObjectMapper().readTree(rawResponse);
        when(mockedObjectMapper.readTree(anyString()))
            .thenReturn(parsedResponse);

        assertThrows(RuntimeException.class,
            () -> conversionService.getLatestRatesByBase(HUF));
    }

}
