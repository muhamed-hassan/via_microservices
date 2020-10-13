package com.practice.infrastructure.integration;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Component;

import com.practice.infrastructure.integration.models.CountryWithBriefView;
import com.practice.infrastructure.integration.models.CountryWithDetailedView;

@Component
public class CountryClientFallback implements CountryClient {

    @Override
    public List<CountryWithDetailedView> getCountriesWithTheirCurrencyCodes() {
        return Collections.emptyList();
    }

    @Override
    public List<CountryWithBriefView> getCountriesByCurrencyCode(String currencyCode) {
        return Collections.emptyList();
    }

}
