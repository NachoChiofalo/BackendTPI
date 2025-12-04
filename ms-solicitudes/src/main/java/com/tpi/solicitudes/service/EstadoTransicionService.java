package com.tpi.solicitudes.service;

import com.tpi.solicitudes.entity.Contenedor;
import com.tpi.solicitudes.entity.Solicitud;
import com.tpi.solicitudes.repository.ContenedorRepository;
import com.tpi.solicitudes.repository.SolicitudRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Servicio especializado en manejar las transiciones de estados
 * entre solicitudes y contenedores basado en eventos de tramos
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EstadoTransicionService {

    private final SolicitudRepository solicitudRepository;
    private final ContenedorRepository contenedorRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${rutas.service.url:http://localhost:8085}")
    private String rutasServiceUrl;

    /*
     * Estados de Solicitud:
     * 1 = "Creada"
     * 2 = "En Proceso"
     * 3 = "Finalizada"
     *
     * Estados de Contenedor:
     * 1 = "Creado"
     * 2 = "En viaje"
     * 3 = "Entregado"
     */

    /**
     * Maneja la transición cuando se inicia un tramo
     * - Solicitud pasa a "En Proceso"
     * - Contenedor pasa a "En Viaje"
     */
    @Transactional
    public void manejarInicioTramo(Integer rutaId) {
        log.info("=== INICIO manejarInicioTramo para ruta: {} ===", rutaId);

        // Buscar solicitudes asociadas a la ruta
        List<Solicitud> solicitudes = solicitudRepository.findByIdRuta(rutaId);
        log.info("Encontradas {} solicitudes para la ruta {}", solicitudes.size(), rutaId);

        if (solicitudes.isEmpty()) {
            log.warn("⚠️ No se encontraron solicitudes asociadas a la ruta {}. Verificar que la solicitud tenga asignada esta ruta.", rutaId);
            return;
        }

        for (Solicitud solicitud : solicitudes) {
            log.info("Procesando solicitud {} con estado actual: {} y contenedor: {}",
                solicitud.getSolicitudId(), solicitud.getEstadoSolicitud(), solicitud.getIdContenedor());

            // Solo actualizar si no está ya en proceso o finalizada
            if (solicitud.getEstadoSolicitud() == 1) { // Si está en "Creada"
                log.info("✓ Solicitud {} está en estado Creada, procediendo a actualizar...", solicitud.getSolicitudId());

                // Actualizar solicitud a "En Proceso"
                solicitud.setEstadoSolicitud(2);
                Solicitud solicitudGuardada = solicitudRepository.save(solicitud);
                log.info("✓ Solicitud {} actualizada a estado 'En Proceso' (estado guardado: {})",
                    solicitud.getSolicitudId(), solicitudGuardada.getEstadoSolicitud());

                // Actualizar contenedor a "En Viaje"
                log.info("→ Iniciando actualización del contenedor {} a estado 2 (En viaje)", solicitud.getIdContenedor());
                actualizarEstadoContenedor(solicitud.getIdContenedor(), 2);
                log.info("✓ Actualización del contenedor {} completada", solicitud.getIdContenedor());
            } else {
                log.info("⊘ Solicitud {} ya está en estado {}, no se actualiza", solicitud.getSolicitudId(), solicitud.getEstadoSolicitud());
            }
        }

        log.info("=== FIN manejarInicioTramo para ruta: {} ===", rutaId);
    }

    /**
     * Maneja la transición cuando se finaliza una solicitud
     * Verifica que todos los tramos estén finalizados antes de finalizar la solicitud
     */
    @Transactional
    public void verificarYFinalizarSolicitud(Integer rutaId) {
        log.info("=== INICIO verificarYFinalizarSolicitud para ruta: {} ===", rutaId);

        // Buscar solicitudes asociadas a la ruta
        List<Solicitud> solicitudes = solicitudRepository.findByIdRuta(rutaId);
        log.info("Encontradas {} solicitudes para la ruta {}", solicitudes.size(), rutaId);

        if (solicitudes.isEmpty()) {
            log.warn("⚠️ No se encontraron solicitudes asociadas a la ruta {}", rutaId);
            return;
        }

        for (Solicitud solicitud : solicitudes) {
            log.info("Procesando solicitud {} con estado actual: {} y contenedor: {}",
                solicitud.getSolicitudId(), solicitud.getEstadoSolicitud(), solicitud.getIdContenedor());

            // Solo procesar si está en proceso
            if (solicitud.getEstadoSolicitud() == 2) { // Si está "En Proceso"
                log.info("✓ Solicitud {} está en estado 'En Proceso', verificando tramos...", solicitud.getSolicitudId());

                // Verificar si todos los tramos de la ruta están finalizados
                if (todosLosTramosDeLaRutaFinalizados(rutaId)) {
                    log.info("✓ Todos los tramos de la ruta {} están finalizados", rutaId);

                    // Finalizar solicitud
                    solicitud.setEstadoSolicitud(3); // "Finalizada"
                    solicitud.setFechaHoraFin(java.time.LocalDateTime.now());
                    Solicitud solicitudGuardada = solicitudRepository.save(solicitud);
                    log.info("✓ Solicitud {} actualizada a estado 'Finalizada' (estado guardado: {})",
                        solicitud.getSolicitudId(), solicitudGuardada.getEstadoSolicitud());

                    // Actualizar contenedor a "Entregado"
                    log.info("→ Iniciando actualización del contenedor {} a estado 3 (Entregado)", solicitud.getIdContenedor());
                    actualizarEstadoContenedor(solicitud.getIdContenedor(), 3);
                    log.info("✓ Actualización del contenedor {} completada", solicitud.getIdContenedor());
                } else {
                    log.info("⊘ No todos los tramos de la ruta {} están finalizados aún", rutaId);
                }
            } else {
                log.info("⊘ Solicitud {} está en estado {}, no se procesa para finalización",
                    solicitud.getSolicitudId(), solicitud.getEstadoSolicitud());
            }
        }

        log.info("=== FIN verificarYFinalizarSolicitud para ruta: {} ===", rutaId);
    }

    /**
     * Actualiza el estado de un contenedor específico
     * Usa la misma transacción del método que lo llama
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void actualizarEstadoContenedor(Integer idContenedor, Integer nuevoEstado) {
        log.info("→→ actualizarEstadoContenedor: Intentando actualizar contenedor {} a estado {}", idContenedor, nuevoEstado);

        Optional<Contenedor> contenedorOpt = contenedorRepository.findById(idContenedor);
        if (contenedorOpt.isPresent()) {
            Contenedor contenedor = contenedorOpt.get();
            Integer estadoAnterior = contenedor.getIdEstadoContenedor();
            log.info("→→ Contenedor {} encontrado - Estado anterior: {}, Estado nuevo: {}", idContenedor, estadoAnterior, nuevoEstado);

            contenedor.setIdEstadoContenedor(nuevoEstado);
            Contenedor contenedorGuardado = contenedorRepository.save(contenedor);
            contenedorRepository.flush(); // Forzar la escritura inmediata a la base de datos

            log.info("→→ Contenedor GUARDADO con ID: {} y estado: {}", contenedorGuardado.getIdContenedor(), contenedorGuardado.getIdEstadoContenedor());

            // Verificar que se guardó correctamente
            Optional<Contenedor> verificacion = contenedorRepository.findById(idContenedor);
            if (verificacion.isPresent()) {
                log.info("✓✓ VERIFICACIÓN: Contenedor {} tiene estado {} en la base de datos",
                    idContenedor, verificacion.get().getIdEstadoContenedor());
            } else {
                log.error("✗✗ ERROR: No se pudo verificar el contenedor después de guardar");
            }

            String nombreEstado = obtenerNombreEstadoContenedor(nuevoEstado);
            log.info("→→ Contenedor {} actualizado exitosamente a estado: {}", idContenedor, nombreEstado);
        } else {
            log.error("✗✗ ERROR: Contenedor no encontrado con ID: {}. Verificar que el contenedor existe en la base de datos.", idContenedor);
        }
    }

    /**
     * Verifica si todos los tramos de una ruta están finalizados
     * Hace una llamada HTTP al microservicio de rutas
     */
    private boolean todosLosTramosDeLaRutaFinalizados(Integer rutaId) {
        try {
            String url = rutasServiceUrl + "/api/tramos/ruta/" + rutaId + "/finalizados";
            log.info("→→ Verificando tramos finalizados en URL: {}", url);

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> resultado = response.getBody();

                Boolean todosFinalizados = (Boolean) resultado.get("todosFinalizados");
                Integer totalTramos = (Integer) resultado.get("totalTramos");
                Integer tramosFinalizados = (Integer) resultado.get("tramosFinalizados");

                log.info("→→ Ruta {}: {} de {} tramos finalizados, todos finalizados: {}",
                    rutaId, tramosFinalizados, totalTramos, todosFinalizados);

                return todosFinalizados != null && todosFinalizados;
            } else {
                log.error("✗✗ Error al verificar tramos de la ruta {}: status {}", rutaId, response.getStatusCode());
                return false;
            }
        } catch (Exception e) {
            log.error("✗✗ Error al verificar tramos para ruta {}: {}", rutaId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Obtiene el nombre descriptivo del estado del contenedor
     */
    private String obtenerNombreEstadoContenedor(Integer estado) {
        return switch (estado) {
            case 1 -> "Creado";
            case 2 -> "En viaje";
            case 3 -> "Entregado";
            default -> "Desconocido";
        };
    }
}
