package com.practice.utils;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;

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
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        HttpEntity requestEntity = new HttpEntity(headers);
        return testRestTemplate.exchange(requestUri, HttpMethod.GET, requestEntity, String.class);
    }

}
