package com.tpi.solicitudes.service;

import com.tpi.solicitudes.dto.ContenedorDetalladoDto;
import com.tpi.solicitudes.entity.Contenedor;
import com.tpi.solicitudes.entity.Solicitud;
import com.tpi.solicitudes.repository.ContenedorRepository;
import com.tpi.solicitudes.repository.SolicitudRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ContenedorService {

    private final ContenedorRepository contenedorRepository;
    private final SolicitudRepository solicitudRepository;

    public List<Contenedor> obtenerTodos() {
        log.info("Obteniendo todos los contenedores");
        return contenedorRepository.findAllWithEstado();
    }

    public Optional<Contenedor> obtenerPorId(Integer id) {
        log.info("Obteniendo contenedor por id: {}", id);
        return contenedorRepository.findByIdWithEstado(id);
    }

    @Transactional
    public Contenedor guardar(Contenedor contenedor) {
        log.info("Guardando contenedor: {}", contenedor.getIdContenedor());
        return contenedorRepository.save(contenedor);
    }

    @Transactional
    public Contenedor actualizar(Integer id, Contenedor actualizado) {
        log.info("Actualizando contenedor: {}", id);
        return contenedorRepository.findById(id)
                .map(c -> {
                    c.setPesoKg(actualizado.getPesoKg());
                    c.setVolumenM3(actualizado.getVolumenM3());
                    c.setIdEstadoContenedor(actualizado.getIdEstadoContenedor());
                    return contenedorRepository.save(c);
                })
                .orElseThrow(() -> new RuntimeException("Contenedor no encontrado con id: " + id));
    }

    @Transactional
    public void eliminar(Integer id) {
        log.info("Eliminando contenedor: {}", id);
        if (contenedorRepository.existsById(id)) {
            contenedorRepository.deleteById(id);
        } else {
            throw new RuntimeException("Contenedor no encontrado con id: " + id);
        }
    }

    /**
     * Retorna todos los contenedores que están asociados a solicitudes no entregadas.
     * No recibe parámetros: URL -> GET /api/contenedores/pendientes
     * Usa estado excluido = 5 (Entregada) según el script de inicialización.
     */
    public List<Contenedor> obtenerPendientes() {
        log.info("Obteniendo contenedores pendientes (solicitudes con estado != 3)");
        // 3 = Finalizada según 01-init-database_Version3.sql
        List<Solicitud> solicitudesPendientes = solicitudRepository.findByEstadoSolicitudNot(3);

        // Extraer ids únicos de contenedor en orden de aparición
        LinkedHashSet<Integer> ids = solicitudesPendientes.stream()
                .map(Solicitud::getIdContenedor)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (ids.isEmpty()) {
            return Collections.emptyList();
        }

        List<Contenedor> contenedores = contenedorRepository.findAllById(ids);
        // Cargar la información del estado para cada contenedor
        contenedores = contenedores.stream()
                .map(c -> contenedorRepository.findByIdWithEstado(c.getIdContenedor()).orElse(c))
                .collect(Collectors.toList());

        // Mantener orden según aparición en las solicitudes (opcional)
        Map<Integer, Integer> order = new HashMap<>();
        int i = 0;
        for (Integer id : ids) order.put(id, i++);
        contenedores.sort(Comparator.comparingInt(c -> order.getOrDefault(c.getIdContenedor(), Integer.MAX_VALUE)));

        return contenedores;
    }

    /**
     * Variante que devuelve DTOs con el nombre del estado poblado.
     * @deprecated Use obtenerTodos() instead which returns full DTOs via the controller
     */
    @Deprecated
    public List<com.tpi.solicitudes.dto.ContenedorDto> obtenerPendientesConEstado() {
        List<Contenedor> contenedores = obtenerPendientes();
        return contenedores.stream()
                .map(c -> com.tpi.solicitudes.dto.ContenedorDto.builder()
                        .idContenedor(c.getIdContenedor())
                        .idEstadoContenedor(c.getIdEstadoContenedor())
                        .nombreEstado(c.getEstadoContenedor() != null ? c.getEstadoContenedor().getNombre() : "Desconocido")
                        .volumenM3(c.getVolumenM3())
                        .pesoKg(c.getPesoKg())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public Contenedor guardarSiNoExiste(Integer idContenedor, Contenedor contenedorACrear) {
        log.info("guardarSiNoExiste contenedor idOpt={}", idContenedor);
        if (idContenedor != null) {
            Optional<Contenedor> existente = contenedorRepository.findById(idContenedor);
            if (existente.isPresent()) return existente.get();
            // si no existe, crearlo usando id proporcionado
        }

        // Si no se provee id o no existía, crear nuevo contenedor.
        if (contenedorACrear == null) {
            throw new RuntimeException("Datos insuficientes para crear contenedor");
        }

        // Si id no fue provisto, generar uno consultando el max id actual (riesgo de race condition)
        if (contenedorACrear.getIdContenedor() == null) {
            Optional<Contenedor> top = contenedorRepository.findTopByOrderByIdContenedorDesc();
            int newId = top.map(c -> c.getIdContenedor() + 1).orElse(1);
            contenedorACrear.setIdContenedor(newId);
        }

        if (contenedorACrear.getIdEstadoContenedor() == null) {
            contenedorACrear.setIdEstadoContenedor(1); // 1 = Disponible por defecto
        }

        if (contenedorACrear.getPesoKg() == null || contenedorACrear.getVolumenM3() == null) {
            throw new RuntimeException("Peso y volumen del contenedor son obligatorios para creación");
        }

        return contenedorRepository.save(contenedorACrear);
    }

    public List<Contenedor> obtenerPorDocumentoCliente(Long numDocCliente) {
        log.info("Obteniendo contenedores para cliente con documento: {}", numDocCliente);
        return contenedorRepository.findContenedoresByNumDocCliente(numDocCliente);
    }

    public Optional<Solicitud> obtenerSolicitudPorContenedor(Integer idContenedor) {
        log.info("Obteniendo solicitud para contenedor: {}", idContenedor);
        return solicitudRepository.findByIdContenedor(idContenedor);
    }

    /**
     * Obtiene información completa del contenedor incluyendo solicitud y tramos
     */
    public ContenedorDetalladoDto obtenerContenedorDetallado(Integer idContenedor, Long numDocCliente) {
        log.info("Obteniendo información detallada del contenedor {} para cliente {}", idContenedor, numDocCliente);

        // Obtener contenedor
        Contenedor contenedor = contenedorRepository.findById(idContenedor)
                .orElseThrow(() -> new RuntimeException("Contenedor no encontrado: " + idContenedor));

        // Obtener solicitud asociada
        Optional<Solicitud> solicitudOpt = obtenerSolicitudPorContenedor(idContenedor);
        if (!solicitudOpt.isPresent()) {
            throw new RuntimeException("No se encontró solicitud para el contenedor: " + idContenedor);
        }

        Solicitud solicitud = solicitudOpt.get();

        // Verificar que el cliente tenga acceso a este contenedor
        if (!solicitud.getNumDocCliente().equals(numDocCliente)) {
            throw new RuntimeException("El cliente no tiene acceso a este contenedor");
        }

        // Obtener estado del contenedor
        String nombreEstadoContenedor = obtenerNombreEstadoContenedor(contenedor.getIdEstadoContenedor());

        // Obtener estado de la solicitud
        String nombreEstadoSolicitud = obtenerNombreEstadoSolicitud(solicitud.getEstadoSolicitud());

        // Obtener tramos de la ruta (llamada a ms-rutas)
        List<ContenedorDetalladoDto.TramoDto> tramos = obtenerTramosPorRuta(solicitud.getIdRuta());

        return ContenedorDetalladoDto.builder()
                .idContenedor(contenedor.getIdContenedor())
                .idEstadoContenedor(contenedor.getIdEstadoContenedor())
                .nombreEstado(nombreEstadoContenedor)
                .volumenM3(contenedor.getVolumenM3())
                .pesoKg(contenedor.getPesoKg())
                .tipoDocCliente(solicitud.getTipoDocCliente())
                .numDocCliente(solicitud.getNumDocCliente())
                .solicitudId(solicitud.getSolicitudId())
                .estadoSolicitud(solicitud.getEstadoSolicitud())
                .nombreEstadoSolicitud(nombreEstadoSolicitud)
                .tramos(tramos)
                .build();
    }

    private String obtenerNombreEstadoContenedor(Integer estadoContenedor) {
        switch (estadoContenedor) {
            case 1: return "Creado";
            case 2: return "En Transporte";
            case 3: return "Entregado";
            default: return "Desconocido";
        }
    }

    private String obtenerNombreEstadoSolicitud(Integer estadoSolicitud) {
        switch (estadoSolicitud) {
            case 1: return "Creada";
            case 2: return "En proceso";
            case 3: return "Finalizada";
            default: return "Desconocido";
        }
    }

    private List<ContenedorDetalladoDto.TramoDto> obtenerTramosPorRuta(Integer rutaId) {
        log.info("Obteniendo tramos para ruta: {}", rutaId);

        try {
            RestTemplate rt = new RestTemplate();
            String msRutasBase = System.getenv().getOrDefault("MS_RUTAS_URL", "http://localhost:8085");
            String url = msRutasBase + "/api/tramos/ruta/" + rutaId;

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> tramosResponse = rt.getForObject(url, List.class);

            if (tramosResponse == null) {
                return List.of();
            }

            return tramosResponse.stream()
                    .map(this::mapearTramoDto)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.warn("Error obteniendo tramos para ruta {}: {}", rutaId, e.getMessage());
            return List.of();
        }
    }

    private ContenedorDetalladoDto.TramoDto mapearTramoDto(Map<String, Object> tramo) {
        return ContenedorDetalladoDto.TramoDto.builder()
                .tramoId(getIntegerFromMap(tramo, "tramoId"))
                .kilometros(getBigDecimalFromMap(tramo, "distancia"))  // Corregido: usar 'distancia' en lugar de 'kilometros'
                .fechaHoraInicio(getStringFromMap(tramo, "fechaHoraInicio"))
                .fechaHoraFin(getStringFromMap(tramo, "fechaHoraFin"))
                .build();
    }

    private Integer getIntegerFromMap(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return null;
    }

    private String getStringFromMap(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }

    private BigDecimal getBigDecimalFromMap(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return BigDecimal.valueOf(((Number) value).doubleValue());
        }
        return null;
    }
}