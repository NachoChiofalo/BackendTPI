package com.tpi.solicitudes.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * DTO que mapea una ruta individual de la respuesta OSRM.
 * Solo contiene los campos de interés: distancia y duración.
 *
 * @param distance Distancia de la ruta en metros.
 * @param duration Duración estimada del viaje en segundos.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record RouteDto(
    // La distancia está en metros
    double distance, 
    
    // La duración está en segundos
    double duration 
) {}