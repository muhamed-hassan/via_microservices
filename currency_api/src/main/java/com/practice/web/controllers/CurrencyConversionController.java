package com.practice.web.controllers;

import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.practice.services.CurrencyConversionService;
import com.practice.web.validators.CurrencyCodeRule;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value = "Currency Conversion API")
@RestController
@RequestMapping(value = "v1")
@Validated
public class CurrencyConversionController {

    @Autowired
    private CurrencyConversionService currencyConversionService;


    @ApiOperation(value = "Get countries with their currency codes")
    @ApiResponses(value = {
        @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Succeeded in retrieval countries with their currency codes",
                        response = Map.class),
        @ApiResponse(code = HttpURLConnection.HTTP_UNAVAILABLE, message = "External endpoint not available")
    })
    @GetMapping("countries")
    public Map<String, String> getCountriesWithTheirCurrencyCodes() {
        return currencyConversionService.getCountriesWithTheirCurrencyCodes();
    }

    @ApiOperation(value = "Get latest rates by base")
    @ApiResponses(value = {
        @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Succeeded in retrieval the latest rates by base",
                        response = String.class),
        @ApiResponse(code = HttpURLConnection.HTTP_UNAVAILABLE, message = "External endpoint not available")
    })
    @GetMapping("countries/{currencyCode}")
    public List<String> getCountriesByCurrencyCode(@PathVariable String currencyCode) {
        return currencyConversionService.getCountriesByCurrencyCode(currencyCode);
    }

    @ApiOperation(value = "Get highest and lowest rates by base")
    @ApiResponses(value = {
        @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Succeeded in retrieval the highest and lowest rates by base",
                        response = Map.class),
        @ApiResponse(code = HttpURLConnection.HTTP_UNAVAILABLE, message = "External endpoint not available")
    })
    @GetMapping("rates/statistics")
    public Map<String, Double> getHighestAndLowestRatesByBase(@RequestParam @CurrencyCodeRule String currencyCode) {
        return currencyConversionService.getHighestAndLowestRatesByBase(currencyCode);
    }

    @ApiOperation(value = "Get latest rates by base")
    @ApiResponses(value = {
        @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Succeeded in retrieval the latest rates by base",
                        response = Map.class),
        @ApiResponse(code = HttpURLConnection.HTTP_UNAVAILABLE, message = "External endpoint not available")
    })
    @GetMapping("rates")
    public Map<String, Double> getLatestRatesByBase(@RequestParam @CurrencyCodeRule String currencyCode) {
        return currencyConversionService.getLatestRatesByBase(currencyCode);
    }

}
