package com.tpi.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.logging.Logger;

/**
 * Filtro para logging de requests y responses
 */
@Component
public class LoggingFilter extends AbstractGatewayFilterFactory<LoggingFilter.Config> {

    private static final Logger logger = Logger.getLogger(LoggingFilter.class.getName());

    public LoggingFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            logger.info("Incoming request: " + request.getMethod() + " " + request.getURI());

            return chain.filter(exchange).then(
                Mono.fromRunnable(() -> {
                    logger.info("Response status: " + exchange.getResponse().getStatusCode());
                })
            );
        };
    }

    public static class Config {
        // Configuración del filtro (por ahora vacía)
    }
}
