package com.tpi.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;

import java.util.HashSet;
import java.util.Set;

/**
 * Configuración de seguridad para el API Gateway (WebFlux)
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityFilterChain(
            ServerHttpSecurity http,
            @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}") String jwkSetUri,
            @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}") String issuerUri) {
        // Converter para mapear roles desde el claim realm_access.roles de Keycloak
        JwtAuthenticationConverter jwtAuthConverter = new JwtAuthenticationConverter();
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        // Keycloak publica roles normalmente en realm_access.roles
        grantedAuthoritiesConverter.setAuthoritiesClaimName("realm_access.roles");
        // Spring Security espera prefijo ROLE_ para matches de hasRole
        grantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");
        jwtAuthConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);

        ReactiveJwtAuthenticationConverterAdapter reactiveConverter = new ReactiveJwtAuthenticationConverterAdapter(jwtAuthConverter);

        // Construir el JwtDecoder reactivo personalizado (sin registrar un bean global con nombre 'jwtDecoder')
        ReactiveJwtDecoder decoder = NimbusReactiveJwtDecoder.withJwkSetUri(jwkSetUri).build();

        // Construir un set de issuers permitidos (añadir variantes comunes en desarrollo)
        Set<String> allowedIssuers = new HashSet<>();
        allowedIssuers.add(issuerUri);
        // variantes comunes cuando se usa localhost desde fuera del docker network
        allowedIssuers.add(issuerUri.replace("tpi-keycloak", "localhost"));
        allowedIssuers.add(issuerUri.replace("tpi-keycloak", "127.0.0.1"));

        OAuth2TokenValidator<Jwt> issuerValidator = token -> {
            String iss = token.getClaimAsString("iss");
            if (iss != null && allowedIssuers.contains(iss)) {
                return OAuth2TokenValidatorResult.success();
            }
            OAuth2Error err = new OAuth2Error("invalid_token", "The iss claim is not valid", null);
            return OAuth2TokenValidatorResult.failure(err);
        };

        // Mantener validación de timestamps (exp,nbf)
        JwtTimestampValidator timeValidator = new JwtTimestampValidator();

        DelegatingOAuth2TokenValidator<Jwt> delegatingValidator = new DelegatingOAuth2TokenValidator<>(timeValidator, issuerValidator);
        // NimbusReactiveJwtDecoder expone setJwtValidator vía la implementación concreta
        if (decoder instanceof NimbusReactiveJwtDecoder) {
            ((NimbusReactiveJwtDecoder) decoder).setJwtValidator(delegatingValidator);
        }

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
                    .pathMatchers(org.springframework.http.HttpMethod.GET, "/api/flotas/transportistas/telefono/{telefono}").permitAll()

                // Rutas de clientes - endpoints básicos de lectura (GET)
                .pathMatchers(org.springframework.http.HttpMethod.GET, "/api/clientes").permitAll()
                .pathMatchers(org.springframework.http.HttpMethod.GET, "/api/clientes/{tipoDocClienteId}/{numDocCliente}").permitAll()
                .pathMatchers(org.springframework.http.HttpMethod.GET, "/api/clientes/buscar/nombres").permitAll()
                .pathMatchers(org.springframework.http.HttpMethod.GET, "/api/clientes/buscar/apellidos").permitAll()

                // Rutas de contenedores - endpoints básicos de lectura (GET)
                .pathMatchers(org.springframework.http.HttpMethod.GET, "/api/contenedores").permitAll()
                //.pathMatchers(org.springframework.http.HttpMethod.GET, "/api/contenedores/{id}").permitAll()

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

                // Rutas de tramos - endpoints básicos de lectura (GET)
                .pathMatchers(org.springframework.http.HttpMethod.GET, "/api/tramos").permitAll()
                .pathMatchers(org.springframework.http.HttpMethod.GET, "/api/tramos/{id}").permitAll()
                .pathMatchers(org.springframework.http.HttpMethod.GET, "/api/tramos/ruta/{rutaId}").permitAll()

                // Rutas de ubicaciones - endpoints básicos de lectura (GET)
                .pathMatchers(org.springframework.http.HttpMethod.GET, "/api/ubicaciones").permitAll()
                .pathMatchers(org.springframework.http.HttpMethod.GET, "/api/ubicaciones/count").permitAll()






                    // Cualquier otra ruta requiere autenticación
                .anyExchange().authenticated())
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtDecoder(decoder).jwtAuthenticationConverter(reactiveConverter)))
            .build();
    }

}
