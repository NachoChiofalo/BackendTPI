package com.tpi.localizaciones.controller;

import com.tpi.localizaciones.dto.osrm.OsrmDistanciaResponse;
import com.tpi.localizaciones.service.OsrmDistanciaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Controlador REST para cálculo de distancias usando OSRM
 */
@RestController
@RequestMapping("/api/osrm")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
@Tag(name = "OSRM", description = "API para cálculo de distancias y rutas usando OSRM")
public class OsrmController {

    private final OsrmDistanciaService osrmDistanciaService;

    /**
     * Calcula la distancia entre dos puntos geográficos
     *
     * @param latOrigen Latitud del punto de origen
     * @param lonOrigen Longitud del punto de origen
     * @param latDestino Latitud del punto de destino
     * @param lonDestino Longitud del punto de destino
     * @param forzarRecalculo Si se debe forzar el recálculo ignorando caché (opcional, default false)
     * @return Distancia en km y duración en minutos
     */
    @GetMapping("/distancia")
    @Operation(summary = "Calcular distancia entre dos puntos",
               description = "Calcula la distancia real por carretera entre dos coordenadas geográficas usando OSRM")
    public ResponseEntity<OsrmDistanciaResponse> calcularDistancia(
            @Parameter(description = "Latitud del origen", example = "-31.4135")
            @RequestParam BigDecimal latOrigen,

            @Parameter(description = "Longitud del origen", example = "-64.18105")
            @RequestParam BigDecimal lonOrigen,

            @Parameter(description = "Latitud del destino", example = "-32.9471")
            @RequestParam BigDecimal latDestino,

            @Parameter(description = "Longitud del destino", example = "-60.6985")
            @RequestParam BigDecimal lonDestino,

            @Parameter(description = "Forzar recálculo ignorando caché")
            @RequestParam(required = false, defaultValue = "false") Boolean forzarRecalculo) {

        log.info("GET /api/osrm/distancia - Calculando distancia desde ({}, {}) hasta ({}, {}). Forzar: {}",
                latOrigen, lonOrigen, latDestino, lonDestino, forzarRecalculo);

        try {
            OsrmDistanciaResponse response = osrmDistanciaService.calcularDistancia(
                    latOrigen, lonOrigen, latDestino, lonDestino, forzarRecalculo);

            if (response.getExitoso()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
            }
        } catch (Exception e) {
            log.error("Error al calcular distancia: {}", e.getMessage(), e);
            OsrmDistanciaResponse errorResponse = OsrmDistanciaResponse.builder()
                    .exitoso(false)
                    .mensajeError("Error interno: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Calcula la distancia incluyendo detalles completos de la ruta
     */
    @GetMapping("/ruta")
    @Operation(summary = "Calcular distancia con detalles de ruta",
               description = "Calcula la distancia e incluye geometría y pasos de navegación de la ruta")
    public ResponseEntity<OsrmDistanciaResponse> calcularDistanciaConRuta(
            @Parameter(description = "Latitud del origen", example = "-31.4135")
            @RequestParam BigDecimal latOrigen,

            @Parameter(description = "Longitud del origen", example = "-64.18105")
            @RequestParam BigDecimal lonOrigen,

            @Parameter(description = "Latitud del destino", example = "-32.9471")
            @RequestParam BigDecimal latDestino,

            @Parameter(description = "Longitud del destino", example = "-60.6985")
            @RequestParam BigDecimal lonDestino) {

        log.info("GET /api/osrm/ruta - Calculando ruta completa desde ({}, {}) hasta ({}, {})",
                latOrigen, lonOrigen, latDestino, lonDestino);

        try {
            OsrmDistanciaResponse response = osrmDistanciaService.calcularDistanciaConRuta(
                    latOrigen, lonOrigen, latDestino, lonDestino);

            if (response.getExitoso()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
            }
        } catch (Exception e) {
            log.error("Error al calcular ruta: {}", e.getMessage(), e);
            OsrmDistanciaResponse errorResponse = OsrmDistanciaResponse.builder()
                    .exitoso(false)
                    .mensajeError("Error interno: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Verifica el estado del servicio OSRM
     */
    @GetMapping("/health")
    @Operation(summary = "Verificar disponibilidad de OSRM",
               description = "Verifica si el servicio OSRM está disponible y respondiendo")
    public ResponseEntity<Map<String, Object>> verificarDisponibilidad() {
        log.info("GET /api/osrm/health - Verificando disponibilidad de OSRM");

        Map<String, Object> response = new HashMap<>();
        boolean disponible = osrmDistanciaService.verificarDisponibilidad();

        response.put("disponible", disponible);
        response.put("servicio", "OSRM");
        response.put("timestamp", System.currentTimeMillis());

        if (disponible) {
            response.put("estado", "OK");
            response.put("mensaje", "Servicio OSRM disponible y respondiendo correctamente");
            return ResponseEntity.ok(response);
        } else {
            response.put("estado", "ERROR");
            response.put("mensaje", "Servicio OSRM no disponible o no responde");
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
        }
    }

    /**
     * Limpia el caché de distancias expiradas
     */
    @DeleteMapping("/cache/limpiar")
    @Operation(summary = "Limpiar caché expirado",
               description = "Elimina del caché las distancias calculadas que ya expiraron")
    public ResponseEntity<Map<String, Object>> limpiarCache() {
        log.info("DELETE /api/osrm/cache/limpiar - Limpiando caché expirado");

        try {
            int eliminadas = osrmDistanciaService.limpiarCacheExpirado();

            Map<String, Object> response = new HashMap<>();
            response.put("exitoso", true);
            response.put("distanciasEliminadas", eliminadas);
            response.put("mensaje", "Caché limpiado exitosamente");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error al limpiar caché: {}", e.getMessage(), e);

            Map<String, Object> response = new HashMap<>();
            response.put("exitoso", false);
            response.put("mensaje", "Error al limpiar caché: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Endpoint de ejemplo con coordenadas predefinidas para testing
     */
    @GetMapping("/ejemplo")
    @Operation(summary = "Ejemplo de cálculo de distancia",
               description = "Calcula la distancia entre Córdoba Capital y Rosario (ejemplo predefinido)")
    public ResponseEntity<OsrmDistanciaResponse> calcularEjemplo() {
        log.info("GET /api/osrm/ejemplo - Calculando ejemplo Córdoba -> Rosario");

        // Córdoba Capital -> Rosario
        BigDecimal latCordoba = new BigDecimal("-31.4135");
        BigDecimal lonCordoba = new BigDecimal("-64.18105");
        BigDecimal latRosario = new BigDecimal("-32.9471");
        BigDecimal lonRosario = new BigDecimal("-60.6985");

        return calcularDistancia(latCordoba, lonCordoba, latRosario, lonRosario, false);
    }
}

