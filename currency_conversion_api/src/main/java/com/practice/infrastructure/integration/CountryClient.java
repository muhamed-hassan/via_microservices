package com.practice.infrastructure.integration;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.practice.infrastructure.integration.models.CountryWithBriefView;
import com.practice.infrastructure.integration.models.CountryWithDetailedView;

@FeignClient(name = "countryClient",
                url = "${external.api.countries}",
                fallback = CountryClientFallback.class)
public interface CountryClient {

    @GetMapping
    List<CountryWithDetailedView> getCountriesWithTheirCurrencyCodes();

    @GetMapping("currency/{currencyCode}")
    List<CountryWithBriefView> getCountriesByCurrencyCode(@PathVariable String currencyCode);

}
