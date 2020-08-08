package com.practice.utils;

import static com.practice.it.helpers.KeysOfHttpHeaders.ACCEPT_HEADER;

import java.nio.charset.StandardCharsets;

import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;

import com.practice.it.helpers.models.HttpRequest;

public final class HttpClient {

    private static TestRestTemplate testRestTemplate;

    private HttpClient() {}

    static {
        testRestTemplate = new TestRestTemplate(new RestTemplateBuilder().rootUri("http://localhost:1111"));
        testRestTemplate.getRestTemplate().setRequestFactory(new HttpComponentsClientHttpRequestFactory());
        testRestTemplate.getRestTemplate().getMessageConverters().add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));
    }

    public static ResponseEntity<String> doRequest(String requestUri) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(ACCEPT_HEADER, MediaType.APPLICATION_JSON_VALUE);
        HttpEntity requestEntity = new HttpEntity(headers);
        return testRestTemplate.exchange(requestUri, HttpMethod.GET, requestEntity, String.class);
    }

    public static <T, R> ResponseEntity<R> doRequest(HttpRequest<T> httpRequest, Class<R> responseType) {
        HttpEntity<T> requestEntity;
        if (httpRequest.getRequestBody() != null) {
            requestEntity = new HttpEntity<>(httpRequest.getRequestBody(), httpRequest.getHeaders());
        } else {
            requestEntity = new HttpEntity<>(httpRequest.getHeaders());
        }
        return testRestTemplate.exchange(httpRequest.getUri(), httpRequest.getHttpMethod(), requestEntity, responseType);
    }

}
