package com.tpi.precios.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class CalculoEnvioResponse {
    private Integer cantidadTramos;
    private BigDecimal cargoGestion; // precioTramo * cantidadTramos
    private BigDecimal costoKmTotal; // suma de (km * costoBaseKm)
    private BigDecimal costoCombustibleTotal; // suma de (km * consumo/100 * precioLitro)
    private BigDecimal costoEstadiaTotal; // costoEstadiaDia * cantidadDepositos
    private BigDecimal precioFinal; // suma de todos
}
