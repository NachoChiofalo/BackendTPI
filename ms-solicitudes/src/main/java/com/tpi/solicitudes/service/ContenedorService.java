package com.tpi.solicitudes.service;

import com.tpi.solicitudes.entity.Contenedor;
import com.tpi.solicitudes.repository.ContenedorRepository;
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
public class ContenedorService {

    private final ContenedorRepository contenedorRepository;

    public List<Contenedor> obtenerTodos() {
        log.info("Obteniendo todos los contenedores");
        return contenedorRepository.findAll();
    }

    public Optional<Contenedor> obtenerPorId(Integer id) {
        log.info("Obteniendo contenedor por id: {}", id);
        return contenedorRepository.findById(id);
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
}