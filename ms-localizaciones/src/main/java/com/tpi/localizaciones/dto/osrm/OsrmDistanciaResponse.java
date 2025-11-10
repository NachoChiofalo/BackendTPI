package com.tpi.localizaciones.dto.osrm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO simplificado para la respuesta de cálculo de distancia
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OsrmDistanciaResponse {

    /**
     * Distancia en kilómetros
     */
    private BigDecimal distanciaKm;

    /**
     * Duración en minutos
     */
    private BigDecimal duracionMinutos;

    /**
     * Distancia original en metros (de OSRM)
     */
    private Double distanciaMetros;

    /**
     * Duración original en segundos (de OSRM)
     */
    private Double duracionSegundos;

    /**
     * Código de respuesta de OSRM
     */
    private String codigo;

    /**
     * Indica si el cálculo fue exitoso
     */
    private Boolean exitoso;

    /**
     * Mensaje de error si lo hubiera
     */
    private String mensajeError;

    /**
     * JSON completo de la ruta (opcional)
     */
    private String rutaJson;
}

