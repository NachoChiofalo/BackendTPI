package com.tpi.rutas.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TramoTentativoDTO {
    private Integer tramoId;
    private Integer rutaId;
    private Integer orden;
    private String dominio;
    private Integer ubicacionOrigenId;
    private Integer ubicacionDestinoId;
    private Integer transportistaId;
    private BigDecimal costoAproximado;
    private BigDecimal costoReal;
    private LocalDate fechaHoraInicio;
    private LocalDate fechaHoraEstimadaFin;
    private LocalDate fechaHoraFin;
}

