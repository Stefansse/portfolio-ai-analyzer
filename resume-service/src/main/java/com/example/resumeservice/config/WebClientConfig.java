package com.example.resumeservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient userServiceWebClient(@Value("${userservice.url}") String userServiceUrl) {
        return WebClient.builder()
                .baseUrl(userServiceUrl)
                .build();
    }

    @Bean
    public WebClient analyticsWebClient(@Value("${analytics.service.url}") String baseUrl) {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }
}