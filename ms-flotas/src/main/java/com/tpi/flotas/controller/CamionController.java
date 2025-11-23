package com.tpi.flotas.controller;

import com.tpi.flotas.dto.CamionDto;
import com.tpi.flotas.entity.Camion;
import com.tpi.flotas.service.CamionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/camiones")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class CamionController {

    private final CamionService camionService;


    @GetMapping
    public ResponseEntity<List<Camion>> obtenerTodos() {
        log.info("GET /api/camiones - Obteniendo todos los camiones");
        List<Camion> camiones = camionService.obtenerTodos();
        return ResponseEntity.ok(camiones);
    }

    @GetMapping("/disponibles")
    public ResponseEntity<List<Camion>> obtenerDisponibles() {
        log.info("GET /api/camiones/disponibles - Obteniendo camiones disponibles");
        List<Camion> camiones = camionService.obtenerDisponibles();
        return ResponseEntity.ok(camiones);
    }

    @GetMapping("/no-disponibles")
    public ResponseEntity<List<Camion>> obtenerNoDisponibles() {
        log.info("GET /api/camiones/no-disponibles - Obteniendo camiones no disponibles");
        List<Camion> camiones = camionService.obtenerNoDisponibles();
        return ResponseEntity.ok(camiones);
    }

    @GetMapping("/{dominio}")
    public ResponseEntity<Camion> obtenerPorDominio(@PathVariable("dominio") String dominio) {
        log.info("GET /api/camiones/{} - Obteniendo camión por dominio", dominio);
        return camionService.obtenerPorDominio(dominio)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * REQUERIMIENTO FUNCIONAL 10: Validar que un camión no supere su capacidad máxima en peso ni volumen
     * - Filtra camiones que cumplan con requisitos mínimos de capacidad
     * - Permite buscar solo entre camiones disponibles
     */
    @GetMapping("/capacidad")
    public ResponseEntity<List<Camion>> obtenerConCapacidad(
            @RequestParam BigDecimal pesoMin,
            @RequestParam BigDecimal volumenMin,
            @RequestParam(defaultValue = "false") Boolean soloDisponibles) {
        log.info("GET /api/camiones/capacidad - Peso: {}, Volumen: {}, Solo disponibles: {}",
                pesoMin, volumenMin, soloDisponibles);

        List<Camion> camiones = soloDisponibles
                ? camionService.obtenerDisponiblesConCapacidad(pesoMin, volumenMin)
                : camionService.obtenerConCapacidadMinima(pesoMin, volumenMin);

        return ResponseEntity.ok(camiones);
    }

    /**
     * REQUERIMIENTO FUNCIONAL 9 (parte 4): Registrar y actualizar camiones (Operador)
     * - Carga camiones con capacidad en peso y volumen
     * - Registra consumo de combustible y costos para cálculo de tarifas
     */
    @PreAuthorize("hasRole('operador')")
    @PostMapping
    public ResponseEntity<Camion> crear(@Valid @RequestBody CamionDto camionDto) {
        log.info("POST /api/camiones - Creando nuevo camión: {}", camionDto.getDominio());

        try {
            Camion camion = new Camion();
            camion.setDominio(camionDto.getDominio());
            camion.setDisponible(camionDto.getDisponible());
            camion.setCapacidadPeso(camionDto.getCapacidadPeso());
            camion.setCapacidadVolumen(camionDto.getCapacidadVolumen());
            camion.setCostoBaseKm(camionDto.getCostoBaseKm());
            camion.setConsumoPromedio(camionDto.getConsumoPromedio());

            Camion camionGuardado = camionService.guardar(camion);
            log.info("Camión creado exitosamente: {}", camionGuardado.getDominio());
            return ResponseEntity.status(HttpStatus.CREATED).body(camionGuardado);
        } catch (Exception e) {
            log.error("Error al crear camión {}: {}", camionDto.getDominio(), e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PreAuthorize("hasRole('operador')")
    @PutMapping("/{dominio}")
    public ResponseEntity<Camion> actualizar(@PathVariable("dominio") String dominio,
                                             @Valid @RequestBody CamionDto camionDto) {
        log.info("PUT /api/camiones/{} - Actualizando camión", dominio);

        try {
            Camion camionActualizado = new Camion();
            camionActualizado.setDominio(dominio);
            camionActualizado.setDisponible(camionDto.getDisponible());
            camionActualizado.setCapacidadPeso(camionDto.getCapacidadPeso());
            camionActualizado.setCapacidadVolumen(camionDto.getCapacidadVolumen());
            camionActualizado.setCostoBaseKm(camionDto.getCostoBaseKm());
            camionActualizado.setConsumoPromedio(camionDto.getConsumoPromedio());

            Camion resultado = camionService.actualizar(dominio, camionActualizado);
            log.info("Camión actualizado exitosamente: {}", dominio);
            return ResponseEntity.ok(resultado);
        } catch (RuntimeException e) {
            log.error("Error al actualizar camión {}: {}", dominio, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }


    @PreAuthorize("hasRole('operador')")
    @DeleteMapping("/{dominio}")
    public ResponseEntity<Void> eliminar(@PathVariable("dominio") String dominio) {
        log.info("DELETE /api/camiones/{} - Eliminando camión", dominio);

        try {
            camionService.eliminar(dominio);
            log.info("Camión eliminado exitosamente: {}", dominio);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            log.error("Error al eliminar camión {}: {}", dominio, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    // Nuevo endpoint para asignar un camión a un tramo (usa la lógica SQL en CamionService)
    @PostMapping("/asignar")
    public ResponseEntity<Map<String, Object>> asignarCamionATramo(
            @RequestParam("tramoId") Integer tramoId,
            @RequestParam("dominio") String dominio) {

        log.info("POST /api/camiones/asignar - Asignando camión {} para tramo {}", dominio, tramoId);
        try {
            Map<String, Object> reservado = camionService.asignarCamionEspecificoATramo(tramoId, dominio);
            if (reservado == null) {
                Map<String, Object> body = new java.util.HashMap<>();
                body.put("message", "No fue posible asignar el camión solicitado");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
            }
            return ResponseEntity.ok(reservado);
        } catch (RuntimeException e) {
            log.error("Error al asignar camión {} al tramo {}: {}", dominio, tramoId, e.getMessage());
            Map<String, Object> body = new java.util.HashMap<>();
            body.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
        }
    }
}
