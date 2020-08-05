package com.practice.it.helpers.models;

import java.util.Map;

public class ResponseFromMockServer {

    private String responseBody;
    private int httpStatus;
    private Map<String, String> headers;

    public ResponseFromMockServer(final String responseBody, final int httpStatus, final Map<String, String> headers) {
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
