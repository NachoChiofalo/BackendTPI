package com.tpi.precios.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TramoDto {

    @NotNull(message = "La ubicación del tramo es obligatoria")
    private Integer ubicacionId;

    // Fecha de entrada al depósito (puede ser null si no aplica)
    private LocalDateTime fechaEntrada;

    // Fecha de salida del depósito (puede ser null si no aplica)
    private LocalDateTime fechaSalida;

    // Tipo opcional: DEPOSITO, ORIGEN, DESTINO
    private String tipo;
}

