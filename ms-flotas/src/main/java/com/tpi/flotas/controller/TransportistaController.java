package com.tpi.flotas.controller;

import com.tpi.flotas.entity.Transportista;
import com.tpi.flotas.service.TransportistaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<Transportista> obtenerPorId(@PathVariable Long id) {
        log.info("GET /api/transportistas/{} - Obteniendo transportista por ID", id);
        return transportistaService.obtenerPorId(id)
                .map(transportista -> ResponseEntity.ok(transportista))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/dni/{dni}")
    public ResponseEntity<Transportista> obtenerPorDni(@PathVariable String dni) {
        log.info("GET /api/transportistas/dni/{} - Obteniendo transportista por DNI", dni);
        return transportistaService.obtenerPorDni(dni)
                .map(transportista -> ResponseEntity.ok(transportista))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<Transportista>> buscarPorTexto(@RequestParam String texto) {
        log.info("GET /api/transportistas/buscar?texto={} - Buscando transportistas", texto);
        List<Transportista> transportistas = transportistaService.buscarPorTexto(texto);
        return ResponseEntity.ok(transportistas);
    }

    @PostMapping
    public ResponseEntity<Transportista> crear(@RequestBody Transportista transportista) {
        log.info("POST /api/transportistas - Creando nuevo transportista: {} {}",
                transportista.getNombre(), transportista.getApellido());
        try {
            Transportista transportistaGuardado = transportistaService.guardar(transportista);
            return ResponseEntity.status(HttpStatus.CREATED).body(transportistaGuardado);
        } catch (Exception e) {
            log.error("Error al crear transportista: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Transportista> actualizar(@PathVariable Long id, @RequestBody Transportista transportista) {
        log.info("PUT /api/transportistas/{} - Actualizando transportista", id);
        try {
            Transportista transportistaActualizado = transportistaService.actualizar(id, transportista);
            return ResponseEntity.ok(transportistaActualizado);
        } catch (RuntimeException e) {
            log.error("Error al actualizar transportista {}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        log.info("DELETE /api/transportistas/{} - Eliminando transportista", id);
        try {
            transportistaService.eliminar(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            log.error("Error al eliminar transportista {}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/count")
    public ResponseEntity<Long> contarTransportistas() {
        log.info("GET /api/transportistas/count - Contando transportistas activos");
        long count = transportistaService.contarTransportistas();
        return ResponseEntity.ok(count);
    }
}
