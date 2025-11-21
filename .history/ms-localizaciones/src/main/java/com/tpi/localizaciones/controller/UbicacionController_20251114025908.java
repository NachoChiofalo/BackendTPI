package com.tpi.localizaciones.controller;

import com.tpi.localizaciones.entity.Ubicacion;
import com.tpi.localizaciones.service.UbicacionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/ubicaciones")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class UbicacionController {

    private final UbicacionService ubicacionService;

    @GetMapping
    public ResponseEntity<List<Ubicacion>> getAllUbicaciones() {
        log.info("GET /api/ubicaciones - Obteniendo todas las ubicaciones");
        List<Ubicacion> ubicaciones = ubicacionService.findAll();
        return ResponseEntity.ok(ubicaciones);
    }

    @GetMapping("/count")
    public ResponseEntity<Long> countUbicaciones() {
        return ResponseEntity.ok(ubicacionService.count());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Ubicacion> getUbicacionById(@PathVariable("id") Integer id) {
        log.info("GET /api/ubicaciones/{} - Obteniendo ubicación por ID", id);
        return ubicacionService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasRole('operador')")
    @PostMapping
    public ResponseEntity<Ubicacion> crearUbicacion(@Valid @RequestBody Ubicacion ubicacion) {
        log.info("POST /api/ubicaciones - Creando nueva ubicación");
        try {
            Ubicacion ubicacionGuardada = ubicacionService.save(ubicacion);
            return ResponseEntity.status(HttpStatus.CREATED).body(ubicacionGuardada);
        } catch (Exception e) {
            log.error("Error al crear ubicación: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}

