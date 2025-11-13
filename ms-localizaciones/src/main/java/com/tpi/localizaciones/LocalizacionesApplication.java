package com.tpi.localizaciones;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Aplicación principal del Microservicio de Localizaciones
 * Gestiona ciudades, ubicaciones y cálculo de distancias con OSRM
 */
@SpringBootApplication
@EnableFeignClients
@ConfigurationPropertiesScan
public class LocalizacionesApplication {

    public static void main(String[] args) {
        SpringApplication.run(LocalizacionesApplication.class, args);
    }
}

