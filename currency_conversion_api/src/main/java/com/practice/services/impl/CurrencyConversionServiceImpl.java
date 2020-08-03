package com.practice.services.impl;

import static java.util.Spliterator.ORDERED;
import static java.util.Spliterators.spliteratorUnknownSize;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.StreamSupport.stream;

import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.practice.exceptions.ServiceNotAvailableException;
import com.practice.integration.CountryProvider;
import com.practice.integration.RateProvider;
import com.practice.services.CurrencyConversionService;

import feign.FeignException;

@Component
public class CurrencyConversionServiceImpl implements CurrencyConversionService {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CountryProvider countryProvider;

    @Autowired
    private RateProvider rateProvider;

    @Cacheable(cacheNames = "CurrencyConversionService::getCountriesWithTheirCurrencyCodes()")
    @Override
    public Map<String, String> getCountriesWithTheirCurrencyCodes() {
        try {
            String responseBody = countryProvider.getCountriesWithTheirCurrencyCodes();
            if (StringUtils.isBlank(responseBody)) {
                throw new ServiceNotAvailableException();
            }
            JsonNode countriesJsonNode = objectMapper.readTree(responseBody);
            return stream(countriesJsonNode.spliterator(), false)
                    .collect(toMap(countryNode -> countryNode.get("name").asText(),
                                    countryNode -> stream(countryNode.get("currencies").spliterator(), false)
                                                    .map(currencyNode -> currencyNode.get("code").asText())
                                                    .collect( joining(","))));
        } catch (FeignException.ServiceUnavailable e) {
            throw new ServiceNotAvailableException();
        } catch (ServiceNotAvailableException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Cacheable(cacheNames = "CurrencyConversionService::getCountriesByCurrencyCode()")
    @Override
    public List<String> getCountriesByCurrencyCode(String currencyCode) {
        try {
            String responseBody = countryProvider.getCountriesByCurrencyCode(currencyCode);
            if (StringUtils.isBlank(responseBody)) {
                throw new ServiceNotAvailableException();
            }
            JsonNode countriesJsonNode = objectMapper.readTree(responseBody);
            return stream(countriesJsonNode.spliterator(), false)
                    .map(countryNode -> countryNode.get("name").asText())
                    .collect(toList());
        } catch (FeignException.ServiceUnavailable e) {
            throw new ServiceNotAvailableException();
        } catch (ServiceNotAvailableException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<String, Double> getHighestAndLowestRatesByBase(String base) {
        DoubleSummaryStatistics statistics = getLatestRatesByBase(base)
                                                    .entrySet()
                                                    .stream()
                                                    .map(Map.Entry::getValue)
                                                    .mapToDouble(Double::doubleValue)
                                                    .summaryStatistics();
        return Map.of("highest", statistics.getMax(),
                            "lowest", statistics.getMin());
    }

    @Override
    public Map<String, Double> getLatestRatesByBase(String base) {
        try {
            String responseBody = rateProvider.getLatestRatesByBase(base);
            if (StringUtils.isBlank(responseBody)) {
                throw new ServiceNotAvailableException();
            }
            JsonNode ratesNode = objectMapper.readTree(responseBody);
            return stream(spliteratorUnknownSize(ratesNode.get("rates").fields(), ORDERED), false)
                        .collect(toMap(Map.Entry::getKey, entry -> entry.getValue().asDouble()));
        } catch (FeignException.ServiceUnavailable e) {
            throw new ServiceNotAvailableException();
        } catch (ServiceNotAvailableException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
