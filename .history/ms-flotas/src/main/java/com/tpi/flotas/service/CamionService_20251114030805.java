package com.tpi.flotas.service;

import com.tpi.flotas.entity.Camion;
import com.tpi.flotas.repository.CamionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CamionService {

    private final CamionRepository camionRepository;

    public List<Camion> obtenerTodos() {
        log.info("Obteniendo todos los camiones");
        return camionRepository.findAll();
    }

    public List<Camion> obtenerDisponibles() {
        log.info("Obteniendo camiones disponibles");
        return camionRepository.findByDisponibleTrue();
    }

    public List<Camion> obtenerNoDisponibles() {
        log.info("Obteniendo camiones no disponibles");
        return camionRepository.findByDisponibleFalse();
    }

    public Optional<Camion> obtenerPorDominio(String dominio) {
        log.info("Obteniendo camión por dominio: {}", dominio);
        return camionRepository.findById(dominio);
    }

    /**
     * REGLA DE NEGOCIO 1: Un camión no puede transportar contenedores que superen su peso o volumen máximo
     * Este método busca camiones que cumplan con la capacidad mínima requerida
     * 
     * @param pesoMin Peso mínimo requerido (peso del contenedor)
     * @param volumenMin Volumen mínimo requerido (volumen del contenedor)
     * @return Lista de camiones con capacidad suficiente
     */
    public List<Camion> obtenerConCapacidadMinima(BigDecimal pesoMin, BigDecimal volumenMin) {
        log.info("Obteniendo camiones con capacidad mínima - Peso: {}, Volumen: {}", pesoMin, volumenMin);
        return camionRepository.findByCapacidadMinima(pesoMin, volumenMin);
    }

    /**
     * REGLA DE NEGOCIO 1: Validación de capacidad para camiones disponibles
     * Filtra solo camiones que estén disponibles Y cumplan con la capacidad requerida
     * 
     * @param pesoMin Peso mínimo requerido (peso del contenedor)
     * @param volumenMin Volumen mínimo requerido (volumen del contenedor)
     * @return Lista de camiones disponibles con capacidad suficiente
     */
    public List<Camion> obtenerDisponiblesConCapacidad(BigDecimal pesoMin, BigDecimal volumenMin) {
        log.info("Obteniendo camiones disponibles con capacidad mínima - Peso: {}, Volumen: {}", pesoMin, volumenMin);
        return camionRepository.findDisponiblesConCapacidadMinima(pesoMin, volumenMin);
    }

    @Transactional
    public Camion guardar(Camion camion) {
        log.info("Guardando camión: {}", camion.getDominio());
        return camionRepository.save(camion);
    }

    @Transactional
    public Camion actualizar(String dominio, Camion camionActualizado) {
        log.info("Actualizando camión: {}", dominio);
        return camionRepository.findById(dominio)
                .map(camion -> {
                    camion.setDisponible(camionActualizado.getDisponible());
                    camion.setCapacidadPeso(camionActualizado.getCapacidadPeso());
                    camion.setCapacidadVolumen(camionActualizado.getCapacidadVolumen());
                    camion.setCostoBaseKm(camionActualizado.getCostoBaseKm());
                    camion.setConsumoPromedio(camionActualizado.getConsumoPromedio());
                    return camionRepository.save(camion);
                })
                .orElseThrow(() -> new RuntimeException("Camión no encontrado con dominio: " + dominio));
    }

    @Transactional
    public void cambiarDisponibilidad(String dominio, Boolean disponible) {
        log.info("Cambiando disponibilidad del camión {} a: {}", dominio, disponible);
        camionRepository.findById(dominio)
                .ifPresentOrElse(
                    camion -> {
                        camion.setDisponible(disponible);
                        camionRepository.save(camion);
                    },
                    () -> { throw new RuntimeException("Camión no encontrado con dominio: " + dominio); }
                );
    }

    @Transactional
    public void eliminar(String dominio) {
        log.info("Eliminando camión: {}", dominio);
        if (camionRepository.existsById(dominio)) {
            camionRepository.deleteById(dominio);
        } else {
            throw new RuntimeException("Camión no encontrado con dominio: " + dominio);
        }
    }
}
