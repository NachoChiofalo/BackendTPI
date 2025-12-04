package com.tpi.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de rutas del API Gateway
 * Define el enrutamiento hacia los microservicios usando URLs directas
 */
@Configuration
public class GatewayConfig {

    @Value("${microservices.flotas.url:http://localhost:8081}")
    private String flotasUrl;

    @Value("${microservices.solicitudes.url:http://localhost:8084}")
    private String solicitudesUrl;

    @Value("${microservices.rutas.url:http://localhost:8085}")
    private String rutasUrl;

    @Value("${microservices.precios.url:http://localhost:8083}")
    private String preciosUrl;

    @Value("${microservices.localizaciones.url:http://localhost:8087}")
    private String localizacionesUrl;

    @Bean
    public RouteLocator gatewayRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
            // Microservicio de Flotas
            .route("ms-flotas", r -> r
                .path("/api/flotas/**")
                .filters(f -> f
                    .circuitBreaker(config -> config
                        .setName("flotas-circuit-breaker")
                        .setFallbackUri("forward:/fallback/flotas"))
                    .addRequestHeader("X-Gateway-Source", "api-gateway"))
                .uri(flotasUrl))

            // Microservicio de Solicitudes
            .route("ms-solicitudes", r -> r
                .path("/api/solicitudes/**")
                .filters(f -> f
                    .circuitBreaker(config -> config
                        .setName("solicitudes-circuit-breaker")
                        .setFallbackUri("forward:/fallback/solicitudes"))
                    .addRequestHeader("X-Gateway-Source", "api-gateway"))
                .uri(solicitudesUrl))

            // Microservicio de Rutas
            .route("ms-rutas", r -> r
                .path("/api/rutas/**")
                .filters(f -> f
                    .circuitBreaker(config -> config
                        .setName("rutas-circuit-breaker")
                        .setFallbackUri("forward:/fallback/rutas"))
                    .addRequestHeader("X-Gateway-Source", "api-gateway"))
                .uri(rutasUrl))

            // Microservicio de Precios
            .route("ms-precios", r -> r
                .path("/api/precios/**")
                .filters(f -> f
                    .circuitBreaker(config -> config
                        .setName("precios-circuit-breaker")
                        .setFallbackUri("forward:/fallback/precios"))
                    .addRequestHeader("X-Gateway-Source", "api-gateway"))
                .uri(preciosUrl))

            // Microservicio de Localizaciones
            .route("ms-localizaciones", r -> r
                .path("/api/localizaciones/**")
                .filters(f -> f
                    .circuitBreaker(config -> config
                        .setName("localizaciones-circuit-breaker")
                        .setFallbackUri("forward:/fallback/localizaciones"))
                    .addRequestHeader("X-Gateway-Source", "api-gateway"))
                .uri(localizacionesUrl))

            .build();
    }
}
