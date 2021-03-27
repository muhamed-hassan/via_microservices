package com.practice.infrastructure.integration;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.practice.infrastructure.integration.models.Rate;

@Profile("!test")
@FeignClient(name = "currency-conversion-api",
                url = "${currency-conversion-api-location}",
                fallback = CurrencyConversionFeignClientFallback.class)
public interface CurrencyConversionFeignClient extends CurrencyConversionClient {

    @GetMapping("v1/rates")
    List<Rate> getLatestRatesByBase(@RequestParam String currencyCode);

}
