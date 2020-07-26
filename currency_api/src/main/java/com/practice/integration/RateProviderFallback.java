package com.practice.integration;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.practice.integration.RateProvider;

//@FeignClient(name = "rateProvider", url = "https://api.exchangeratesapi.io/latest", fallback = RateProviderFallback.class)
@Component
public class RateProviderFallback implements RateProvider {

    private static final ResponseEntity<String> EMPTY_RESPONSE = ResponseEntity.of(Optional.of(StringUtils.EMPTY));

    @Override
    public ResponseEntity<String> getLatestRatesByBase(String base) {
        return EMPTY_RESPONSE;
    }
}
