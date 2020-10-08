package com.practice.infrastructure.integration;

import java.util.Collections;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class CurrencyConversionFeignClientFallback implements CurrencyConversionFeignClient {

    @Override
    public Map<String, Double> getLatestRatesByBase(String currencyCode) {
        return Collections.emptyMap();
    }

}
