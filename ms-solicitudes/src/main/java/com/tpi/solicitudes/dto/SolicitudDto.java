package com.tpi.solicitudes.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;

@Data
@Schema(description = "DTO para la creación y actualización de solicitudes de transporte")
public class SolicitudDto {

    @Schema(description = "Identificador único de la solicitud", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @NotBlank
    @Schema(description = "Descripción detallada de la solicitud", example = "Transporte de contenedores desde Puerto Buenos Aires", required = true)
    private String descripcion;

    @NotNull
    @Schema(description = "Ubicación de origen del transporte", example = "Puerto Buenos Aires", required = true)
    private String origen;

    @NotNull
    @Schema(description = "Ubicación de destino del transporte", example = "Depósito Córdoba", required = true)
    private String destino;

    @Positive
    @Schema(description = "Peso de la carga en kilogramos", example = "15000", required = true)
    private Double peso;

    @Schema(description = "Tipo de contenedor requerido", example = "20' DRY", allowableValues = {"20' DRY", "40' DRY", "40' HC", "20' REEFER", "40' REEFER"})
    private String tipoContenedor;

    @Schema(description = "Fecha y hora de creación de la solicitud", example = "2025-01-15T10:30:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime fechaCreacion;

    @Schema(description = "Estado actual de la solicitud", example = "PENDIENTE", accessMode = Schema.AccessMode.READ_ONLY,
            allowableValues = {"PENDIENTE", "EN_PROCESO", "ASIGNADA", "EN_TRANSITO", "COMPLETADA", "CANCELADA"})
    private String estado;

    @Schema(description = "Observaciones adicionales sobre la solicitud", example = "Carga frágil - manejar con cuidado")
    private String observaciones;
}
