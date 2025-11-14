package com.tpi.precios.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CalculoEnvioPromedioRequest {
    @NotNull
    private List<TramoCostoDto> tramosBase; // contiene solo distancia por tramo (costoBaseKm/consumo se tomarán de cada camión)

    @NotNull
    private List<TruckCostoDto> camiones;

    @NotNull
    @Min(0)
    private Integer cantidadDepositos;

    @NotNull
    private BigDecimal costoEstadiaDia;

    private Integer tarifaId; // opcional
}
