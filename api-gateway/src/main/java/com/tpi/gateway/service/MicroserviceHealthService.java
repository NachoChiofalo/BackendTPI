package com.tpi.gateway.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Servicio para verificar el estado de los microservicios
 * Reemplaza la funcionalidad de health checks de Eureka
 */
@Service
@Slf4j
public class MicroserviceHealthService {

    private final WebClient webClient;

    @Value("${microservices.flotas.url}")
    private String flotasUrl;

    @Value("${microservices.solicitudes.url}")
    private String solicitudesUrl;

    @Value("${microservices.rutas.url}")
    private String rutasUrl;

    @Value("${microservices.precios.url}")
    private String preciosUrl;

    @Value("${microservices.localizaciones.url}")
    private String localizacionesUrl;

    public MicroserviceHealthService() {
        this.webClient = WebClient.builder()
            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024))
            .build();
    }

    /**
     * Verifica el estado de todos los microservicios
     */
    public Mono<Map<String, String>> checkAllServices() {
        Map<String, String> serviceStatus = new HashMap<>();

        return Mono.zip(
            checkServiceHealth("flotas", flotasUrl),
            checkServiceHealth("solicitudes", solicitudesUrl),
            checkServiceHealth("rutas", rutasUrl),
            checkServiceHealth("precios", preciosUrl),
            checkServiceHealth("localizaciones", localizacionesUrl)
        ).map(tuple -> {
            serviceStatus.put("flotas", tuple.getT1());
            serviceStatus.put("solicitudes", tuple.getT2());
            serviceStatus.put("rutas", tuple.getT3());
            serviceStatus.put("precios", tuple.getT4());
            serviceStatus.put("localizaciones", tuple.getT5());
            return serviceStatus;
        }).onErrorReturn(serviceStatus);
    }

    /**
     * Verifica el estado de un microservicio específico
     */
    public Mono<String> checkServiceHealth(String serviceName, String serviceUrl) {
        return webClient.get()
            .uri(serviceUrl + "/actuator/health")
            .retrieve()
            .bodyToMono(String.class)
            .map(response -> "UP")
            .timeout(Duration.ofSeconds(5))
            .onErrorReturn("DOWN")
            .doOnSuccess(status -> log.debug("Service {} is {}", serviceName, status))
            .doOnError(error -> log.warn("Service {} health check failed: {}", serviceName, error.getMessage()));
    }

    /**
     * Verifica si un servicio específico está disponible
     */
    public Mono<Boolean> isServiceAvailable(String serviceName) {
        String serviceUrl = getServiceUrl(serviceName);
        if (serviceUrl == null) {
            return Mono.just(false);
        }

        return checkServiceHealth(serviceName, serviceUrl)
            .map("UP"::equals);
    }

    private String getServiceUrl(String serviceName) {
        return switch (serviceName.toLowerCase()) {
            case "flotas" -> flotasUrl;
            case "solicitudes" -> solicitudesUrl;
            case "rutas" -> rutasUrl;
            case "precios" -> preciosUrl;
            case "localizaciones" -> localizacionesUrl;
            default -> null;
        };
    }
}
