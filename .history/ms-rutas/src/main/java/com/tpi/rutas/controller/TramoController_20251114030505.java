package com.tpi.rutas.controller;

import com.tpi.rutas.entity.Tramo;
import com.tpi.rutas.service.TramoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tramos")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class TramoController {

    private final TramoService tramoService;

    @GetMapping
    public ResponseEntity<List<Tramo>> obtenerTodos() {
        log.info("GET /api/tramos - Obteniendo todos los tramos");
        return ResponseEntity.ok(tramoService.obtenerTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Tramo> obtenerPorId(@PathVariable("id") Integer id) {
        log.info("GET /api/tramos/{} - Obtener por id", id);
        return tramoService.obtenerPorId(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/ruta/{rutaId}")
    public ResponseEntity<List<Tramo>> obtenerPorRuta(@PathVariable("rutaId") Integer rutaId) {
        log.info("GET /api/tramos/ruta/{} - Obtener por ruta", rutaId);
        return ResponseEntity.ok(tramoService.obtenerPorRuta(rutaId));
    }

    /**
     * REQUERIMIENTO FUNCIONAL 7 (parte 1): Consultar tramos asignados (Transportista)
     * - Permite al transportista ver todos los tramos que tiene asignados
     */
    @PreAuthorize("hasRole('transportista')")
    @GetMapping("/transportista/{transportistaId}")
    public ResponseEntity<List<Tramo>> obtenerPorTransportista(@PathVariable("transportistaId") Integer transportistaId) {
        log.info("GET /api/tramos/transportista/{} - Obtener tramos del transportista", transportistaId);
        return ResponseEntity.ok(tramoService.obtenerPorTransportista(transportistaId));
    }

    @PostMapping
    public ResponseEntity<Tramo> crear(@Valid @RequestBody Tramo tramo) {
        log.info("POST /api/tramos - Creando tramo ID: {}", tramo.getTramoId());
        try {
            Tramo guardado = tramoService.guardar(tramo);
            return ResponseEntity.status(HttpStatus.CREATED).body(guardado);
        } catch (Exception e) {
            log.error("Error al crear tramo: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Tramo> actualizar(@PathVariable("id") Integer id, @Valid @RequestBody Tramo tramo) {
        log.info("PUT /api/tramos/{} - Actualizando", id);
        try {
            Tramo actualizado = tramoService.actualizar(id, tramo);
            return ResponseEntity.ok(actualizado);
        } catch (RuntimeException e) {
            log.error("Error al actualizar tramo {}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * REQUERIMIENTO FUNCIONAL 6: Asignar camión a un tramo de traslado de un contenedor (Operador)
     * - Asigna un camión específico (por dominio) a un tramo de la ruta
     * - Valida disponibilidad y capacidad del camión
     */
    @PreAuthorize("hasRole('operador')")
    @PutMapping("/{tramoId}/asignar-camion")
    public ResponseEntity<Tramo> asignarCamion(
            @PathVariable("tramoId") Integer tramoId,
            @RequestBody Map<String, String> request) {
        log.info("PUT /api/tramos/{}/asignar-camion - Asignando camión", tramoId);
        try {
            String dominio = request.get("dominio");
            if (dominio == null || dominio.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            Tramo actualizado = tramoService.asignarCamion(tramoId, dominio);
            return ResponseEntity.ok(actualizado);
        } catch (RuntimeException e) {
            log.error("Error al asignar camión al tramo {}: {}", tramoId, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * REQUERIMIENTO FUNCIONAL 7 (parte 2): Determinar el inicio de un tramo de traslado (Transportista)
     * - Registra la fecha/hora de inicio del tramo
     * - Marca el comienzo del transporte del contenedor en este tramo
     */
    @PreAuthorize("hasRole('transportista')")
    @PostMapping("/{tramoId}/iniciar")
    public ResponseEntity<Tramo> iniciarTramo(@PathVariable("tramoId") Integer tramoId) {
        log.info("POST /api/tramos/{}/iniciar - Iniciando tramo", tramoId);
        try {
            Tramo actualizado = tramoService.iniciarTramo(tramoId);
            return ResponseEntity.ok(actualizado);
        } catch (RuntimeException e) {
            log.error("Error al iniciar tramo {}: {}", tramoId, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * REQUERIMIENTO FUNCIONAL 7 (parte 3): Determinar el fin de un tramo de traslado (Transportista)
     * - Registra la fecha/hora de finalización del tramo
     * - Permite calcular tiempo real y costos de estadía en depósitos
     */
    @PreAuthorize("hasRole('transportista')")
    @PostMapping("/{tramoId}/finalizar")
    public ResponseEntity<Tramo> finalizarTramo(@PathVariable("tramoId") Integer tramoId) {
        log.info("POST /api/tramos/{}/finalizar - Finalizando tramo", tramoId);
        try {
            Tramo actualizado = tramoService.finalizarTramo(tramoId);
            return ResponseEntity.ok(actualizado);
        } catch (RuntimeException e) {
            log.error("Error al finalizar tramo {}: {}", tramoId, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable("id") Integer id) {
        log.info("DELETE /api/tramos/{} - Eliminando", id);
        try {
            tramoService.eliminar(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            log.error("Error al eliminar tramo {}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
}