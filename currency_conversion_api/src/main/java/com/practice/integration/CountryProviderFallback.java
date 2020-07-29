package com.practice.integration;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.practice.integration.constants.Responses;

@Component
public class CountryProviderFallback implements CountryProvider {

    @Override
    public ResponseEntity<String> getCountriesWithTheirCurrencyCodes() {
        return Responses.getEmptyResponse();
    }

    @Override
    public ResponseEntity<String> getCountriesByCurrencyCode(String currencyCode) {
        return Responses.getEmptyResponse();
    }

}
