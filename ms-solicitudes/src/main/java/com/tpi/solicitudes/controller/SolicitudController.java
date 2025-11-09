package com.tpi.solicitudes.controller;

import com.tpi.solicitudes.dto.SolicitudDto;
import com.tpi.solicitudes.entity.Contenedor;
import com.tpi.solicitudes.entity.Cliente;
import com.tpi.solicitudes.entity.Solicitud;
import com.tpi.solicitudes.service.ContenedorService;
import com.tpi.solicitudes.service.ClienteService;
import com.tpi.solicitudes.service.SolicitudService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/solicitudes")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class SolicitudController {

    private final SolicitudService solicitudService;
    private final ClienteService clienteService;
    private final ContenedorService contenedorService;

    @GetMapping
    public ResponseEntity<List<Solicitud>> obtenerTodos() {
        log.info("GET /api/solicitudes - Obteniendo todas las solicitudes");
        return ResponseEntity.ok(solicitudService.obtenerTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Solicitud> obtenerPorId(@PathVariable Long id) {
        log.info("GET /api/solicitudes/{} - Obteniendo por id", id);
        return solicitudService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<Solicitud>> buscarPorNumero(@RequestParam String numero) {
        log.info("GET /api/solicitudes/buscar - Buscando por numero: {}", numero);
        return ResponseEntity.ok(solicitudService.buscarPorNumero(numero));
    }

    @PostMapping
    public ResponseEntity<Solicitud> crear(@Valid @RequestBody SolicitudDto dto) {
        log.info("POST /api/solicitudes - Creando solicitud: {}", dto.getNumero());
        try {
            // mapear DTO a entidad
            Solicitud s = mapearDtoAEntidad(dto);
            Solicitud guardada = solicitudService.guardar(s);
            return ResponseEntity.status(HttpStatus.CREATED).body(guardada);
        } catch (Exception e) {
            log.error("Error al crear solicitud: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Solicitud> actualizar(@PathVariable Long id, @Valid @RequestBody SolicitudDto dto) {
        log.info("PUT /api/solicitudes/{} - Actualizando", id);
        try {
            Solicitud s = mapearDtoAEntidad(dto);
            Solicitud actualizado = solicitudService.actualizar(id, s);
            return ResponseEntity.ok(actualizado);
        } catch (RuntimeException e) {
            log.error("Error al actualizar solicitud {}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        log.info("DELETE /api/solicitudes/{} - Eliminando", id);
        try {
            solicitudService.eliminar(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            log.error("Error al eliminar solicitud {}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    private Solicitud mapearDtoAEntidad(SolicitudDto dto) {
        Solicitud s = Solicitud.builder()
                .id(dto.getId())
                .numero(dto.getNumero())
                .origenLatitud(dto.getOrigenLatitud())
                .origenLongitud(dto.getOrigenLongitud())
                .origenDireccion(dto.getOrigenDireccion())
                .destinoLatitud(dto.getDestinoLatitud())
                .destinoLongitud(dto.getDestinoLongitud())
                .destinoDireccion(dto.getDestinoDireccion())
                .observaciones(dto.getObservaciones())
                .fechaRetiroProgramada(dto.getFechaRetiroProgramada())
                .fechaEntregaProgramada(dto.getFechaEntregaProgramada())
                .costoEstimado(dto.getCostoEstimado())
                .tiempoEstimado(dto.getTiempoEstimado())
                .costoFinal(dto.getCostoFinal())
                .tiempoReal(dto.getTiempoReal())
                .prioridad(dto.getPrioridad() != null ? Solicitud.PrioridadSolicitud.valueOf(dto.getPrioridad()) : Solicitud.PrioridadSolicitud.NORMAL)
                .build();

        // Asociar contenedor
        if (dto.getContenedorId() != null) {
            Contenedor contenedor = contenedorService.obtenerPorId(dto.getContenedorId()).orElse(null);
            s.setContenedor(contenedor);
        }

        // Asociar cliente
        if (dto.getClienteId() != null) {
            Cliente cliente = clienteService.obtenerPorId(dto.getClienteId()).orElse(null);
            s.setCliente(cliente);
        }

        if (dto.getEstado() != null) {
            try {
                s.setEstado(Solicitud.EstadoSolicitud.valueOf(dto.getEstado()));
            } catch (IllegalArgumentException ex) {
                // ignore invalid state, will be validated elsewhere
            }
        }

        return s;
    }
}