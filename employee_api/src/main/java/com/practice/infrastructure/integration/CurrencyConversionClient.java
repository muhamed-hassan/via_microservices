package com.practice.infrastructure.integration;

import java.util.List;

import com.practice.infrastructure.integration.models.Rate;

public interface CurrencyConversionClient {

    List<Rate> getLatestRatesByBase(String currencyCode);

}
