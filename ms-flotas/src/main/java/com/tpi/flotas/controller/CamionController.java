package com.tpi.flotas.controller;

import com.tpi.flotas.dto.CamionDto;
import com.tpi.flotas.service.CamionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/camiones")
@RequiredArgsConstructor
@Validated
public class CamionController {

    private final CamionService camionService;

    @GetMapping
    public ResponseEntity<List<CamionDto>> obtenerTodos() {
        List<CamionDto> camiones = camionService.obtenerTodos();
        return ResponseEntity.ok(camiones);
    }

    @GetMapping("/disponibles")
    public ResponseEntity<List<CamionDto>> obtenerDisponibles() {
        List<CamionDto> camiones = camionService.obtenerDisponibles();
        return ResponseEntity.ok(camiones);
    }

    @GetMapping("/{dominio}")
    public ResponseEntity<CamionDto> obtenerPorDominio(@PathVariable String dominio) {
        return camionService.obtenerPorDominio(dominio)
                .map(camion -> ResponseEntity.ok(camion))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/tipo/{tipoCamion}")
    public ResponseEntity<List<CamionDto>> obtenerPorTipo(@PathVariable String tipoCamion) {
        List<CamionDto> camiones = camionService.obtenerPorTipo(tipoCamion);
        return ResponseEntity.ok(camiones);
    }

    @GetMapping("/capacidad")
    public ResponseEntity<List<CamionDto>> buscarPorCapacidad(
            @RequestParam Double pesoMinimo,
            @RequestParam Double volumenMinimo) {
        List<CamionDto> camiones = camionService.buscarPorCapacidad(pesoMinimo, volumenMinimo);
        return ResponseEntity.ok(camiones);
    }

    @PostMapping
    public ResponseEntity<CamionDto> crear(@Valid @RequestBody CamionDto camionDto) {
        try {
            CamionDto camionCreado = camionService.crear(camionDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(camionCreado);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{dominio}")
    public ResponseEntity<CamionDto> actualizar(
            @PathVariable String dominio,
            @Valid @RequestBody CamionDto camionDto) {
        try {
            CamionDto camionActualizado = camionService.actualizar(dominio, camionDto);
            return ResponseEntity.ok(camionActualizado);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{dominio}")
    public ResponseEntity<Void> eliminar(@PathVariable String dominio) {
        try {
            camionService.eliminar(dominio);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{dominio}/transportista/{transportistaId}")
    public ResponseEntity<CamionDto> asignarTransportista(
            @PathVariable String dominio,
            @PathVariable Long transportistaId) {
        try {
            CamionDto camionActualizado = camionService.asignarTransportista(dominio, transportistaId);
            return ResponseEntity.ok(camionActualizado);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
