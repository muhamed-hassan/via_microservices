package com.practice.integration;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.practice.integration.CountryProvider;

//@FeignClient(name = "countryProvider", url = "https://restcountries.eu/rest/v2", fallback = CountryProvider.class)
@Component
public class CountryProviderFallback implements CountryProvider {

    private static final ResponseEntity<String> EMPTY_RESPONSE = ResponseEntity.of(Optional.of(StringUtils.EMPTY));


    @Override
    public ResponseEntity<String> getCountriesWithTheirCurrencyCodes() {
        //StringUtils.EMPTY
        return EMPTY_RESPONSE;
    }

    @Override
    public ResponseEntity<String> getCountriesByCurrencyCode(String currencyCode) {
        return EMPTY_RESPONSE;
    }

}
