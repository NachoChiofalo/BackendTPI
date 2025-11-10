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

    @NotBlank
    @Size(max = 50)
    private String identificacion;

    @NotNull
    private BigDecimal peso;

    @NotNull
    private BigDecimal volumen;

    @Size(max = 200)
    private String descripcion;

    private String estado;

    private String tipoContenedor;

    private Long clienteId;

    private BigDecimal ubicacionActualLatitud;
    private BigDecimal ubicacionActualLongitud;
    private String ubicacionDescripcion;
}