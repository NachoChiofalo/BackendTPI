package com.tpi.rutas.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO para representar la información de una solicitud obtenida del microservicio de solicitudes
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitudDTO {
    private Integer solicitudId;
    private Integer tipoDocCliente;
    private Long numDocCliente;
    private Integer estadoSolicitud;
    private Integer idContenedor;
    private Integer idRuta;
    private Integer idUbicacionOrigen;
    private Integer idUbicacionDestino;
    private BigDecimal costoEstimado;
    private BigDecimal costoReal;
    private LocalDateTime fechaHoraFin;
    private LocalDateTime fechaHoraEstimadaFin;
    private LocalDateTime fechaHoraInicio;
    private String textoAdicional;
}
