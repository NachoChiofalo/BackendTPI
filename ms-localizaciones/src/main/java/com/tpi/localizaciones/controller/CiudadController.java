package com.tpi.localizaciones.controller;

import com.tpi.localizaciones.dto.CiudadDto;
import com.tpi.localizaciones.entity.Ciudad;
import com.tpi.localizaciones.service.CiudadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/ciudades")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class CiudadController {

    private final CiudadService ciudadService;

    @GetMapping
    public ResponseEntity<List<Ciudad>> obtenerTodas() {
        log.info("GET /api/ciudades - Obteniendo todas las ciudades");
        return ResponseEntity.ok(ciudadService.obtenerTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Ciudad> obtenerPorId(@PathVariable Long id) {
        log.info("GET /api/ciudades/{} - Obteniendo por id", id);
        return ciudadService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<Ciudad>> buscarPorNombre(@RequestParam String nombre) {
        log.info("GET /api/ciudades/buscar - Buscando por nombre: {}", nombre);
        return ResponseEntity.ok(ciudadService.buscarPorNombre(nombre));
    }

    @GetMapping("/provincia")
    public ResponseEntity<List<Ciudad>> buscarPorProvincia(@RequestParam String provincia) {
        log.info("GET /api/ciudades/provincia - Buscando por provincia: {}", provincia);
        return ResponseEntity.ok(ciudadService.buscarPorProvincia(provincia));
    }

    @GetMapping("/pais")
    public ResponseEntity<List<Ciudad>> buscarPorPais(@RequestParam String pais) {
        log.info("GET /api/ciudades/pais - Buscando por país: {}", pais);
        return ResponseEntity.ok(ciudadService.buscarPorPais(pais));
    }

    @GetMapping("/activas")
    public ResponseEntity<List<Ciudad>> obtenerActivas() {
        log.info("GET /api/ciudades/activas - Obteniendo ciudades activas");
        return ResponseEntity.ok(ciudadService.obtenerActivas());
    }

    @GetMapping("/codigo-postal")
    public ResponseEntity<List<Ciudad>> buscarPorCodigoPostal(@RequestParam String codigoPostal) {
        log.info("GET /api/ciudades/codigo-postal - Buscando por código postal: {}", codigoPostal);
        return ResponseEntity.ok(ciudadService.buscarPorCodigoPostal(codigoPostal));
    }

    @PostMapping
    public ResponseEntity<Ciudad> crear(@Valid @RequestBody CiudadDto ciudadDto) {
        log.info("POST /api/ciudades - Creando nueva ciudad: {}", ciudadDto.getNombre());
        try {
            Ciudad ciudad = mapearDtoAEntidad(ciudadDto);
            Ciudad guardada = ciudadService.guardar(ciudad);
            return ResponseEntity.status(HttpStatus.CREATED).body(guardada);
        } catch (Exception e) {
            log.error("Error al crear ciudad: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Ciudad> actualizar(@PathVariable Long id,
                                           @Valid @RequestBody CiudadDto ciudadDto) {
        log.info("PUT /api/ciudades/{} - Actualizando", id);
        try {
            Ciudad ciudad = mapearDtoAEntidad(ciudadDto);
            Ciudad actualizada = ciudadService.actualizar(id, ciudad);
            return ResponseEntity.ok(actualizada);
        } catch (RuntimeException e) {
            log.error("Error al actualizar ciudad {}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        log.info("DELETE /api/ciudades/{} - Eliminando", id);
        try {
            ciudadService.eliminar(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            log.error("Error al eliminar ciudad {}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<Void> cambiarEstado(@PathVariable Long id,
                                            @RequestParam Boolean activa) {
        log.info("PATCH /api/ciudades/{}/estado - Cambiando estado a: {}", id, activa);
        try {
            ciudadService.cambiarEstado(id, activa);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            log.error("Error al cambiar estado de ciudad {}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    private Ciudad mapearDtoAEntidad(CiudadDto dto) {
        return Ciudad.builder()
                .id(dto.getId())
                .nombre(dto.getNombre())
                .provincia(dto.getProvincia())
                .pais(dto.getPais())
                .codigoPostal(dto.getCodigoPostal())
                .latitud(dto.getLatitud())
                .longitud(dto.getLongitud())
                .zonaHoraria(dto.getZonaHoraria())
                .activa(dto.getActiva() != null ? dto.getActiva() : true)
                .build();
    }
}