package com.practice.it.models;

import java.util.Map;

public final class ResponseFromMockServer {

    private final String responseBody;
    private final int httpStatus;
    private final Map<String, String> headers;

    public ResponseFromMockServer(String responseBody, int httpStatus, Map<String, String> headers) {
        this.responseBody = responseBody;
        this.httpStatus = httpStatus;
        this.headers = headers;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

}
