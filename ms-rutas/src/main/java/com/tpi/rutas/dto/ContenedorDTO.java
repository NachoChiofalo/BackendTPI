package com.tpi.rutas.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO para representar la información de un contenedor obtenida del microservicio de solicitudes
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContenedorDTO {
    private Integer idContenedor;
    private BigDecimal volumenM3;
    private BigDecimal pesoKg;
    private Integer idEstadoContenedor;
}
