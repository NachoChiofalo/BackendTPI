package com.tpi.precios.service;

import com.tpi.precios.entity.Tarifa;
import com.tpi.precios.repository.TarifaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class TarifaService {

    private final TarifaRepository tarifaRepository;

    public List<Tarifa> obtenerTodas() {
        log.info("Obteniendo todas las tarifas");
        return tarifaRepository.findAll();
    }

    public List<Tarifa> obtenerTarifasVigentes() {
        log.info("Obteniendo tarifas vigentes");
        return tarifaRepository.findTarifasVigentes();
    }

    public Optional<Tarifa> obtenerPorId(Integer id) {
        log.info("Obteniendo tarifa por ID: {}", id);
        return tarifaRepository.findById(id);
    }

    public Optional<Tarifa> obtenerTarifaVigenteMasReciente() {
        log.info("Obteniendo tarifa vigente más reciente");
        List<Tarifa> vigentes = tarifaRepository.findTarifasVigentesOrdenadasPorInicioDesc();
        if (vigentes == null || vigentes.isEmpty()) return Optional.empty();
        if (vigentes.size() > 1) {
            log.warn("Se encontraron {} tarifas vigentes; se utilizará la más reciente con fechaInicio={} (tarifaId={})",
                    vigentes.size(), vigentes.get(0).getFechaVigenciaInicio(), vigentes.get(0).getTarifaId());
        }
        return Optional.of(vigentes.get(0));
    }

    public List<Tarifa> obtenerTarifasVigentesEn(LocalDate fecha) {
        log.info("Obteniendo tarifas vigentes en fecha: {}", fecha);
        return tarifaRepository.findTarifasVigentesEn(fecha);
    }

    public List<Tarifa> obtenerTarifasPorRangoFechas(LocalDate fechaInicio, LocalDate fechaFin) {
        log.info("Obteniendo tarifas por rango de fechas: {} - {}", fechaInicio, fechaFin);
        return tarifaRepository.findByRangoFechas(fechaInicio, fechaFin);
    }

    public List<Tarifa> obtenerTarifasQueVencenProximamente(int diasProximidad) {
        LocalDate fechaInicio = LocalDate.now();
        LocalDate fechaFin = fechaInicio.plusDays(diasProximidad);
        log.info("Obteniendo tarifas que vencen en los próximos {} días", diasProximidad);
        return tarifaRepository.findTarifasQueVencenEntre(fechaInicio, fechaFin);
    }

    public List<Tarifa> obtenerTarifasFuturas() {
        log.info("Obteniendo tarifas futuras");
        return tarifaRepository.findTarifasFuturas();
    }

    @Transactional
    public Tarifa crearTarifa(Tarifa tarifa) {
        log.info("Creando nueva tarifa");
        validarTarifa(tarifa);
        return tarifaRepository.save(tarifa);
    }

    @Transactional
    public Tarifa actualizarTarifa(Integer id, Tarifa tarifaActualizada) {
        log.info("Actualizando tarifa con ID: {}", id);
        return tarifaRepository.findById(id)
                .map(tarifaExistente -> {
                    tarifaExistente.setPrecioCombustibleLitro(tarifaActualizada.getPrecioCombustibleLitro());
                    tarifaExistente.setPrecioKmKg(tarifaActualizada.getPrecioKmKg());
                    tarifaExistente.setPrecioKmM3(tarifaActualizada.getPrecioKmM3());
                    tarifaExistente.setFechaVigenciaInicio(tarifaActualizada.getFechaVigenciaInicio());
                    tarifaExistente.setFechaVigenciaFin(tarifaActualizada.getFechaVigenciaFin());
                    tarifaExistente.setPrecioTramo(tarifaActualizada.getPrecioTramo());
                    validarTarifa(tarifaExistente);
                    return tarifaRepository.save(tarifaExistente);
                })
                .orElseThrow(() -> new RuntimeException("Tarifa no encontrada con ID: " + id));
    }

    @Transactional
    public void eliminarTarifa(Integer id) {
        log.info("Eliminando tarifa con ID: {}", id);
        if (!tarifaRepository.existsById(id)) {
            throw new RuntimeException("Tarifa no encontrada con ID: " + id);
        }
        tarifaRepository.deleteById(id);
    }

    private void validarTarifa(Tarifa tarifa) {
        if (tarifa.getFechaVigenciaFin().isBefore(tarifa.getFechaVigenciaInicio())) {
            throw new IllegalArgumentException("La fecha de fin debe ser posterior a la fecha de inicio");
        }
    }
}
