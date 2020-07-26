package com.practice.it;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;
import static org.skyscreamer.jsonassert.JSONCompareMode.NON_EXTENSIBLE;

import java.text.MessageFormat;
import java.util.Map;
import java.util.stream.Stream;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.mock.web.MockHttpServletResponse;

import com.practice.it.helpers.models.RequestToMockServer;
import com.practice.it.helpers.models.ResponseFromEmployeeApi;
import com.practice.it.helpers.models.ResponseFromMockServer;

public class CurrencyConversionControllerIT extends BaseControllerIT {

    private static final String COUNTRIES_JSON = "countries.json";
    private static final String ALL_COUNTRIES_URI_OF_EXTERNAL_API = "/rest/v2/all";
    private static final String COUNTRIES_WITH_CURRENCY_CODES_FROM_EXTERNAL_API = EXTERNAL_API_MAPPINGS_DIR + COUNTRIES_JSON;
    private static final String ALL_COUNTRIES_URI_OF_CURRENT_API = "/api/v1/countries";
    private static final String EXPECTED_COUNTRIES_WITH_CURRENCY_CODES_FROM_CURRENT_API = EXPECTED_FROM_CURRENT_API_MAPPINGS_DIR + COUNTRIES_JSON;

    private static final String COUNTRY_OF_HUF = "country-of-huf.json";
    private static final String COUNTRIES_BY_BASE_URI_OF_EXTERNAL_API = "/rest/v2/currency/{0}";
    private static final String COUNTRY_OF_HUF_FROM_EXTERNAL_API = EXTERNAL_API_MAPPINGS_DIR + COUNTRY_OF_HUF;
    private static final String COUNTRIES_BY_BASE_URI_OF_CURRENT_API = "/api/v1/countries/{0}";
    private static final String EXPECTED_COUNTRIES_FROM_CURRENT_API = EXPECTED_FROM_CURRENT_API_MAPPINGS_DIR + COUNTRY_OF_HUF;

    private static final String LATEST_RATES_OF_HUF = "latest-rates-of-huf.json";
    private static final String LATEST_RATES_URI_OF_EXTERNAL_API = "/latest?base={0}";
    private static final String LATEST_RATES_OF_HUF_FROM_EXTERNAL_API = EXTERNAL_API_MAPPINGS_DIR + LATEST_RATES_OF_HUF;
    private static final String LATEST_RATES_URI_OF_CURRENT_API = "/api/v1/rates?base={0}";
    private static final String EXPECTED_LATEST_RATES_OF_HUF_FROM_CURRENT_API = EXPECTED_FROM_CURRENT_API_MAPPINGS_DIR + LATEST_RATES_OF_HUF;

    private static final String LOWEST_AND_HIGHEST_RATES_OF_HUF = "lowest-and-highest-rates-of-huf.json";
    private static final String LOWEST_AND_HIGHEST_URI_OF_CURRENT_API = "/api/v1/rates/statistics?base={0}";
    private static final String EXPECTED_LOWEST_AND_HIGHEST_RATES_OF_HUF_FROM_CURRENT_API =
        EXPECTED_FROM_CURRENT_API_MAPPINGS_DIR + LOWEST_AND_HIGHEST_RATES_OF_HUF;

    private static final String BASE = "HUF";

    private static Map<String, String> countriesApiResponseHeaders = Map.ofEntries(
                        Map.entry("Date", "Mon, 20 Apr 2020 08:22:55 GMT"), Map.entry("Content-Type", "application/json;charset=utf-8"),
                        Map.entry("Transfer-Encoding", "chunked"), Map.entry("Connection", "keep-alive"), Map.entry("Access-Control-Allow-Origin", "*"),
                        Map.entry("Access-Control-Allow-Methods", "GET"), Map.entry("Access-Control-Allow-Headers", "Accept, X-Requested-With"),
                        Map.entry("Cache-Control", "public, max-age=86400"), Map.entry("CF-Cache-Status", "DYNAMIC"),
                        Map.entry("Expect-CT", "max-age=604800, report-uri=\"https://report-uri.cloudflare.com/cdn-cgi/beacon/expect-ct\""),
                        Map.entry("Server", "cloudflare"),
                        Map.entry("CF-RAY", "586d75d119d0fcad-VIE"), Map.entry("Content-Encoding", "br"));

    private static Map<String, String> ratesApiResponseHeaders = Map.ofEntries(
                        Map.entry("Date", "Mon, 20 Apr 2020 08:22:55 GMT"), Map.entry("Content-Type", "application/json"),
                        Map.entry("Transfer-Encoding", "chunked"), Map.entry("Connection", "keep-alive"),
                        Map.entry("Access-Control-Allow-Origin", "*"), Map.entry("Access-Control-Allow-Methods", "GET"),
                        Map.entry("Access-Control-Allow-Headers", "Accept, X-Requested-With"), Map.entry("Access-Control-Allow-Credentials", "true"),
                        Map.entry("Cache-Control", "max-age=1800"), Map.entry("CF-Cache-Status", "HIT"),
                        Map.entry("Age", "1190"), Map.entry("Expect-CT", "max-age=604800, report-uri=\"https://report-uri.cloudflare.com/cdn-cgi/beacon/expect-ct\""),
                        Map.entry("Server", "cloudflare"), Map.entry("CF-RAY", "586f01391a3ad40f-BUD"),
                        Map.entry("Content-Encoding", "br"));

    @ParameterizedTest
    @MethodSource("provideArgumentsForTestGettersToReturnCorrectResponse")
    public void testGetters_When3rdPartyApiIsAvailable_ThenReturnCorrectResponse(RequestToMockServer request, ResponseFromMockServer response,
                    String actualRequestUri, ResponseFromEmployeeApi responseFromEmployeeApi) throws Exception {
        prepareStubServer(request, response);
        String expectedProcessedResponse = responseFromEmployeeApi.getExpectedProcessedResponse();

        MockHttpServletResponse mockHttpServletResponse = fireRequest(get(actualRequestUri), responseFromEmployeeApi.isEncodeResponseWithUTF8());

        assertThat(mockHttpServletResponse.getStatus(), equalTo(OK.value()));
        if (responseFromEmployeeApi.getResponseClassWrapper() == JSONObject.class) {
            JSONAssert.assertEquals(expectedProcessedResponse, new JSONObject(mockHttpServletResponse.getContentAsString()), NON_EXTENSIBLE);
        } else if (responseFromEmployeeApi.getResponseClassWrapper() == JSONArray.class) {
            JSONAssert.assertEquals(expectedProcessedResponse, new JSONArray(mockHttpServletResponse.getContentAsString()), NON_EXTENSIBLE);
        }
    }

    private static Stream<Arguments> provideArgumentsForTestGettersToReturnCorrectResponse() throws Exception {
        ResponseFromMockServer allCountriesFromMockServer = new ResponseFromMockServer(readJsonFrom(COUNTRIES_WITH_CURRENCY_CODES_FROM_EXTERNAL_API), OK.value(), countriesApiResponseHeaders);
        ResponseFromEmployeeApi allCountriesFromEmployeeApi = new ResponseFromEmployeeApi(readJsonFrom(EXPECTED_COUNTRIES_WITH_CURRENCY_CODES_FROM_CURRENT_API), true, JSONObject.class);
        ResponseFromMockServer countriesOfHufFromMockServer = new ResponseFromMockServer(readJsonFrom(COUNTRY_OF_HUF_FROM_EXTERNAL_API), OK.value(), countriesApiResponseHeaders);
        ResponseFromEmployeeApi countriesOfHufFromEmployeeApi = new ResponseFromEmployeeApi(readJsonFrom(EXPECTED_COUNTRIES_FROM_CURRENT_API), true, JSONArray.class);
        ResponseFromMockServer lowestAndHighestRatesOfHUFFromMockServer = new ResponseFromMockServer(readJsonFrom(LATEST_RATES_OF_HUF_FROM_EXTERNAL_API), OK.value(), ratesApiResponseHeaders);
        ResponseFromEmployeeApi lowestAndHighestRatesOfHUFFromEmployeeApi = new ResponseFromEmployeeApi(readJsonFrom(EXPECTED_LOWEST_AND_HIGHEST_RATES_OF_HUF_FROM_CURRENT_API), true, JSONObject.class);
        ResponseFromMockServer latestRatesOfHufFromMockServer = new ResponseFromMockServer(readJsonFrom(LATEST_RATES_OF_HUF_FROM_EXTERNAL_API), OK.value(), ratesApiResponseHeaders);
        ResponseFromEmployeeApi latestRatesOfHufFromEmployeeApi = new ResponseFromEmployeeApi(readJsonFrom(EXPECTED_LATEST_RATES_OF_HUF_FROM_CURRENT_API), true, JSONObject.class);

        return Stream.of(
            Arguments.of(new RequestToMockServer(ALL_COUNTRIES_URI_OF_EXTERNAL_API), allCountriesFromMockServer,
                            ALL_COUNTRIES_URI_OF_CURRENT_API, allCountriesFromEmployeeApi),
            Arguments.of(new RequestToMockServer(MessageFormat.format(COUNTRIES_BY_BASE_URI_OF_EXTERNAL_API, BASE)), countriesOfHufFromMockServer,
                            MessageFormat.format(COUNTRIES_BY_BASE_URI_OF_CURRENT_API, BASE), countriesOfHufFromEmployeeApi),
            Arguments.of(new RequestToMockServer(MessageFormat.format(LATEST_RATES_URI_OF_EXTERNAL_API, BASE)), lowestAndHighestRatesOfHUFFromMockServer,
                            MessageFormat.format(LOWEST_AND_HIGHEST_URI_OF_CURRENT_API, BASE), lowestAndHighestRatesOfHUFFromEmployeeApi),
            Arguments.of(new RequestToMockServer(MessageFormat.format(LATEST_RATES_URI_OF_EXTERNAL_API, BASE)), latestRatesOfHufFromMockServer,
                            MessageFormat.format(LATEST_RATES_URI_OF_CURRENT_API, BASE), latestRatesOfHufFromEmployeeApi)
        );
    }

    @ParameterizedTest
    @MethodSource("provideArgumentsForTestGettersToReturnEmptyResponse")
    public void testGetters_When3rdPartyApiIsDown_ThenReturnEmptyResponse(RequestToMockServer requestToMockServer, ResponseFromMockServer responseFromMockServer,
                    String actualRequestUri, ResponseFromEmployeeApi responseFromEmployeeApi) throws Exception {
        prepareStubServer(requestToMockServer, responseFromMockServer);

        MockHttpServletResponse httpServletResponse = fireRequest(get(actualRequestUri), responseFromEmployeeApi.isEncodeResponseWithUTF8());

        assertThat(httpServletResponse.getStatus(), equalTo(OK.value()));
        if (responseFromEmployeeApi.getResponseClassWrapper() == JSONObject.class) {
            JSONAssert.assertEquals(responseFromEmployeeApi.getExpectedProcessedResponse(), new JSONObject(httpServletResponse.getContentAsString()), NON_EXTENSIBLE);
        } else if (responseFromEmployeeApi.getResponseClassWrapper() == JSONArray.class) {
            JSONAssert.assertEquals(responseFromEmployeeApi.getExpectedProcessedResponse(), new JSONArray(httpServletResponse.getContentAsString()), NON_EXTENSIBLE);
        }
    }

    private static Stream<Arguments> provideArgumentsForTestGettersToReturnEmptyResponse() throws Exception {
        ResponseFromMockServer allCountriesFromMockServer = new ResponseFromMockServer("[]", SERVICE_UNAVAILABLE.value(), countriesApiResponseHeaders);
        ResponseFromEmployeeApi allCountriesFromEmployeeApi = new ResponseFromEmployeeApi("{}", true, JSONObject.class);
        ResponseFromMockServer countriesOfHufFromMockServer = new ResponseFromMockServer("[]", SERVICE_UNAVAILABLE.value(), countriesApiResponseHeaders);
        ResponseFromEmployeeApi countriesOfHufFromEmployeeApi = new ResponseFromEmployeeApi("[]", true, JSONArray.class);
        ResponseFromMockServer lowestAndHighestRatesOfHUFFromMockServer = new ResponseFromMockServer("{}", SERVICE_UNAVAILABLE.value(), ratesApiResponseHeaders);
        ResponseFromEmployeeApi lowestAndHighestRatesOfHUFFromEmployeeApi = new ResponseFromEmployeeApi("{\"highest\":-1.0,\"lowest\":-1.0}", true, JSONObject.class);
        ResponseFromMockServer latestRatesOfHufFromMockServer = new ResponseFromMockServer("{}", SERVICE_UNAVAILABLE.value(), ratesApiResponseHeaders);
        ResponseFromEmployeeApi latestRatesOfHufFromEmployeeApi = new ResponseFromEmployeeApi("{}", true, JSONObject.class);

        return Stream.of(
            Arguments.of(new RequestToMockServer(ALL_COUNTRIES_URI_OF_EXTERNAL_API), allCountriesFromMockServer,
                            ALL_COUNTRIES_URI_OF_CURRENT_API, allCountriesFromEmployeeApi),
            Arguments.of(new RequestToMockServer(MessageFormat.format(COUNTRIES_BY_BASE_URI_OF_EXTERNAL_API, BASE)), countriesOfHufFromMockServer,
                            MessageFormat.format(COUNTRIES_BY_BASE_URI_OF_CURRENT_API, BASE), countriesOfHufFromEmployeeApi),
            Arguments.of(new RequestToMockServer(MessageFormat.format(LATEST_RATES_URI_OF_EXTERNAL_API, BASE)), lowestAndHighestRatesOfHUFFromMockServer,
                            MessageFormat.format(LOWEST_AND_HIGHEST_URI_OF_CURRENT_API, BASE), lowestAndHighestRatesOfHUFFromEmployeeApi),
            Arguments.of(new RequestToMockServer(MessageFormat.format(LATEST_RATES_URI_OF_EXTERNAL_API, BASE)), latestRatesOfHufFromMockServer,
                            MessageFormat.format(LATEST_RATES_URI_OF_CURRENT_API, BASE), latestRatesOfHufFromEmployeeApi)
        );
    }

}
