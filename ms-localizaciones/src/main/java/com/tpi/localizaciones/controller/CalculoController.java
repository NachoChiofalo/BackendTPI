package com.tpi.localizaciones.controller;

import com.tpi.localizaciones.entity.Ubicacion;
import com.tpi.localizaciones.repository.UbicacionRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/calculo")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class CalculoController {

    private final UbicacionRepository ubicacionRepository;

    @GetMapping("/distancia-tiempo")
    public ResponseEntity<DistanciaTiempoResponse> distanciaTiempo(
            @RequestParam("origenId") Integer origenId,
            @RequestParam("destinoId") Integer destinoId,
            @RequestParam(value = "velocidadKmh", required = false) Double velocidadKmh
    ) {
        velocidadKmh = (velocidadKmh == null || velocidadKmh <= 0) ? 60.0 : velocidadKmh;

        Ubicacion origen = ubicacionRepository.findById(origenId)
                .orElseThrow(() -> new IllegalArgumentException("Ubicación origen no encontrada: " + origenId));
        Ubicacion destino = ubicacionRepository.findById(destinoId)
                .orElseThrow(() -> new IllegalArgumentException("Ubicación destino no encontrada: " + destinoId));

        double lat1 = Double.parseDouble(origen.getLatitud());
        double lon1 = Double.parseDouble(origen.getLongitud());
        double lat2 = Double.parseDouble(destino.getLatitud());
        double lon2 = Double.parseDouble(destino.getLongitud());

        double distanciaKm = haversine(lat1, lon1, lat2, lon2);
        double duracionHoras = distanciaKm / velocidadKmh;

        DistanciaTiempoResponse resp = new DistanciaTiempoResponse(round2(distanciaKm), round2(duracionHoras));
        return ResponseEntity.ok(resp);
    }

    private static double haversine(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371.0; // km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    private static double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    @Data
    @AllArgsConstructor
    public static class DistanciaTiempoResponse {
        private Double distanciaKm;
        private Double duracionHoras;
    }
}
