package com.practice.infrastructure.integration;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class RateProviderFallback implements RateProvider {

    @Override
    public String getLatestRatesByBase(String base) {
        return StringUtils.EMPTY;
    }

}
