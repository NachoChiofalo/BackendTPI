package com.tpi.localizaciones.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tpi.localizaciones.dto.DistanciaDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeoService {

    @Value("${osrm.base-url:http://localhost:5000}")
    private String osrmBaseUrl;

    private final ObjectMapper mapper = new ObjectMapper();

    public DistanciaDTO calcularDistancia(String origen, String destino) throws Exception {
        // Origen y destino vienen en formato lat,long
        String url = osrmBaseUrl + "/route/v1/driving/" + origen + ";" + destino + "?overview=false";

        String body;
        try {
            body = WebClient.create()
                    .get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (Exception e) {
            log.error("Error llamando a OSRM: {}", e.getMessage());
            throw new RuntimeException("Error consultando OSRM", e);
        }

        JsonNode root = mapper.readTree(body);
        if (!"Ok".equals(root.path("code").asText())) {
            throw new RuntimeException("Error en respuesta de OSRM: " + root.path("code").asText());
        }

        JsonNode route = root.path("routes").get(0);
        double meters = route.path("distance").asDouble();
        double durationSec = route.path("duration").asDouble();

        DistanciaDTO dto = new DistanciaDTO();
        dto.setOrigen(origen);
        dto.setDestino(destino);
        dto.setKilometros(meters / 1000.0);
        dto.setDuracionTexto(String.format("%.0f min", durationSec / 60.0));

        return dto;
    }
}