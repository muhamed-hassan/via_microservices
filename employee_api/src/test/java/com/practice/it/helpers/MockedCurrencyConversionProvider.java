package com.practice.it.helpers;

import static com.practice.utils.Mappings.LATEST_RATES_OF_ISK_JSON;
import static com.practice.utils.MappingsCache.getMappingFromExternalApi;

import java.util.Map;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.practice.infrastructure.integration.CurrencyConversionProvider;

@Primary
@Component
public class MockedCurrencyConversionProvider implements CurrencyConversionProvider {

    @Override
    public Map<String, Double> getLatestRatesByBase(String currencyCode) {
        String expectedProcessedResponse = getMappingFromExternalApi(LATEST_RATES_OF_ISK_JSON);
        Map<String, Double> result = null;
        try {
            result = new ObjectMapper().readValue(expectedProcessedResponse, Map.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return result;
    }

}
