package com.tpi.rutas.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO para la respuesta de tramos
 * Excluye costoReal y muestra fechas como LocalDateTime
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TramoResponseDTO {

    private Integer tramoId;
    private Integer rutaId;
    private Integer tipoTramoId;
    private String dominio;
    private Integer ubicacionOrigenId;
    private Integer transportistaId;
    private Integer ubicacionDestinoId;

    // Cambiado de costoAproximado a distancia
    private BigDecimal distancia;

    // Fechas como LocalDateTime con formato específico
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fechaHoraInicio;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fechaHoraFin;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fechaHoraEstimadaFin;
}
