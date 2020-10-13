package com.practice.application;

import java.util.List;

import com.practice.infrastructure.integration.models.CountryWithBriefView;
import com.practice.infrastructure.integration.models.CountryWithDetailedView;
import com.practice.infrastructure.integration.models.StatisticsOfRates;
import com.practice.infrastructure.integration.models.Rates;

public interface CurrencyConversionService {

    List<CountryWithDetailedView> getCountriesWithTheirCurrencyCodes();

    List<CountryWithBriefView> getCountriesByCurrencyCode(String currencyCode);

    StatisticsOfRates getHighestAndLowestRatesByBase(String base);

    Rates getLatestRatesByBase(String base);

}
