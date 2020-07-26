package com.practice.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

public interface CurrencyConversionProvider {

    JsonNode getCountriesWithTheirCurrencyCodes() throws JsonProcessingException;

    JsonNode getCountriesByCurrencyCode(String currencyCode) throws JsonProcessingException;

    JsonNode getLatestRatesByBase(String base) throws JsonProcessingException;

}
