package com.tpi.solicitudes.controller;

import com.tpi.solicitudes.entity.Solicitud;
import com.tpi.solicitudes.service.SolicitudService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/solicitudes")
@RequiredArgsConstructor
@Slf4j
public class SolicitudController {

    private final SolicitudService solicitudService;

    @GetMapping
    public ResponseEntity<List<Solicitud>> obtenerTodos() {
        log.info("GET /api/solicitudes - Obteniendo todas las solicitudes");
        List<Solicitud> solicitudes = solicitudService.obtenerTodos();
        return ResponseEntity.ok(solicitudes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Solicitud> obtenerPorId(@PathVariable("id") Integer id) {
        log.info("GET /api/solicitudes/{} - Obteniendo solicitud por ID", id);
        return solicitudService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Solicitudes service is running");
    }
}

