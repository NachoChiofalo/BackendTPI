package com.tpi.solicitudes.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeguimientoDto {
    private Integer solicitudId;
    private Integer contenedorId;
    private String estadoActual;
    private String ubicacionActual;
    private List<EstadoHistorial> historial;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EstadoHistorial {
        private String estado;
        private String descripcion;
        private LocalDate fecha;
        private String ubicacion;
    }
}
