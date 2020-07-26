package com.practice.it.helpers.models;

public class ResponseFromEmployeeApi {

    private String expectedProcessedResponse;
    private boolean encodeResponseWithUTF8;
    private Class<?> responseClassWrapper;

    public ResponseFromEmployeeApi(final String expectedProcessedResponse, final boolean encodeResponseWithUTF8, final Class<?> responseClassWrapper) {
        this.expectedProcessedResponse = expectedProcessedResponse;
        this.encodeResponseWithUTF8 = encodeResponseWithUTF8;
        this.responseClassWrapper = responseClassWrapper;
    }

    public String getExpectedProcessedResponse() {
        return expectedProcessedResponse;
    }

    public boolean isEncodeResponseWithUTF8() {
        return encodeResponseWithUTF8;
    }

    public Class<?> getResponseClassWrapper() {
        return responseClassWrapper;
    }

}
