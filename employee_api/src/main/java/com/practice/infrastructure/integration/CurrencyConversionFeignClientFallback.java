package com.practice.infrastructure.integration;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Component;

import com.practice.infrastructure.integration.models.Rate;

@Component
public class CurrencyConversionFeignClientFallback implements CurrencyConversionFeignClient {

    @Override
    public List<Rate> getLatestRatesByBase(String currencyCode) {
        return Collections.emptyList();
    }

}
