package com.tpi.solicitudes.controller;

import com.tpi.solicitudes.dto.ClienteDto;
import com.tpi.solicitudes.entity.Cliente;
import com.tpi.solicitudes.service.ClienteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ClienteController {

    private final ClienteService clienteService;

    @GetMapping
    public ResponseEntity<List<Cliente>> obtenerTodos() {
        log.info("GET /api/clientes - Obteniendo todos los clientes");
        return ResponseEntity.ok(clienteService.obtenerTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cliente> obtenerPorId(@PathVariable Long id) {
        log.info("GET /api/clientes/{} - Obteniendo por id", id);
        return clienteService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<Cliente>> buscarPorNombre(@RequestParam String nombre) {
        log.info("GET /api/clientes/buscar - Buscando por nombre: {}", nombre);
        return ResponseEntity.ok(clienteService.buscarPorNombre(nombre));
    }

    @PostMapping
    public ResponseEntity<Cliente> crear(@Valid @RequestBody ClienteDto dto) {
        log.info("POST /api/clientes - Creando cliente: {}", dto.getNombre());
        try {
            Cliente cliente = mapearDtoAEntidad(dto);
            Cliente guardado = clienteService.guardar(cliente);
            return ResponseEntity.status(HttpStatus.CREATED).body(guardado);
        } catch (Exception e) {
            log.error("Error al crear cliente: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Cliente> actualizar(@PathVariable Long id, @Valid @RequestBody ClienteDto dto) {
        log.info("PUT /api/clientes/{} - Actualizando", id);
        try {
            Cliente cliente = mapearDtoAEntidad(dto);
            Cliente actualizado = clienteService.actualizar(id, cliente);
            return ResponseEntity.ok(actualizado);
        } catch (RuntimeException e) {
            log.error("Error al actualizar cliente {}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        log.info("DELETE /api/clientes/{} - Eliminando", id);
        try {
            clienteService.eliminar(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            log.error("Error al eliminar cliente {}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    private Cliente mapearDtoAEntidad(ClienteDto dto) {
        return Cliente.builder()
                .id(dto.getId())
                .nombre(dto.getNombre())
                .email(dto.getEmail())
                .telefono(dto.getTelefono())
                .direccion(dto.getDireccion())
                .numeroDocumento(dto.getNumeroDocumento())
                .tipoDocumento(dto.getTipoDocumento() != null ? Cliente.TipoDocumento.valueOf(dto.getTipoDocumento()) : null)
                .activo(dto.getActivo() != null ? dto.getActivo() : true)
                .build();
    }
}