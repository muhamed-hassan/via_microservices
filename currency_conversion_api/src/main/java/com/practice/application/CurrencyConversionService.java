package com.practice.application;

import java.util.List;
import java.util.Map;

public interface CurrencyConversionService {

    Map<String, String> getCountriesWithTheirCurrencyCodes();

    List<String> getCountriesByCurrencyCode(String currencyCode);

    Map<String, Double> getHighestAndLowestRatesByBase(String base);

    Map<String, Double> getLatestRatesByBase(String base);

}
