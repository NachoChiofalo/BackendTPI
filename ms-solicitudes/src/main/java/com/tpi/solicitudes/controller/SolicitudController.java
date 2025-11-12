package com.tpi.solicitudes.controller;

import com.tpi.solicitudes.service.SolicitudService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador de ejemplo para probar la funcionalidad de ruteo OSRM.
 * Este endpoint simula la actualización de una Solicitud existente con
 * la distancia y duración calculadas.
 */
@RestController
@RequestMapping("/api/v1/solicitudes")
public class SolicitudController {

    @Autowired
    private SolicitudService solicitudService;

    /**
     * Endpoint de prueba para ejecutar el cálculo de ruta OSRM.
     * * @param solicitudId El ID de la Solicitud a actualizar (ej: 1).
     * @return El objeto Solicitud actualizado con los campos de ruteo.
     */
    @PostMapping("/{solicitudId}/calcular-ruta")
    public ResponseEntity<?> calcularRuta(@PathVariable Long solicitudId) {
        try {
            // Llama al servicio que contiene la lógica de Feign Client
            Object solicitudActualizada = solicitudService.calcularDistanciaYTiempoEstimado(solicitudId);
            
            // Retorna la solicitud actualizada con distancia, tiempo y costo
            return ResponseEntity.ok(solicitudActualizada);

        } catch (RuntimeException e) {
            // Manejo simple de errores (ej: solicitud no encontrada o error de OSRM)
            return ResponseEntity.badRequest().body("Error al calcular ruta: " + e.getMessage());
        }
    }
}