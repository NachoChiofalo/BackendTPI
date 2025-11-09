package com.tpi.precios.controller;

import com.tpi.precios.dto.CalculoPrecioDto;
import com.tpi.precios.dto.SolicitudCotizacionDto;
import com.tpi.precios.service.CalculoPrecioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/cotizaciones")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class CotizacionController {

    private final CalculoPrecioService calculoPrecioService;

    @PostMapping("/calcular")
    public ResponseEntity<CalculoPrecioDto> calcularPrecio(@Valid @RequestBody SolicitudCotizacionDto solicitud) {
        log.info("POST /api/cotizaciones/calcular - Calculando precio para cotización");
        try {
            CalculoPrecioDto resultado = calculoPrecioService.calcularPrecio(solicitud);
            return ResponseEntity.ok(resultado);
        } catch (RuntimeException e) {
            log.error("Error al calcular precio: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error interno al calcular precio", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/calcular-con-tarifa")
    public ResponseEntity<CalculoPrecioDto> calcularPrecioConTarifa(@Valid @RequestBody CalculoPrecioDto calculoDto) {
        log.info("POST /api/cotizaciones/calcular-con-tarifa - Calculando precio con tarifa específica");
        try {
            CalculoPrecioDto resultado = calculoPrecioService.calcularPrecioConTarifa(calculoDto);
            return ResponseEntity.ok(resultado);
        } catch (RuntimeException e) {
            log.error("Error al calcular precio con tarifa: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error interno al calcular precio con tarifa", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Servicio de cotizaciones funcionando correctamente");
    }
}
