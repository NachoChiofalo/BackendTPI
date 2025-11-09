package com.tpi.localizaciones.service;

import com.tpi.localizaciones.entity.Ciudad;
import com.tpi.localizaciones.repository.CiudadRepository;
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
public class CiudadService {

    private final CiudadRepository ciudadRepository;

    public List<Ciudad> obtenerTodas() {
        log.info("Obteniendo todas las ciudades");
        return ciudadRepository.findAll();
    }

    public Optional<Ciudad> obtenerPorId(Long id) {
        log.info("Obteniendo ciudad por id: {}", id);
        return ciudadRepository.findById(id);
    }

    public List<Ciudad> buscarPorNombre(String nombre) {
        log.info("Buscando ciudades por nombre: {}", nombre);
        return ciudadRepository.findByNombreContainingIgnoreCase(nombre);
    }

    public List<Ciudad> buscarPorProvincia(String provincia) {
        log.info("Buscando ciudades por provincia: {}", provincia);
        return ciudadRepository.findByProvinciaContainingIgnoreCase(provincia);
    }

    public List<Ciudad> buscarPorPais(String pais) {
        log.info("Buscando ciudades por país: {}", pais);
        return ciudadRepository.findByPaisContainingIgnoreCase(pais);
    }

    public List<Ciudad> obtenerActivas() {
        log.info("Obteniendo ciudades activas");
        return ciudadRepository.findByActivaTrue();
    }

    public List<Ciudad> buscarPorCodigoPostal(String codigoPostal) {
        log.info("Buscando ciudades por código postal: {}", codigoPostal);
        return ciudadRepository.findByCodigoPostal(codigoPostal);
    }

    @Transactional
    public Ciudad guardar(Ciudad ciudad) {
        log.info("Guardando ciudad: {}", ciudad.getNombre());
        return ciudadRepository.save(ciudad);
    }

    @Transactional
    public Ciudad actualizar(Long id, Ciudad actualizada) {
        log.info("Actualizando ciudad: {}", id);
        return ciudadRepository.findById(id)
                .map(c -> {
                    c.setNombre(actualizada.getNombre());
                    c.setProvincia(actualizada.getProvincia());
                    c.setPais(actualizada.getPais());
                    c.setCodigoPostal(actualizada.getCodigoPostal());
                    c.setLatitud(actualizada.getLatitud());
                    c.setLongitud(actualizada.getLongitud());
                    c.setZonaHoraria(actualizada.getZonaHoraria());
                    c.setActiva(actualizada.getActiva());
                    return ciudadRepository.save(c);
                })
                .orElseThrow(() -> new RuntimeException("Ciudad no encontrada con id: " + id));
    }

    @Transactional
    public void eliminar(Long id) {
        log.info("Eliminando ciudad: {}", id);
        if (ciudadRepository.existsById(id)) {
            ciudadRepository.deleteById(id);
        } else {
            throw new RuntimeException("Ciudad no encontrada con id: " + id);
        }
    }

    @Transactional
    public void cambiarEstado(Long id, Boolean activa) {
        log.info("Cambiando estado de ciudad {} a: {}", id, activa);
        ciudadRepository.findById(id)
                .ifPresentOrElse(
                        ciudad -> {
                            ciudad.setActiva(activa);
                            ciudadRepository.save(ciudad);
                        },
                        () -> {
                            throw new RuntimeException("Ciudad no encontrada con id: " + id);
                        }
                );
    }
}