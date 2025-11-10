package com.tpi.solicitudes.controller;

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

    @GetMapping("/{tipoDocClienteId}/{numDocCliente}")
    public ResponseEntity<Cliente> obtenerPorId(
            @PathVariable Integer tipoDocClienteId,
            @PathVariable Long numDocCliente) {
        log.info("GET /api/clientes/{}/{} - Obteniendo por id compuesto", tipoDocClienteId, numDocCliente);
        return clienteService.obtenerPorId(tipoDocClienteId, numDocCliente)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/buscar/nombres")
    public ResponseEntity<List<Cliente>> buscarPorNombres(@RequestParam String nombres) {
        log.info("GET /api/clientes/buscar/nombres - Buscando por nombres: {}", nombres);
        return ResponseEntity.ok(clienteService.buscarPorNombres(nombres));
    }

    @GetMapping("/buscar/apellidos")
    public ResponseEntity<List<Cliente>> buscarPorApellidos(@RequestParam String apellidos) {
        log.info("GET /api/clientes/buscar/apellidos - Buscando por apellidos: {}", apellidos);
        return ResponseEntity.ok(clienteService.buscarPorApellidos(apellidos));
    }

    @PostMapping
    public ResponseEntity<Cliente> crear(@Valid @RequestBody Cliente cliente) {
        log.info("POST /api/clientes - Creando cliente: {} {}", cliente.getNombres(), cliente.getApellidos());
        try {
            Cliente guardado = clienteService.guardar(cliente);
            return ResponseEntity.status(HttpStatus.CREATED).body(guardado);
        } catch (Exception e) {
            log.error("Error al crear cliente: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{tipoDocClienteId}/{numDocCliente}")
    public ResponseEntity<Cliente> actualizar(
            @PathVariable Integer tipoDocClienteId,
            @PathVariable Long numDocCliente,
            @Valid @RequestBody Cliente cliente) {
        log.info("PUT /api/clientes/{}/{} - Actualizando", tipoDocClienteId, numDocCliente);
        try {
            Cliente actualizado = clienteService.actualizar(tipoDocClienteId, numDocCliente, cliente);
            return ResponseEntity.ok(actualizado);
        } catch (RuntimeException e) {
            log.error("Error al actualizar cliente: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{tipoDocClienteId}/{numDocCliente}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Integer tipoDocClienteId,
            @PathVariable Long numDocCliente) {
        log.info("DELETE /api/clientes/{}/{} - Eliminando", tipoDocClienteId, numDocCliente);
        try {
            clienteService.eliminar(tipoDocClienteId, numDocCliente);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            log.error("Error al eliminar cliente: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
}