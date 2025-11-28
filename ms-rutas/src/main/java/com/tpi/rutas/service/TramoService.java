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

    public Optional<Tramo> obtenerPorId(Integer id) {
        log.info("Obteniendo tramo por id: {}", id);
        return tramoRepository.findById(id);
    }

    public List<Tramo> obtenerPorRuta(Integer rutaId) {
        log.info("Obteniendo tramos por ruta: {}", rutaId);
        return tramoRepository.findByRutaId(rutaId);
    }

    @Transactional
    public Tramo guardar(Tramo tramo) {
        log.info("Guardando tramo: {}", tramo.getTramoId());
        return tramoRepository.save(tramo);
    }

    @Transactional
    public Tramo actualizar(Integer id, Tramo actualizado) {
        log.info("Actualizando tramo: {}", id);
        return tramoRepository.findById(id)
                .map(t -> {
                    t.setRutaId(actualizado.getRutaId());
                    t.setTipoTramoId(actualizado.getTipoTramoId());
                    t.setDominio(actualizado.getDominio());
                    t.setUbicacionOrigenId(actualizado.getUbicacionOrigenId());
                    t.setTransportistaId(actualizado.getTransportistaId());
                    t.setUbicacionDestinoId(actualizado.getUbicacionDestinoId());
                    t.setCostoAproximado(actualizado.getCostoAproximado());
                    t.setCostoReal(actualizado.getCostoReal());
                    t.setFechaHoraInicio(actualizado.getFechaHoraInicio());
                    t.setFechaHoraFin(actualizado.getFechaHoraFin());
                    t.setFechaHoraEstimadaFin(actualizado.getFechaHoraEstimadaFin());
                    return tramoRepository.save(t);
                })
                .orElseThrow(() -> new RuntimeException("Tramo no encontrado con id: " + id));
    }

    @Transactional
    public void eliminar(Integer id) {
        log.info("Eliminando tramo: {}", id);
        if (tramoRepository.existsById(id)) {
            tramoRepository.deleteById(id);
        } else {
            throw new RuntimeException("Tramo no encontrado con id: " + id);
        }
    }

    /**
     * REGLA DE NEGOCIO 1: Un camión no puede transportar contenedores que superen su peso o volumen máximo
     * Esta validación debe hacerse antes de asignar el camión al tramo
     * El controller debe validar que el camión tenga capacidad suficiente antes de llamar a este método
     */
    @Transactional
    public Tramo asignarCamion(Integer tramoId, String dominio) {
        log.info("Asignando camión {} al tramo {}", dominio, tramoId);
        return tramoRepository.findById(tramoId)
                .map(tramo -> {
                    tramo.setDominio(dominio);
                    return tramoRepository.save(tramo);
                })
                .orElseThrow(() -> new RuntimeException("Tramo no encontrado con id: " + tramoId));
    }

    // Nuevo método: asignar con validación explícita de rutaId + tramoId
    @Transactional
    public Tramo asignarCamion(Integer rutaId, Integer tramoId, String dominio) {
        log.info("Asignando camión {} al tramo {} de la ruta {}", dominio, tramoId, rutaId);
        return tramoRepository.findByTramoIdAndRutaId(tramoId, rutaId)
                .map(tramo -> {
                    tramo.setDominio(dominio);
                    return tramoRepository.save(tramo);
                })
                .orElseThrow(() -> new RuntimeException("Tramo no encontrado con id: " + tramoId + " para la ruta: " + rutaId));
    }

    /**
     * REGLA DE NEGOCIO 7 (parte 1): Determinar el inicio de un tramo
     * Los tramos deben registrar fechas estimadas y reales para calcular el desempeño del servicio
     */
    @Transactional
    public Tramo iniciarTramo(Integer rutaId, Integer tramoId) {
        log.info("Iniciando tramo: {} de la ruta: {}", tramoId, rutaId);
        return tramoRepository.findByTramoIdAndRutaId(tramoId, rutaId)
                .map(tramo -> {
                    tramo.setFechaHoraInicio(java.time.LocalDate.now());
                    return tramoRepository.save(tramo);
                })
                .orElseThrow(() -> new RuntimeException("Tramo no encontrado con id: " + tramoId + " para la ruta: " + rutaId));
    }

    // Compatibilidad: método antiguo por tramoId sólo (delegará buscando ruta implícita)
    @Transactional
    public Tramo iniciarTramo(Integer tramoId) {
        log.info("Iniciando tramo (sin validar ruta): {}", tramoId);
        return tramoRepository.findById(tramoId)
                .map(tramo -> {
                    tramo.setFechaHoraInicio(java.time.LocalDate.now());
                    return tramoRepository.save(tramo);
                })
                .orElseThrow(() -> new RuntimeException("Tramo no encontrado con id: " + tramoId));
    }

    /**
     * REGLA DE NEGOCIO 7 (parte 2): Determinar el fin de un tramo
     * Los tramos deben registrar fechas reales para:
     * - Calcular el desempeño del servicio (comparar fecha real vs estimada)
     * - Calcular estadía en depósitos (diferencia entre fecha fin de un tramo y fecha inicio del siguiente)
     * - Determinar costos reales de transporte
     */
    @Transactional
    public Tramo finalizarTramo(Integer rutaId, Integer tramoId) {
        log.info("Finalizando tramo: {} de la ruta: {}", tramoId, rutaId);
        return tramoRepository.findByTramoIdAndRutaId(tramoId, rutaId)
                .map(tramo -> {
                    tramo.setFechaHoraFin(java.time.LocalDate.now());
                    return tramoRepository.save(tramo);
                })
                .orElseThrow(() -> new RuntimeException("Tramo no encontrado con id: " + tramoId + " para la ruta: " + rutaId));
    }

    // Compatibilidad: método antiguo por tramoId sólo
    @Transactional
    public Tramo finalizarTramo(Integer tramoId) {
        log.info("Finalizando tramo (sin validar ruta): {}", tramoId);
        return tramoRepository.findById(tramoId)
                .map(tramo -> {
                    tramo.setFechaHoraFin(java.time.LocalDate.now());
                    return tramoRepository.save(tramo);
                })
                .orElseThrow(() -> new RuntimeException("Tramo no encontrado con id: " + tramoId));
    }

    public List<Tramo> obtenerPorTransportista(Integer transportistaId) {
        log.info("Obteniendo tramos del transportista: {}", transportistaId);
        return tramoRepository.findByTransportistaId(transportistaId);
    }
}
