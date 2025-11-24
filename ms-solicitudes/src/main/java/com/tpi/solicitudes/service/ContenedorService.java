package com.tpi.solicitudes.service;

import com.tpi.solicitudes.entity.Contenedor;
import com.tpi.solicitudes.entity.Solicitud;
import com.tpi.solicitudes.repository.ContenedorRepository;
import com.tpi.solicitudes.repository.SolicitudRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ContenedorService {

    private final ContenedorRepository contenedorRepository;
    private final SolicitudRepository solicitudRepository;

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

    /**
     * Retorna todos los contenedores que están asociados a solicitudes no entregadas.
     * No recibe parámetros: URL -> GET /api/contenedores/pendientes
     * Usa estado excluido = 5 (Entregada) según el script de inicialización.
     */
    public List<Contenedor> obtenerPendientes() {
        log.info("Obteniendo contenedores pendientes (solicitudes con estado != 5)");
        // 5 = Entregada según 01-init-database_Version3.sql
        List<Solicitud> solicitudesPendientes = solicitudRepository.findByEstadoSolicitudNot(5);

        // Extraer ids únicos de contenedor en orden de aparición
        LinkedHashSet<Integer> ids = solicitudesPendientes.stream()
                .map(Solicitud::getIdContenedor)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (ids.isEmpty()) {
            return Collections.emptyList();
        }

        List<Contenedor> contenedores = contenedorRepository.findAllById(ids);

        // Mantener orden según aparición en las solicitudes (opcional)
        Map<Integer, Integer> order = new HashMap<>();
        int i = 0;
        for (Integer id : ids) order.put(id, i++);
        contenedores.sort(Comparator.comparingInt(c -> order.getOrDefault(c.getIdContenedor(), Integer.MAX_VALUE)));

        return contenedores;
    }

    /**
     * Variante que devuelve DTOs con el nombre del estado poblado.
     */
    public List<com.tpi.solicitudes.dto.ContenedorDto> obtenerPendientesConEstado() {
        List<Contenedor> contenedores = obtenerPendientes();

        if (contenedores.isEmpty()) return Collections.emptyList();

        // Map estático basado en 01-init-database_Version3.sql (id_estado_contenedor -> nombre)
        Map<Integer, String> estadoMap = new HashMap<>();
        estadoMap.put(1, "Disponible");
        estadoMap.put(2, "Cargando");
        estadoMap.put(3, "Cargado");
        estadoMap.put(4, "En Tránsito");
        estadoMap.put(5, "Descargando");
        estadoMap.put(6, "Mantenimiento");

        List<com.tpi.solicitudes.dto.ContenedorDto> dtos = new ArrayList<>();
        for (Contenedor c : contenedores) {
            com.tpi.solicitudes.dto.ContenedorDto dto = com.tpi.solicitudes.dto.ContenedorDto.builder()
                    .identificacion(c.getIdContenedor() != null ? String.valueOf(c.getIdContenedor()) : null)
                    .peso(c.getPesoKg())
                    .volumen(c.getVolumenM3())
                    .estado(estadoMap.getOrDefault(c.getIdEstadoContenedor(), "Desconocido"))
                    .build();
            dtos.add(dto);
        }

        return dtos;
    }
}