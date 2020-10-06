package com.practice.infrastructure.integration;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "rateProvider",
                url = "${external.api.rates}",
                fallback = RateProviderFallback.class)
public interface RateProvider {

    @GetMapping
    String getLatestRatesByBase(@RequestParam String base);

}

