package com.tpi.solicitudes.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContenedorDetalladoDto {

    // Información del contenedor
    private Integer idContenedor;
    private Integer idEstadoContenedor;
    private String nombreEstado;
    private BigDecimal volumenM3;
    private BigDecimal pesoKg;

    // Información del cliente
    private Integer tipoDocCliente;
    private Long numDocCliente;

    // Información de la solicitud asociada
    private Integer solicitudId;
    private Integer estadoSolicitud;
    private String nombreEstadoSolicitud;

    // Información de los tramos
    private List<TramoDto> tramos;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TramoDto {
        private Integer tramoId;
        private BigDecimal kilometros;
        private String fechaHoraInicio;
        private String fechaHoraFin;
    }
}
