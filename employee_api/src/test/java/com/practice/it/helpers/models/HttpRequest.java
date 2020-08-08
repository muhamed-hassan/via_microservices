package com.practice.it.helpers.models;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

/* **************************************************************************
 * Developed by : Muhamed Hassan                                            *
 * LinkedIn     : https://www.linkedin.com/in/muhamed-hassan/               *
 * GitHub       : https://github.com/muhamed-hassan                         *
 * ************************************************************************ */
public final class HttpRequest<T> {

    private String uri;

    private HttpHeaders headers;

    private HttpMethod httpMethod;

    private T requestBody;

    public static <T> HttpRequest from(String uri, HttpHeaders headers, HttpMethod httpMethod, T requestBody) {
        HttpRequest<T> httpRequest = new HttpRequest<>();
        httpRequest.uri = uri;
        httpRequest.headers = headers;
        httpRequest.httpMethod = httpMethod;
        httpRequest.requestBody = requestBody;
        return httpRequest;
    }

    public static HttpRequest from(String uri, HttpHeaders headers, HttpMethod httpMethod) {
        HttpRequest<Void> httpRequest = new HttpRequest<>();
        httpRequest.uri = uri;
        httpRequest.headers = headers;
        httpRequest.httpMethod = httpMethod;
        return httpRequest;
    }

    public String getUri() {
        return uri;
    }

    public HttpHeaders getHeaders() {
        return headers;
    }

    public HttpMethod getHttpMethod() {
        return httpMethod;
    }

    public T getRequestBody() {
        return requestBody;
    }

}
