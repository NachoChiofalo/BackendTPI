package com.tpi.rutas.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RutaTentativaDTO {
    private Integer rutaId;
    private Integer cantidadTramos;
    private Integer cantidadDepositos;
    private List<TramoTentativoDTO> tramos;
    private BigDecimal tiempoEstimadoHoras;
    private BigDecimal costoEstimadoTotal;
}

