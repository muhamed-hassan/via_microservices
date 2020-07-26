package com.practice.services.impl;

import static java.util.Spliterator.ORDERED;
import static java.util.Spliterators.spliteratorUnknownSize;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.joining;

import java.util.Collections;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.practice.exceptions.ResponseProcessingFailureException;
import com.practice.configs.constants.ExceptionMessages;
import com.practice.integration.CurrencyConversionProvider;
import com.practice.services.CurrencyConversionService;
import com.practice.transfomers.CurrncyConversionTransformer;

@Component
public class CurrencyConversionServiceImpl implements CurrencyConversionService {

    private Logger logger = LoggerFactory.getLogger(CurrencyConversionServiceImpl.class);

    @Autowired
    private CurrencyConversionProvider currencyConversionProvider;

    @Autowired
    private CurrncyConversionTransformer currncyConversionTransformer;

    @HystrixCommand(fallbackMethod = "fallbackOfGetCountriesWithTheirCurrencyCodes")
    @Override
    public Map<String, String> getCountriesWithTheirCurrencyCodes() {
        Map<String, String> countriesWithTheirCurrencyCodes = null;
        try {
            JsonNode countriesJsonNode = currencyConversionProvider.getCountriesWithTheirCurrencyCodes();
            if (countriesJsonNode.isArray()) {
                countriesWithTheirCurrencyCodes = currncyConversionTransformer.transform(
                                                    countriesJsonNode.spliterator(),
                                                    countryNode -> countryNode.get("name").asText(),
                                                    countryNode -> currncyConversionTransformer.transform(
                                                                        countryNode.get("currencies").spliterator(),
                                                                        currencyNode -> currencyNode.get("code").asText(),
                                                                        joining(",")));
            }
        } catch (Exception e) {
            logger.error(ExceptionMessages.FAILED_TO_PROCESS_THE_RESPONSE);
            throw new ResponseProcessingFailureException(e);
        }
        return countriesWithTheirCurrencyCodes;
    }

    private Map<String, String> fallbackOfGetCountriesWithTheirCurrencyCodes() {
        return Collections.emptyMap();
    }

    @HystrixCommand(fallbackMethod = "fallbackOfGetCountriesByCurrencyCode")
    @Override
    public List<String> getCountriesByCurrencyCode(final String currencyCode) {
        List<String> countries = null;
        try {
            JsonNode countriesJsonNode = currencyConversionProvider.getCountriesByCurrencyCode(currencyCode);
            if (countriesJsonNode.isArray()) {
                countries = currncyConversionTransformer.transform(
                                    countriesJsonNode.spliterator(),
                                    countryNode -> countryNode.get("name").asText(),
                                    toList());
            }
        } catch (Exception e) {
            logger.error(ExceptionMessages.FAILED_TO_PROCESS_THE_RESPONSE);
            throw new ResponseProcessingFailureException(e);
        }
        return countries;
    }

    private List<String> fallbackOfGetCountriesByCurrencyCode(final String currencyCode) {
        return Collections.emptyList();
    }

    @HystrixCommand(fallbackMethod = "fallbackOfGetHighestAndLowestRatesByBase")
    @Override
    public Map<String, Double> getHighestAndLowestRatesByBase(final String base) {
        DoubleSummaryStatistics doubleSummaryStatistics = currncyConversionTransformer.getStatistics(
                                                                getLatestRatesByBase(base).entrySet().stream(),
                                                                Map.Entry::getValue);
        return Map.of("highest", doubleSummaryStatistics.getMax(), "lowest", doubleSummaryStatistics.getMin());
    }

    private Map<String, Double> fallbackOfGetHighestAndLowestRatesByBase(final String base) {
        return Map.of("highest", -1.0, "lowest", -1.0);
    }

    @HystrixCommand(fallbackMethod = "fallbackOfGetLatestRatesByBase")
    @Override
    public Map<String, Double> getLatestRatesByBase(final String base) {
        Map<String, Double> latestRatesUsingBase = null;
        try {
            JsonNode ratesNode = currencyConversionProvider.getLatestRatesByBase(base);
            latestRatesUsingBase = currncyConversionTransformer.transform(
                                        spliteratorUnknownSize(ratesNode.get("rates").fields(), ORDERED),
                                        entry -> entry.getKey(), entry -> entry.getValue().asDouble());
        } catch (Exception e) {
            logger.error(ExceptionMessages.FAILED_TO_PROCESS_THE_RESPONSE);
            throw new ResponseProcessingFailureException(e);
        }
        return latestRatesUsingBase;
    }

    private Map<String, Double> fallbackOfGetLatestRatesByBase(final String base) {
        return Collections.emptyMap();
    }

}
