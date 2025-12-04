package com.tpi.rutas.service;

import com.tpi.rutas.dto.CamionDTO;
import com.tpi.rutas.dto.ContenedorDTO;
import com.tpi.rutas.dto.SolicitudDTO;
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

import java.math.BigDecimal;
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

                    // NUEVA VALIDACIÓN: Verificar capacidad si el tramo tiene rutaId
                    if (tramo.getRutaId() != null) {
                        try {
                            log.info("🔍 Iniciando validación de capacidad del camión {} para el tramo {} de la ruta {}",
                                    dominio.trim(), tramoId, tramo.getRutaId());

                            // Obtener información del camión
                            CamionDTO camion = obtenerInformacionCamion(dominio.trim());

                            // Obtener la solicitud asociada a la ruta
                            SolicitudDTO solicitud = obtenerSolicitudPorRuta(tramo.getRutaId());

                            // Obtener información del contenedor
                            ContenedorDTO contenedor = obtenerInformacionContenedor(solicitud.getIdContenedor());

                            // Validar capacidades
                            validarCapacidadCamionContenedor(camion, contenedor, tramo.getRutaId(), tramoId);
                        } catch (Exception e) {
                            log.warn("⚠️ No se pudo validar la capacidad para el tramo {} (método legacy): {}", tramoId, e.getMessage());
                            // En el método legacy, registramos el warning pero no impedimos la asignación
                            // para mantener compatibilidad hacia atrás
                        }
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

                    // NUEVA VALIDACIÓN: Verificar que el camión puede transportar el contenedor
                    log.info("🔍 Iniciando validación de capacidad del camión {} para la ruta {}", dominio.trim(), rutaId);

                    // Obtener información del camión
                    CamionDTO camion = obtenerInformacionCamion(dominio.trim());

                    // Obtener la solicitud asociada a la ruta
                    SolicitudDTO solicitud = obtenerSolicitudPorRuta(rutaId);

                    // Obtener información del contenedor
                    ContenedorDTO contenedor = obtenerInformacionContenedor(solicitud.getIdContenedor());

                    // Validar capacidades
                    validarCapacidadCamionContenedor(camion, contenedor, rutaId, tramoId);

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
     * Obtiene la información de un camión desde el microservicio de flotas
     */
    private CamionDTO obtenerInformacionCamion(String dominio) {
        try {
            String url = flotasServiceUrl + "/api/camiones/" + dominio;
            log.info("Obteniendo información del camión {} desde URL: {}", dominio, url);

            // Usar Map para obtener la respuesta como JSON genérico y luego mapearlo
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.info("Información del camión {} obtenida exitosamente", dominio);
                Map<String, Object> camionData = response.getBody();

                // Mapear manualmente los datos al DTO
                CamionDTO camionDTO = CamionDTO.builder()
                    .dominio((String) camionData.get("dominio"))
                    .disponible((Boolean) camionData.get("disponible"))
                    .capacidadPeso(new BigDecimal(camionData.get("capacidadPeso").toString()))
                    .capacidadVolumen(new BigDecimal(camionData.get("capacidadVolumen").toString()))
                    .costoBaseKm(new BigDecimal(camionData.get("costoBaseKm").toString()))
                    .consumoPromedio(new BigDecimal(camionData.get("consumoPromedio").toString()))
                    .build();

                return camionDTO;
            } else {
                log.error("Error al obtener información del camión {}: Status {}", dominio, response.getStatusCode());
                throw new TramoValidationException("CAMION_NO_ENCONTRADO",
                    "No se pudo obtener la información del camión con dominio: " + dominio);
            }
        } catch (Exception e) {
            log.error("Error al comunicarse con el servicio de flotas para el camión {}: {}", dominio, e.getMessage());
            throw new TramoValidationException("ERROR_SERVICIO_FLOTAS",
                "No se pudo comunicar con el servicio de flotas para obtener la información del camión: " + dominio);
        }
    }

    /**
     * Obtiene la solicitud asociada a una ruta desde el microservicio de solicitudes
     */
    private SolicitudDTO obtenerSolicitudPorRuta(Integer rutaId) {
        try {
            String url = solicitudesServiceUrl + "/api/solicitudes/por-ruta/" + rutaId;
            log.info("Obteniendo solicitud de la ruta {} desde URL: {}", rutaId, url);

            // Usar Map para obtener la respuesta como JSON genérico y luego mapearlo
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.info("Solicitud de la ruta {} obtenida exitosamente", rutaId);
                Map<String, Object> solicitudData = response.getBody();

                // Mapear manualmente los datos al DTO
                SolicitudDTO solicitudDTO = SolicitudDTO.builder()
                    .solicitudId((Integer) solicitudData.get("solicitudId"))
                    .idContenedor((Integer) solicitudData.get("idContenedor"))
                    .idRuta((Integer) solicitudData.get("idRuta"))
                    .tipoDocCliente((Integer) solicitudData.get("tipoDocCliente"))
                    .numDocCliente(Long.valueOf(solicitudData.get("numDocCliente").toString()))
                    .estadoSolicitud((Integer) solicitudData.get("estadoSolicitud"))
                    .idUbicacionOrigen((Integer) solicitudData.get("idUbicacionOrigen"))
                    .idUbicacionDestino((Integer) solicitudData.get("idUbicacionDestino"))
                    .build();

                return solicitudDTO;
            } else {
                log.error("Error al obtener solicitud de la ruta {}: Status {}", rutaId, response.getStatusCode());
                throw new TramoValidationException("SOLICITUD_NO_ENCONTRADA",
                    "No se encontró una solicitud asociada a la ruta: " + rutaId);
            }
        } catch (Exception e) {
            log.error("Error al comunicarse con el servicio de solicitudes para la ruta {}: {}", rutaId, e.getMessage());
            throw new TramoValidationException("ERROR_SERVICIO_SOLICITUDES",
                "No se pudo comunicar con el servicio de solicitudes para obtener la solicitud de la ruta: " + rutaId);
        }
    }

    /**
     * Obtiene la información de un contenedor desde el microservicio de solicitudes
     */
    private ContenedorDTO obtenerInformacionContenedor(Integer idContenedor) {
        try {
            String url = solicitudesServiceUrl + "/api/contenedores/interno/" + idContenedor;
            log.info("Obteniendo información del contenedor {} desde URL: {}", idContenedor, url);

            // Usar Map para obtener la respuesta como JSON genérico y luego mapearlo
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.info("Información del contenedor {} obtenida exitosamente", idContenedor);
                Map<String, Object> contenedorData = response.getBody();

                // Mapear manualmente los datos al DTO
                ContenedorDTO contenedorDTO = ContenedorDTO.builder()
                    .idContenedor((Integer) contenedorData.get("idContenedor"))
                    .pesoKg(new BigDecimal(contenedorData.get("pesoKg").toString()))
                    .volumenM3(new BigDecimal(contenedorData.get("volumenM3").toString()))
                    .idEstadoContenedor((Integer) contenedorData.get("idEstadoContenedor"))
                    .build();

                return contenedorDTO;
            } else {
                log.error("Error al obtener información del contenedor {}: Status {}", idContenedor, response.getStatusCode());
                throw new TramoValidationException("CONTENEDOR_NO_ENCONTRADO",
                    "No se pudo obtener la información del contenedor con ID: " + idContenedor);
            }
        } catch (Exception e) {
            log.error("Error al comunicarse con el servicio de solicitudes para el contenedor {}: {}", idContenedor, e.getMessage());
            throw new TramoValidationException("ERROR_SERVICIO_SOLICITUDES",
                "No se pudo comunicar con el servicio de solicitudes para obtener la información del contenedor: " + idContenedor);
        }
    }

    /**
     * Valida que el contenedor no supere las capacidades del camión
     */
    private void validarCapacidadCamionContenedor(CamionDTO camion, ContenedorDTO contenedor, Integer rutaId, Integer tramoId) {
        log.info("Validando capacidad del camión {} para el contenedor {} en tramo {} de ruta {}",
                camion.getDominio(), contenedor.getIdContenedor(), tramoId, rutaId);

        // Validar peso
        if (contenedor.getPesoKg().compareTo(camion.getCapacidadPeso()) > 0) {
            String mensaje = String.format(
                "El contenedor %d excede la capacidad de peso del camión %s. Peso del contenedor: %.2f kg, Capacidad del camión: %.2f kg",
                contenedor.getIdContenedor(),
                camion.getDominio(),
                contenedor.getPesoKg(),
                camion.getCapacidadPeso()
            );
            log.error(mensaje);
            throw new TramoValidationException("CAPACIDAD_PESO_EXCEDIDA", mensaje);
        }

        // Validar volumen
        if (contenedor.getVolumenM3().compareTo(camion.getCapacidadVolumen()) > 0) {
            String mensaje = String.format(
                "El contenedor %d excede la capacidad de volumen del camión %s. Volumen del contenedor: %.2f m³, Capacidad del camión: %.2f m³",
                contenedor.getIdContenedor(),
                camion.getDominio(),
                contenedor.getVolumenM3(),
                camion.getCapacidadVolumen()
            );
            log.error(mensaje);
            throw new TramoValidationException("CAPACIDAD_VOLUMEN_EXCEDIDA", mensaje);
        }

        log.info("✅ Validación de capacidad exitosa. Camión {} puede transportar el contenedor {}",
                camion.getDominio(), contenedor.getIdContenedor());
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
