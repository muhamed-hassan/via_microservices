package com.practice.it.models;

import java.util.Map;

public class ResponseFromMockServer {

    private String bodyFile;
    private int status;
    private Map<String, String> headers;

    public ResponseFromMockServer withBodyFile(String bodyFile) {
        this.bodyFile = bodyFile;
        return this;
    }

    public ResponseFromMockServer withStatus(int status) {
        this.status = status;
        return this;
    }

    public ResponseFromMockServer withHeaders(Map<String, String> headers) {
        this.headers = headers;
        return this;
    }

    public String getBodyFile() {
        return bodyFile;
    }

    public int getStatus() {
        return status;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

}
