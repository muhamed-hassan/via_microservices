package com.practice.application;

import java.util.DoubleSummaryStatistics;
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

import feign.FeignException;

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
        try {
            return countryClient.getCountriesWithTheirCurrencyCodes();
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
    public List<CountryWithBriefView> getCountriesByCurrencyCode(String currencyCode) {
        try {
            return countryClient.getCountriesByCurrencyCode(currencyCode);
        } catch (FeignException.ServiceUnavailable e) {
            throw new ServiceNotAvailableException();
        }
    }

    @Override
    public StatisticsOfRates getHighestAndLowestRatesByBase(String base) {
        DoubleSummaryStatistics statistics = getLatestRatesByBase(base)
                                                    .getRates()
                                                    .entrySet()
                                                    .stream()
                                                    .map(Map.Entry::getValue)
                                                    .mapToDouble(Double::doubleValue)
                                                    .summaryStatistics();
        return new StatisticsOfRates(statistics.getMin(), statistics.getMax());
    }

    @Override
    public Rates getLatestRatesByBase(String base) {
        try {
            return rateClient.getLatestRatesByBase(base);
        } catch (FeignException.ServiceUnavailable e) {
            throw new ServiceNotAvailableException();
        }
    }

}
