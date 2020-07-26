package com.practice.services.impl;

import static java.util.Spliterator.ORDERED;
import static java.util.Spliterators.spliteratorUnknownSize;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import java.util.Collections;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.practice.integration.CountryProvider;
import com.practice.integration.RateProvider;
import com.practice.services.CurrencyConversionService;
import com.practice.transfomers.CurrncyConversionTransformer;

@Component
public class CurrencyConversionServiceImpl implements CurrencyConversionService {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CountryProvider countryProvider;

    @Autowired
    private RateProvider rateProvider;

    @Autowired
    private CurrncyConversionTransformer currncyConversionTransformer;

    @Cacheable(cacheNames = "allCountries")
    @Override
    public Map<String, String> getCountriesWithTheirCurrencyCodes() {
        Map<String, String> countriesWithTheirCurrencyCodes;
        try {
            String responseBody = countryProvider.getCountriesWithTheirCurrencyCodes().getBody();
            if (responseBody.isEmpty()) {
                countriesWithTheirCurrencyCodes = Collections.emptyMap();
            } else {
                JsonNode countriesJsonNode = objectMapper.readTree(responseBody);
                countriesWithTheirCurrencyCodes = currncyConversionTransformer.transform(
                                                            countriesJsonNode.spliterator(),
                                                            countryNode -> countryNode.get("name").asText(),
                                                            countryNode -> currncyConversionTransformer.transform(
                                                                countryNode.get("currencies").spliterator(),
                                                                currencyNode -> currencyNode.get("code").asText(),
                                                                joining(",")));
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return countriesWithTheirCurrencyCodes;
    }

    @Cacheable(cacheNames = "countriesByCurrencyCode")
    @Override
    public List<String> getCountriesByCurrencyCode(String currencyCode) {
        List<String> countries;
        try {
            String responseBody = countryProvider.getCountriesByCurrencyCode(currencyCode).getBody();
            if (responseBody.isEmpty()) {
                countries = Collections.emptyList();
            } else {
                JsonNode countriesJsonNode = objectMapper.readTree(responseBody);
                countries = currncyConversionTransformer.transform(
                                        countriesJsonNode.spliterator(),
                                        countryNode -> countryNode.get("name").asText(),
                                        toList());
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return countries;
    }

    @Override
    public Map<String, Double> getHighestAndLowestRatesByBase(String base) {
        Map<String, Double> latestRatesUsingBase = getLatestRatesByBase(base);
        if (latestRatesUsingBase.isEmpty()) {
            return latestRatesUsingBase;
        }
        DoubleSummaryStatistics doubleSummaryStatistics = currncyConversionTransformer.getStatistics(
                                                                    latestRatesUsingBase.entrySet().stream(),
                                                                    Map.Entry::getValue);
        return Map.of("highest", doubleSummaryStatistics.getMax(),
                            "lowest", doubleSummaryStatistics.getMin());
    }

    @Override
    public Map<String, Double> getLatestRatesByBase(String base) {
        Map<String, Double> latestRatesUsingBase;
        try {
            String responseBody = rateProvider.getLatestRatesByBase(base).getBody();
            if (responseBody.isEmpty()) {
                latestRatesUsingBase = Collections.emptyMap();
            } else {
                JsonNode ratesNode = objectMapper.readTree(responseBody);
                latestRatesUsingBase = currncyConversionTransformer.transform(
                                                    spliteratorUnknownSize(ratesNode.get("rates").fields(), ORDERED),
                                                    entry -> entry.getKey(), entry -> entry.getValue().asDouble());
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return latestRatesUsingBase;
    }

}
