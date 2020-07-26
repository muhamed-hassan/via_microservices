package com.practice.integration.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.practice.integration.CurrencyConversionProvider;

@Component
public class CurrencyConversionProviderImpl implements CurrencyConversionProvider {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${currencies.uri}")
    private String currenciesUri;

    @Value("${countries.uri}")
    private String countriesUri;

    @Value("${latest-rates.uri}")
    private String latestRatesUri;

    @Override
    public JsonNode getCountriesWithTheirCurrencyCodes() throws JsonProcessingException {
        String response = restTemplate.getForObject(countriesUri, String.class);
        return objectMapper.readTree(response);
    }

    @Override
    public JsonNode getCountriesByCurrencyCode(String currencyCode) throws JsonProcessingException {
        String response = restTemplate.getForObject(currenciesUri + currencyCode, String.class);
        return objectMapper.readTree(response);
    }

    @Override
    public JsonNode getLatestRatesByBase(String base) throws JsonProcessingException {
        String response = restTemplate.getForObject(latestRatesUri + "?base=" + base, String.class);
        return objectMapper.readTree(response);
    }

}
