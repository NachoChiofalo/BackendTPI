package com.tpi.localizaciones.controller;

import com.tpi.localizaciones.dto.DistanciaDTO;
import com.tpi.localizaciones.service.GeoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * API EXTERNA OBLIGATORIA: Integración con OSRM
 * 
 * Endpoint que expone la funcionalidad de cálculo de distancias usando OSRM.
 * Cumple con el requerimiento del enunciado de integración con API externa
 * para consultar distancia entre dos puntos expresados en latitud y longitud.
 * 
 * OSRM (Open Source Routing Machine) es utilizado en lugar de Google Maps.
 * 
 * Uso:
 * GET /api/distancia?origen=lon,lat&destino=lon,lat
 * 
 * Ejemplo:
 * GET /api/distancia?origen=-58.3816,-34.6037&destino=-57.9545,-34.9215
 * 
 * Retorna: { "origen": "...", "destino": "...", "kilometros": 56.8, "duracionTexto": "42 min" }
 */
@RestController
@RequestMapping("/api/distancia")
@RequiredArgsConstructor
public class GeoController {

    private final GeoService geoService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public DistanciaDTO obtenerDistancia(
            @RequestParam("origen") String origen,
            @RequestParam("destino") String destino) throws Exception {
        return geoService.calcularDistancia(origen, destino);
    }
}