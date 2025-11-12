package com.tpi.solicitudes.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/**
 * DTO raíz para mapear la respuesta completa de la API de OSRM.
 *
 * @param code Código de respuesta del servicio (debería ser "Ok" para éxito).
 * @param routes Lista de objetos RouteDto generados.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record OsrmRouteResponseDto(
    String code,
    List<RouteDto> routes
) {}