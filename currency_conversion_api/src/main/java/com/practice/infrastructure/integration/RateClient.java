package com.practice.infrastructure.integration;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.practice.infrastructure.integration.models.Rates;

@FeignClient(name = "rateProvider",
                url = "${external.api.rates}",
                fallback = RateProviderFallback.class)
public interface RateClient {

    @GetMapping
    Rates getLatestRatesByBase(@RequestParam String base);

}


