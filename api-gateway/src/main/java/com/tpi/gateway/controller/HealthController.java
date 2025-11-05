package com.tpi.gateway.controller;

import com.tpi.gateway.service.MicroserviceHealthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Controlador de health check para el API Gateway
 */
@RestController
@RequestMapping("/health")
@RequiredArgsConstructor
public class HealthController {

    private final MicroserviceHealthService microserviceHealthService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "API Gateway");
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("version", "1.0.0");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/ready")
    public ResponseEntity<Map<String, Object>> ready() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "READY");
        response.put("service", "API Gateway");
        response.put("timestamp", LocalDateTime.now().toString());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/live")
    public ResponseEntity<Map<String, Object>> live() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "ALIVE");
        response.put("service", "API Gateway");
        response.put("timestamp", LocalDateTime.now().toString());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/services")
    public Mono<ResponseEntity<Map<String, Object>>> servicesHealth() {
        return microserviceHealthService.checkAllServices()
            .map(serviceStatus -> {
                Map<String, Object> response = new HashMap<>();
                response.put("timestamp", LocalDateTime.now().toString());
                response.put("gateway", "UP");
                response.put("services", serviceStatus);

                // Determinar el estado general
                boolean allUp = serviceStatus.values().stream()
                    .allMatch("UP"::equals);
                response.put("overall_status", allUp ? "UP" : "DEGRADED");

                return ResponseEntity.ok(response);
            })
            .onErrorReturn(ResponseEntity.internalServerError()
                .body(Map.of(
                    "error", "Failed to check services health",
                    "timestamp", LocalDateTime.now().toString(),
                    "overall_status", "UNKNOWN"
                )));
    }
}
