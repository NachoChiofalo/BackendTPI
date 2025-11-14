package com.tpi.solicitudes.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class NuevaSolicitudDto {
    // Datos del cliente
    @NotNull
    private Integer tipoDocClienteId;
    @NotNull
    private Long numDocCliente;
    @NotBlank
    private String nombres;
    @NotBlank
    private String apellidos;
    @NotBlank
    private String domicilio;
    @NotBlank
    private String telefono;

    // Datos del contenedor
    @NotNull @Min(0)
    private BigDecimal volumenM3;
    @NotNull @Min(0)
    private BigDecimal pesoKg;

    // Datos de la solicitud
    @NotNull
    private Integer idRuta;
    @NotNull
    private Integer idUbicacionOrigen;
    @NotNull
    private Integer idUbicacionDestino;

    private String textoAdicional;
}
