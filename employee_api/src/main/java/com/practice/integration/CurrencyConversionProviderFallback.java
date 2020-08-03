package com.practice.integration;

import java.util.Collections;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class CurrencyConversionProviderFallback implements CurrencyConversionProvider {

    @Override
    public Map<String, Double> getLatestRatesByBase(String currencyCode) {
        return Collections.emptyMap();
    }

}
