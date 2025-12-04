package com.tpi.solicitudes.controller;

import com.tpi.solicitudes.dto.ContenedorDetalladoDto;
import com.tpi.solicitudes.dto.ContenedorDto;
import com.tpi.solicitudes.entity.Contenedor;
import com.tpi.solicitudes.entity.Solicitud;
import com.tpi.solicitudes.mapper.ContenedorMapper;
import com.tpi.solicitudes.service.ContenedorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/contenedores")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ContenedorController {

    private final ContenedorService contenedorService;
    private final ContenedorMapper contenedorMapper;


    @GetMapping
    public ResponseEntity<List<ContenedorDto>> obtenerTodos() {
        log.info("GET /api/contenedores - Obteniendo todos los contenedores");
        List<Contenedor> contenedores = contenedorService.obtenerTodos();

        List<ContenedorDto> contenedoresDto = contenedores.stream()
                .map(contenedor -> {
                    Optional<Solicitud> solicitud = contenedorService.obtenerSolicitudPorContenedor(contenedor.getIdContenedor());
                    return contenedorMapper.toDto(contenedor, solicitud.orElse(null));
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(contenedoresDto);
    }

    @PreAuthorize("hasRole('cliente')")
    @GetMapping("/{id}")
    public ResponseEntity<ContenedorDto> obtenerPorId(@PathVariable("id") Integer id) {
        log.info("GET /api/contenedores/{} - Obteniendo por id", id);
        Optional<Contenedor> contenedor = contenedorService.obtenerPorId(id);

        if (contenedor.isPresent()) {
            Optional<Solicitud> solicitud = contenedorService.obtenerSolicitudPorContenedor(id);
            ContenedorDto contenedorDto = contenedorMapper.toDto(contenedor.get(), solicitud.orElse(null));
            return ResponseEntity.ok(contenedorDto);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Endpoint interno para obtener información básica de un contenedor
     * Usado por otros microservicios para validaciones (sin requerir autenticación de cliente)
     */
    @GetMapping("/interno/{id}")
    public ResponseEntity<Contenedor> obtenerPorIdInterno(@PathVariable("id") Integer id) {
        log.info("GET /api/contenedores/interno/{} - Obteniendo contenedor para validaciones internas", id);
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

    @PreAuthorize("hasRole('operador')")
    @GetMapping("/pendientes")
    public ResponseEntity<List<ContenedorDto>> obtenerPendientes() {
        log.info("GET /api/contenedores/pendientes - Obteniendo contenedores pendientes (sin parámetros)");
        List<Contenedor> pendientes = contenedorService.obtenerPendientes();

        List<ContenedorDto> pendientesDto = pendientes.stream()
                .map(contenedor -> {
                    Optional<Solicitud> solicitud = contenedorService.obtenerSolicitudPorContenedor(contenedor.getIdContenedor());
                    return contenedorMapper.toDto(contenedor, solicitud.orElse(null));
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(pendientesDto);
    }

    @PreAuthorize("hasRole('cliente') or hasRole('operador')")
    @GetMapping("/cliente/{numDocCliente}")
    public ResponseEntity<List<ContenedorDto>> obtenerPorDocumentoCliente(@PathVariable("numDocCliente") Long numDocCliente) {
        log.info("GET /api/contenedores/cliente/{} - Obteniendo contenedores por documento de cliente", numDocCliente);

        List<Contenedor> contenedores = contenedorService.obtenerPorDocumentoCliente(numDocCliente);

        List<ContenedorDto> contenedoresDto = contenedores.stream()
                .map(contenedor -> {
                    Optional<Solicitud> solicitud = contenedorService.obtenerSolicitudPorContenedor(contenedor.getIdContenedor());
                    return contenedorMapper.toDto(contenedor, solicitud.orElse(null));
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(contenedoresDto);
    }

    @PreAuthorize("hasRole('cliente') or hasRole('operador')")
    @GetMapping("/cliente/{numDocCliente}/contenedor/{idContenedor}/detallado")
    public ResponseEntity<ContenedorDetalladoDto> obtenerContenedorDetallado(
            @PathVariable("numDocCliente") Long numDocCliente,
            @PathVariable("idContenedor") Integer idContenedor) {
        log.info("GET /api/contenedores/cliente/{}/contenedor/{}/detallado - Obteniendo contenedor detallado", numDocCliente, idContenedor);

        try {
            ContenedorDetalladoDto contenedorDetallado = contenedorService.obtenerContenedorDetallado(idContenedor, numDocCliente);
            return ResponseEntity.ok(contenedorDetallado);
        } catch (RuntimeException e) {
            log.error("Error obteniendo contenedor detallado {}: {}", idContenedor, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
}