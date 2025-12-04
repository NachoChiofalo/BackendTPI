package com.tpi.rutas.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO para representar la información de un camión obtenida del microservicio de flotas
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CamionDTO {
    private String dominio;
    private BigDecimal capacidadPeso;
    private BigDecimal capacidadVolumen;
    private Boolean disponible;
    private BigDecimal costoBaseKm;
    private BigDecimal consumoPromedio;
}
