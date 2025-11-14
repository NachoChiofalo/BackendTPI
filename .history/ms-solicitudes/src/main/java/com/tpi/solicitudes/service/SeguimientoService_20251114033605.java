package com.tpi.solicitudes.service;

import com.tpi.solicitudes.dto.SeguimientoDto;
import com.tpi.solicitudes.entity.Solicitud;
import com.tpi.solicitudes.repository.SolicitudRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SeguimientoService {

    private final SolicitudRepository solicitudRepository;

    /**
     * REGLA DE NEGOCIO 6: El seguimiento debe mostrar los estados del envío en orden cronológico
     * Genera un historial completo del estado de una solicitud/contenedor
     * mostrando todos los estados por los que ha pasado ordenados por fecha
     * 
    
    public SeguimientoDto obtenerSeguimiento(Integer solicitudId) {
        log.info("Obteniendo seguimiento para solicitud {}", solicitudId);

        Solicitud solicitud = solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada: " + solicitudId));

        List<SeguimientoDto.EstadoHistorial> historial = new ArrayList<>();

        // Estado inicial: Solicitud creada
        historial.add(SeguimientoDto.EstadoHistorial.builder()
                .estado("SOLICITUD_CREADA")
                .descripcion("Solicitud registrada en el sistema")
                .fecha(solicitud.getFechaHoraInicio())
                .ubicacion("Origen")
                .build());

        // Si tiene ruta asignada
        if (solicitud.getIdRuta() != null) {
            historial.add(SeguimientoDto.EstadoHistorial.builder()
                    .estado("RUTA_ASIGNADA")
                    .descripcion("Ruta de traslado planificada")
                    .fecha(solicitud.getFechaHoraInicio())
                    .ubicacion("En planificación")
                    .build());
        }

        // Estado final: Entregado
        if (solicitud.getFechaHoraFin() != null) {
            historial.add(SeguimientoDto.EstadoHistorial.builder()
                    .estado("ENTREGADO")
                    .descripcion("Contenedor entregado en destino final")
                    .fecha(solicitud.getFechaHoraFin())
                    .ubicacion("Destino final")
                    .build());
        }

        // Ordenar historial por fecha
        historial.sort((h1, h2) -> {
            if (h1.getFecha() == null) return 1;
            if (h2.getFecha() == null) return -1;
            return h1.getFecha().compareTo(h2.getFecha());
        });

        String estadoActual = determinarEstadoActual(solicitud, historial);
        String ubicacionActual = historial.isEmpty() ? "Sin información" : 
                historial.get(historial.size() - 1).getUbicacion();

        return SeguimientoDto.builder()
                .solicitudId(solicitud.getSolicitudId())
                .contenedorId(solicitud.getIdContenedor())
                .estadoActual(estadoActual)
                .ubicacionActual(ubicacionActual)
                .historial(historial)
                .build();
    }

    private String determinarEstadoActual(Solicitud solicitud, List<SeguimientoDto.EstadoHistorial> historial) {
        if (solicitud.getFechaHoraFin() != null) {
            return "ENTREGADO";
        }

        if (!historial.isEmpty()) {
            SeguimientoDto.EstadoHistorial ultimoEstado = historial.get(historial.size() - 1);
            return ultimoEstado.getEstado();
        }

        switch (solicitud.getEstadoSolicitud()) {
            case 1: return "BORRADOR";
            case 2: return "PROGRAMADA";
            case 3: return "EN_TRANSITO";
            case 4: return "ENTREGADA";
            default: return "DESCONOCIDO";
        }
    }
}
