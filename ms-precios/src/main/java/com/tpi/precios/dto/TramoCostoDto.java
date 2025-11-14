package com.tpi.precios.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TramoCostoDto {
    @NotNull
    @DecimalMin("0.0")
    private BigDecimal distanciaKm;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal costoBaseKm;

    // litros cada 100km
    @NotNull
    @DecimalMin("0.0")
    private BigDecimal consumoPromedio;
}
