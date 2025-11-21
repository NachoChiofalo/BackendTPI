package com.tpi.solicitudes.controller;

import com.tpi.solicitudes.entity.Solicitud;
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

    @PreAuthorize("hasRole('cliente')")
    @GetMapping("/cliente")
    public ResponseEntity<List<Solicitud>> obtenerPorCliente(
            @RequestParam Integer tipoDoc,
            @RequestParam Long numDoc) {
        log.info("GET /api/solicitudes/cliente - Obteniendo solicitudes del cliente: {} - {}", tipoDoc, numDoc);
        List<Solicitud> solicitudes = solicitudService.obtenerPorCliente(tipoDoc, numDoc);
        return ResponseEntity.ok(solicitudes);
    }

    @PreAuthorize("hasRole('operador')")
    @GetMapping("/pendientes")
    public ResponseEntity<List<Solicitud>> obtenerPendientes() {
        log.info("GET /api/solicitudes/pendientes - Obteniendo solicitudes pendientes");
        List<Solicitud> solicitudes = solicitudService.obtenerPendientes();
        return ResponseEntity.ok(solicitudes);
    }

    @PreAuthorize("hasRole('operador')")
    @PutMapping("/{solicitudId}/asignar-ruta")
    public ResponseEntity<Solicitud> asignarRuta(
            @PathVariable("solicitudId") Integer solicitudId,
            @RequestBody java.util.Map<String, Integer> request) {
        log.info("PUT /api/solicitudes/{}/asignar-ruta - Asignando ruta", solicitudId);
        try {
            Integer rutaId = request.get("rutaId");
            if (rutaId == null) {
                return ResponseEntity.badRequest().build();
            }
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