package com.tpi.localizaciones.controller;

import com.tpi.localizaciones.dto.DistanciaCalculadaDto;
import com.tpi.localizaciones.entity.DistanciaCalculada;
import com.tpi.localizaciones.entity.Ubicacion;
import com.tpi.localizaciones.entity.EstadoValidacion;
import com.tpi.localizaciones.service.DistanciaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.time.LocalDateTime;
import java.util.List;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/distancias")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class DistanciaController {

    private final DistanciaService distanciaService;

    @GetMapping
    public ResponseEntity<List<DistanciaCalculada>> obtenerTodas() {
        log.info("GET /api/distancias - Obteniendo todas las distancias");
        return ResponseEntity.ok(distanciaService.obtenerTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DistanciaCalculada> obtenerPorId(@PathVariable Long id) {
        log.info("GET /api/distancias/{} - Obteniendo por id", id);
        return distanciaService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/validada")
    public ResponseEntity<DistanciaCalculada> obtenerUltimaValidada(
            @RequestParam Long origenId,
            @RequestParam Long destinoId) {
        log.info("GET /api/distancias/validada - Buscando última validada entre {} y {}", 
                origenId, destinoId);
        
        LocalDateTime fechaMinima = LocalDateTime.now().minusDays(30); // último mes
        return distanciaService.obtenerUltimaDistanciaValidada(origenId, destinoId, fechaMinima)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<DistanciaCalculada>> obtenerPorEstado(
            @PathVariable EstadoValidacion estado) {
        log.info("GET /api/distancias/estado/{} - Buscando por estado", estado);
        return ResponseEntity.ok(distanciaService.obtenerPorEstado(estado));
    }

    @GetMapping("/origen/{origenId}")
    public ResponseEntity<List<DistanciaCalculada>> obtenerPorOrigen(
            @PathVariable Long origenId) {
        log.info("GET /api/distancias/origen/{} - Buscando por origen", origenId);
        return ResponseEntity.ok(distanciaService.obtenerPorOrigen(origenId));
    }

    @GetMapping("/destino/{destinoId}")
    public ResponseEntity<List<DistanciaCalculada>> obtenerPorDestino(
            @PathVariable Long destinoId) {
        log.info("GET /api/distancias/destino/{} - Buscando por destino", destinoId);
        return ResponseEntity.ok(distanciaService.obtenerPorDestino(destinoId));
    }

    @GetMapping("/ruta")
    public ResponseEntity<DistanciaCalculada> obtenerPorOrigenYDestino(
            @RequestParam Long origenId,
            @RequestParam Long destinoId) {
        log.info("GET /api/distancias/ruta - Buscando entre {} y {}", origenId, destinoId);
        return distanciaService.obtenerPorOrigenYDestino(origenId, destinoId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<DistanciaCalculada> crear(
            @Valid @RequestBody DistanciaCalculadaDto distanciaDto) {
        log.info("POST /api/distancias - Creando nueva distancia calculada");
        try {
            DistanciaCalculada distancia = mapearDtoAEntidad(distanciaDto);
            DistanciaCalculada guardada = distanciaService.guardar(distancia);
            return ResponseEntity.status(HttpStatus.CREATED).body(guardada);
        } catch (Exception e) {
            log.error("Error al crear distancia calculada: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<DistanciaCalculada> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody DistanciaCalculadaDto distanciaDto) {
        log.info("PUT /api/distancias/{} - Actualizando", id);
        try {
            DistanciaCalculada distancia = mapearDtoAEntidad(distanciaDto);
            DistanciaCalculada actualizada = distanciaService.actualizar(id, distancia);
            return ResponseEntity.ok(actualizada);
        } catch (RuntimeException e) {
            log.error("Error al actualizar distancia {}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        log.info("DELETE /api/distancias/{} - Eliminando", id);
        try {
            distanciaService.eliminar(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            log.error("Error al eliminar distancia {}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{id}/validar")
    public ResponseEntity<DistanciaCalculada> validar(
            @PathVariable Long id,
            @RequestParam(required = false) String observaciones) {
        log.info("PATCH /api/distancias/{}/validar - Validando", id);
        try {
            DistanciaCalculada validada = distanciaService.validar(id, observaciones);
            return ResponseEntity.ok(validada);
        } catch (RuntimeException e) {
            log.error("Error al validar distancia {}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{id}/rechazar")
    public ResponseEntity<DistanciaCalculada> rechazar(
            @PathVariable Long id,
            @RequestParam(required = false) String observaciones) {
        log.info("PATCH /api/distancias/{}/rechazar - Rechazando", id);
        try {
            DistanciaCalculada rechazada = distanciaService.rechazar(id, observaciones);
            return ResponseEntity.ok(rechazada);
        } catch (RuntimeException e) {
            log.error("Error al rechazar distancia {}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    private DistanciaCalculada mapearDtoAEntidad(DistanciaCalculadaDto dto) {
        return DistanciaCalculada.builder()
                .id(dto.getId())
                .distanciaKm(dto.getDistanciaKm())
        // La entidad usa 'duracionMinutos' (BigDecimal) y 'momentoCalculo' (LocalDateTime).
        .duracionMinutos(dto.getTiempoEstimadoMinutos() != null
            ? new BigDecimal(dto.getTiempoEstimadoMinutos())
            : null)
        .estadoValidacion(dto.getEstadoValidacion())
        .observaciones(dto.getObservaciones())
        .momentoCalculo(dto.getFechaCalculo() != null ? dto.getFechaCalculo() : LocalDateTime.now())
        // Referencias a ubicaciones si se proporcionaron IDs
        .ubicacionOrigen(dto.getUbicacionOrigenId() != null ? Ubicacion.builder().id(dto.getUbicacionOrigenId()).build() : null)
        .ubicacionDestino(dto.getUbicacionDestinoId() != null ? Ubicacion.builder().id(dto.getUbicacionDestinoId()).build() : null)
                .build();
    }
}