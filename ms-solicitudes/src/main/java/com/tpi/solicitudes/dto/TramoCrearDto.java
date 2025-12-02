package com.tpi.solicitudes.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO para crear un tramo en el microservicio ms-rutas
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TramoCrearDto {
    private Integer tramoId;
    private Integer rutaId;
    private Integer tipoTramoId;
    private String dominio; // "SIN_ASG" para tramos sin camión asignado
    private Integer ubicacionOrigenId;
    private Integer transportistaId;
    private Integer ubicacionDestinoId;
    private BigDecimal costoAproximado;
    private BigDecimal costoReal;
    private String fechaHoraInicio; // Formato ISO: yyyy-MM-dd
    private String fechaHoraFin;
    private String fechaHoraEstimadaFin;
}

