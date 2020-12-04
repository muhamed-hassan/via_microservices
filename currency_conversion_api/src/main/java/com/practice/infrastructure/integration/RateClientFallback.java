package com.practice.infrastructure.integration;

import java.util.Collections;

import org.springframework.stereotype.Component;

import com.practice.infrastructure.integration.models.Rates;

@Component
public class RateClientFallback implements RateClient {

    private final Rates EMPTY_RATES = new Rates(Collections.emptyMap());

    @Override
    public Rates getLatestRatesByBase(String base) {
        return EMPTY_RATES;
    }

}
