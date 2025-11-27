package com.tpi.solicitudes.service;

import com.tpi.solicitudes.dto.SolicitudCrearDto;
import com.tpi.solicitudes.entity.Cliente;
import com.tpi.solicitudes.entity.Contenedor;
import com.tpi.solicitudes.entity.Solicitud;
import com.tpi.solicitudes.repository.SolicitudRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class SolicitudService {

    private final SolicitudRepository solicitudRepository;
    private final ClienteService clienteService;
    private final ContenedorService contenedorService;

    public List<Solicitud> obtenerTodos() {
        log.info("Obteniendo todas las solicitudes");
        return solicitudRepository.findAll();
    }

    public Optional<Solicitud> obtenerPorId(Integer id) {
        log.info("Obteniendo solicitud por id: {}", id);
        return solicitudRepository.findById(id);
    }

    @Transactional
    public Solicitud crear(Solicitud solicitud) {
        log.info("Creando nueva solicitud");
        return solicitudRepository.save(solicitud);
    }

    /**
     * Crea una solicitud a partir del DTO que contiene datos de cliente y contenedor.
     * - Crea cliente si no existe
     * - Crea contenedor si no existe
     * - Fija estado inicial BORRADOR = 1
     */
    @Transactional
    public Solicitud crearConDetalles(SolicitudCrearDto dto) {
        log.info("crearConDetalles: creando solicitud con cliente y contenedor si es necesario");

        // 1) Validaciones básicas
        if (dto == null) throw new IllegalArgumentException("Solicitud vacía");
        if (dto.getCliente() == null) throw new IllegalArgumentException("Cliente es requerido");
        if (dto.getContenedor() == null) throw new IllegalArgumentException("Contenedor es requerido");

        // 2) Cliente: crear si no existe
        SolicitudCrearDto.ClienteInfo ci = dto.getCliente();
        Cliente clienteEntidad = Cliente.builder()
                .tipoDocClienteId(ci.getTipoDocumento())
                .numDocCliente(ci.getNumDocumento())
                .nombres(ci.getNombres() != null ? ci.getNombres() : "SIN_NOMBRE")
                .apellidos(ci.getApellidos() != null ? ci.getApellidos() : "SIN_APELLIDO")
                .domicilio(ci.getDomicilio() != null ? ci.getDomicilio() : "")
                .telefono(ci.getTelefono() != null ? ci.getTelefono() : "")
                .build();

        Cliente clientePersistido = clienteService.guardarSiNoExiste(
                clienteEntidad.getTipoDocClienteId(), clienteEntidad.getNumDocCliente(), clienteEntidad);

        // 3) Contenedor: crear si no existe
        SolicitudCrearDto.ContenedorInfo contInfo = dto.getContenedor();
        Contenedor contEntidad = new Contenedor();
        if (contInfo.getIdContenedor() != null) contEntidad.setIdContenedor(contInfo.getIdContenedor());
        if (contInfo.getPeso() != null) contEntidad.setPesoKg(contInfo.getPeso());
        if (contInfo.getVolumen() != null) contEntidad.setVolumenM3(contInfo.getVolumen());
        if (contInfo.getIdEstadoContenedor() != null) contEntidad.setIdEstadoContenedor(contInfo.getIdEstadoContenedor());

        Contenedor contPersistido = contenedorService.guardarSiNoExiste(contInfo.getIdContenedor(), contEntidad);

        // 4) Crear la entidad Solicitud y poblarla mínimamente. Necesitamos generar un id para la solicitud (si no hay @GeneratedValue)
        // Aquí asumimos que la tabla no tiene generación automática; usar el max id + 1 como heurística.
        Integer newSolicitudId = generateNewSolicitudId();

        Solicitud solicitud = Solicitud.builder()
                .solicitudId(newSolicitudId)
                .tipoDocCliente(clientePersistido.getTipoDocClienteId())
                .numDocCliente(clientePersistido.getNumDocCliente())
                .estadoSolicitud(1) // 1 = BORRADOR
                .idContenedor(contPersistido.getIdContenedor())
                .idRuta(dto.getIdRuta())
                .idUbicacionOrigen(dto.getIdUbicacionOrigen())
                .idUbicacionDestino(dto.getIdUbicacionDestino())
                .costoEstimado(null)
                .costoReal(null)
                .fechaHoraInicio(null)
                .fechaHoraEstimadaFin(null)
                .fechaHoraFin(null)
                .textoAdicional(dto.getObservaciones())
                .build();

        Solicitud guardada = solicitudRepository.save(solicitud);
        log.info("Solicitud creada con id {} asociada al cliente {}-{} y contenedor {}",
                guardada.getSolicitudId(), guardada.getTipoDocCliente(), guardada.getNumDocCliente(), guardada.getIdContenedor());
        return guardada;
    }

    private Integer generateNewSolicitudId() {
        // Intento simple: buscar max id entre las solicitudes actuales
        Optional<Solicitud> top = solicitudRepository.findAll().stream()
                .max(Comparator.comparing(Solicitud::getSolicitudId));
        return top.map(s -> s.getSolicitudId() + 1).orElse(1);
    }

    public List<Solicitud> obtenerPorCliente(Integer tipoDoc, Long numDoc) {
        log.info("Obteniendo solicitudes del cliente: {} - {}", tipoDoc, numDoc);
        return solicitudRepository.findByTipoDocClienteAndNumDocCliente(tipoDoc, numDoc);
    }

    public List<Solicitud> obtenerPendientes() {
        log.info("Obteniendo solicitudes pendientes");
        // Estados: 1=borrador, 2=programada, 3=en tránsito, 4=entregada
        // Pendientes son las que no están entregadas (estado != 4)
        return solicitudRepository.findByEstadoSolicitudNot(4);
    }

    public List<Solicitud> obtenerPorUbicacionDestino(Integer ubicacionId) {
        log.info("Obteniendo solicitudes con destino en ubicación: {}", ubicacionId);
        return solicitudRepository.findByIdUbicacionDestino(ubicacionId);
    }

    @Transactional
    public Solicitud asignarRuta(Integer solicitudId, Integer rutaId) {
        log.info("Asignando ruta {} a solicitud {}", rutaId, solicitudId);
        return solicitudRepository.findById(solicitudId)
                .map(solicitud -> {
                    solicitud.setIdRuta(rutaId);
                    return solicitudRepository.save(solicitud);
                })
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada con id: " + solicitudId));
    }

    /**
     * Finaliza la solicitud y registra el tiempo real y costo real.
     * Implementación mínima y síncrona: llama a ms-localizaciones para obtener coords y distancia
     * y a ms-precios para calcular el precio. No crea archivos nuevos.
     */
    @Transactional
    public Solicitud finalizarYRegistrarCalculos(Integer solicitudId) {
        log.info("Finalizando solicitud {} y registrando cálculos reales (modo simple)", solicitudId);

        Solicitud solicitud = solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada: " + solicitudId));

        // Obtener contenedor para peso/volumen
        Contenedor contenedor = contenedorService.obtenerPorId(solicitud.getIdContenedor())
                .orElseThrow(() -> new RuntimeException("Contenedor no encontrado: " + solicitud.getIdContenedor()));

        // RestTemplate local con timeouts cortos
        SimpleClientHttpRequestFactory rf = new SimpleClientHttpRequestFactory();
        rf.setConnectTimeout(5000);
        rf.setReadTimeout(10000);
        RestTemplate rt = new RestTemplate(rf);

        // URLs ajustadas a la configuración de docker-compose
        String msLocalBase = "http://ms-localizaciones:8087";
        String msPreciosBase = "http://ms-precios:8083";

        try {
            // 1) Intentar obtener coordenadas y distancia desde ms-localizaciones (opcional)
            Double kilometros = null;
            String duracionTexto = null;
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> origen = rt.getForObject(msLocalBase + "/api/ubicaciones/{id}", Map.class, solicitud.getIdUbicacionOrigen());
                @SuppressWarnings("unchecked")
                Map<String, Object> destino = rt.getForObject(msLocalBase + "/api/ubicaciones/{id}", Map.class, solicitud.getIdUbicacionDestino());

                if (origen != null && destino != null) {
                    String origenCoords = Objects.toString(origen.get("longitud"), "") + "," + Objects.toString(origen.get("latitud"), "");
                    String destinoCoords = Objects.toString(destino.get("longitud"), "") + "," + Objects.toString(destino.get("latitud"), "");

                    @SuppressWarnings("unchecked")
                    Map<String, Object> distanciaResp = rt.getForObject(msLocalBase + "/api/distancia?origen={o}&destino={d}", Map.class, origenCoords, destinoCoords);

                    if (distanciaResp != null) {
                        Object kmObj = distanciaResp.get("kilometros");
                        if (kmObj instanceof Number) kilometros = ((Number) kmObj).doubleValue();
                        else if (kmObj != null) kilometros = Double.parseDouble(String.valueOf(kmObj));
                        duracionTexto = distanciaResp.get("duracionTexto") != null ? String.valueOf(distanciaResp.get("duracionTexto")) : null;
                    }
                } else {
                    log.warn("ms-localizaciones devolvió ubicaciones nulas para ids {} -> {}", solicitud.getIdUbicacionOrigen(), solicitud.getIdUbicacionDestino());
                }
            } catch (ResourceAccessException rae) {
                // Fallback: ms-localizaciones no disponible (Connection refused), seguir con ms-precios
                log.warn("ms-localizaciones no disponible (fallback): {}. Se continuará llamando a ms-precios para cálculo estimado", rae.getMessage());
            }

             // 3) Llamar a ms-precios para calcular el costo real usando /api/cotizaciones/calcular
             Map<String, Object> payload = new HashMap<>();
             payload.put("ubicacionOrigenId", solicitud.getIdUbicacionOrigen());
             payload.put("ubicacionDestinoId", solicitud.getIdUbicacionDestino());
             payload.put("pesoKg", contenedor.getPesoKg());
             payload.put("volumenM3", contenedor.getVolumenM3());
             payload.put("tipoServicio", null);
             payload.put("esUrgente", false);
             payload.put("observaciones", solicitud.getTextoAdicional());
             payload.put("tipoDocCliente", solicitud.getTipoDocCliente());
             payload.put("numDocCliente", solicitud.getNumDocCliente());

             HttpHeaders headers = new HttpHeaders();
             headers.setContentType(MediaType.APPLICATION_JSON);
             HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(payload, headers);

             @SuppressWarnings("unchecked")
             Map<String, Object> precioResp = rt.postForObject(msPreciosBase + "/api/cotizaciones/calcular", requestEntity, Map.class);

             BigDecimal precioFinal = null;
             if (precioResp != null && precioResp.get("precioFinal") != null) {
                 precioFinal = new BigDecimal(String.valueOf(precioResp.get("precioFinal")));
             }

             // 4) Guardar en la entidad solicitud: costoReal y marcar fechas (uso LocalDateTime ahora)
             if (precioFinal != null) {
                 solicitud.setCostoReal(precioFinal);
             }

             // Fecha inicio si no estaba y fecha fin ahora
             LocalDateTime ahora = LocalDateTime.now();
             if (solicitud.getFechaHoraInicio() == null) solicitud.setFechaHoraInicio(ahora);
             solicitud.setFechaHoraFin(ahora);

             // Actualizar estado a entregada (4)
             solicitud.setEstadoSolicitud(4);

             Solicitud guardada = solicitudRepository.save(solicitud);

             log.info("Solicitud {} actualizada: costoReal={} km={} duracion={}", solicitudId, precioFinal, kilometros, duracionTexto);
             return guardada;

         } catch (Exception e) {
             log.error("Error finalizando solicitud {}: {}", solicitudId, e.getMessage(), e);
             throw new RuntimeException("No se pudo finalizar y registrar cálculos: " + e.getMessage(), e);
         }
    }

    // Obtener el estado actual de un contenedor para un cliente (una sola solicitud más reciente)
    public Optional<Solicitud> obtenerEstadoContenedorParaCliente(Integer idContenedor, Integer tipoDoc, Long numDoc) {
        log.info("Obteniendo estado del contenedor {} para cliente {} - {}", idContenedor, tipoDoc, numDoc);
        return solicitudRepository.findTopByIdContenedorAndTipoDocClienteAndNumDocClienteOrderByFechaHoraInicioDesc(
                idContenedor, tipoDoc, numDoc);
    }

    // Modificado: obtener todos los contenedores relacionados a un cliente con su solicitud más reciente
    public List<Solicitud> obtenerContenedoresPorClienteConEstado(Integer tipoDoc, Long numDoc) {
        log.info("Obteniendo contenedores y estado más reciente para cliente {} - {}", tipoDoc, numDoc);
        // Usar el método del repositorio que ya devuelve ordenado por fechaHoraInicio desc
        List<Solicitud> solicitudes = solicitudRepository.findByTipoDocClienteAndNumDocClienteOrderByFechaHoraInicioDesc(tipoDoc, numDoc);

        solicitudes.sort((a, b) -> {
            if (a.getFechaHoraInicio() == null && b.getFechaHoraInicio() == null) return 0;
            if (a.getFechaHoraInicio() == null) return 1;
            if (b.getFechaHoraInicio() == null) return -1;
            return b.getFechaHoraInicio().compareTo(a.getFechaHoraInicio()); // desc
        });

        // Mantener un map por idContenedor con la primera (más reciente) aparición, guardando la Solicitud completa
        Map<Integer, Solicitud> latestByContenedor = new LinkedHashMap<>();
        for (Solicitud s : solicitudes) {
            Integer idCont = s.getIdContenedor();
            if (!latestByContenedor.containsKey(idCont)) {
                latestByContenedor.put(idCont, s);
            }
        }

        return new ArrayList<>(latestByContenedor.values());
    }
}
