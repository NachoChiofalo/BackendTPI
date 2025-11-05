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
        log.info("Obteniendo todos los camiones activos");
        return camionRepository.findByActivoTrue();
    }

    public List<Camion> obtenerDisponibles() {
        log.info("Obteniendo camiones disponibles");
        return camionRepository.findByActivoTrueAndDisponibleTrue();
    }

    public Optional<Camion> obtenerPorDominio(String dominio) {
        log.info("Obteniendo camión por dominio: {}", dominio);
        return camionRepository.findById(dominio);
    }

    public List<Camion> obtenerPorTransportista(Long transportistaId) {
        log.info("Obteniendo camiones del transportista: {}", transportistaId);
        return camionRepository.findByTransportistaIdAndActivoTrue(transportistaId);
    }

    public List<Camion> obtenerPorTipo(Long tipoCamionId) {
        log.info("Obteniendo camiones del tipo: {}", tipoCamionId);
        return camionRepository.findByTipoCamionIdAndActivoTrue(tipoCamionId);
    }

    public List<Camion> obtenerConCapacidad(BigDecimal pesoMin, BigDecimal volumenMin) {
        log.info("Obteniendo camiones con capacidad mínima - Peso: {}, Volumen: {}", pesoMin, volumenMin);
        return camionRepository.findDisponiblesConCapacidad(pesoMin, volumenMin);
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
                    // Actualizar solo los campos permitidos
                    if (camionActualizado.getDisponible() != null) {
                        camion.setDisponible(camionActualizado.getDisponible());
                    }
                    if (camionActualizado.getDepositoActualId() != null) {
                        camion.setDepositoActualId(camionActualizado.getDepositoActualId());
                    }
                    if (camionActualizado.getTransportistaId() != null) {
                        camion.setTransportistaId(camionActualizado.getTransportistaId());
                    }
                    return camionRepository.save(camion);
                })
                .orElseThrow(() -> new RuntimeException("Camión no encontrado: " + dominio));
    }

    @Transactional
    public void eliminar(String dominio) {
        log.info("Marcando como inactivo el camión: {}", dominio);
        camionRepository.findById(dominio)
                .ifPresentOrElse(
                        camion -> {
                            camion.setActivo(false);
                            camion.setDisponible(false);
                            camionRepository.save(camion);
                        },
                        () -> {
                            throw new RuntimeException("Camión no encontrado: " + dominio);
                        }
                );
    }
}
