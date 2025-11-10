package com.tpi.solicitudes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class MsSolicitudesApplication {

    public static void main(String[] args) {
        // Establecer TimeZone UTC para toda la aplicación
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        SpringApplication.run(MsSolicitudesApplication.class, args);
    }
}

