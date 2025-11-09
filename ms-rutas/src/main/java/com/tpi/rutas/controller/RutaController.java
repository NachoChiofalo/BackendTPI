package com.tpi.rutas.controller;

import com.tpi.rutas.entity.Ruta;
import com.tpi.rutas.service.RutaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/rutas")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class RutaController {

    private final RutaService rutaService;

    @GetMapping
    public ResponseEntity<List<Ruta>> obtenerTodos() {
        log.info("GET /api/rutas - Obteniendo todas las rutas");
        return ResponseEntity.ok(rutaService.obtenerTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Ruta> obtenerPorId(@PathVariable Long id) {
        log.info("GET /api/rutas/{} - Obtener por id", id);
        return rutaService.obtenerPorId(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/codigo")
    public ResponseEntity<List<Ruta>> buscarPorCodigo(@RequestParam String codigo) {
        log.info("GET /api/rutas/codigo - Buscar por codigo: {}", codigo);
        return ResponseEntity.ok(rutaService.buscarPorCodigo(codigo));
    }

    @PostMapping
    public ResponseEntity<Ruta> crear(@Valid @RequestBody Ruta ruta) {
        log.info("POST /api/rutas - Creando ruta: {}", ruta.getCodigo());
        try {
            Ruta guardada = rutaService.guardar(ruta);
            return ResponseEntity.status(HttpStatus.CREATED).body(guardada);
        } catch (Exception e) {
            log.error("Error al crear ruta {}: {}", ruta.getCodigo(), e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Ruta> actualizar(@PathVariable Long id, @Valid @RequestBody Ruta ruta) {
        log.info("PUT /api/rutas/{} - Actualizando", id);
        try {
            Ruta actualizado = rutaService.actualizar(id, ruta);
            return ResponseEntity.ok(actualizado);
        } catch (RuntimeException e) {
            log.error("Error al actualizar ruta {}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        log.info("DELETE /api/rutas/{} - Eliminando", id);
        try {
            rutaService.eliminar(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            log.error("Error al eliminar ruta {}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
}
