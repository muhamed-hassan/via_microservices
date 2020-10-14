package com.practice.it.helpers;

import java.util.Map;

public final class ExternalApiResponseHeaders {

    public static final Map<String, String> COUNTRIES_API_RESPONSE_HEADERS = Map.ofEntries(Map.entry("Date", "Mon, 20 Apr 2020 08:22:55 GMT"),
        Map.entry("Content-Type", "application/json;charset=utf-8"), Map.entry("Transfer-Encoding", "chunked"),
        Map.entry("Connection", "keep-alive"), Map.entry("Access-Control-Allow-Origin", "*"),
        Map.entry("Access-Control-Allow-Methods", "GET"), Map.entry("Access-Control-Allow-Headers", "Accept, X-Requested-With"),
        Map.entry("Cache-Control", "public, max-age=86400"), Map.entry("CF-Cache-Status", "DYNAMIC"),
        Map.entry("Expect-CT", "max-age=604800, report-uri=\"https://report-uri.cloudflare.com/cdn-cgi/beacon/expect-ct\""),
        Map.entry("Server", "cloudflare"), Map.entry("CF-RAY", "586d75d119d0fcad-VIE"), Map.entry("Content-Encoding", "br"));

    public static final Map<String, String> RATES_API_RESPONSE_HEADERS = Map.ofEntries(Map.entry("Date", "Mon, 20 Apr 2020 08:22:55 GMT"),
        Map.entry("Content-Type", "application/json"), Map.entry("Transfer-Encoding", "chunked"),
        Map.entry("Connection", "keep-alive"), Map.entry("Access-Control-Allow-Origin", "*"),
        Map.entry("Access-Control-Allow-Methods", "GET"), Map.entry("Access-Control-Allow-Headers", "Accept, X-Requested-With"),
        Map.entry("Access-Control-Allow-Credentials", "true"), Map.entry("Cache-Control", "max-age=1800"),
        Map.entry("CF-Cache-Status", "HIT"), Map.entry("Age", "1190"),
        Map.entry("Expect-CT", "max-age=604800, report-uri=\"https://report-uri.cloudflare.com/cdn-cgi/beacon/expect-ct\""),
        Map.entry("Server", "cloudflare"), Map.entry("CF-RAY", "586f01391a3ad40f-BUD"), Map.entry("Content-Encoding", "br"));

    private ExternalApiResponseHeaders() {}

}
