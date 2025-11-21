package com.tpi.localizaciones.controller;

import com.tpi.localizaciones.entity.Deposito;
import com.tpi.localizaciones.service.DepositoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/depositos")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class DepositoController {

    private final DepositoService depositoService;

    @GetMapping
    public ResponseEntity<List<Deposito>> getAllDepositos() {
        log.info("GET /api/depositos - Obteniendo todos los depósitos");
        List<Deposito> depositos = depositoService.findAll();
        return ResponseEntity.ok(depositos);
    }

    @GetMapping("/count")
    public ResponseEntity<Long> countDepositos() {
        return ResponseEntity.ok(depositoService.count());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Deposito> getDepositoById(@PathVariable("id") Integer id) {
        log.info("GET /api/depositos/{} - Obteniendo depósito por ID", id);
        return depositoService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * REQUERIMIENTO FUNCIONAL 9 (parte 1): Registrar y actualizar depósitos (Operador)
     * - Permite crear nuevos depósitos con su ubicación
     */
    @PreAuthorize("hasRole('operador')")
    @PostMapping
    public ResponseEntity<Deposito> crearDeposito(@Valid @RequestBody Deposito deposito) {
        log.info("POST /api/depositos - Creando nuevo depósito: {}", deposito.getNombre());
        try {
            Deposito depositoGuardado = depositoService.save(deposito);
            return ResponseEntity.status(HttpStatus.CREATED).body(depositoGuardado);
        } catch (Exception e) {
            log.error("Error al crear depósito: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * REQUERIMIENTO FUNCIONAL 9 (parte 2): Registrar y actualizar depósitos (Operador)
     * - Permite modificar información de depósitos existentes
     */
    @PreAuthorize("hasRole('operador')")
    @PutMapping("/{id}")
    public ResponseEntity<Deposito> actualizarDeposito(
            @PathVariable("id") Integer id,
            @Valid @RequestBody Deposito deposito) {
        log.info("PUT /api/depositos/{} - Actualizando depósito", id);
        try {
            Deposito depositoActualizado = depositoService.update(id, deposito);
            return ResponseEntity.ok(depositoActualizado);
        } catch (RuntimeException e) {
            log.error("Error al actualizar depósito {}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
}

