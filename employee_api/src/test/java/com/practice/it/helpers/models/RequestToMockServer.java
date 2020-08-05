package com.practice.it.helpers.models;

public class RequestToMockServer {

    private String requestPath;

    public RequestToMockServer(final String requestPath) {
        this.requestPath = requestPath;
    }

    public String getRequestPath() {
        return requestPath;
    }

}
