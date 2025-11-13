package com.tpi.solicitudes.controller;

import com.tpi.solicitudes.entity.Contenedor;
import com.tpi.solicitudes.service.ContenedorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/contenedores")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ContenedorController {

    private final ContenedorService contenedorService;

    @PreAuthorize("hasRole('cliente')")
    @GetMapping
    public ResponseEntity<List<Contenedor>> obtenerTodos() {
        log.info("GET /api/contenedores - Obteniendo todos los contenedores");
        return ResponseEntity.ok(contenedorService.obtenerTodos());
    }

    @PreAuthorize("hasRole('cliente')")
    @GetMapping("/{id}")
    public ResponseEntity<Contenedor> obtenerPorId(@PathVariable("id") Integer id) {
        log.info("GET /api/contenedores/{} - Obteniendo por id", id);
        return contenedorService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasRole('operador')")
    @PostMapping
    public ResponseEntity<Contenedor> crear(@Valid @RequestBody Contenedor contenedor) {
        log.info("POST /api/contenedores - Creando contenedor: {}", contenedor.getIdContenedor());
        try {
            Contenedor guardado = contenedorService.guardar(contenedor);
            return ResponseEntity.status(HttpStatus.CREATED).body(guardado);
        } catch (Exception e) {
            log.error("Error al crear contenedor: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PreAuthorize("hasRole('operador')")
    @PutMapping("/{id}")
    public ResponseEntity<Contenedor> actualizar(@PathVariable("id") Integer id, @Valid @RequestBody Contenedor contenedor) {
        log.info("PUT /api/contenedores/{} - Actualizando", id);
        try {
            Contenedor actualizado = contenedorService.actualizar(id, contenedor);
            return ResponseEntity.ok(actualizado);
        } catch (RuntimeException e) {
            log.error("Error al actualizar contenedor {}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @PreAuthorize("hasRole('operador')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable("id") Integer id) {
        log.info("DELETE /api/contenedores/{} - Eliminando", id);
        try {
            contenedorService.eliminar(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            log.error("Error al eliminar contenedor {}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
}