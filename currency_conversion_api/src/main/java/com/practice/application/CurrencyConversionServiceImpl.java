package com.practice.application;

import java.util.List;
import java.util.Map;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.practice.infrastructure.integration.CountryClient;
import com.practice.infrastructure.integration.RateClient;
import com.practice.infrastructure.integration.models.CountryWithBriefView;
import com.practice.infrastructure.integration.models.CountryWithDetailedView;
import com.practice.infrastructure.integration.models.Rates;
import com.practice.infrastructure.integration.models.StatisticsOfRates;

@Service
public class CurrencyConversionServiceImpl implements CurrencyConversionService {

    private final CountryClient countryClient;

    private final RateClient rateClient;

    public CurrencyConversionServiceImpl(CountryClient countryClient, RateClient rateClient) {
        this.countryClient = countryClient;
        this.rateClient = rateClient;
    }

    @Cacheable(cacheNames = "CurrencyConversionService::getCountriesWithTheirCurrencyCodes()")
    @Override
    public List<CountryWithDetailedView> getCountriesWithTheirCurrencyCodes() {
        return countryClient.getCountriesWithTheirCurrencyCodes();
    }

    @Cacheable(cacheNames = "CurrencyConversionService::getCountriesByCurrencyCode()")
    @Override
    public List<CountryWithBriefView> getCountriesByCurrencyCode(String currencyCode) {
        return countryClient.getCountriesByCurrencyCode(currencyCode);
    }

    @Override
    public StatisticsOfRates getHighestAndLowestRatesByBase(String base) {
        var latestRates = getLatestRatesByBase(base).getRates();
        StatisticsOfRates statisticsOfRates;
        if (latestRates.isEmpty()) {
            statisticsOfRates = new StatisticsOfRates();
        } else {
            var statistics = latestRates.entrySet()
                                                                .stream()
                                                                .map(Map.Entry::getValue)
                                                                .mapToDouble(Double::doubleValue)
                                                                .summaryStatistics();
            statisticsOfRates = new StatisticsOfRates(statistics.getMin(), statistics.getMax());
        }
        return statisticsOfRates;
    }

    @Override
    public Rates getLatestRatesByBase(String base) {
        return rateClient.getLatestRatesByBase(base);
    }

}
