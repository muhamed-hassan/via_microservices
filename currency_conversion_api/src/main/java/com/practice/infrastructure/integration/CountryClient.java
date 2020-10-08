package com.practice.infrastructure.integration;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "countryProvider",
                url = "${external.api.countries}",
                fallback = CountryClientFallback.class)
public interface CountryClient {

    @GetMapping
    String getCountriesWithTheirCurrencyCodes();

    @GetMapping("currency/{currencyCode}")
    String getCountriesByCurrencyCode(@PathVariable String currencyCode);

}
