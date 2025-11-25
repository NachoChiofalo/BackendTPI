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
                .idRuta(dto.getIdRuta() != null ? dto.getIdRuta() : 0)
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
