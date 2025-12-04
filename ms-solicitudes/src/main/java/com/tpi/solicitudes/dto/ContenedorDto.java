package com.tpi.solicitudes.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContenedorDto {

    private Integer idContenedor;

    private Integer idEstadoContenedor;

    private String nombreEstado;

    @NotNull
    private BigDecimal volumenM3;

    @NotNull
    private BigDecimal pesoKg;

    // Información del cliente asociado a través de la solicitud
    private Integer tipoDocCliente;
    private Long numDocCliente;
}