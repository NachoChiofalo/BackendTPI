package com.tpi.localizaciones.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tpi.localizaciones.dto.DistanciaDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

/**
 * INTEGRACIÓN CON API EXTERNA: OSRM (Open Source Routing Machine)
 * 
 * Servicio que integra con OSRM para calcular distancias y duraciones reales entre dos puntos.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GeoService {

    // El valor puede venir de application.properties (osrm.base-url) o de la variable de entorno OSRM_URL
    @Value("${osrm.base-url:${OSRM_URL:http://tpi-osrm:5000}}")
    private String osrmBaseUrl;

    private final ObjectMapper mapper = new ObjectMapper();

    // WebClient inyectado y configurado en WebClientConfig
    private final WebClient osrmWebClient;

    public DistanciaDTO calcularDistancia(String origen, String destino) throws Exception {
        String origenTrim = origen.trim();
        String destinoTrim = destino.trim();
        String coords = origenTrim + ";" + destinoTrim;

        log.debug("Preparando llamada a OSRM baseUrl={} coords={}", osrmBaseUrl, coords);

        String body;
        try {
            long start = System.nanoTime();
            body = osrmWebClient
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/route/v1/driving/{coords}")
                            .queryParam("overview", "false")
                            .build(coords))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(Duration.ofSeconds(15));
            long elapsedMs = (System.nanoTime() - start) / 1_000_000;
            log.debug("Respuesta de OSRM recibida en {} ms", elapsedMs);
        } catch (Exception e) {
            // Loguear la excepción completa para facilitar diagnóstico (incluye ClosedChannelException si ocurre)
            log.error("Error llamando a OSRM (baseUrl={}, coords={}): {}", osrmBaseUrl, coords, e.getMessage(), e);
            throw new RuntimeException("Error consultando OSRM", e);
        }

        if (body == null) {
            log.error("Respuesta vacía de OSRM para coords={}", coords);
            throw new RuntimeException("Respuesta vacía de OSRM");
        }

        JsonNode root = mapper.readTree(body);
        if (!"Ok".equals(root.path("code").asText())) {
            throw new RuntimeException("Error en respuesta de OSRM: " + root.path("code").asText());
        }

        JsonNode route = root.path("routes").get(0);
        double meters = route.path("distance").asDouble();
        double durationSec = route.path("duration").asDouble();

        DistanciaDTO dto = new DistanciaDTO();
        dto.setOrigen(origenTrim);
        dto.setDestino(destinoTrim);
        dto.setKilometros(meters / 1000.0);
        dto.setDuracionTexto(String.format("%.0f min", durationSec / 60.0));

        return dto;
    }
}