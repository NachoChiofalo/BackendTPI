package com.tpi.rutas.controller;

import com.tpi.rutas.entity.Ruta;
import com.tpi.rutas.service.CalculoRutaService;
import com.tpi.rutas.service.RutaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rutas")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class RutaController {

    private final RutaService rutaService;
    private final CalculoRutaService calculoRutaService;

    @GetMapping
    public ResponseEntity<List<Ruta>> obtenerTodos() {
        log.info("GET /api/rutas - Obteniendo todas las rutas");
        return ResponseEntity.ok(rutaService.obtenerTodos());
    }

    /**
     * REQUERIMIENTO FUNCIONAL 3: Consultar rutas tentativas con todos los tramos sugeridos y el tiempo y costo estimados (Operador)
     * - Permite visualizar las opciones de rutas disponibles antes de asignarlas
     * - En una implementación completa incluiría cálculo de tiempo y costo estimado
     */
    @PreAuthorize("hasRole('operador')")
    @GetMapping("/tentativas")
    public ResponseEntity<List<Ruta>> obtenerRutasTentativas() {
        log.info("GET /api/rutas/tentativas - Obteniendo rutas tentativas");
        // Por ahora retorna todas las rutas disponibles
        // En una implementación completa, aquí se calcularían rutas tentativas
        // basándose en origen, destino, y depósitos disponibles
        return ResponseEntity.ok(rutaService.obtenerTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Ruta> obtenerPorId(@PathVariable("id") Integer id) {
        log.info("GET /api/rutas/{} - Obtener por id", id);
        return rutaService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Ruta> crear(@Valid @RequestBody Ruta ruta) {
        log.info("POST /api/rutas - Creando ruta con ID: {}", ruta.getRutaId());
        try {
            Ruta guardada = rutaService.guardar(ruta);
            return ResponseEntity.status(HttpStatus.CREATED).body(guardada);
        } catch (Exception e) {
            log.error("Error al crear ruta: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Ruta> actualizar(@PathVariable("id") Integer id, @Valid @RequestBody Ruta ruta) {
        log.info("PUT /api/rutas/{} - Actualizando", id);
        try {
            Ruta actualizado = rutaService.actualizar(id, ruta);
            return ResponseEntity.ok(actualizado);
        } catch (RuntimeException e) {
            log.error("Error al actualizar ruta {}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * REGLA DE NEGOCIO 2: Calcular costo real completo incluyendo estadía en depósitos
     * REGLA DE NEGOCIO 7: Calcular tiempo real transcurrido
     */
    @PreAuthorize("hasRole('operador')")
    @GetMapping("/{id}/costo-real")
    public ResponseEntity<Map<String, Object>> calcularCostoReal(
            @PathVariable("id") Integer id,
            @RequestParam(defaultValue = "100") BigDecimal costoPorDiaDeposito) {
        log.info("GET /api/rutas/{}/costo-real - Calculando costo real", id);
        try {
            BigDecimal costoReal = calculoRutaService.calcularCostoRealCompleto(id, costoPorDiaDeposito);
            BigDecimal costoEstadia = calculoRutaService.calcularCostoEstadia(id, costoPorDiaDeposito);
            long tiempoReal = calculoRutaService.calcularTiempoReal(id);

            Map<String, Object> resultado = new HashMap<>();
            resultado.put("rutaId", id);
            resultado.put("costoRealTotal", costoReal);
            resultado.put("costoEstadia", costoEstadia);
            resultado.put("tiempoRealDias", tiempoReal);

            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            log.error("Error al calcular costo real de ruta {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * REGLA DE NEGOCIO 5: Tiempo estimado se calcula en base a las distancias
     */
    @PreAuthorize("hasRole('operador')")
    @PostMapping("/tiempo-estimado")
    public ResponseEntity<Map<String, Object>> calcularTiempoEstimado(
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/rutas/tiempo-estimado - Calculando tiempo estimado");
        try {
            BigDecimal distanciaTotal = new BigDecimal(request.get("distanciaTotal").toString());
            int cantidadDepositos = Integer.parseInt(request.get("cantidadDepositos").toString());

            BigDecimal tiempoEstimado = calculoRutaService.calcularTiempoEstimado(distanciaTotal, cantidadDepositos);

            Map<String, Object> resultado = new HashMap<>();
            resultado.put("distanciaTotal", distanciaTotal);
            resultado.put("cantidadDepositos", cantidadDepositos);
            resultado.put("tiempoEstimadoHoras", tiempoEstimado);
            resultado.put("tiempoEstimadoDias", tiempoEstimado.divide(new BigDecimal("24"), 2, RoundingMode.HALF_UP));

            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            log.error("Error al calcular tiempo estimado: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable("id") Integer id) {
        log.info("DELETE /api/rutas/{} - Eliminando", id);
        try {
            rutaService.eliminar(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            log.error("Error al eliminar ruta {}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
}