package com.tpi.localizaciones.dto;

import com.tpi.localizaciones.entity.EstadoValidacion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DistanciaCalculadaDto {

    private Long id;

    @NotNull(message = "El ID de origen es obligatorio")
    private Long ubicacionOrigenId;

    @NotNull(message = "El ID de destino es obligatorio")
    private Long ubicacionDestinoId;

    @NotNull(message = "La distancia en kilómetros es obligatoria")
    @DecimalMin(value = "0.0", message = "La distancia debe ser mayor o igual a 0")
    private BigDecimal distanciaKm;

    @NotNull(message = "El tiempo estimado es obligatorio")
    @Min(value = 1, message = "El tiempo estimado debe ser mayor a 0")
    private Integer tiempoEstimadoMinutos;

    private EstadoValidacion estadoValidacion;

    private String observaciones;

    private LocalDateTime fechaCalculo;

    // Campos adicionales para mostrar información de las ubicaciones
    private String origenNombre;
    private String destinoNombre;
    private String origenDireccion;
    private String destinoDireccion;
}