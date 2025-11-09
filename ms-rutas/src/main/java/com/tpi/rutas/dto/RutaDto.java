package com.tpi.rutas.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RutaDto {
    private Long id;

    @NotBlank
    @Size(max = 150)
    private String nombre;

    @Size(max = 1000)
    private String descripcion;

    private String origenDescripcion;
    private String destinoDescripcion;
}
