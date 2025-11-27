package com.tpi.solicitudes.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
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
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime fecha;
        private String ubicacion;
    }
}
