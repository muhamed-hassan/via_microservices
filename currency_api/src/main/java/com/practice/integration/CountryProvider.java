package com.practice.integration;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

@FeignClient(name = "countryProvider", url = "https://restcountries.eu/rest/v2", fallback = CountryProviderFallback.class)
public interface CountryProvider {

    @GetMapping//("https://restcountries.eu/rest/v2")
    ResponseEntity<String> getCountriesWithTheirCurrencyCodes();

    @GetMapping("currency/{currencyCode}")
    ResponseEntity<String> getCountriesByCurrencyCode(@PathVariable String currencyCode);



}
