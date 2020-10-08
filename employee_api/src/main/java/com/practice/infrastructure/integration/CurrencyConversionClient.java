package com.practice.infrastructure.integration;

import java.util.Map;

public interface CurrencyConversionClient {

    Map<String, Double> getLatestRatesByBase(String currencyCode);

}
