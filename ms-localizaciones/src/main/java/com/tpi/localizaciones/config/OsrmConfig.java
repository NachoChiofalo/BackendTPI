package com.tpi.localizaciones.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Configuración para el cliente OSRM
 */
@Configuration
@ConfigurationProperties(prefix = "osrm")
@Data
public class OsrmConfig {

    /**
     * URL base del servidor OSRM
     * Por defecto: http://localhost:5000
     */
    private String baseUrl = "http://localhost:5000";

    /**
     * Perfil de enrutamiento (car, bike, foot)
     * Por defecto: driving (equivalente a car)
     */
    private String profile = "driving";

    /**
     * Timeout de conexión en milisegundos
     */
    private Integer connectionTimeout = 5000;

    /**
     * Timeout de lectura en milisegundos
     */
    private Integer readTimeout = 10000;

    /**
     * Si OSRM está habilitado
     */
    private Boolean enabled = true;

    /**
     * Número máximo de reintentos
     */
    private Integer maxRetries = 3;

    @Bean
    public RestTemplate osrmRestTemplate() {
        return new RestTemplate();
    }
}

