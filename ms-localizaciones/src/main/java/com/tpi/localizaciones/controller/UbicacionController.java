package com.tpi.localizaciones.controller;

import com.tpi.localizaciones.entity.Ubicacion;
import com.tpi.localizaciones.service.UbicacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ubicaciones")
@RequiredArgsConstructor
public class UbicacionController {

    private final UbicacionService ubicacionService;

    @GetMapping
    public ResponseEntity<List<Ubicacion>> getAllUbicaciones() {
        List<Ubicacion> ubicaciones = ubicacionService.findAll();
        return ResponseEntity.ok(ubicaciones);
    }

    @GetMapping("/count")
    public ResponseEntity<Long> countUbicaciones() {
        return ResponseEntity.ok(ubicacionService.count());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Ubicacion> getUbicacionById(@PathVariable Integer id) {
        return ubicacionService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}

