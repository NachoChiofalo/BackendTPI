package com.tpi.localizaciones.service;

import com.tpi.localizaciones.entity.DistanciaCalculada;
import com.tpi.localizaciones.entity.EstadoValidacion;
import com.tpi.localizaciones.repository.DistanciaCalculadaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DistanciaService {

    private final DistanciaCalculadaRepository distanciaRepository;

    public List<DistanciaCalculada> obtenerTodas() {
        log.info("Obteniendo todas las distancias calculadas");
        return distanciaRepository.findAll();
    }

    public Optional<DistanciaCalculada> obtenerPorId(Long id) {
        log.info("Obteniendo distancia calculada por id: {}", id);
        return distanciaRepository.findById(id);
    }

    public Optional<DistanciaCalculada> obtenerUltimaDistanciaValidada(Long origenId, Long destinoId, 
                                                                      LocalDateTime fechaMinima) {
        log.info("Obteniendo última distancia validada entre origen {} y destino {}", origenId, destinoId);
        return distanciaRepository.findUltimaDistanciaValidada(origenId, destinoId, fechaMinima);
    }

    public List<DistanciaCalculada> obtenerPorEstado(EstadoValidacion estado) {
        log.info("Obteniendo distancias por estado: {}", estado);
        return distanciaRepository.findByEstadoValidacion(estado);
    }

    public List<DistanciaCalculada> obtenerPorOrigen(Long origenId) {
        log.info("Obteniendo distancias por origen: {}", origenId);
        return distanciaRepository.findByUbicacionOrigenId(origenId);
    }

    public List<DistanciaCalculada> obtenerPorDestino(Long destinoId) {
        log.info("Obteniendo distancias por destino: {}", destinoId);
        return distanciaRepository.findByUbicacionDestinoId(destinoId);
    }

    public Optional<DistanciaCalculada> obtenerPorOrigenYDestino(Long origenId, Long destinoId) {
        log.info("Obteniendo distancia entre origen {} y destino {}", origenId, destinoId);
        return distanciaRepository.findByOrigenYDestino(origenId, destinoId);
    }

    @Transactional
    public DistanciaCalculada guardar(DistanciaCalculada distancia) {
        log.info("Guardando distancia calculada entre {} y {}", 
                distancia.getUbicacionOrigen().getId(), 
                distancia.getUbicacionDestino().getId());
        return distanciaRepository.save(distancia);
    }

    @Transactional
    public DistanciaCalculada actualizar(Long id, DistanciaCalculada actualizada) {
        log.info("Actualizando distancia calculada: {}", id);
        return distanciaRepository.findById(id)
                .map(d -> {
                    d.setDistanciaKm(actualizada.getDistanciaKm());
                    d.setTiempoEstimadoMinutos(actualizada.getTiempoEstimadoMinutos());
                    d.setEstadoValidacion(actualizada.getEstadoValidacion());
                    d.setObservaciones(actualizada.getObservaciones());
                    d.setMomentoCalculo(LocalDateTime.now());
                    return distanciaRepository.save(d);
                })
                .orElseThrow(() -> new RuntimeException("Distancia calculada no encontrada con id: " + id));
    }

    @Transactional
    public void eliminar(Long id) {
        log.info("Eliminando distancia calculada: {}", id);
        if (distanciaRepository.existsById(id)) {
            distanciaRepository.deleteById(id);
        } else {
            throw new RuntimeException("Distancia calculada no encontrada con id: " + id);
        }
    }

    @Transactional
    public DistanciaCalculada validar(Long id, String observaciones) {
        log.info("Validando distancia calculada: {}", id);
        return distanciaRepository.findById(id)
                .map(d -> {
                    d.setEstadoValidacion(EstadoValidacion.VALIDADA);
                    d.setObservaciones(observaciones);
                    return distanciaRepository.save(d);
                })
                .orElseThrow(() -> new RuntimeException("Distancia calculada no encontrada con id: " + id));
    }

    @Transactional
    public DistanciaCalculada rechazar(Long id, String observaciones) {
        log.info("Rechazando distancia calculada: {}", id);
        return distanciaRepository.findById(id)
                .map(d -> {
                    d.setEstadoValidacion(EstadoValidacion.RECHAZADA);
                    d.setObservaciones(observaciones);
                    return distanciaRepository.save(d);
                })
                .orElseThrow(() -> new RuntimeException("Distancia calculada no encontrada con id: " + id));
    }
}