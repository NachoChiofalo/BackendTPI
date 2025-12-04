package com.tpi.solicitudes.controller;

import com.tpi.solicitudes.service.EstadoTransicionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller para recibir notificaciones de cambios de estado desde el microservicio de rutas
 */
@RestController
@RequestMapping("/api/solicitudes/notificaciones")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class NotificacionEstadoController {

    private final EstadoTransicionService estadoTransicionService;

    /**
     * Recibe notificación cuando se inicia un tramo
     * Actualiza estados de solicitud y contenedor
     */
    @PostMapping("/tramo-iniciado")
    public ResponseEntity<Map<String, String>> notificarTramoIniciado(@RequestBody Map<String, Integer> request) {
        log.info("POST /api/solicitudes/notificaciones/tramo-iniciado - Recibida notificación");

        Integer rutaId = request.get("rutaId");
        Integer tramoId = request.get("tramoId");

        if (rutaId == null) {
            log.error("rutaId es requerido en la notificación de tramo iniciado");
            return ResponseEntity.badRequest().body(Map.of("error", "rutaId es requerido"));
        }

        try {
            estadoTransicionService.manejarInicioTramo(rutaId);
            log.info("Estados actualizados exitosamente para inicio de tramo {} en ruta {}", tramoId, rutaId);
            return ResponseEntity.ok(Map.of("status", "Estados actualizados exitosamente"));
        } catch (Exception e) {
            log.error("Error al actualizar estados para inicio de tramo: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("error", "Error al actualizar estados"));
        }
    }

    /**
     * Recibe notificación cuando se finaliza un tramo
     * Verifica si la solicitud debe finalizarse
     */
    @PostMapping("/tramo-finalizado")
    public ResponseEntity<Map<String, String>> notificarTramoFinalizado(@RequestBody Map<String, Integer> request) {
        log.info("POST /api/solicitudes/notificaciones/tramo-finalizado - Recibida notificación");

        Integer rutaId = request.get("rutaId");
        Integer tramoId = request.get("tramoId");

        if (rutaId == null) {
            log.error("rutaId es requerido en la notificación de tramo finalizado");
            return ResponseEntity.badRequest().body(Map.of("error", "rutaId es requerido"));
        }

        try {
            estadoTransicionService.verificarYFinalizarSolicitud(rutaId);
            log.info("Estados verificados exitosamente para finalización de tramo {} en ruta {}", tramoId, rutaId);
            return ResponseEntity.ok(Map.of("status", "Estados verificados exitosamente"));
        } catch (Exception e) {
            log.error("Error al verificar estados para finalización de tramo: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("error", "Error al verificar estados"));
        }
    }
}
