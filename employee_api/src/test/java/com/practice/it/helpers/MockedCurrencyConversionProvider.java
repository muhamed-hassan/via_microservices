package com.practice.it.helpers;

import static com.practice.utils.Mappings.LATEST_RATES_OF_ISK_JSON;
import static com.practice.utils.MappingsCache.getMappingFromExternalApi;

import java.util.List;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.practice.infrastructure.integration.CurrencyConversionClient;
import com.practice.infrastructure.integration.models.Rate;

@Primary
@Component
public class MockedCurrencyConversionProvider implements CurrencyConversionClient {

    @Override
    public List<Rate> getLatestRatesByBase(String currencyCode) {
        var expectedProcessedResponse = getMappingFromExternalApi(LATEST_RATES_OF_ISK_JSON);
        try {
            return new ObjectMapper().readValue(expectedProcessedResponse, new TypeReference<>() { });
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}
