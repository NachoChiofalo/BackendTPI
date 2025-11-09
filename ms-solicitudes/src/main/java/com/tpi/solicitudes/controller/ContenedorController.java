package com.tpi.solicitudes.controller;

import com.tpi.solicitudes.dto.ContenedorDto;
import com.tpi.solicitudes.entity.Contenedor;
import com.tpi.solicitudes.service.ContenedorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @GetMapping
    public ResponseEntity<List<Contenedor>> obtenerTodos() {
        log.info("GET /api/contenedores - Obteniendo todos los contenedores");
        return ResponseEntity.ok(contenedorService.obtenerTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Contenedor> obtenerPorId(@PathVariable String id) {
        log.info("GET /api/contenedores/{} - Obteniendo por id", id);
        return contenedorService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<Contenedor>> buscarPorCliente(@PathVariable Long clienteId) {
        log.info("GET /api/contenedores/cliente/{} - Buscando por cliente", clienteId);
        return ResponseEntity.ok(contenedorService.buscarPorClienteId(clienteId));
    }

    @PostMapping
    public ResponseEntity<Contenedor> crear(@Valid @RequestBody ContenedorDto dto) {
        log.info("POST /api/contenedores - Creando contenedor: {}", dto.getIdentificacion());
        try {
            Contenedor c = mapearDtoAEntidad(dto);
            Contenedor guardado = contenedorService.guardar(c);
            return ResponseEntity.status(HttpStatus.CREATED).body(guardado);
        } catch (Exception e) {
            log.error("Error al crear contenedor: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Contenedor> actualizar(@PathVariable String id, @Valid @RequestBody ContenedorDto dto) {
        log.info("PUT /api/contenedores/{} - Actualizando", id);
        try {
            Contenedor c = mapearDtoAEntidad(dto);
            Contenedor actualizado = contenedorService.actualizar(id, c);
            return ResponseEntity.ok(actualizado);
        } catch (RuntimeException e) {
            log.error("Error al actualizar contenedor {}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable String id) {
        log.info("DELETE /api/contenedores/{} - Eliminando", id);
        try {
            contenedorService.eliminar(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            log.error("Error al eliminar contenedor {}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    private Contenedor mapearDtoAEntidad(ContenedorDto dto) {
        return Contenedor.builder()
                .identificacion(dto.getIdentificacion())
                .peso(dto.getPeso())
                .volumen(dto.getVolumen())
                .descripcion(dto.getDescripcion())
                .estado(dto.getEstado() != null ? Contenedor.EstadoContenedor.valueOf(dto.getEstado()) : Contenedor.EstadoContenedor.REGISTRADO)
                .tipoContenedor(dto.getTipoContenedor() != null ? Contenedor.TipoContenedor.valueOf(dto.getTipoContenedor()) : null)
                .ubicacionActualLatitud(dto.getUbicacionActualLatitud())
                .ubicacionActualLongitud(dto.getUbicacionActualLongitud())
                .ubicacionDescripcion(dto.getUbicacionDescripcion())
                .build();
    }
}