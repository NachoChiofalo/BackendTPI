package com.tpi.flotas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

import java.util.TimeZone;

@SpringBootApplication
@EnableFeignClients
public class FlotasApplication {

    public static void main(String[] args) {
        // Establecer UTC como zona horaria por defecto para evitar problemas con PostgreSQL
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        SpringApplication.run(FlotasApplication.class, args);
    }
}
