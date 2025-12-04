package com.tpi.rutas.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        // Timeouts extendidos para operaciones como rutas tentativas
        factory.setConnectTimeout(10000); // 10 segundos para conectar
        factory.setReadTimeout(60000);    // 60 segundos para leer respuesta

        RestTemplate restTemplate = new RestTemplate(factory);
        return restTemplate;
    }
}
