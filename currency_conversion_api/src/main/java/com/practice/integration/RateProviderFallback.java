package com.practice.integration;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.practice.integration.constants.Responses;

@Component
public class RateProviderFallback implements RateProvider {

    @Override
    public ResponseEntity<String> getLatestRatesByBase(String base) {
        return Responses.getEmptyResponse();
    }

}
