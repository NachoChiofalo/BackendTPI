package com.tpi.rutas.service;

import com.tpi.rutas.entity.Tramo;
import com.tpi.rutas.repository.TramoRepository;
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
public class TramoService {

    private final TramoRepository tramoRepository;

    public List<Tramo> obtenerTodos() {
        log.info("Obteniendo todos los tramos");
        return tramoRepository.findAll();
    }

    public Optional<Tramo> obtenerPorId(Long id) {
        log.info("Obteniendo tramo por id: {}", id);
        return tramoRepository.findById(id);
    }

    public List<Tramo> obtenerPorRuta(Long rutaId) {
        log.info("Obteniendo tramos por ruta: {}", rutaId);
        return tramoRepository.findByRutaIdOrderByOrdenAsc(rutaId);
    }

    @Transactional
    public Tramo guardar(Tramo tramo) {
        log.info("Guardando tramo, orden: {}", tramo.getOrden());
        return tramoRepository.save(tramo);
    }

    @Transactional
    public Tramo actualizar(Long id, Tramo actualizado) {
        log.info("Actualizando tramo: {}", id);
        return tramoRepository.findById(id)
                .map(t -> {
                    t.setOrden(actualizado.getOrden());
                    t.setOrigenLatitud(actualizado.getOrigenLatitud());
                    t.setOrigenLongitud(actualizado.getOrigenLongitud());
                    t.setOrigenDescripcion(actualizado.getOrigenDescripcion());
                    t.setDestinoLatitud(actualizado.getDestinoLatitud());
                    t.setDestinoLongitud(actualizado.getDestinoLongitud());
                    t.setDestinoDescripcion(actualizado.getDestinoDescripcion());
                    t.setTipoTramo(actualizado.getTipoTramo());
                    t.setDistanciaKm(actualizado.getDistanciaKm());
                    t.setTiempoEstimadoMinutos(actualizado.getTiempoEstimadoMinutos());
                    t.setObservaciones(actualizado.getObservaciones());
                    return tramoRepository.save(t);
                })
                .orElseThrow(() -> new RuntimeException("Tramo no encontrado con id: " + id));
    }

    @Transactional
    public void eliminar(Long id) {
        log.info("Eliminando tramo: {}", id);
        if (tramoRepository.existsById(id)) {
            tramoRepository.deleteById(id);
        } else {
            throw new RuntimeException("Tramo no encontrado con id: " + id);
        }
    }
}
