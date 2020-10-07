package com.practice.infrastructure.integration;

import java.util.Map;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "currency-conversion-api", primary = false,
                fallback = CurrencyConversionProviderFallback.class,
                qualifier = "currencyConversionProvider")
public interface CurrencyConversionProvider {

    @GetMapping("v1/rates")
    Map<String, Double> getLatestRatesByBase(@RequestParam String currencyCode);

}
