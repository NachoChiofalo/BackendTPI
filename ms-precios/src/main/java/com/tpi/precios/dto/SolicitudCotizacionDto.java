package com.tpi.precios.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitudCotizacionDto {

    @NotNull(message = "La ubicación de origen es obligatoria")
    private Integer ubicacionOrigenId;

    @NotNull(message = "La ubicación de destino es obligatoria")
    private Integer ubicacionDestinoId;

    @NotNull(message = "El peso en kilogramos es obligatorio")
    @DecimalMin(value = "0.1", message = "El peso debe ser mayor a 0")
    private BigDecimal pesoKg;

    @NotNull(message = "El volumen en m³ es obligatorio")
    @DecimalMin(value = "0.1", message = "El volumen debe ser mayor a 0")
    private BigDecimal volumenM3;

    private String tipoServicio;
    private Boolean esUrgente;
    private String observaciones;

    // Información del cliente
    private Integer tipoDocCliente;
    private Long numDocCliente;
}
