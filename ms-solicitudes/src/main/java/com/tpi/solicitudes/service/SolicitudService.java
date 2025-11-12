package com.tpi.solicitudes.service;

import com.tpi.solicitudes.client.OsrmFeignClient;
import com.tpi.solicitudes.client.dto.OsrmRouteResponseDto;
import com.tpi.solicitudes.client.dto.RouteDto;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

// -------------------------------------------------------------------------
// 🚨 NOTA IMPORTANTE: Esta es una clase PLACEHOLDER simplificada para la 
// ENTIDAD/MODELO Solicitud. En tu proyecto, esta clase ya debe existir 
// en tu paquete de "entity" o "model" con sus respectivos Getters/Setters.
// -------------------------------------------------------------------------
@Data
class Solicitud {
    private Long id;
    // Coordenadas de la Solicitud (asumimos que ya están llenas)
    private Double origenLat;
    private Double origenLng;
    private Double destinoLat;
    private Double destinoLng;
    
    // Campos que serán actualizados por el servicio OSRM
    private Double distanciaMetros;
    private Double tiempoEstimadoSegundos; 
    private BigDecimal costoEstimado;
    
    // Constructor placeholder
    public Solicitud(Long id, double oLat, double oLng, double dLat, double dLng) {
        this.id = id;
        this.origenLat = oLat;
        this.origenLng = oLng;
        this.destinoLat = dLat;
        this.destinoLng = dLng;
    }
}
// -------------------------------------------------------------------------

@Service
public class SolicitudService {

    // 1. INYECCIÓN DEL CLIENTE FEIGN
    @Autowired
    private OsrmFeignClient osrmClient;

    /**
     * Calcula la distancia y el tiempo estimado de la ruta entre origen y destino
     * usando el servicio OSRM y actualiza la entidad Solicitud.
     *
     * @param solicitudId El ID de la Solicitud a procesar.
     * @return La Solicitud actualizada.
     */
    public Solicitud calcularDistanciaYTiempoEstimado(Long solicitudId) {
        
        // 1. OBTENER LA SOLICITUD (Mock, en realidad usarías un Repositorio)
        // Sustituir con: Solicitud solicitud = solicitudRepository.findById(solicitudId).orElseThrow(...);
        // Usamos un mock para el ejemplo:
        Solicitud solicitud = getMockSolicitud(solicitudId)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));

        // 2. FORMATEAR COORDENADAS PARA OSRM
        // Formato: "lng1,lat1;lng2,lat2"
        String coordinates = String.format(
            "%.6f,%.6f;%.6f,%.6f", 
            solicitud.getOrigenLng(), solicitud.getOrigenLat(), 
            solicitud.getDestinoLng(), solicitud.getDestinoLat()
        );
        
        // 3. LLAMAR AL CLIENTE FEIGN
        OsrmRouteResponseDto response = osrmClient.getRouteData(coordinates);
        
        // 4. PROCESAR RESPUESTA
        if ("Ok".equals(response.code()) && response.routes() != null && !response.routes().isEmpty()) {
            
            RouteDto route = response.routes().get(0);
            
            double distanceInMeters = route.distance();
            double durationInSeconds = route.duration();
            
            // 5. APLICAR LÓGICA DE NEGOCIO Y ACTUALIZAR LA ENTIDAD
            
            // a. Actualizar campos de distancia y tiempo
            solicitud.setDistanciaMetros(distanceInMeters);
            solicitud.setTiempoEstimadoSegundos(durationInSeconds);
            
            // b. Calcular Costo Estimado (LÓGICA DE NEGOCIO DE EJEMPLO)
            double distanceInKm = distanceInMeters / 1000.0;
            // *Asumimos un costo base por km de 5.50 para el cálculo de ejemplo*
            final BigDecimal COSTO_BASE_POR_KM = new BigDecimal("5.50");
            
            BigDecimal costoTotal = COSTO_BASE_POR_KM
                .multiply(new BigDecimal(distanceInKm))
                .setScale(2, RoundingMode.HALF_UP); // Redondear a dos decimales
                
            solicitud.setCostoEstimado(costoTotal);
            
            // 6. GUARDAR LA ENTIDAD (Sustituir por: solicitudRepository.save(solicitud);)
            // System.out.println("Solicitud " + solicitudId + " actualizada con éxito.");
            
        } else {
            // Manejo de Error: Se podría registrar un log o devolver un error más específico
            System.err.println("Error al obtener ruta de OSRM. Código de respuesta: " + response.code());
            throw new RuntimeException("No se pudo calcular la ruta para la Solicitud ID: " + solicitudId);
        }
        
        return solicitud;
    }

    // MOCK para simular la obtención de una Solicitud de la base de datos
    private Optional<Solicitud> getMockSolicitud(Long id) {
        if (id.equals(1L)) {
            // Coordenadas de ejemplo (simulando origen y destino)
            return Optional.of(new Solicitud(1L, -31.416667, -64.183333, -31.400000, -64.200000));
        }
        return Optional.empty();
    }
}