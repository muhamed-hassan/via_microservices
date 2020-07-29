package com.practice.configs;

import java.util.List;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CachingConfig {

    public static final String ALL_COUNTRIES = "allCountries";
    public static final String COUNTRIES_BY_CURRENCY_CODE = "countriesByCurrencyCode";

    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(List.of(new ConcurrentMapCache(ALL_COUNTRIES),
                                        new ConcurrentMapCache(COUNTRIES_BY_CURRENCY_CODE)));
        return cacheManager;
    }

}
