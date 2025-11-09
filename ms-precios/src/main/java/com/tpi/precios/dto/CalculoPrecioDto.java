package com.tpi.precios.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CalculoPrecioDto {

    private Long calculoId;
    private Integer tarifaId;

    @NotNull(message = "La distancia en kilómetros es obligatoria")
    @DecimalMin(value = "0.1", message = "La distancia debe ser mayor a 0")
    private BigDecimal distanciaKm;

    @NotNull(message = "El peso en kilogramos es obligatorio")
    @DecimalMin(value = "0.1", message = "El peso debe ser mayor a 0")
    private BigDecimal pesoKg;

    @NotNull(message = "El volumen en m³ es obligatorio")
    @DecimalMin(value = "0.1", message = "El volumen debe ser mayor a 0")
    private BigDecimal volumenM3;

    private BigDecimal precioBase;
    private BigDecimal precioTotal;
    private BigDecimal precioFinal;
    private LocalDateTime fechaCalculo;
    private String tipoCalculo;
    private String estadoCalculo;
    private String observaciones;

    // Campos adicionales para la cotización
    private Integer ubicacionOrigenId;
    private Integer ubicacionDestinoId;
    private String tipoServicio;
    private Boolean esUrgente;
    private BigDecimal factorUrgencia;
}
