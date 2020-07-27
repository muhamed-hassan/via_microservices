package com.practice.it.helpers;

public final class InternalEndpoints {

    private InternalEndpoints() {}

    public static final String ALL_COUNTRIES_INTERNAL = "/v1/countries";

    public static final String COUNTRIES_BY_BASE_INTERNAL = "/v1/countries/{0}";
    public static final String COUNTRIES_BY_BASE_INTERNAL_WITH_INVALID_CURRENCY_CODE = "/v1/countries/XXXX";

    public static final String LATEST_RATES_INTERNAL = "/v1/rates?currencyCode={0}";
    public static final String LATEST_RATES_INTERNAL_MALFORMED = "/v1/rates?currency-code={0}";
    public static final String LATEST_RATES_INTERNAL_WITH_INVALID_CURRENCY_CODE = "/v1/rates?currencyCode=XXXX";

    public static final String LOWEST_AND_HIGHEST_RATE_INTERNAL = "/v1/rates/statistics?currencyCode={0}";
    public static final String LOWEST_AND_HIGHEST_RATE_INTERNAL_MALFORMED = "/v1/rates/statistics?currency-code={0}";
    public static final String LOWEST_AND_HIGHEST_RATE_INTERNAL_WITH_INVALID_CURRENCY_CODE = "/v1/rates/statistics?currencyCode=XXXX";

}
