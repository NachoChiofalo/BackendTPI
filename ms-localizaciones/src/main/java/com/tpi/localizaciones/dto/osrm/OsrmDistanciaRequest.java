package com.tpi.localizaciones.dto.osrm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO para solicitar cálculo de distancia usando OSRM
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OsrmDistanciaRequest {

    private BigDecimal latitudOrigen;
    private BigDecimal longitudOrigen;

    private BigDecimal latitudDestino;
    private BigDecimal longitudDestino;

    /**
     * Si se debe incluir la geometría de la ruta
     */
    @Builder.Default
    private Boolean incluirGeometria = false;

    /**
     * Si se deben incluir los pasos de navegación
     */
    @Builder.Default
    private Boolean incluirPasos = false;

    /**
     * Si se debe incluir overview de la ruta
     */
    @Builder.Default
    private Boolean incluirOverview = false;
}

