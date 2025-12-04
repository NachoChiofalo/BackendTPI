package com.tpi.rutas.service;

import com.tpi.rutas.entity.Tramo;
import com.tpi.rutas.exception.TramoValidationException;
import com.tpi.rutas.repository.TramoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class TramoService {

    private final TramoRepository tramoRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${solicitudes.service.url:http://localhost:8084}")
    private String solicitudesServiceUrl;

    @Value("${flotas.service.url:http://localhost:8082}")
    private String flotasServiceUrl;

    public List<Tramo> obtenerTodos() {
        log.info("Obteniendo todos los tramos");
        return tramoRepository.findAll();
    }

    public Optional<Tramo> obtenerPorId(Integer id) {
        log.info("Obteniendo tramo por id: {}", id);
        return tramoRepository.findById(id);
    }

    public List<Tramo> obtenerPorRuta(Integer rutaId) {
        log.info("Obteniendo tramos por ruta: {}", rutaId);
        return tramoRepository.findByRutaId(rutaId);
    }

    @Transactional
    public Tramo guardar(Tramo tramo) {
        log.info("Guardando tramo: {}", tramo.getTramoId());
        return tramoRepository.save(tramo);
    }

    @Transactional
    public Tramo actualizar(Integer id, Tramo actualizado) {
        log.info("Actualizando tramo: {}", id);
        return tramoRepository.findById(id)
                .map(t -> {
                    t.setRutaId(actualizado.getRutaId());
                    t.setTipoTramoId(actualizado.getTipoTramoId());
                    t.setDominio(actualizado.getDominio());
                    t.setUbicacionOrigenId(actualizado.getUbicacionOrigenId());
                    t.setTransportistaId(actualizado.getTransportistaId());
                    t.setUbicacionDestinoId(actualizado.getUbicacionDestinoId());
                    t.setCostoAproximado(actualizado.getCostoAproximado());
                    t.setCostoReal(actualizado.getCostoReal());
                    t.setFechaHoraInicio(actualizado.getFechaHoraInicio());
                    t.setFechaHoraFin(actualizado.getFechaHoraFin());
                    t.setFechaHoraEstimadaFin(actualizado.getFechaHoraEstimadaFin());
                    return tramoRepository.save(t);
                })
                .orElseThrow(() -> new RuntimeException("Tramo no encontrado con id: " + id));
    }

    @Transactional
    public void eliminar(Integer id) {
        log.info("Eliminando tramo: {}", id);
        if (tramoRepository.existsById(id)) {
            tramoRepository.deleteById(id);
        } else {
            throw new RuntimeException("Tramo no encontrado con id: " + id);
        }
    }

    /**
     * REGLA DE NEGOCIO 1: Un camión no puede transportar contenedores que superen su peso o volumen máximo
     * Esta validación debe hacerse antes de asignar el camión al tramo
     * El controller debe validar que el camión tenga capacidad suficiente antes de llamar a este método
     */
    @Transactional
    public Tramo asignarCamion(Integer tramoId, String dominio) {
        log.info("Asignando camión {} al tramo {}", dominio, tramoId);
        return tramoRepository.findById(tramoId)
                .map(tramo -> {
                    // Validación: no debe estar ya iniciado
                    if (tramo.getFechaHoraInicio() != null) {
                        throw new TramoValidationException("TRAMO_YA_INICIADO",
                            "No se puede asignar camión al tramo " + tramoId + " porque ya está iniciado");
                    }

                    // Validación: dominio no debe estar vacío
                    if (dominio == null || dominio.trim().isEmpty()) {
                        throw new TramoValidationException("DOMINIO_INVALIDO",
                            "El dominio del camión no puede estar vacío");
                    }

                    tramo.setDominio(dominio.trim());
                    Tramo tramoGuardado = tramoRepository.save(tramo);

                    // Cambiar disponibilidad del camión a false
                    cambiarDisponibilidadCamion(dominio.trim(), false, "asignado al tramo " + tramoId);

                    return tramoGuardado;
                })
                .orElseThrow(() -> new RuntimeException("Tramo no encontrado con id: " + tramoId));
    }

    // Nuevo método: asignar con validación explícita de rutaId + tramoId
    @Transactional
    public Tramo asignarCamion(Integer rutaId, Integer tramoId, String dominio) {
        log.info("Asignando camión {} al tramo {} de la ruta {}", dominio, tramoId, rutaId);
        return tramoRepository.findByTramoIdAndRutaId(tramoId, rutaId)
                .map(tramo -> {
                    // Validación: no debe estar ya iniciado
                    if (tramo.getFechaHoraInicio() != null) {
                        throw new TramoValidationException("TRAMO_YA_INICIADO",
                            "No se puede asignar camión al tramo " + tramoId + " porque ya está iniciado");
                    }

                    // Validación: dominio no debe estar vacío
                    if (dominio == null || dominio.trim().isEmpty()) {
                        throw new TramoValidationException("DOMINIO_INVALIDO",
                            "El dominio del camión no puede estar vacío");
                    }

                    tramo.setDominio(dominio.trim());
                    Tramo tramoGuardado = tramoRepository.save(tramo);
                    log.info("✅ Tramo {} guardado con dominio: {}", tramoId, dominio.trim());

                    // Cambiar disponibilidad del camión a false
                    log.info("🚛 Iniciando cambio de disponibilidad del camión {} a false", dominio.trim());
                    cambiarDisponibilidadCamion(dominio.trim(), false, "asignado al tramo " + tramoId + " de la ruta " + rutaId);

                    return tramoGuardado;
                })
                .orElseThrow(() -> new RuntimeException("Tramo no encontrado con id: " + tramoId + " para la ruta: " + rutaId));
    }

    /**
     * NUEVO REQUERIMIENTO: Asignar transportista a un tramo
     * - Valida que el tramo no esté ya iniciado
     * - Permite asignar un transportista específico por tramo
     */
    @Transactional
    public Tramo asignarTransportista(Integer rutaId, Integer tramoId, Integer transportistaId) {
        log.info("Asignando transportista {} al tramo {} de la ruta {}", transportistaId, tramoId, rutaId);
        return tramoRepository.findByTramoIdAndRutaId(tramoId, rutaId)
                .map(tramo -> {
                    // Validación: no debe estar ya iniciado
                    if (tramo.getFechaHoraInicio() != null) {
                        throw new TramoValidationException("TRAMO_YA_INICIADO",
                            "No se puede asignar transportista al tramo " + tramoId + " porque ya está iniciado");
                    }

                    // Validación: transportista ID no debe ser nulo
                    if (transportistaId == null || transportistaId <= 0) {
                        throw new TramoValidationException("TRANSPORTISTA_INVALIDO",
                            "El ID del transportista debe ser un número válido mayor a 0");
                    }

                    tramo.setTransportistaId(transportistaId);
                    return tramoRepository.save(tramo);
                })
                .orElseThrow(() -> new RuntimeException("Tramo no encontrado con id: " + tramoId + " para la ruta: " + rutaId));
    }

    /**
     * REGLA DE NEGOCIO 7 (parte 1): Determinar el inicio de un tramo
     * Los tramos deben registrar fechas estimadas y reales para calcular el desempeño del servicio
     * VALIDACIÓN: Para poder iniciar un tramo, primero debe estar asignado un camión Y un transportista
     */
    @Transactional
    public Tramo iniciarTramo(Integer rutaId, Integer tramoId) {
        log.info("Iniciando tramo: {} de la ruta: {}", tramoId, rutaId);
        return tramoRepository.findByTramoIdAndRutaId(tramoId, rutaId)
                .map(tramo -> {
                    // Validación: debe tener camión asignado
                    if (tramo.getDominio() == null || tramo.getDominio().isEmpty()) {
                        throw new TramoValidationException("CAMION_NO_ASIGNADO",
                            "No se puede iniciar el tramo " + tramoId + " porque no tiene un camión asignado");
                    }

                    // Validación: debe tener transportista asignado
                    if (tramo.getTransportistaId() == null) {
                        throw new TramoValidationException("TRANSPORTISTA_NO_ASIGNADO",
                            "No se puede iniciar el tramo " + tramoId + " porque no tiene un transportista asignado");
                    }

                    // Validación: no debe estar ya iniciado
                    if (tramo.getFechaHoraInicio() != null) {
                        throw new TramoValidationException("TRAMO_YA_INICIADO",
                            "El tramo " + tramoId + " ya está iniciado desde " + tramo.getFechaHoraInicio());
                    }

                    tramo.setFechaHoraInicio(java.time.LocalDateTime.now());

                    // Calcular fecha estimada de fin de manera aleatoria (1-5 días)
                    int diasAleatorios = (int) (Math.random() * 5) + 1;
                    tramo.setFechaHoraEstimadaFin(java.time.LocalDateTime.now().plusDays(diasAleatorios));

                    Tramo tramoGuardado = tramoRepository.save(tramo);

                    // Notificar al microservicio de solicitudes sobre el inicio del tramo
                    notificarInicioTramo(rutaId, tramoId);

                    return tramoGuardado;
                })
                .orElseThrow(() -> new RuntimeException("Tramo no encontrado con id: " + tramoId + " para la ruta: " + rutaId));
    }

    // Compatibilidad: método antiguo por tramoId sólo (delegará buscando ruta implícita)
    @Transactional
    public Tramo iniciarTramo(Integer tramoId) {
        log.info("Iniciando tramo (sin validar ruta): {}", tramoId);
        return tramoRepository.findById(tramoId)
                .map(tramo -> {
                    // Validación: debe tener camión asignado
                    if (tramo.getDominio() == null || tramo.getDominio().isEmpty()) {
                        throw new TramoValidationException("CAMION_NO_ASIGNADO",
                            "No se puede iniciar el tramo " + tramoId + " porque no tiene un camión asignado");
                    }

                    // Validación: debe tener transportista asignado
                    if (tramo.getTransportistaId() == null) {
                        throw new TramoValidationException("TRANSPORTISTA_NO_ASIGNADO",
                            "No se puede iniciar el tramo " + tramoId + " porque no tiene un transportista asignado");
                    }

                    // Validación: no debe estar ya iniciado
                    if (tramo.getFechaHoraInicio() != null) {
                        throw new TramoValidationException("TRAMO_YA_INICIADO",
                            "El tramo " + tramoId + " ya está iniciado desde " + tramo.getFechaHoraInicio());
                    }

                    tramo.setFechaHoraInicio(java.time.LocalDateTime.now());

                    // Calcular fecha estimada de fin de manera aleatoria (1-5 días)
                    int diasAleatorios = (int) (Math.random() * 5) + 1;
                    tramo.setFechaHoraEstimadaFin(java.time.LocalDateTime.now().plusDays(diasAleatorios));

                    return tramoRepository.save(tramo);
                })
                .orElseThrow(() -> new RuntimeException("Tramo no encontrado con id: " + tramoId));
    }

    /**
     * REGLA DE NEGOCIO 7 (parte 2): Determinar el fin de un tramo
     * Los tramos deben registrar fechas reales para:
     * - Calcular el desempeño del servicio (comparar fecha real vs estimada)
     * - Calcular estadía en depósitos (diferencia entre fecha fin de un tramo y fecha inicio del siguiente)
     * - Determinar costos reales de transporte
     * VALIDACIÓN: Para poder finalizar un tramo, este primero debe estar iniciado
     */
    @Transactional
    public Tramo finalizarTramo(Integer rutaId, Integer tramoId) {
        log.info("Finalizando tramo: {} de la ruta: {}", tramoId, rutaId);
        return tramoRepository.findByTramoIdAndRutaId(tramoId, rutaId)
                .map(tramo -> {
                    // Validación: debe estar iniciado
                    if (tramo.getFechaHoraInicio() == null) {
                        throw new TramoValidationException("TRAMO_NO_INICIADO",
                            "No se puede finalizar el tramo " + tramoId + " porque no está iniciado");
                    }

                    // Validación: no debe estar ya finalizado
                    if (tramo.getFechaHoraFin() != null) {
                        throw new TramoValidationException("TRAMO_YA_FINALIZADO",
                            "El tramo " + tramoId + " ya está finalizado desde " + tramo.getFechaHoraFin());
                    }

                    tramo.setFechaHoraFin(java.time.LocalDateTime.now());
                    Tramo tramoGuardado = tramoRepository.save(tramo);

                    // Liberar el camión (cambiar disponibilidad a true)
                    if (tramo.getDominio() != null && !tramo.getDominio().isEmpty()) {
                        cambiarDisponibilidadCamion(tramo.getDominio(), true, "tramo " + tramoId + " finalizado");
                    }

                    // Notificar al microservicio de solicitudes sobre la finalización del tramo
                    notificarFinalizacionTramo(rutaId, tramoId);

                    return tramoGuardado;
                })
                .orElseThrow(() -> new RuntimeException("Tramo no encontrado con id: " + tramoId + " para la ruta: " + rutaId));
    }

    // Compatibilidad: método antiguo por tramoId sólo
    @Transactional
    public Tramo finalizarTramo(Integer tramoId) {
        log.info("Finalizando tramo (sin validar ruta): {}", tramoId);
        return tramoRepository.findById(tramoId)
                .map(tramo -> {
                    // Validación: debe estar iniciado
                    if (tramo.getFechaHoraInicio() == null) {
                        throw new TramoValidationException("TRAMO_NO_INICIADO",
                            "No se puede finalizar el tramo " + tramoId + " porque no está iniciado");
                    }

                    // Validación: no debe estar ya finalizado
                    if (tramo.getFechaHoraFin() != null) {
                        throw new TramoValidationException("TRAMO_YA_FINALIZADO",
                            "El tramo " + tramoId + " ya está finalizado desde " + tramo.getFechaHoraFin());
                    }

                    tramo.setFechaHoraFin(java.time.LocalDateTime.now());
                    Tramo tramoGuardado = tramoRepository.save(tramo);

                    // Liberar el camión (cambiar disponibilidad a true)
                    if (tramo.getDominio() != null && !tramo.getDominio().isEmpty()) {
                        cambiarDisponibilidadCamion(tramo.getDominio(), true, "tramo " + tramoId + " finalizado");
                    }

                    return tramoGuardado;
                })
                .orElseThrow(() -> new RuntimeException("Tramo no encontrado con id: " + tramoId));
    }

    public List<Tramo> obtenerPorTransportista(Integer transportistaId) {
        log.info("Obteniendo tramos del transportista: {}", transportistaId);
        return tramoRepository.findByTransportistaId(transportistaId);
    }

    /**
     * Notifica al microservicio de solicitudes sobre el inicio de un tramo
     */
    private void notificarInicioTramo(Integer rutaId, Integer tramoId) {
        try {
            String url = solicitudesServiceUrl + "/api/solicitudes/notificaciones/tramo-iniciado";
            Map<String, Integer> payload = new HashMap<>();
            payload.put("rutaId", rutaId);
            payload.put("tramoId", tramoId);

            log.info("Enviando notificación de inicio de tramo a URL: {} con payload: {}", url, payload);

            ResponseEntity<Map> response = restTemplate.postForEntity(url, payload, Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Notificación de inicio de tramo enviada exitosamente para tramo {} ruta {}. Respuesta: {}",
                        tramoId, rutaId, response.getBody());
            } else {
                log.warn("Error al enviar notificación de inicio de tramo. Status: {}, Body: {}",
                        response.getStatusCode(), response.getBody());
            }
        } catch (Exception e) {
            log.error("Error al notificar inicio de tramo {} ruta {}: {}", tramoId, rutaId, e.getMessage(), e);
            // No interrumpir el flujo principal si falla la notificación
        }
    }

    /**
     * Notifica al microservicio de solicitudes sobre la finalización de un tramo
     */
    private void notificarFinalizacionTramo(Integer rutaId, Integer tramoId) {
        try {
            String url = solicitudesServiceUrl + "/api/solicitudes/notificaciones/tramo-finalizado";
            Map<String, Integer> payload = new HashMap<>();
            payload.put("rutaId", rutaId);
            payload.put("tramoId", tramoId);

            ResponseEntity<Map> response = restTemplate.postForEntity(url, payload, Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Notificación de finalización de tramo enviada exitosamente para tramo {} ruta {}", tramoId, rutaId);
            } else {
                log.warn("Error al enviar notificación de finalización de tramo: {}", response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Error al notificar finalización de tramo {} ruta {}: {}", tramoId, rutaId, e.getMessage());
            // No interrumpir el flujo principal si falla la notificación
        }
    }

    /**
     * Cambia la disponibilidad de un camión en el microservicio de flotas
     */
    private void cambiarDisponibilidadCamion(String dominio, Boolean disponible, String motivo) {
        log.info("=== INICIANDO cambiarDisponibilidadCamion ===");
        log.info("Dominio: {}, Disponible: {}, Motivo: {}", dominio, disponible, motivo);
        log.info("URL del servicio de flotas configurada: {}", flotasServiceUrl);

        try {
            String url = flotasServiceUrl + "/api/camiones/" + dominio + "/disponibilidad?disponible=" + disponible;
            log.info("URL completa a llamar: {}", url);

            ResponseEntity<Map> response = restTemplate.postForEntity(url, null, Map.class);
            log.info("Respuesta recibida - Status: {}, Body: {}", response.getStatusCode(), response.getBody());

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("✅ Disponibilidad del camión {} actualizada exitosamente a {}", dominio, disponible);
            } else {
                log.warn("⚠️ Error al cambiar disponibilidad del camión {} - Status: {}", dominio, response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("❌ Error al cambiar disponibilidad del camión {}: {}", dominio, e.getMessage(), e);
            // No interrumpir el flujo principal si falla la actualización de disponibilidad
        }

        log.info("=== FIN cambiarDisponibilidadCamion ===");
    }
}
