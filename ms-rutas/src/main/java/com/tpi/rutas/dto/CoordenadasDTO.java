package com.tpi.rutas.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;

/**
 * DTO para recibir coordenadas de origen y destino
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoordenadasDTO {

    @NotNull(message = "La latitud de origen es obligatoria")
    @DecimalMin(value = "-90.0", message = "La latitud debe estar entre -90 y 90 grados")
    @DecimalMax(value = "90.0", message = "La latitud debe estar entre -90 y 90 grados")
    private Double latitudOrigen;

    @NotNull(message = "La longitud de origen es obligatoria")
    @DecimalMin(value = "-180.0", message = "La longitud debe estar entre -180 y 180 grados")
    @DecimalMax(value = "180.0", message = "La longitud debe estar entre -180 y 180 grados")
    private Double longitudOrigen;

    @NotNull(message = "La latitud de destino es obligatoria")
    @DecimalMin(value = "-90.0", message = "La latitud debe estar entre -90 y 90 grados")
    @DecimalMax(value = "90.0", message = "La latitud debe estar entre -90 y 90 grados")
    private Double latitudDestino;

    @NotNull(message = "La longitud de destino es obligatoria")
    @DecimalMin(value = "-180.0", message = "La longitud debe estar entre -180 y 180 grados")
    @DecimalMax(value = "180.0", message = "La longitud debe estar entre -180 y 180 grados")
    private Double longitudDestino;
}
