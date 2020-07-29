package com.practice.integration;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "countryProvider",
                url = "${external.api.countries}",
                fallback = CountryProviderFallback.class)
public interface CountryProvider {

    @GetMapping
    ResponseEntity<String> getCountriesWithTheirCurrencyCodes();

    @GetMapping("currency/{currencyCode}")
    ResponseEntity<String> getCountriesByCurrencyCode(@PathVariable String currencyCode);

}
