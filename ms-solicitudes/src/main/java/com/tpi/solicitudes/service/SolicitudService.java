package com.tpi.solicitudes.service;

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