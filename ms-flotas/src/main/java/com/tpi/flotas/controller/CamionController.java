package com.tpi.flotas.controller;

import com.tpi.flotas.entity.Camion;
import com.tpi.flotas.service.CamionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/camiones")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class CamionController {

    private final CamionService camionService;

    @GetMapping
    public ResponseEntity<List<Camion>> obtenerTodos() {
        log.info("GET /api/camiones - Obteniendo todos los camiones");
        List<Camion> camiones = camionService.obtenerTodos();
        return ResponseEntity.ok(camiones);
    }

    @GetMapping("/disponibles")
    public ResponseEntity<List<Camion>> obtenerDisponibles() {
        log.info("GET /api/camiones/disponibles - Obteniendo camiones disponibles");
        List<Camion> camiones = camionService.obtenerDisponibles();
        return ResponseEntity.ok(camiones);
    }

    @GetMapping("/{dominio}")
    public ResponseEntity<Camion> obtenerPorDominio(@PathVariable String dominio) {
        log.info("GET /api/camiones/{} - Obteniendo camión por dominio", dominio);
        return camionService.obtenerPorDominio(dominio)
                .map(camion -> ResponseEntity.ok(camion))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/transportista/{transportistaId}")
    public ResponseEntity<List<Camion>> obtenerPorTransportista(@PathVariable Long transportistaId) {
        log.info("GET /api/camiones/transportista/{} - Obteniendo camiones del transportista", transportistaId);
        List<Camion> camiones = camionService.obtenerPorTransportista(transportistaId);
        return ResponseEntity.ok(camiones);
    }

    @GetMapping("/tipo/{tipoCamionId}")
    public ResponseEntity<List<Camion>> obtenerPorTipo(@PathVariable Long tipoCamionId) {
        log.info("GET /api/camiones/tipo/{} - Obteniendo camiones del tipo", tipoCamionId);
        List<Camion> camiones = camionService.obtenerPorTipo(tipoCamionId);
        return ResponseEntity.ok(camiones);
    }

    @GetMapping("/capacidad")
    public ResponseEntity<List<Camion>> obtenerConCapacidad(
            @RequestParam BigDecimal pesoMin,
            @RequestParam BigDecimal volumenMin) {
        log.info("GET /api/camiones/capacidad - Peso mín: {}, Volumen mín: {}", pesoMin, volumenMin);
        List<Camion> camiones = camionService.obtenerConCapacidad(pesoMin, volumenMin);
        return ResponseEntity.ok(camiones);
    }

    @PostMapping
    public ResponseEntity<Camion> crear(@RequestBody Camion camion) {
        log.info("POST /api/camiones - Creando nuevo camión: {}", camion.getDominio());
        try {
            Camion camionGuardado = camionService.guardar(camion);
            return ResponseEntity.status(HttpStatus.CREATED).body(camionGuardado);
        } catch (Exception e) {
            log.error("Error al crear camión: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{dominio}")
    public ResponseEntity<Camion> actualizar(@PathVariable String dominio, @RequestBody Camion camion) {
        log.info("PUT /api/camiones/{} - Actualizando camión", dominio);
        try {
            Camion camionActualizado = camionService.actualizar(dominio, camion);
            return ResponseEntity.ok(camionActualizado);
        } catch (RuntimeException e) {
            log.error("Error al actualizar camión {}: {}", dominio, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{dominio}")
    public ResponseEntity<Void> eliminar(@PathVariable String dominio) {
        log.info("DELETE /api/camiones/{} - Eliminando camión", dominio);
        try {
            camionService.eliminar(dominio);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            log.error("Error al eliminar camión {}: {}", dominio, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
}
