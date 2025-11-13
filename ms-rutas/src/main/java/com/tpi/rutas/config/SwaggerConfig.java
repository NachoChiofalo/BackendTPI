package com.tpi.rutas.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Value("${server.port:8085}")
    private String serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .servers(List.of(
                    new Server().url("http://localhost:" + serverPort).description("Servidor Local"),
                    new Server().url("http://api-gateway:8080").description("API Gateway")
                ))
                .info(new Info()
                        .title("Microservicio de Rutas - API")
                        .description("API para la gestión y cálculo de rutas de transporte")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Equipo de Desarrollo TPI")
                                .email("desarrollo@tpi.com")
                                .url("https://github.com/tpi-backend-logistica"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")));
    }
}
