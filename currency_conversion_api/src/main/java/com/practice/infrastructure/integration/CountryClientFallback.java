package com.practice.infrastructure.integration;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class CountryClientFallback implements CountryClient {

    @Override
    public String getCountriesWithTheirCurrencyCodes() {
        return StringUtils.EMPTY;
    }

    @Override
    public String getCountriesByCurrencyCode(String currencyCode) {
        return StringUtils.EMPTY;
    }

}
