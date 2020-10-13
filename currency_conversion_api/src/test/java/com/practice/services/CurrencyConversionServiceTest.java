package com.practice.services;

import static com.practice.utils.Constants.ISK;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.practice.application.CurrencyConversionService;
import com.practice.application.CurrencyConversionServiceImpl;
import com.practice.application.ServiceNotAvailableException;
import com.practice.infrastructure.integration.CountryClient;
import com.practice.infrastructure.integration.RateClient;
import com.practice.infrastructure.integration.models.CountryWithBriefView;
import com.practice.infrastructure.integration.models.CountryWithDetailedView;
import com.practice.infrastructure.integration.models.Currency;
import com.practice.infrastructure.integration.models.Rates;
import com.practice.infrastructure.integration.models.StatisticsOfRates;

import feign.FeignException;

@ExtendWith(MockitoExtension.class)
class CurrencyConversionServiceTest {

    private CountryClient countryClient;

    private RateClient rateClient;

    private CurrencyConversionService conversionService;

    @BeforeEach
    void injectRefs() {
        countryClient = mock(CountryClient.class);
        rateClient = spy(RateClient.class);
        conversionService = new CurrencyConversionServiceImpl(countryClient, rateClient);
    }

    @Test
    void testGetCountriesWithTheirCurrencyCodesWhenExternalApiAvailableAndItsResponseIsValidThenReturnProcessedData() {
        var countryWithDetailedView = new CountryWithDetailedView();
        countryWithDetailedView.setName("Iceland");
        var currencies = new ArrayList<Currency>();
        var currency = new Currency();
        currency.setCode("ISK");
        currencies.add(currency);
        countryWithDetailedView.setCurrencies(currencies);
        var expectedResult = List.of(countryWithDetailedView);
        when(countryClient.getCountriesWithTheirCurrencyCodes())
            .thenReturn(expectedResult);

        var actualResult = conversionService.getCountriesWithTheirCurrencyCodes();

        assertEquals(expectedResult, actualResult);
    }

    @Test
    void testGetCountriesWithTheirCurrencyCodesWhenExternalApiNotAvailableThenThrowServiceNotAvailableException() {
        doThrow(FeignException.ServiceUnavailable.class)
            .when(countryClient).getCountriesWithTheirCurrencyCodes();

        assertThrows(ServiceNotAvailableException.class,
            () -> conversionService.getCountriesWithTheirCurrencyCodes());
    }

    @Test
    void testGetCountriesByCurrencyCodeWhenExternalApiAvailableAndItsResponseIsValidThenReturnProcessedData() {
        var countryWithBriefView = new CountryWithBriefView();
        countryWithBriefView.setName("Iceland");
        var expectedResult = List.of(countryWithBriefView);
        when(countryClient.getCountriesByCurrencyCode(anyString()))
            .thenReturn(expectedResult);

        var actualResult = conversionService.getCountriesByCurrencyCode(ISK);

        assertEquals(expectedResult, actualResult);
    }

    @Test
    void testGetCountriesByCurrencyCodeWhenExternalApiNotAvailableThenThrowServiceNotAvailableException() {
        doThrow(FeignException.ServiceUnavailable.class)
            .when(countryClient).getCountriesByCurrencyCode(anyString());

        assertThrows(ServiceNotAvailableException.class,
            () -> conversionService.getCountriesByCurrencyCode(ISK));
    }

    @Test
    void testGetHighestAndLowestRatesByBaseWhenExternalApiAvailableAndItsResponseIsValidThenReturnProcessedData() {
        var rates = new Rates(latestRatesOfIsk());
        doReturn(rates)
            .when(rateClient).getLatestRatesByBase(anyString());
        var expectedResult = new StatisticsOfRates(0.0055509191, 106.6376838235);

        var actualResult = conversionService.getHighestAndLowestRatesByBase(ISK);

        assertEquals(expectedResult, actualResult);
    }

    @Test
    void testGetHighestAndLowestRatesByBaseWhenExternalApiNotAvailableThenThrowServiceNotAvailableException() {
        doThrow(FeignException.ServiceUnavailable.class)
            .when(rateClient).getLatestRatesByBase(anyString());

        assertThrows(ServiceNotAvailableException.class,
            () -> conversionService.getHighestAndLowestRatesByBase(ISK));
    }

    @Test
    void testGetLatestRatesByBaseWhenExternalApiAvailableAndItsResponseIsValidThenReturnProcessedData() {
        var expectedResult = new Rates(latestRatesOfIsk());
        when(rateClient.getLatestRatesByBase(anyString()))
            .thenReturn(expectedResult);

        var actualResult = conversionService.getLatestRatesByBase(ISK);

        assertEquals(expectedResult, actualResult);
    }

    @Test
    void testGetLatestRatesByBaseWhenExternalApiNotAvailableThenThrowServiceNotAvailableException() {
        doThrow(FeignException.ServiceUnavailable.class)
            .when(rateClient).getLatestRatesByBase(anyString());

        assertThrows(ServiceNotAvailableException.class,
            () -> conversionService.getLatestRatesByBase(ISK));
    }

    private Map<String, Double> latestRatesOfIsk() {
        var rates = new HashMap<String, Double>();
        rates.put("CAD", 0.0094669118);
        rates.put("HKD", 0.0559742647);
        rates.put("ISK", 1.0);
        rates.put("PHP", 0.3512561275);
        rates.put("DKK", 0.0456078431);
        rates.put("HUF", 2.2071078431);
        rates.put("CZK", 0.167622549);
        rates.put("GBP", 0.0055509191);
        rates.put("RON", 0.0298713235);
        rates.put("SEK", 0.0633762255);
        rates.put("IDR", 106.6376838235);
        rates.put("INR", 0.529564951);
        rates.put("BRL", 0.0400667892);
        rates.put("RUB", 0.5563976716);
        rates.put("HRK", 0.0464368873);
        rates.put("JPY", 0.7621323529);
        rates.put("THB", 0.2247303922);
        rates.put("CHF", 0.0065772059);
        rates.put("EUR", 0.006127451);
        rates.put("MYR", 0.0299154412);
        rates.put("BGN", 0.0119840686);
        rates.put("TRY", 0.0570802696);
        rates.put("CNY", 0.0486446078);
        rates.put("NOK", 0.0661292892);
        rates.put("NZD", 0.0108547794);
        rates.put("ZAR", 0.1191458333);
        rates.put("USD", 0.0072224265);
        rates.put("MXN", 0.1532984069);
        rates.put("SGD", 0.0098082108);
        rates.put("AUD", 0.0100508578);
        rates.put("ILS", 0.0244454657);
        rates.put("KRW", 8.2781862745);
        rates.put("PLN", 0.0274822304);
        return rates;
    }

}
