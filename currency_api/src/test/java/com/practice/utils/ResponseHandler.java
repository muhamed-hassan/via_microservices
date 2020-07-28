package com.practice.utils;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;

public final class ResponseHandler {
    public static final String BASE_MAPPINGS_DIR = "__files/";

    public static final String ERRORS_DIR = "errors/";

    public static final String RESPONSES_OF_EXTERNAL_API_DIR = "external/";

    public static final String EXPECTED_RESPONSES_OF_INTERNAL_API_DIR = "expected/";

    public static final String COUNTRIES_JSON = "countries.json";
    public static final String COUNTRIES_WITH_INVALID_COUNTRY_NAME_JSON = "countries_with_invalid_country_name.json";
    public static final String COUNTRIES_WITH_INVALID_CURRENCIES_JSON = "countries_with_invalid_currencies.json";
    public static final String COUNTRIES_WITH_INVALID_CODE_JSON = "countries_with_invalid_code.json";
    public static final String COUNTRIES_WITH_INVALID_COUNTRY_NAME_AND_CURRENCIES_AND_CODE_JSON = "countries_with_invalid_country_name_and_currencies_and_code.json";
    public static final String COUNTRIES_WITH_INVALID_COUNTRY_NAME_AND_CURRENCIES_JSON = "countries_with_invalid_country_name_and_currencies.json";
    public static final String COUNTRIES_WITH_INVALID_CURRENCIES_AND_CODE_JSON = "countries_with_invalid_currencies_and_code.json";

    public static final String COUNTRY_OF_HUF_JSON = "country-of-huf.json";
    public static final String COUNTRY_OF_HUF_WITH_INVALID_COUNTRY_NAME_JSON = "country-of-huf_with_invalid_country_name.json";

    public static final String LATEST_RATES_OF_HUF_JSON = "latest-rates-of-huf.json";
    public static final String LATEST_RATES_OF_HUF_WITH_INVALID_RATES_JSON = "latest-rates-of-huf_with_invalid_rates.json";

    public static final String LOWEST_AND_HIGHEST_RATES_OF_HUF_JSON = "lowest-and-highest-rates-of-huf.json";

    public static final String SERVICE_NOT_AVAILABLE_JSON = "service_not_available.json";
    public static final String MISSING_CURRENCY_CODE_JSON = "missing_currencyCode_error.json";
    public static final String INVALID_CURRENCY_CODE_JSON = "invalid_currencyCode_error.json";
    private ResponseHandler() {}

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static String readJsonFrom(String responseLocation)
        throws Exception {
        String content = Files.readAllLines(Paths.get(ClassLoader.getSystemResource(BASE_MAPPINGS_DIR + responseLocation).toURI()),
            Charset.forName(StandardCharsets.UTF_8.name()))
            .stream()
            .collect(Collectors.joining());
        return OBJECT_MAPPER.readTree(content).toString();
    }

    public static ObjectMapper getObjectMapper() {
        return OBJECT_MAPPER;
    }

}
