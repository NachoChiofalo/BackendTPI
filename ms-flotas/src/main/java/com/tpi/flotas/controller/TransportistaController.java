package com.tpi.flotas.controller;

import com.tpi.flotas.dto.TransportistaDto;
import com.tpi.flotas.entity.Transportista;
import com.tpi.flotas.service.TransportistaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/transportistas")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class TransportistaController {

    private final TransportistaService transportistaService;

    @GetMapping
    public ResponseEntity<List<Transportista>> obtenerTodos() {
        log.info("GET /api/transportistas - Obteniendo todos los transportistas");
        List<Transportista> transportistas = transportistaService.obtenerTodos();
        return ResponseEntity.ok(transportistas);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Transportista> obtenerPorId(@PathVariable("id") Integer id) {
        log.info("GET /api/transportistas/{} - Obteniendo transportista por ID", id);
        return transportistaService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/telefono/{telefono}")
    public ResponseEntity<Transportista> obtenerPorTelefono(@PathVariable("telefono") Long telefono) {
        log .info("GET /api/transportistas/telefono/{} - Obteniendo transportista por teléfono", telefono);
        return transportistaService.obtenerPorTelefono(telefono)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasRole('operador')")
    @PostMapping
    public ResponseEntity<Transportista> crear(@Valid @RequestBody TransportistaDto transportistaDto) {
        log.info("POST /api/transportistas - Creando nuevo transportista");

        Transportista transportista = new Transportista();
        transportista.setTransportistaId(transportistaDto.getTransportistaId());
        transportista.setNombre(transportistaDto.getNombre());
        transportista.setApellido(transportistaDto.getApellido());
        transportista.setTelefono(transportistaDto.getTelefono());

        Transportista transportistaGuardado = transportistaService.guardar(transportista);
        return ResponseEntity.status(HttpStatus.CREATED).body(transportistaGuardado);
    }

    @PreAuthorize("hasRole('operador')")
    @PutMapping("/{id}")
    public ResponseEntity<Transportista> actualizar(@PathVariable("id") Integer id,
                                                   @Valid @RequestBody TransportistaDto transportistaDto) {
        log.info("PUT /api/transportistas/{} - Actualizando transportista", id);

        try {
            Transportista transportistaActualizado = new Transportista();
            transportistaActualizado.setNombre(transportistaDto.getNombre());
            transportistaActualizado.setApellido(transportistaDto.getApellido());
            transportistaActualizado.setTelefono(transportistaDto.getTelefono());

            Transportista resultado = transportistaService.actualizar(id, transportistaActualizado);
            return ResponseEntity.ok(resultado);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }



}
