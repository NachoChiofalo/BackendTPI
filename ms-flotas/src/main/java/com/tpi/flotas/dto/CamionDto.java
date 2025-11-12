package com.tpi.flotas.dto;

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
public class CamionDto {

    @NotBlank(message = "El dominio es obligatorio")
    @Size(max = 10, message = "El dominio no puede exceder 10 caracteres")
    private String dominio;

    @NotNull(message = "El estado de disponibilidad es obligatorio")
    private Boolean disponible;

    @NotNull(message = "La capacidad de peso es obligatoria")
    @DecimalMin(value = "0.1", message = "La capacidad de peso debe ser mayor a 0")
    private BigDecimal capacidadPeso;

    @NotNull(message = "La capacidad de volumen es obligatoria")
    @DecimalMin(value = "0.1", message = "La capacidad de volumen debe ser mayor a 0")
    private BigDecimal capacidadVolumen;

    @NotNull(message = "El costo base por km es obligatorio")
    @DecimalMin(value = "0.1", message = "El costo base por km debe ser mayor a 0")
    private BigDecimal costoBaseKm;

    @NotNull(message = "El consumo promedio es obligatorio")
    @DecimalMin(value = "0.1", message = "El consumo promedio debe ser mayor a 0")
    private BigDecimal consumoPromedio;

    private BigDecimal ubicacionActualLat;

    private BigDecimal ubicacionActualLng;

    private String ubicacionDescripcion;

    private Boolean activo;

    // DTO anidado para información del transportista
    private TransportistaInfoDto transportista;
}
