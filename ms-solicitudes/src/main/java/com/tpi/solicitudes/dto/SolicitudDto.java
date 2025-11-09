package com.tpi.solicitudes.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitudDto {

    private Long id;

    @NotBlank
    @Size(max = 50)
    private String numero;

    @NotNull
    private String contenedorId; // string id from Contenedor.identificacion

    @NotNull
    private Long clienteId;

    @NotNull
    private BigDecimal origenLatitud;

    @NotNull
    private BigDecimal origenLongitud;

    @NotBlank
    @Size(max = 500)
    private String origenDireccion;

    @NotNull
    private BigDecimal destinoLatitud;

    @NotNull
    private BigDecimal destinoLongitud;

    @NotBlank
    @Size(max = 500)
    private String destinoDireccion;

    private String estado;

    private String observaciones;

    private LocalDateTime fechaRetiroProgramada;
    private LocalDateTime fechaEntregaProgramada;

    private BigDecimal costoEstimado;
    private Integer tiempoEstimado;
    private BigDecimal costoFinal;
    private Integer tiempoReal;
    private String prioridad;
}