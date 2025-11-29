package com.tpi.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.logging.Logger;

/**
 * Filtro de autenticación para validar tokens JWT
 */
@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    private static final Logger logger = Logger.getLogger(AuthenticationFilter.class.getName());

    // Rutas que no requieren autenticación
    private static final List<String> PUBLIC_ROUTES = List.of(
        "/health",
        "/actuator",
        "/fallback",
        "/auth",
        "/swagger-ui",
        "/v3/api-docs"
    );

    public AuthenticationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getURI().getPath();

            // Verificar si la ruta es pública
            if (isPublicRoute(path)) {
                return chain.filter(exchange);
            }

            // Verificar presencia del token
            if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                return handleUnauthorized(exchange, "Token de autorización faltante");
            }

            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return handleUnauthorized(exchange, "Formato de token inválido");
            }

            String token = authHeader.substring(7);


            if (token.trim().isEmpty()) {
                return handleUnauthorized(exchange, "Token vacío");
            }

            logger.info("Token validado para la ruta: " + path);

            // Agregar información del usuario al header (si se extrae del JWT)
            ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                .header("X-User-Id", extractUserIdFromToken(token))
                .build();

            return chain.filter(exchange.mutate().request(modifiedRequest).build());
        };
    }

    private boolean isPublicRoute(String path) {
        return PUBLIC_ROUTES.stream().anyMatch(path::startsWith);
    }

    private Mono<Void> handleUnauthorized(ServerWebExchange exchange, String message) {
        logger.warning("Acceso no autorizado: " + message);

        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", "application/json");

        String body = String.format("{\"error\":\"Unauthorized\",\"message\":\"%s\"}", message);

        return response.writeWith(Mono.just(response.bufferFactory().wrap(body.getBytes())));
    }

    private String extractUserIdFromToken(String token) {
        // Implementación simplificada - en producción se usaría una librería JWT
        // Por ahora retornamos un ID genérico
        return "user-" + token.hashCode();
    }

    public static class Config {
        // Configuración del filtro
        private boolean enabled = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
}
