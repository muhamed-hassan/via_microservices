package com.practice.web.controllers;

import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.practice.services.CurrencyConversionService;
import com.practice.configs.constants.Messages;
import com.practice.configs.constants.Patterns;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value = "Currency Conversion API")
@RestController
@RequestMapping(value = "api/v1")
public class CurrencyConversionController {

    @Autowired
    private CurrencyConversionService currencyConversionService;

    @ApiOperation(value = "Get countries with their currency codes")
    @ApiResponses(value = {
        @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Succeeded in retrieval countries with their currency codes", response = Map.class)
    })
    @GetMapping(path = "countries", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, String> getCountriesWithTheirCurrencyCodes() {
        return currencyConversionService.getCountriesWithTheirCurrencyCodes();
    }

    @ApiOperation(value = "Get latest rates by base")
    @ApiResponses(value = {
        @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Succeeded in retrieval the latest rates by base", response = String.class)
    })
    @GetMapping(path = "countries/{currencyCode}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<String> getCountriesByCurrencyCode(@PathVariable("currencyCode") final String currencyCode) {
        return currencyConversionService.getCountriesByCurrencyCode(currencyCode);
    }

    @ApiOperation(value = "Get highest and lowest rates by base")
    @ApiResponses(value = {
        @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Succeeded in retrieval the highest and lowest rates by base", response = Map.class)
    })
    @GetMapping(path = "rates/statistics", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Double> getHighestAndLowestRatesByBase(
        @RequestParam("base") @Pattern(regexp = Patterns.CURRENCY_CODE_PATTERN, message = Messages.CURRENCY_CODE_SHOULD_BE_3_LETTERS) final String base) {
        return currencyConversionService.getHighestAndLowestRatesByBase(base);
    }

    @ApiOperation(value = "Get latest rates by base")
    @ApiResponses(value = {
        @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Succeeded in retrieval the latest rates by base", response = Map.class)
    })
    @GetMapping(path = "rates", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Double> getLatestRatesByBase(
        @RequestParam("base") @Pattern(regexp = Patterns.CURRENCY_CODE_PATTERN, message = Messages.CURRENCY_CODE_SHOULD_BE_3_LETTERS) final String base) {
        return currencyConversionService.getLatestRatesByBase(base);
    }

}
