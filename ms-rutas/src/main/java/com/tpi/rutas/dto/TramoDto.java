package com.tpi.rutas.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TramoDto {

    private Long id;

    @NotNull
    private Integer orden;

    @NotNull
    private Long rutaId;

    @NotNull
    private BigDecimal origenLatitud;

    @NotNull
    private BigDecimal origenLongitud;

    @Size(max = 500)
    private String origenDescripcion;

    private BigDecimal destinoLatitud;
    private BigDecimal destinoLongitud;

    @Size(max = 500)
    private String destinoDescripcion;
}
