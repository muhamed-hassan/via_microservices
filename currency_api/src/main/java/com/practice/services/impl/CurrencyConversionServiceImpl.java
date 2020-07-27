package com.practice.services.impl;

import static java.util.Spliterator.ORDERED;
import static java.util.Spliterators.spliteratorUnknownSize;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.practice.exceptions.ServiceNotAvailable;
import com.practice.integration.CountryProvider;
import com.practice.integration.RateProvider;
import com.practice.services.CurrencyConversionService;
import com.practice.transfomers.CurrncyConversionTransformer;

import feign.FeignException;

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
        try {
            String responseBody = countryProvider.getCountriesWithTheirCurrencyCodes().getBody();
            JsonNode countriesJsonNode = objectMapper.readTree(responseBody);
            return currncyConversionTransformer.transform(countriesJsonNode.spliterator(),
                                                            countryNode -> countryNode.get("name").asText(),
                                                            countryNode -> currncyConversionTransformer.transform(
                                                                countryNode.get("currencies").spliterator(),
                                                                currencyNode -> currencyNode.get("code").asText(),
                                                                joining(",")));
        } catch (FeignException.ServiceUnavailable e) {
            throw new ServiceNotAvailable();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Cacheable(cacheNames = "countriesByCurrencyCode")
    @Override
    public List<String> getCountriesByCurrencyCode(String currencyCode) {
        try {
            String responseBody = countryProvider.getCountriesByCurrencyCode(currencyCode).getBody();
            JsonNode countriesJsonNode = objectMapper.readTree(responseBody);
            return currncyConversionTransformer.transform(countriesJsonNode.spliterator(),
                                                            countryNode -> countryNode.get("name").asText(),
                                                            toList());
        } catch (FeignException.ServiceUnavailable e) {
            throw new ServiceNotAvailable();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<String, Double> getHighestAndLowestRatesByBase(String base) {
        DoubleSummaryStatistics doubleSummaryStatistics = currncyConversionTransformer.getStatistics(
                                                                    getLatestRatesByBase(base).entrySet().stream(),
                                                                    Map.Entry::getValue);
        return Map.of("highest", doubleSummaryStatistics.getMax(),
                            "lowest", doubleSummaryStatistics.getMin());
    }

    @Override
    public Map<String, Double> getLatestRatesByBase(String base) {
        try {
            String responseBody = rateProvider.getLatestRatesByBase(base).getBody();
            JsonNode ratesNode = objectMapper.readTree(responseBody);
            return currncyConversionTransformer.transform(spliteratorUnknownSize(ratesNode.get("rates").fields(), ORDERED),
                                                            entry -> entry.getKey(),
                                                            entry -> entry.getValue().asDouble());
        } catch (FeignException.ServiceUnavailable e) {
            throw new ServiceNotAvailable();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}
