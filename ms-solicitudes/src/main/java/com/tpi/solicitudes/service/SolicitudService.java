package com.tpi.solicitudes.service;

import com.tpi.solicitudes.entity.Solicitud;
import com.tpi.solicitudes.repository.SolicitudRepository;
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
public class SolicitudService {

    private final SolicitudRepository solicitudRepository;

    public List<Solicitud> obtenerTodos() {
        log.info("Obteniendo todas las solicitudes");
        return solicitudRepository.findAll();
    }

    public Optional<Solicitud> obtenerPorId(Integer id) {
        log.info("Obteniendo solicitud por id: {}", id);
        return solicitudRepository.findById(id);
    }
}