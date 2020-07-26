package com.practice.configs;

import java.time.Duration;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    private static final long DEFAULT_TIMEOUT = 5_000L;

    @Bean
    public RestTemplate restTemplate(final RestTemplateBuilder builder) {
        return builder.setConnectTimeout(Duration.ofMillis(DEFAULT_TIMEOUT))
                        .setReadTimeout(Duration.ofMillis(DEFAULT_TIMEOUT))
                        .build();
    }

}
