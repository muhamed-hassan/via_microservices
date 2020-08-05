package com.practice.it.configs;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.stereotype.Component;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

@Component
public class WireMockServerConfig {

    private WireMockServer wireMockServer;

    @PostConstruct
    public void init() {
        wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().port(5555));
        wireMockServer.start();
    }

    @PreDestroy
    public void dispose() {
        wireMockServer.stop();
    }

    public WireMockServer wireMockServer() {
        return wireMockServer;
    }

}
