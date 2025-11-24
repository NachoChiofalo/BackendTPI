package com.tpi.solicitudes.controller;

import com.tpi.solicitudes.dto.SeguimientoDto;
import com.tpi.solicitudes.entity.Solicitud;
import com.tpi.solicitudes.service.SeguimientoService;
import com.tpi.solicitudes.service.SolicitudService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    private final SeguimientoService seguimientoService;

    @GetMapping
    public ResponseEntity<List<Solicitud>> obtenerTodos() {
        log.info("GET /api/solicitudes - Obteniendo todas las solicitudes");
        List<Solicitud> solicitudes = solicitudService.obtenerTodos();
        return ResponseEntity.ok(solicitudes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Solicitud> obtenerPorId(@PathVariable("id") Integer id) {
        log.info("GET /api/solicitudes/{} - Obteniendo solicitud por ID", id);
        return solicitudService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * REQUERIMIENTO FUNCIONAL 1: Registrar una nueva solicitud de transporte de contenedor (Cliente)
     * - La solicitud incluye la creación del contenedor con su identificación única
     * - La solicitud incluye el registro del cliente si no existe previamente
     * - Las solicitudes deben registrar un estado
     */
    @PreAuthorize("hasRole('cliente')")
    @PostMapping
    public ResponseEntity<Solicitud> crear(@Valid @RequestBody Solicitud solicitud) {
        log.info("POST /api/solicitudes - Creando nueva solicitud");
        try {
            Solicitud solicitudGuardada = solicitudService.crear(solicitud);
            return ResponseEntity.status(HttpStatus.CREATED).body(solicitudGuardada);
        } catch (Exception e) {
            log.error("Error al crear solicitud: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * REQUERIMIENTO FUNCIONAL 2: Consultar el estado del transporte de un contenedor (Cliente)
     * - Permite al cliente ver todas sus solicitudes y su estado actual
     */
    @PreAuthorize("hasRole('cliente')")
    @GetMapping("/cliente")
    public ResponseEntity<List<Solicitud>> obtenerPorCliente(
            @RequestParam("tipoDoc") Integer tipoDoc,
            @RequestParam("numDoc") Long numDoc) {
        log.info("GET /api/solicitudes/cliente - Obteniendo solicitudes del cliente: {} - {}", tipoDoc, numDoc);
        List<Solicitud> solicitudes = solicitudService.obtenerPorCliente(tipoDoc, numDoc);
        return ResponseEntity.ok(solicitudes);
    }

    /**
     * Endpoint adicional: Consultar el estado actual de un contenedor para un cliente
     * URL: GET /api/solicitudes/cliente/contenedor/{idContenedor}?tipoDoc=..&numDoc=..
     */
    @PreAuthorize("hasRole('cliente')")
    @GetMapping("/cliente/contenedor/{idContenedor}")
    public ResponseEntity<Solicitud> obtenerEstadoContenedorPorCliente(
            @PathVariable("idContenedor") Integer idContenedor,
            @RequestParam("tipoDoc") Integer tipoDoc,
            @RequestParam("numDoc") Long numDoc) {
        log.info("GET /api/solicitudes/cliente/contenedor/{} - Cliente {}-{} consulta estado del contenedor", idContenedor, tipoDoc, numDoc);
        return solicitudService.obtenerEstadoContenedorParaCliente(idContenedor, tipoDoc, numDoc)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Consultar todos los contenedores relacionados a un cliente y su estado más reciente
     * URL: GET /api/solicitudes/cliente/contenedores?tipoDoc=..&numDoc=..
     */
    @PreAuthorize("hasRole('cliente')")
    @GetMapping("/cliente/contenedores")
    public ResponseEntity<List<Solicitud>> obtenerContenedoresPorCliente(
            @RequestParam("tipoDoc") Integer tipoDoc,
            @RequestParam("numDoc") Long numDoc) {
        log.info("GET /api/solicitudes/cliente/contenedores - Cliente {}-{} solicita listado de contenedores", tipoDoc, numDoc);
        List<Solicitud> contenedores = solicitudService.obtenerContenedoresPorClienteConEstado(tipoDoc, numDoc);
        return ResponseEntity.ok(contenedores);
    }

    /**
     * REGLA DE NEGOCIO 6: El seguimiento debe mostrar los estados del envío en orden cronológico
     * Retorna el historial completo de estados de una solicitud
     */
    @PreAuthorize("hasRole('cliente')")
    @GetMapping("/{id}/seguimiento")
    public ResponseEntity<SeguimientoDto> obtenerSeguimiento(@PathVariable("id") Integer id) {
        log.info("GET /api/solicitudes/{}/seguimiento - Obteniendo seguimiento", id);
        try {
            SeguimientoDto seguimiento = seguimientoService.obtenerSeguimiento(id);
            return ResponseEntity.ok(seguimiento);
        } catch (RuntimeException e) {
            log.error("Error al obtener seguimiento de solicitud {}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * REQUERIMIENTO FUNCIONAL 5: Consultar todos los contenedores pendientes de entrega y su ubicación/estado con filtros (Operador)
     * - Retorna solicitudes que no están en estado "entregada"
     */
    @PreAuthorize("hasRole('operador')")
    @GetMapping("/pendientes")
    public ResponseEntity<List<Solicitud>> obtenerPendientes() {
        log.info("GET /api/solicitudes/pendientes - Obteniendo solicitudes pendientes");
        List<Solicitud> solicitudes = solicitudService.obtenerPendientes();
        return ResponseEntity.ok(solicitudes);
    }

    /**
     * REQUERIMIENTO FUNCIONAL 4: Asignar una ruta con todos sus tramos a la solicitud (Operador)
     * - Asocia una ruta previamente calculada a una solicitud específica
     */
    @PreAuthorize("hasRole('operador')")
    @PutMapping("/{solicitudId}/ruta/{rutaId}")
    public ResponseEntity<Solicitud> asignarRuta(
            @PathVariable("solicitudId") Integer solicitudId,
            @PathVariable("rutaId") Integer rutaId) {
        log.info("PUT /api/solicitudes/{}/ruta/{} - Asignando ruta", solicitudId, rutaId);
        try {
            Solicitud actualizada = solicitudService.asignarRuta(solicitudId, rutaId);
            return ResponseEntity.ok(actualizada);
        } catch (RuntimeException e) {
            log.error("Error al asignar ruta a solicitud {}: {}", solicitudId, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Solicitudes service is running");
    }
}