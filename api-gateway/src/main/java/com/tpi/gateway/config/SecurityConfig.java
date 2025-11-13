package com.tpi.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

/**
 * Configuración de seguridad para el API Gateway (WebFlux)
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http) {
        return http
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .authorizeExchange(exchanges -> exchanges
                // Rutas públicas
                .pathMatchers("/actuator/**").permitAll()
                .pathMatchers("/fallback/**").permitAll()
                .pathMatchers("/health/**").permitAll()
                .pathMatchers("/swagger-ui/**").permitAll()
                .pathMatchers("/v3/api-docs/**").permitAll()
                // Rutas de autenticación (cuando se implemente)
                .pathMatchers("/auth/**").permitAll()

                // Rutas de flotas - endpoints de lectura públicos (GET)
                .pathMatchers(org.springframework.http.HttpMethod.GET, "/api/flotas/camiones").permitAll()
                .pathMatchers(org.springframework.http.HttpMethod.GET, "/api/flotas/camiones/disponibles").permitAll()
                .pathMatchers(org.springframework.http.HttpMethod.GET, "/api/flotas/camiones/no-disponibles").permitAll()
                .pathMatchers(org.springframework.http.HttpMethod.GET, "/api/flotas/camiones/capacidad").permitAll()
                .pathMatchers(org.springframework.http.HttpMethod.GET, "/api/flotas/camiones/{dominio}").permitAll()
                .pathMatchers(org.springframework.http.HttpMethod.GET, "/api/flotas/transportistas").permitAll()
                .pathMatchers(org.springframework.http.HttpMethod.GET, "/api/flotas/transportistas/{id}").permitAll()

                // Rutas de clientes - endpoints básicos de lectura (GET)
                .pathMatchers(org.springframework.http.HttpMethod.GET, "/api/clientes").permitAll()
                .pathMatchers(org.springframework.http.HttpMethod.GET, "/api/clientes/{tipoDocClienteId}/{numDocCliente}").permitAll()
                .pathMatchers(org.springframework.http.HttpMethod.GET, "/api/clientes/buscar/nombres").permitAll()
                .pathMatchers(org.springframework.http.HttpMethod.GET, "/api/clientes/buscar/apellidos").permitAll()

                // Rutas de contenedores - endpoints básicos de lectura (GET)
                .pathMatchers(org.springframework.http.HttpMethod.GET, "/api/contenedores").permitAll()
                .pathMatchers(org.springframework.http.HttpMethod.GET, "/api/contenedores/{id}").permitAll()

                // Rutas de depósitos - endpoints básicos de lectura (GET)
                .pathMatchers(org.springframework.http.HttpMethod.GET, "/api/depositos").permitAll()
                .pathMatchers(org.springframework.http.HttpMethod.GET, "/api/depositos/count").permitAll()
                .pathMatchers(org.springframework.http.HttpMethod.GET, "/api/depositos/{id}").permitAll()

                // Rutas de rutas - endpoints básicos de lectura (GET)
                .pathMatchers(org.springframework.http.HttpMethod.GET, "/api/rutas").permitAll()
                .pathMatchers(org.springframework.http.HttpMethod.GET, "/api/rutas/{id}").permitAll()

                // Rutas de solicitudes - endpoints básicos de lectura (GET)
                .pathMatchers(org.springframework.http.HttpMethod.GET, "/api/solicitudes").permitAll()
                .pathMatchers(org.springframework.http.HttpMethod.GET, "/api/solicitudes/{id}").permitAll()
                .pathMatchers("/api/solicitudes/health").permitAll()

                // Rutas de tarifas - endpoints básicos de lectura (GET)
                .pathMatchers(org.springframework.http.HttpMethod.GET, "/api/tarifas").permitAll()
                .pathMatchers(org.springframework.http.HttpMethod.GET, "/api/tarifas/vigentes").permitAll()
                .pathMatchers(org.springframework.http.HttpMethod.GET, "/api/tarifas/vigente-actual").permitAll()
                .pathMatchers(org.springframework.http.HttpMethod.GET, "/api/tarifas/{id}").permitAll()
                .pathMatchers(org.springframework.http.HttpMethod.GET, "/api/tarifas/vigentes-en").permitAll()
                .pathMatchers(org.springframework.http.HttpMethod.GET, "/api/tarifas/por-rango").permitAll()
                .pathMatchers(org.springframework.http.HttpMethod.GET, "/api/tarifas/vencen-proximamente").permitAll()
                .pathMatchers(org.springframework.http.HttpMethod.GET, "/api/tarifas/futuras").permitAll()

                // Rutas de tramos - endpoints básicos de lectura (GET)
                .pathMatchers(org.springframework.http.HttpMethod.GET, "/api/tramos").permitAll()
                .pathMatchers(org.springframework.http.HttpMethod.GET, "/api/tramos/{id}").permitAll()
                .pathMatchers(org.springframework.http.HttpMethod.GET, "/api/tramos/ruta/{rutaId}").permitAll()

                // Rutas de ubicaciones - endpoints básicos de lectura (GET)
                .pathMatchers(org.springframework.http.HttpMethod.GET, "/api/ubicaciones").permitAll()
                .pathMatchers(org.springframework.http.HttpMethod.GET, "/api/ubicaciones/count").permitAll()
                .pathMatchers(org.springframework.http.HttpMethod.GET, "/api/ubicaciones/{id}").permitAll()

                // Cualquier otra ruta requiere autenticación
                .anyExchange().authenticated())
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(withDefaults()))
            .build();
    }
}
