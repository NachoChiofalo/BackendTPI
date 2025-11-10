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

    public Optional<Ruta> obtenerPorId(Integer id) {
        log.info("Obteniendo ruta por id: {}", id);
        return rutaRepository.findById(id);
    }

    @Transactional
    public Ruta guardar(Ruta ruta) {
        log.info("Guardando ruta: {}", ruta.getRutaId());
        return rutaRepository.save(ruta);
    }

    @Transactional
    public Ruta actualizar(Integer id, Ruta actualizado) {
        log.info("Actualizando ruta: {}", id);
        return rutaRepository.findById(id)
                .map(r -> {
                    r.setCantidadTramos(actualizado.getCantidadTramos());
                    r.setCantidadDepositos(actualizado.getCantidadDepositos());
                    return rutaRepository.save(r);
                })
                .orElseThrow(() -> new RuntimeException("Ruta no encontrada con id: " + id));
    }

    @Transactional
    public void eliminar(Integer id) {
        log.info("Eliminando ruta: {}", id);
        if (rutaRepository.existsById(id)) {
            rutaRepository.deleteById(id);
        } else {
            throw new RuntimeException("Ruta no encontrada con id: " + id);
        }
    }
}
