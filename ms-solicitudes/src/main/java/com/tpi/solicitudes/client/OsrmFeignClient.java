package com.tpi.solicitudes.client;

import com.tpi.solicitudes.client.dto.OsrmRouteResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Cliente Feign declarativo para interactuar con el servicio de ruteo OSRM.
 */
@FeignClient(
    name = "osrm-router", 
    url = "http://router.project-osrm.org/route/v1/driving"
)
public interface OsrmFeignClient {

    /**
     * Solicita información de ruta a OSRM.
     * * @param coordinates Cadena de coordenadas en formato "lng1,lat1;lng2,lat2".
     * @return OsrmRouteResponseDto que contiene la lista de rutas (distance, duration).
     */
    @GetMapping("/{coordinates}")
    OsrmRouteResponseDto getRouteData(
            @PathVariable("coordinates") String coordinates
    );
}