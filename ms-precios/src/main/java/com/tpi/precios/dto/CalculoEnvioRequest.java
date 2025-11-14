package com.tpi.precios.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CalculoEnvioRequest {
    @NotNull
    private List<TramoCostoDto> tramos;

    @NotNull
    @Min(0)
    private Integer cantidadDepositos;

    @NotNull
    private BigDecimal costoEstadiaDia;

    // Opcional: usar tarifa vigente si es null
    private Integer tarifaId;
}
