package com.tpi.gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Controlador de fallback para cuando los microservicios no están disponibles
 */
@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/flotas")
    public ResponseEntity<Map<String, Object>> flotasFallback() {
        return createFallbackResponse("Microservicio de Flotas");
    }

    @GetMapping("/solicitudes")
    public ResponseEntity<Map<String, Object>> solicitudesFallback() {
        return createFallbackResponse("Microservicio de Solicitudes");
    }

    @GetMapping("/rutas")
    public ResponseEntity<Map<String, Object>> rutasFallback() {
        return createFallbackResponse("Microservicio de Rutas");
    }

    @GetMapping("/precios")
    public ResponseEntity<Map<String, Object>> preciosFallback() {
        return createFallbackResponse("Microservicio de Precios");
    }

    @GetMapping("/localizaciones")
    public ResponseEntity<Map<String, Object>> localizacionesFallback() {
        return createFallbackResponse("Microservicio de Localizaciones");
    }

    private ResponseEntity<Map<String, Object>> createFallbackResponse(String serviceName) {
        Map<String, Object> response = Map.of(
            "error", "Service Unavailable",
            "message", serviceName + " temporalmente no disponible. Por favor, intente más tarde.",
            "timestamp", LocalDateTime.now().toString(),
            "status", HttpStatus.SERVICE_UNAVAILABLE.value()
        );

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }
}
