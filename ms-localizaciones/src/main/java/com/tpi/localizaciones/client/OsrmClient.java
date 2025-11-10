package com.tpi.localizaciones.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tpi.localizaciones.config.OsrmConfig;
import com.tpi.localizaciones.dto.osrm.OsrmDistanciaRequest;
import com.tpi.localizaciones.dto.osrm.OsrmDistanciaResponse;
import com.tpi.localizaciones.dto.osrm.OsrmRouteResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Cliente para interactuar con la API de OSRM
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OsrmClient {

    private final OsrmConfig osrmConfig;
    private final RestTemplate osrmRestTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Calcula la distancia y duración entre dos puntos usando OSRM
     *
     * @param request Datos de origen y destino
     * @return Respuesta con distancia y duración calculadas
     */
    public OsrmDistanciaResponse calcularDistancia(OsrmDistanciaRequest request) {
        if (!osrmConfig.getEnabled()) {
            log.warn("OSRM está deshabilitado en la configuración");
            return construirRespuestaError("OSRM deshabilitado", false);
        }

        try {
            log.info("Calculando distancia OSRM desde ({}, {}) hasta ({}, {})",
                    request.getLongitudOrigen(), request.getLatitudOrigen(),
                    request.getLongitudDestino(), request.getLatitudDestino());

            String url = construirUrl(request);
            log.debug("URL OSRM: {}", url);

            OsrmRouteResponse response = osrmRestTemplate.getForObject(url, OsrmRouteResponse.class);

            if (response == null || !"Ok".equals(response.getCode())) {
                String codigo = response != null ? response.getCode() : "NULL_RESPONSE";
                log.error("Error en respuesta OSRM: {}", codigo);
                return construirRespuestaError("Error OSRM: " + codigo, false);
            }

            if (response.getRoutes() == null || response.getRoutes().isEmpty()) {
                log.error("OSRM no retornó rutas");
                return construirRespuestaError("No se encontró una ruta", false);
            }

            OsrmRouteResponse.Route route = response.getRoutes().get(0);

            return construirRespuestaExitosa(route, request.getIncluirGeometria() ?
                    serializarRuta(response) : null);

        } catch (Exception e) {
            log.error("Error al consultar OSRM: {}", e.getMessage(), e);
            return construirRespuestaError("Error al conectar con OSRM: " + e.getMessage(), false);
        }
    }

    /**
     * Construye la URL para la consulta a OSRM
     * Formato: {baseUrl}/route/v1/{profile}/{lon1},{lat1};{lon2},{lat2}?params
     */
    private String construirUrl(OsrmDistanciaRequest request) {
        // OSRM espera coordenadas en formato longitud,latitud (NO latitud,longitud)
        String coordenadas = String.format("%s,%s;%s,%s",
                request.getLongitudOrigen().toPlainString(),
                request.getLatitudOrigen().toPlainString(),
                request.getLongitudDestino().toPlainString(),
                request.getLatitudDestino().toPlainString()
        );

        UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl(osrmConfig.getBaseUrl())
                .pathSegment("route", "v1", osrmConfig.getProfile())
                .path("/" + coordenadas);

        // Parámetros opcionales
        if (request.getIncluirGeometria()) {
            builder.queryParam("geometries", "geojson");
        } else {
            builder.queryParam("overview", "false");
        }

        if (request.getIncluirPasos()) {
            builder.queryParam("steps", "true");
        }

        // Siempre incluir alternativas para mejor precisión
        builder.queryParam("alternatives", "false");

        return builder.build().toUriString();
    }

    /**
     * Construye una respuesta exitosa a partir de la ruta de OSRM
     */
    private OsrmDistanciaResponse construirRespuestaExitosa(OsrmRouteResponse.Route route, String rutaJson) {
        // Convertir metros a kilómetros
        BigDecimal distanciaKm = BigDecimal.valueOf(route.getDistance())
                .divide(BigDecimal.valueOf(1000), 3, RoundingMode.HALF_UP);

        // Convertir segundos a minutos
        BigDecimal duracionMinutos = BigDecimal.valueOf(route.getDuration())
                .divide(BigDecimal.valueOf(60), 1, RoundingMode.HALF_UP);

        return OsrmDistanciaResponse.builder()
                .distanciaKm(distanciaKm)
                .duracionMinutos(duracionMinutos)
                .distanciaMetros(route.getDistance())
                .duracionSegundos(route.getDuration())
                .codigo("Ok")
                .exitoso(true)
                .rutaJson(rutaJson)
                .build();
    }

    /**
     * Construye una respuesta de error
     */
    private OsrmDistanciaResponse construirRespuestaError(String mensaje, boolean exitoso) {
        return OsrmDistanciaResponse.builder()
                .exitoso(exitoso)
                .mensajeError(mensaje)
                .codigo("ERROR")
                .build();
    }

    /**
     * Serializa la ruta completa a JSON
     */
    private String serializarRuta(OsrmRouteResponse response) {
        try {
            return objectMapper.writeValueAsString(response);
        } catch (Exception e) {
            log.error("Error al serializar ruta: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Verifica si OSRM está disponible
     */
    public boolean verificarDisponibilidad() {
        try {
            String url = osrmConfig.getBaseUrl() + "/route/v1/driving/-64.18105,-31.4135;-60.6985,-32.9471?overview=false";
            OsrmRouteResponse response = osrmRestTemplate.getForObject(url, OsrmRouteResponse.class);
            return response != null && "Ok".equals(response.getCode());
        } catch (Exception e) {
            log.error("OSRM no está disponible: {}", e.getMessage());
            return false;
        }
    }
}

