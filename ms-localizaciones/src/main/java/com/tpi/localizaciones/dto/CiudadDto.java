package com.tpi.localizaciones.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CiudadDto {

    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String nombre;

    @Size(max = 100, message = "La provincia no puede exceder 100 caracteres")
    private String provincia;

    @Size(max = 100, message = "El país no puede exceder 100 caracteres")
    private String pais;

    @Size(max = 10, message = "El código postal no puede exceder 10 caracteres")
    private String codigoPostal;

    @DecimalMin(value = "-90.0000000", message = "La latitud debe estar entre -90 y 90")
    @DecimalMax(value = "90.0000000", message = "La latitud debe estar entre -90 y 90")
    private BigDecimal latitud;

    @DecimalMin(value = "-180.0000000", message = "La longitud debe estar entre -180 y 180")
    @DecimalMax(value = "180.0000000", message = "La longitud debe estar entre -180 y 180")
    private BigDecimal longitud;

    @Size(max = 50, message = "La zona horaria no puede exceder 50 caracteres")
    private String zonaHoraria;

    private Boolean activa;
}