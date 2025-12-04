package com.tpi.rutas.controller;

import com.tpi.rutas.dto.ErrorResponseDTO;
import com.tpi.rutas.dto.TramoResponseDTO;
import com.tpi.rutas.entity.Tramo;
import com.tpi.rutas.exception.TramoValidationException;
import com.tpi.rutas.mapper.TramoMapper;
import com.tpi.rutas.service.TramoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tramos")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class TramoController {

    private final TramoService tramoService;
    private final TramoMapper tramoMapper;

    @GetMapping
    public ResponseEntity<List<TramoResponseDTO>> obtenerTodos() {
        log.info("GET /api/tramos - Obteniendo todos los tramos");
        List<Tramo> tramos = tramoService.obtenerTodos();
        return ResponseEntity.ok(tramoMapper.toResponseDTOList(tramos));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TramoResponseDTO> obtenerPorId(@PathVariable("id") Integer id) {
        log.info("GET /api/tramos/{} - Obtener por id", id);
        return tramoService.obtenerPorId(id)
                .map(tramo -> ResponseEntity.ok(tramoMapper.toResponseDTO(tramo)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/ruta/{rutaId}")
    public ResponseEntity<List<TramoResponseDTO>> obtenerPorRuta(@PathVariable("rutaId") Integer rutaId) {
        log.info("GET /api/tramos/ruta/{} - Obtener por ruta", rutaId);
        List<Tramo> tramos = tramoService.obtenerPorRuta(rutaId);
        return ResponseEntity.ok(tramoMapper.toResponseDTOList(tramos));
    }

    /**
     * REQUERIMIENTO FUNCIONAL 7 (parte 1): Consultar tramos asignados (Transportista)
     * - Permite al transportista ver todos los tramos que tiene asignados
     */
    @PreAuthorize("hasRole('transportista')")
    @GetMapping("/transportista/{transportistaId}")
    public ResponseEntity<List<TramoResponseDTO>> obtenerPorTransportista(@PathVariable("transportistaId") Integer transportistaId) {
        log.info("GET /api/tramos/transportista/{} - Obtener tramos del transportista", transportistaId);
        List<Tramo> tramos = tramoService.obtenerPorTransportista(transportistaId);
        return ResponseEntity.ok(tramoMapper.toResponseDTOList(tramos));
    }

    @PreAuthorize("hasRole('transportista')")
    @PostMapping
    public ResponseEntity<Tramo> crear(@Valid @RequestBody Tramo tramo) {
        log.info("POST /api/tramos - Creando tramo ID: {}", tramo.getTramoId());
        try {
            Tramo guardado = tramoService.guardar(tramo);
            return ResponseEntity.status(HttpStatus.CREATED).body(guardado);
        } catch (Exception e) {
            log.error("Error al crear tramo: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Endpoint interno para uso entre microservicios - Sin autenticación
     * Permite a otros microservicios crear tramos automáticamente
     */
    @PostMapping("/interno")
    public ResponseEntity<Tramo> crearInterno(@RequestBody Tramo tramo) {
        log.info("POST /api/tramos/interno - Creando tramo ID: {} (llamada interna)", tramo.getTramoId());
        try {
            Tramo guardado = tramoService.guardar(tramo);
            return ResponseEntity.status(HttpStatus.CREATED).body(guardado);
        } catch (Exception e) {
            log.error("Error al crear tramo: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PreAuthorize("hasRole('transportista')")
    @PutMapping("/{id}")
    public ResponseEntity<Tramo> actualizar(@PathVariable("id") Integer id, @Valid @RequestBody Tramo tramo) {
        log.info("PUT /api/tramos/{} - Actualizando", id);
        try {
            Tramo actualizado = tramoService.actualizar(id, tramo);
            return ResponseEntity.ok(actualizado);
        } catch (RuntimeException e) {
            log.error("Error al actualizar tramo {}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * REQUERIMIENTO FUNCIONAL 6: Asignar camión a un tramo de traslado de un contenedor (Operador)
     * - Asigna un camión específico (por dominio) a un tramo de la ruta
     * - Valida disponibilidad y capacidad del camión
     */
    @PreAuthorize("hasRole('operador')")
    @PutMapping("/rutas/{rutaId}/tramos/{tramoId}/asignar-camion")
    public ResponseEntity<?> asignarCamion(
            @PathVariable("rutaId") Integer rutaId,
            @PathVariable("tramoId") Integer tramoId,
            @RequestBody Map<String, String> request) {
        log.info("PUT /api/tramos/rutas/{}/tramos/{}/asignar-camion - Asignando camión", rutaId, tramoId);
        try {
            String dominio = request.get("dominio");
            if (dominio == null || dominio.isEmpty()) {
                ErrorResponseDTO error = ErrorResponseDTO.builder()
                        .error("DOMINIO_REQUERIDO")
                        .mensaje("El dominio del camión es requerido")
                        .codigo("VALIDATION_ERROR")
                        .timestamp(LocalDateTime.now())
                        .path("/api/tramos/rutas/" + rutaId + "/tramos/" + tramoId + "/asignar-camion")
                        .build();
                return ResponseEntity.badRequest().body(error);
            }
            Tramo actualizado = tramoService.asignarCamion(rutaId, tramoId, dominio);
            TramoResponseDTO responseDTO = tramoMapper.toResponseDTO(actualizado);
            return ResponseEntity.ok(responseDTO);
        } catch (TramoValidationException e) {
            log.error("Error de validación al asignar camión: {}", e.getMessage());
            ErrorResponseDTO error = ErrorResponseDTO.builder()
                    .error(e.getCodigo())
                    .mensaje(e.getMessage())
                    .codigo("VALIDATION_ERROR")
                    .timestamp(LocalDateTime.now())
                    .path("/api/tramos/rutas/" + rutaId + "/tramos/" + tramoId + "/asignar-camion")
                    .build();
            return ResponseEntity.badRequest().body(error);
        } catch (RuntimeException e) {
            log.error("Error al asignar camión al tramo {} de la ruta {}: {}", tramoId, rutaId, e.getMessage());
            ErrorResponseDTO error = ErrorResponseDTO.builder()
                    .error("TRAMO_NO_ENCONTRADO")
                    .mensaje("No se encontró el tramo " + tramoId + " en la ruta " + rutaId)
                    .codigo("NOT_FOUND")
                    .timestamp(LocalDateTime.now())
                    .path("/api/tramos/rutas/" + rutaId + "/tramos/" + tramoId + "/asignar-camion")
                    .build();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    // Endpoint antiguo por compatibilidad: mantiene la funcionalidad previa (sin validar ruta)
    @Deprecated
    @PreAuthorize("hasRole('operador')")
    @PutMapping("/{tramoId}/asignar-camion")
    public ResponseEntity<?> asignarCamionCompat(
            @PathVariable("tramoId") Integer tramoId,
            @RequestBody Map<String, String> request) {
        log.info("PUT /api/tramos/{}/asignar-camion (compat) - Asignando camión sin validar ruta", tramoId);
        try {
            String dominio = request.get("dominio");
            if (dominio == null || dominio.isEmpty()) {
                ErrorResponseDTO error = ErrorResponseDTO.builder()
                        .error("DOMINIO_REQUERIDO")
                        .mensaje("El dominio del camión es requerido")
                        .codigo("VALIDATION_ERROR")
                        .timestamp(LocalDateTime.now())
                        .path("/api/tramos/" + tramoId + "/asignar-camion")
                        .build();
                return ResponseEntity.badRequest().body(error);
            }
            Tramo actualizado = tramoService.asignarCamion(tramoId, dominio);
            TramoResponseDTO responseDTO = tramoMapper.toResponseDTO(actualizado);
            return ResponseEntity.ok(responseDTO);
        } catch (TramoValidationException e) {
            log.error("Error de validación al asignar camión: {}", e.getMessage());
            ErrorResponseDTO error = ErrorResponseDTO.builder()
                    .error(e.getCodigo())
                    .mensaje(e.getMessage())
                    .codigo("VALIDATION_ERROR")
                    .timestamp(LocalDateTime.now())
                    .path("/api/tramos/" + tramoId + "/asignar-camion")
                    .build();
            return ResponseEntity.badRequest().body(error);
        } catch (RuntimeException e) {
            log.error("Error al asignar camión al tramo {}: {}", tramoId, e.getMessage());
            ErrorResponseDTO error = ErrorResponseDTO.builder()
                    .error("TRAMO_NO_ENCONTRADO")
                    .mensaje("No se encontró el tramo " + tramoId)
                    .codigo("NOT_FOUND")
                    .timestamp(LocalDateTime.now())
                    .path("/api/tramos/" + tramoId + "/asignar-camion")
                    .build();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    /**
     * NUEVO REQUERIMIENTO: Asignar transportista a un tramo (Operador)
     * - Permite asignar un transportista específico a un tramo
     * - Valida que el tramo no esté ya iniciado
     */
    @PreAuthorize("hasRole('operador')")
    @PutMapping("/rutas/{rutaId}/tramos/{tramoId}/asignar-transportista")
    public ResponseEntity<?> asignarTransportista(
            @PathVariable("rutaId") Integer rutaId,
            @PathVariable("tramoId") Integer tramoId,
            @RequestBody Map<String, Integer> request) {
        log.info("PUT /api/tramos/rutas/{}/tramos/{}/asignar-transportista - Asignando transportista", rutaId, tramoId);
        try {
            Integer transportistaId = request.get("transportistaId");
            if (transportistaId == null) {
                ErrorResponseDTO error = ErrorResponseDTO.builder()
                        .error("TRANSPORTISTA_REQUERIDO")
                        .mensaje("El ID del transportista es requerido")
                        .codigo("VALIDATION_ERROR")
                        .timestamp(LocalDateTime.now())
                        .path("/api/tramos/rutas/" + rutaId + "/tramos/" + tramoId + "/asignar-transportista")
                        .build();
                return ResponseEntity.badRequest().body(error);
            }
            Tramo actualizado = tramoService.asignarTransportista(rutaId, tramoId, transportistaId);
            TramoResponseDTO responseDTO = tramoMapper.toResponseDTO(actualizado);
            return ResponseEntity.ok(responseDTO);
        } catch (TramoValidationException e) {
            log.error("Error de validación al asignar transportista: {}", e.getMessage());
            ErrorResponseDTO error = ErrorResponseDTO.builder()
                    .error(e.getCodigo())
                    .mensaje(e.getMessage())
                    .codigo("VALIDATION_ERROR")
                    .timestamp(LocalDateTime.now())
                    .path("/api/tramos/rutas/" + rutaId + "/tramos/" + tramoId + "/asignar-transportista")
                    .build();
            return ResponseEntity.badRequest().body(error);
        } catch (RuntimeException e) {
            log.error("Error al asignar transportista al tramo {} de la ruta {}: {}", tramoId, rutaId, e.getMessage());
            ErrorResponseDTO error = ErrorResponseDTO.builder()
                    .error("TRAMO_NO_ENCONTRADO")
                    .mensaje("No se encontró el tramo " + tramoId + " en la ruta " + rutaId)
                    .codigo("NOT_FOUND")
                    .timestamp(LocalDateTime.now())
                    .path("/api/tramos/rutas/" + rutaId + "/tramos/" + tramoId + "/asignar-transportista")
                    .build();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    /**
     * REQUERIMIENTO FUNCIONAL 7 (parte 2): Determinar el inicio de un tramo de traslado (Transportista)
     * - Registra la fecha/hora de inicio del tramo
     * - Marca el comienzo del transporte del contenedor en este tramo
     */
    @PreAuthorize("hasRole('transportista')")
    @PostMapping("/rutas/{rutaId}/tramos/{tramoId}/iniciar")
    public ResponseEntity<?> iniciarTramo(@PathVariable("rutaId") Integer rutaId, @PathVariable("tramoId") Integer tramoId) {
        log.info("POST /api/tramos/rutas/{}/tramos/{}/iniciar - Iniciando tramo", rutaId, tramoId);
        try {
            Tramo actualizado = tramoService.iniciarTramo(rutaId, tramoId);
            TramoResponseDTO responseDTO = tramoMapper.toResponseDTO(actualizado);
            return ResponseEntity.ok(responseDTO);
        } catch (TramoValidationException e) {
            log.error("Error de validación al iniciar tramo: {}", e.getMessage());
            ErrorResponseDTO error = ErrorResponseDTO.builder()
                    .error(e.getCodigo())
                    .mensaje(e.getMessage())
                    .codigo("VALIDATION_ERROR")
                    .timestamp(LocalDateTime.now())
                    .path("/api/tramos/rutas/" + rutaId + "/tramos/" + tramoId + "/iniciar")
                    .build();
            return ResponseEntity.badRequest().body(error);
        } catch (RuntimeException e) {
            log.error("Error al iniciar tramo {} de ruta {}: {}", tramoId, rutaId, e.getMessage());
            ErrorResponseDTO error = ErrorResponseDTO.builder()
                    .error("TRAMO_NO_ENCONTRADO")
                    .mensaje("No se encontró el tramo " + tramoId + " en la ruta " + rutaId)
                    .codigo("NOT_FOUND")
                    .timestamp(LocalDateTime.now())
                    .path("/api/tramos/rutas/" + rutaId + "/tramos/" + tramoId + "/iniciar")
                    .build();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    /**
     * REQUERIMIENTO FUNCIONAL 7 (parte 3): Determinar el fin de un tramo de traslado (Transportista)
     * - Registra la fecha/hora de finalización del tramo
     * - Permite calcular tiempo real y costos de estadía en depósitos
     */
    @PreAuthorize("hasRole('transportista')")
    @PostMapping("/rutas/{rutaId}/tramos/{tramoId}/finalizar")
    public ResponseEntity<?> finalizarTramo(@PathVariable("rutaId") Integer rutaId, @PathVariable("tramoId") Integer tramoId) {
        log.info("POST /api/tramos/rutas/{}/tramos/{}/finalizar - Finalizando tramo", rutaId, tramoId);
        try {
            Tramo actualizado = tramoService.finalizarTramo(rutaId, tramoId);
            TramoResponseDTO responseDTO = tramoMapper.toResponseDTO(actualizado);
            return ResponseEntity.ok(responseDTO);
        } catch (TramoValidationException e) {
            log.error("Error de validación al finalizar tramo: {}", e.getMessage());
            ErrorResponseDTO error = ErrorResponseDTO.builder()
                    .error(e.getCodigo())
                    .mensaje(e.getMessage())
                    .codigo("VALIDATION_ERROR")
                    .timestamp(LocalDateTime.now())
                    .path("/api/tramos/rutas/" + rutaId + "/tramos/" + tramoId + "/finalizar")
                    .build();
            return ResponseEntity.badRequest().body(error);
        } catch (RuntimeException e) {
            log.error("Error al finalizar tramo {} de ruta {}: {}", tramoId, rutaId, e.getMessage());
            ErrorResponseDTO error = ErrorResponseDTO.builder()
                    .error("TRAMO_NO_ENCONTRADO")
                    .mensaje("No se encontró el tramo " + tramoId + " en la ruta " + rutaId)
                    .codigo("NOT_FOUND")
                    .timestamp(LocalDateTime.now())
                    .path("/api/tramos/rutas/" + rutaId + "/tramos/" + tramoId + "/finalizar")
                    .build();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }


    @PreAuthorize("hasRole('operador')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable("id") Integer id) {
        log.info("DELETE /api/tramos/{} - Eliminando", id);
        try {
            tramoService.eliminar(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            log.error("Error al eliminar tramo {}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Endpoint para verificar si todos los tramos de una ruta están finalizados
     * Usado por el microservicio de solicitudes para determinar si una solicitud debe finalizarse
     */
    @GetMapping("/ruta/{rutaId}/finalizados")
    public ResponseEntity<Map<String, Object>> verificarTramosFinalizados(@PathVariable("rutaId") Integer rutaId) {
        log.info("GET /api/tramos/ruta/{}/finalizados - Verificando estado de tramos", rutaId);

        List<Tramo> tramos = tramoService.obtenerPorRuta(rutaId);

        if (tramos.isEmpty()) {
            Map<String, Object> response = Map.of(
                "rutaId", rutaId,
                "totalTramos", 0,
                "tramosFinalizados", 0,
                "todosFinalizados", false,
                "mensaje", "No se encontraron tramos para la ruta"
            );
            return ResponseEntity.ok(response);
        }

        long tramosFinalizados = tramos.stream()
            .mapToLong(t -> t.getFechaHoraFin() != null ? 1 : 0)
            .sum();

        boolean todosFinalizados = tramosFinalizados == tramos.size();

        Map<String, Object> response = Map.of(
            "rutaId", rutaId,
            "totalTramos", tramos.size(),
            "tramosFinalizados", tramosFinalizados,
            "todosFinalizados", todosFinalizados
        );

        return ResponseEntity.ok(response);
    }
}