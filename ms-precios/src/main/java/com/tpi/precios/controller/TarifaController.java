package com.tpi.precios.controller;

import com.tpi.precios.dto.TarifaDto;
import com.tpi.precios.entity.Tarifa;
import com.tpi.precios.service.TarifaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tarifas")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class TarifaController {

    private final TarifaService tarifaService;

    @PreAuthorize("hasAnyRole('cliente', 'transportista', 'operador')")
    @GetMapping
    public ResponseEntity<List<TarifaDto>> obtenerTodas() {
        log.info("GET /api/tarifas - Obteniendo todas las tarifas");
        List<Tarifa> tarifas = tarifaService.obtenerTodas();
        List<TarifaDto> tarifasDto = tarifas.stream()
                .map(this::convertirADto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(tarifasDto);
    }

    @PreAuthorize("hasAnyRole('cliente', 'transportista', 'operador')")
    @GetMapping("/vigentes")
    public ResponseEntity<List<TarifaDto>> obtenerVigentes() {
        log.info("GET /api/tarifas/vigentes - Obteniendo tarifas vigentes");
        List<Tarifa> tarifas = tarifaService.obtenerTarifasVigentes();
        List<TarifaDto> tarifasDto = tarifas.stream()
                .map(this::convertirADto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(tarifasDto);
    }

    @PreAuthorize("hasAnyRole('cliente', 'transportista', 'operador')")
    @GetMapping("/vigente-actual")
    public ResponseEntity<TarifaDto> obtenerVigenteActual() {
        log.info("GET /api/tarifas/vigente-actual - Obteniendo tarifa vigente más reciente");
        return tarifaService.obtenerTarifaVigenteMasReciente()
                .map(tarifa -> ResponseEntity.ok(convertirADto(tarifa)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TarifaDto> obtenerPorId(@PathVariable("id") Integer id) {
        log.info("GET /api/tarifas/{} - Obteniendo tarifa por ID", id);
        return tarifaService.obtenerPorId(id)
                .map(tarifa -> ResponseEntity.ok(convertirADto(tarifa)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAnyRole('cliente', 'transportista', 'operador')")
    @GetMapping("/vigentes-en")
    public ResponseEntity<List<TarifaDto>> obtenerVigentesEn(@RequestParam String fecha) {
        log.info("GET /api/tarifas/vigentes-en?fecha={} - Obteniendo tarifas vigentes en fecha", fecha);
        try {
            LocalDate fechaBusqueda = LocalDate.parse(fecha);
            List<Tarifa> tarifas = tarifaService.obtenerTarifasVigentesEn(fechaBusqueda);
            List<TarifaDto> tarifasDto = tarifas.stream()
                    .map(this::convertirADto)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(tarifasDto);
        } catch (Exception e) {
            log.error("Error al parsear fecha: {}", fecha, e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PreAuthorize("hasAnyRole('cliente', 'transportista', 'operador')")
    @GetMapping("/por-rango")
    public ResponseEntity<List<TarifaDto>> obtenerPorRangoFechas(
            @RequestParam String fechaInicio,
            @RequestParam String fechaFin) {
        log.info("GET /api/tarifas/por-rango?fechaInicio={}&fechaFin={}", fechaInicio, fechaFin);
        try {
            LocalDate inicio = LocalDate.parse(fechaInicio);
            LocalDate fin = LocalDate.parse(fechaFin);
            List<Tarifa> tarifas = tarifaService.obtenerTarifasPorRangoFechas(inicio, fin);
            List<TarifaDto> tarifasDto = tarifas.stream()
                    .map(this::convertirADto)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(tarifasDto);
        } catch (Exception e) {
            log.error("Error al parsear fechas: {} - {}", fechaInicio, fechaFin, e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PreAuthorize("hasAnyRole('cliente', 'transportista', 'operador')")
    @GetMapping("/vencen-proximamente")
    public ResponseEntity<List<TarifaDto>> obtenerQueVencenProximamente(
            @RequestParam(defaultValue = "30") int dias) {
        log.info("GET /api/tarifas/vencen-proximamente?dias={}", dias);
        List<Tarifa> tarifas = tarifaService.obtenerTarifasQueVencenProximamente(dias);
        List<TarifaDto> tarifasDto = tarifas.stream()
                .map(this::convertirADto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(tarifasDto);
    }

    @PreAuthorize("hasAnyRole('cliente', 'transportista', 'operador')")
    @GetMapping("/futuras")
    public ResponseEntity<List<TarifaDto>> obtenerFuturas() {
        log.info("GET /api/tarifas/futuras - Obteniendo tarifas futuras");
        List<Tarifa> tarifas = tarifaService.obtenerTarifasFuturas();
        List<TarifaDto> tarifasDto = tarifas.stream()
                .map(this::convertirADto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(tarifasDto);
    }

    /**
     * REQUERIMIENTO FUNCIONAL 9 (parte 5): Registrar y actualizar tarifas (Operador)
     * - Modifica parámetros de tarifación: costo por km, combustible, estadía
     * - Configura valores base para cálculo de costos estimados y reales
     */
    @PreAuthorize("hasRole('operador')")
    @PostMapping
    public ResponseEntity<TarifaDto> crear(@Valid @RequestBody TarifaDto tarifaDto) {
        log.info("POST /api/tarifas - Creando nueva tarifa");
        try {
            Tarifa tarifa = convertirAEntidad(tarifaDto);
            Tarifa tarifaCreada = tarifaService.crearTarifa(tarifa);
            return ResponseEntity.status(HttpStatus.CREATED).body(convertirADto(tarifaCreada));
        } catch (Exception e) {
            log.error("Error al crear tarifa", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PreAuthorize("hasRole('operador')")
    @PutMapping("/{id}")
    public ResponseEntity<TarifaDto> actualizar(@PathVariable("id") Integer id, @Valid @RequestBody TarifaDto tarifaDto) {
        log.info("PUT /api/tarifas/{} - Actualizando tarifa", id);
        try {
            Tarifa tarifa = convertirAEntidad(tarifaDto);
            Tarifa tarifaActualizada = tarifaService.actualizarTarifa(id, tarifa);
            return ResponseEntity.ok(convertirADto(tarifaActualizada));
        } catch (RuntimeException e) {
            log.error("Error al actualizar tarifa: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error al actualizar tarifa", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PreAuthorize("hasRole('operador')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable("id") Integer id) {
        log.info("DELETE /api/tarifas/{} - Eliminando tarifa", id);
        try {
            tarifaService.eliminarTarifa(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            log.error("Error al eliminar tarifa: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    private TarifaDto convertirADto(Tarifa tarifa) {
        return TarifaDto.builder()
                .tarifaId(tarifa.getTarifaId())
                .precioCombustibleLitro(tarifa.getPrecioCombustibleLitro())
                .precioKmKg(tarifa.getPrecioKmKg())
                .precioKmM3(tarifa.getPrecioKmM3())
                .fechaVigenciaInicio(tarifa.getFechaVigenciaInicio())
                .fechaVigenciaFin(tarifa.getFechaVigenciaFin())
                .precioTramo(tarifa.getPrecioTramo())
                .precioEstadiaDia(tarifa.getPrecioEstadiaDia())
                .tipoTarifa(tarifa.getTipoTarifa() != null ? tarifa.getTipoTarifa().name() : null)
                .modalidadCalculo(tarifa.getModalidadCalculo() != null ? tarifa.getModalidadCalculo().name() : null)
                .vigente(tarifa.esVigente())
                .build();
    }

    private Tarifa convertirAEntidad(TarifaDto dto) {
        return Tarifa.builder()
                .tarifaId(dto.getTarifaId())
                .precioCombustibleLitro(dto.getPrecioCombustibleLitro())
                .precioKmKg(dto.getPrecioKmKg())
                .precioKmM3(dto.getPrecioKmM3())
                .fechaVigenciaInicio(dto.getFechaVigenciaInicio())
                .fechaVigenciaFin(dto.getFechaVigenciaFin())
                .precioTramo(dto.getPrecioTramo())
                .precioEstadiaDia(dto.getPrecioEstadiaDia())
                .tipoTarifa(dto.getTipoTarifa() != null ? Tarifa.TipoTarifa.valueOf(dto.getTipoTarifa()) : null)
                .modalidadCalculo(dto.getModalidadCalculo() != null ? Tarifa.ModalidadCalculo.valueOf(dto.getModalidadCalculo()) : null)
                .build();
    }
}
