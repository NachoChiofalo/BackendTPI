package com.tpi.rutas.controller;

import com.tpi.rutas.dto.CoordenadasDTO;
import com.tpi.rutas.dto.RutaTentativaDTO;
import com.tpi.rutas.dto.TramoTentativoDTO;
import com.tpi.rutas.entity.Ruta;
import com.tpi.rutas.entity.Tramo;
import com.tpi.rutas.service.CalculoRutaService;
import com.tpi.rutas.service.RutaService;
import com.tpi.rutas.service.RutaTentativaService;
import com.tpi.rutas.service.TramoService;
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
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/rutas")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class RutaController {

    private final RutaService rutaService;
    private final CalculoRutaService calculoRutaService;
    private final TramoService tramoService;
    private final RutaTentativaService rutaTentativaService;

    @GetMapping
    public ResponseEntity<List<Ruta>> obtenerTodos() {
        log.info("GET /api/rutas - Obteniendo todas las rutas");
        return ResponseEntity.ok(rutaService.obtenerTodos());
    }

    /**
     * REQUERIMIENTO FUNCIONAL 3: Consultar rutas tentativas con todos los tramos sugeridos y el tiempo y costo estimados (Operador)
     * - Recibe coordenadas de origen y destino
     * - Calcula rutas alternativas utilizando la misma lógica que la creación de solicitudes
     * - Considera depósitos intermedios según las reglas de negocio
     * - Devuelve exactamente 3 opciones de rutas tentativas
     */
    @PreAuthorize("hasRole('operador')")
    @PostMapping("/tentativas")
    public ResponseEntity<List<RutaTentativaDTO>> obtenerRutasTentativas(@Valid @RequestBody CoordenadasDTO coordenadas) {
        log.info("POST /api/rutas/tentativas - Generando rutas tentativas para origen ({},{}) -> destino ({},{})",
                coordenadas.getLatitudOrigen(), coordenadas.getLongitudOrigen(),
                coordenadas.getLatitudDestino(), coordenadas.getLongitudDestino());

        try {
            List<RutaTentativaDTO> rutasTentativas = rutaTentativaService.generarRutasTentativas(
                    coordenadas.getLatitudOrigen(),
                    coordenadas.getLongitudOrigen(),
                    coordenadas.getLatitudDestino(),
                    coordenadas.getLongitudDestino()
            );

            if (rutasTentativas.isEmpty()) {
                log.warn("No se pudieron generar rutas tentativas para las coordenadas proporcionadas");
                return ResponseEntity.noContent().build();
            }

            log.info("Se generaron {} rutas tentativas exitosamente", rutasTentativas.size());
            return ResponseEntity.ok(rutasTentativas);

        } catch (Exception e) {
            log.error("Error generando rutas tentativas: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Ruta> obtenerPorId(@PathVariable("id") Integer id) {
        log.info("GET /api/rutas/{} - Obtener por id", id);
        return rutaService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasRole('operador')")
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

    /**
     * Endpoint interno para uso entre microservicios - Sin autenticación
     * Permite a otros microservicios crear rutas automáticamente
     */
    @PostMapping("/interno")
    public ResponseEntity<Ruta> crearInterno(@RequestBody Ruta ruta) {
        log.info("POST /api/rutas/interno - Creando ruta con ID: {} (llamada interna)", ruta.getRutaId());
        try {
            Ruta guardada = rutaService.guardar(ruta);
            return ResponseEntity.status(HttpStatus.CREATED).body(guardada);
        } catch (Exception e) {
            log.error("Error al crear ruta: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PreAuthorize("hasRole('operador')")
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

    @PreAuthorize("hasRole('operador')")
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