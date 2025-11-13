package com.tpi.localizaciones.controller;

import com.tpi.localizaciones.dto.DistanciaDTO;
import com.tpi.localizaciones.service.GeoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/distancia")
@RequiredArgsConstructor
public class GeoController {

    private final GeoService geoService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public DistanciaDTO obtenerDistancia(
            @RequestParam("origen") String origen,
            @RequestParam("destino") String destino) throws Exception {
        return geoService.calcularDistancia(origen, destino);
    }
}