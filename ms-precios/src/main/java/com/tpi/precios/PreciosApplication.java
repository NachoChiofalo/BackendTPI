package com.tpi.precios;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class PreciosApplication {

    public static void main(String[] args) {
        // Establecer UTC como zona horaria por defecto para evitar problemas con PostgreSQL
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        SpringApplication.run(PreciosApplication.class, args);
    }
}
