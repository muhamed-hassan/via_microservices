package com.practice.infrastructure.integration;

import java.util.Map;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Profile("!test")
@FeignClient(name = "currency-conversion-api",
                fallback = CurrencyConversionFeignClientFallback.class)
public interface CurrencyConversionFeignClient extends CurrencyConversionClient {

    @GetMapping("v1/rates")
    Map<String, Double> getLatestRatesByBase(@RequestParam String currencyCode);

}
