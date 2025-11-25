package com.tpi.localizaciones.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@Slf4j
public class WebClientConfig {

    @Value("${osrm.base-url:${OSRM_URL:http://tpi-osrm:5000}}")
    private String osrmBaseUrl;

    @Value("${osrm.connection-timeout:5000}")
    private int connectTimeoutMillis;

    @Value("${osrm.read-timeout:10000}")
    private int readTimeoutMillis;

    @Bean
    public WebClient osrmWebClient() {
        return WebClient.builder()
                .baseUrl(osrmBaseUrl)
                .filter(logRequest())
                .filter(logResponse())
                .build();
    }

    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            log.debug("OSRM request: {} {}", clientRequest.method(), clientRequest.url());
            return reactor.core.publisher.Mono.just(clientRequest);
        });
    }

    private ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            log.debug("OSRM response status: {}", clientResponse.statusCode());
            return reactor.core.publisher.Mono.just(clientResponse);
        });
    }
}
