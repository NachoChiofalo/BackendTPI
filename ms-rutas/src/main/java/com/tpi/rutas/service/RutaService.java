package com.tpi.rutas.service;

import com.tpi.rutas.entity.Ruta;
import com.tpi.rutas.repository.RutaRepository;
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
public class RutaService {

    private final RutaRepository rutaRepository;

    public List<Ruta> obtenerTodos() {
        log.info("Obteniendo todas las rutas");
        return rutaRepository.findAll();
    }

    public Optional<Ruta> obtenerPorId(Long id) {
        log.info("Obteniendo ruta por id: {}", id);
        return rutaRepository.findById(id);
    }

    public List<Ruta> buscarPorCodigo(String codigo) {
        log.info("Buscando rutas por codigo: {}", codigo);
        return rutaRepository.findByCodigoContainingIgnoreCase(codigo);
    }

    public List<Ruta> buscarPorSolicitudId(Long solicitudId) {
        log.info("Buscando rutas por solicitudId: {}", solicitudId);
        return rutaRepository.findBySolicitudId(solicitudId);
    }

    @Transactional
    public Ruta guardar(Ruta ruta) {
        log.info("Guardando ruta: {}", ruta.getCodigo());
        return rutaRepository.save(ruta);
    }

    @Transactional
    public Ruta actualizar(Long id, Ruta actualizado) {
        log.info("Actualizando ruta: {}", id);
        return rutaRepository.findById(id)
                .map(r -> {
                    r.setCodigo(actualizado.getCodigo());
                    r.setSolicitudId(actualizado.getSolicitudId());
                    r.setObservaciones(actualizado.getObservaciones());
                    r.setPrioridad(actualizado.getPrioridad());
                    r.setTipoRuta(actualizado.getTipoRuta());
                    return rutaRepository.save(r);
                })
                .orElseThrow(() -> new RuntimeException("Ruta no encontrada con id: " + id));
    }

    @Transactional
    public void eliminar(Long id) {
        log.info("Eliminando ruta: {}", id);
        if (rutaRepository.existsById(id)) {
            rutaRepository.deleteById(id);
        } else {
            throw new RuntimeException("Ruta no encontrada con id: " + id);
        }
    }
}
