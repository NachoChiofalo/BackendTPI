package com.tpi.rutas.controller;

import com.tpi.rutas.entity.Tramo;
import com.tpi.rutas.service.TramoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

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
    public ResponseEntity<Tramo> obtenerPorId(@PathVariable Integer id) {
        log.info("GET /api/tramos/{} - Obtener por id", id);
        return tramoService.obtenerPorId(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/ruta/{rutaId}")
    public ResponseEntity<List<Tramo>> obtenerPorRuta(@PathVariable Integer rutaId) {
        log.info("GET /api/tramos/ruta/{} - Obtener por ruta", rutaId);
        return ResponseEntity.ok(tramoService.obtenerPorRuta(rutaId));
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
    public ResponseEntity<Tramo> actualizar(@PathVariable Integer id, @Valid @RequestBody Tramo tramo) {
        log.info("PUT /api/tramos/{} - Actualizando", id);
        try {
            Tramo actualizado = tramoService.actualizar(id, tramo);
            return ResponseEntity.ok(actualizado);
        } catch (RuntimeException e) {
            log.error("Error al actualizar tramo {}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
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
