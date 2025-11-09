package com.tpi.solicitudes.service;

import com.tpi.solicitudes.entity.Solicitud;
import com.tpi.solicitudes.repository.SolicitudRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public Optional<Solicitud> obtenerPorId(Long id) {
        log.info("Obteniendo solicitud por id: {}", id);
        return solicitudRepository.findById(id);
    }

    public List<Solicitud> buscarPorNumero(String numero) {
        log.info("Buscando solicitudes por numero: {}", numero);
        return solicitudRepository.findByNumeroContainingIgnoreCase(numero);
    }

    public List<Solicitud> buscarPorClienteId(Long clienteId) {
        log.info("Buscando solicitudes por clienteId: {}", clienteId);
        return solicitudRepository.findByClienteId(clienteId);
    }

    public List<Solicitud> buscarPorEstado(String estado) {
        log.info("Buscando solicitudes por estado: {}", estado);
        return solicitudRepository.findByEstado(estado);
    }

    @Transactional
    public Solicitud guardar(Solicitud solicitud) {
        log.info("Guardando solicitud: {}", solicitud.getNumero());
        return solicitudRepository.save(solicitud);
    }

    @Transactional
    public Solicitud actualizar(Long id, Solicitud actualizada) {
        log.info("Actualizando solicitud: {}", id);
        return solicitudRepository.findById(id)
                .map(s -> {
                    s.setNumero(actualizada.getNumero());
                    s.setContenedor(actualizada.getContenedor());
                    s.setCliente(actualizada.getCliente());
                    s.setOrigenLatitud(actualizada.getOrigenLatitud());
                    s.setOrigenLongitud(actualizada.getOrigenLongitud());
                    s.setOrigenDireccion(actualizada.getOrigenDireccion());
                    s.setDestinoLatitud(actualizada.getDestinoLatitud());
                    s.setDestinoLongitud(actualizada.getDestinoLongitud());
                    s.setDestinoDireccion(actualizada.getDestinoDireccion());
                    s.setEstado(actualizada.getEstado());
                    s.setObservaciones(actualizada.getObservaciones());
                    s.setFechaRetiroProgramada(actualizada.getFechaRetiroProgramada());
                    s.setFechaEntregaProgramada(actualizada.getFechaEntregaProgramada());
                    s.setCostoEstimado(actualizada.getCostoEstimado());
                    s.setTiempoEstimado(actualizada.getTiempoEstimado());
                    s.setCostoFinal(actualizada.getCostoFinal());
                    s.setTiempoReal(actualizada.getTiempoReal());
                    s.setPrioridad(actualizada.getPrioridad());
                    return solicitudRepository.save(s);
                })
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada con id: " + id));
    }

    @Transactional
    public void eliminar(Long id) {
        log.info("Eliminando solicitud: {}", id);
        if (solicitudRepository.existsById(id)) {
            solicitudRepository.deleteById(id);
        } else {
            throw new RuntimeException("Solicitud no encontrada con id: " + id);
        }
    }
}