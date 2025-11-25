package com.tpi.precios.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TarifaDto {

    private Integer tarifaId;

    @NotNull(message = "El precio del combustible por litro es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio del combustible debe ser mayor a 0")
    private BigDecimal precioCombustibleLitro;

    @NotNull(message = "El precio por km/kg es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio por km/kg debe ser mayor a 0")
    private BigDecimal precioKmKg;

    @NotNull(message = "El precio por km/m³ es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio por km/m³ debe ser mayor a 0")
    private BigDecimal precioKmM3;

    @NotNull(message = "La fecha de vigencia inicio es obligatoria")
    private LocalDate fechaVigenciaInicio;

    @NotNull(message = "La fecha de vigencia fin es obligatoria")
    private LocalDate fechaVigenciaFin;

    @NotNull(message = "El precio del tramo es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio del tramo debe ser mayor a 0")
    private BigDecimal precioTramo;

    @NotNull(message = "El precio por día de estadía es obligatorio")
    @DecimalMin(value = "0.00", message = "El precio por día de estadía no puede ser negativo")
    private BigDecimal precioEstadiaDia;

    private String tipoTarifa;
    private String modalidadCalculo;
    private Boolean vigente;

    @AssertTrue(message = "La fecha de fin debe ser posterior a la fecha de inicio")
    public boolean isFechasValidas() {
        if (fechaVigenciaInicio != null && fechaVigenciaFin != null) {
            return fechaVigenciaFin.isAfter(fechaVigenciaInicio);
        }
        return true;
    }
}
