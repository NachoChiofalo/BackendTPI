package com.tpi.localizaciones.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad Ciudad - Representa las ciudades del sistema
 * Esquema: localizaciones
 */
@Entity
@Table(name = "ciudades", schema = "localizaciones")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ciudad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 100)
    private String provincia;

    @Column(nullable = false, length = 50)
    private String pais;

    @Column(name = "codigo_postal", length = 10)
    private String codigoPostal;

    @Column(name = "codigo_area", length = 10)
    private String codigoArea;

    // Coordenadas centrales de la ciudad
    @Column(name = "latitud_centro", precision = 10, scale = 7)
    private BigDecimal latitudCentro;

    @Column(name = "longitud_centro", precision = 10, scale = 7)
    private BigDecimal longitudCentro;

    // Información adicional
    @Column(name = "poblacion")
    private Long poblacion;

    @Column(name = "zona_horaria", length = 50)
    private String zonaHoraria;

    @Enumerated(EnumType.STRING)
    @Column(name = "categoria_ciudad", length = 20)
    @Builder.Default
    private CategoriaCiudad categoriaCiudad = CategoriaCiudad.CIUDAD;

    // Límites geográficos (bounding box)
    @Column(name = "limite_norte", precision = 10, scale = 7)
    private BigDecimal limiteNorte;

    @Column(name = "limite_sur", precision = 10, scale = 7)
    private BigDecimal limiteSur;

    @Column(name = "limite_este", precision = 10, scale = 7)
    private BigDecimal limiteEste;

    @Column(name = "limite_oeste", precision = 10, scale = 7)
    private BigDecimal limiteOeste;

    // Información logística
    @Column(name = "tiene_puerto")
    @Builder.Default
    private Boolean tienePuerto = false;

    @Column(name = "tiene_aeropuerto")
    @Builder.Default
    private Boolean tieneAeropuerto = false;

    @Column(name = "tiene_terminal_cargas")
    @Builder.Default
    private Boolean tieneTerminalCargas = false;

    @Column(name = "restricciones_vehiculos", length = 500)
    private String restriccionesVehiculos;

    // Relaciones
    @OneToMany(mappedBy = "ciudad", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Ubicacion> ubicaciones = new ArrayList<>();

    @Column(name = "activa")
    @Builder.Default
    private Boolean activa = true;

    // Auditoría
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Enums
    public enum CategoriaCiudad {
        CAPITAL_FEDERAL,    // Ciudad Autónoma de Buenos Aires
        CAPITAL_PROVINCIAL, // Capital de provincia
        CIUDAD,            // Ciudad importante
        MUNICIPIO,         // Municipio
        LOCALIDAD,         // Localidad pequeña
        ZONA_RURAL        // Zona rural
    }

    // Métodos de conveniencia
    public void addUbicacion(Ubicacion ubicacion) {
        ubicaciones.add(ubicacion);
        ubicacion.setCiudad(this);
    }

    public void removeUbicacion(Ubicacion ubicacion) {
        ubicaciones.remove(ubicacion);
        ubicacion.setCiudad(null);
    }

    public String getNombreCompleto() {
        return String.format("%s, %s, %s", nombre, provincia, pais);
    }

    public boolean tieneCoordenadasCompletas() {
        return latitudCentro != null && longitudCentro != null;
    }

    public boolean tieneLimitesDefinidos() {
        return limiteNorte != null && limiteSur != null && 
               limiteEste != null && limiteOeste != null;
    }

    public double calcularDistanciaA(BigDecimal latDestino, BigDecimal lngDestino) {
        if (!tieneCoordenadasCompletas() || latDestino == null || lngDestino == null) {
            return 0.0;
        }

        double lat1 = Math.toRadians(this.latitudCentro.doubleValue());
        double lon1 = Math.toRadians(this.longitudCentro.doubleValue());
        double lat2 = Math.toRadians(latDestino.doubleValue());
        double lon2 = Math.toRadians(lngDestino.doubleValue());

        double dLat = lat2 - lat1;
        double dLon = lon2 - lon1;

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(lat1) * Math.cos(lat2) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        final double RADIO_TIERRA_KM = 6371.0;
        return RADIO_TIERRA_KM * c;
    }

    public boolean contienePunto(BigDecimal latitud, BigDecimal longitud) {
        if (!tieneLimitesDefinidos() || latitud == null || longitud == null) {
            return false;
        }

        return latitud.compareTo(limiteSur) >= 0 && 
               latitud.compareTo(limiteNorte) <= 0 &&
               longitud.compareTo(limiteOeste) >= 0 && 
               longitud.compareTo(limiteEste) <= 0;
    }

    public String getResumenLogistico() {
        StringBuilder sb = new StringBuilder();
        sb.append("Servicios logísticos: ");
        
        List<String> servicios = new ArrayList<>();
        if (tienePuerto) servicios.add("Puerto");
        if (tieneAeropuerto) servicios.add("Aeropuerto");
        if (tieneTerminalCargas) servicios.add("Terminal de cargas");
        
        if (servicios.isEmpty()) {
            sb.append("Ninguno");
        } else {
            sb.append(String.join(", ", servicios));
        }
        
        return sb.toString();
    }

    public boolean esCapital() {
        return categoriaCiudad == CategoriaCiudad.CAPITAL_FEDERAL || 
               categoriaCiudad == CategoriaCiudad.CAPITAL_PROVINCIAL;
    }

    public boolean esUrbana() {
        return categoriaCiudad != CategoriaCiudad.ZONA_RURAL;
    }

    // Validaciones
    public boolean validarCoordenadas() {
        if (!tieneCoordenadasCompletas()) return false;
        
        // Validar rangos válidos de coordenadas para Argentina
        // Latitud: aproximadamente entre -55° y -21°
        // Longitud: aproximadamente entre -73° y -53°
        double lat = latitudCentro.doubleValue();
        double lng = longitudCentro.doubleValue();
        
        return lat >= -55.0 && lat <= -21.0 && lng >= -73.0 && lng <= -53.0;
    }

    public boolean validarLimites() {
        if (!tieneLimitesDefinidos()) return true; // No obligatorios
        
        return limiteNorte.compareTo(limiteSur) > 0 && 
               limiteEste.compareTo(limiteOeste) > 0;
    }

    public String getInfoCompleta() {
        return String.format("%s (%s) - %s - Población: %s - %s", 
                           getNombreCompleto(), 
                           categoriaCiudad,
                           codigoPostal != null ? "CP: " + codigoPostal : "Sin CP",
                           poblacion != null ? String.format("%,d hab.", poblacion) : "No disponible",
                           getResumenLogistico());
    }

    // Métodos para búsquedas geográficas
    public boolean estaEnRadio(BigDecimal latitudCentral, BigDecimal longitudCentral, double radioKm) {
        double distancia = calcularDistanciaA(latitudCentral, longitudCentral);
        return distancia <= radioKm;
    }

    public boolean esAccesibleParaCamiones() {
        return restriccionesVehiculos == null || 
               !restriccionesVehiculos.toLowerCase().contains("prohibido") &&
               !restriccionesVehiculos.toLowerCase().contains("restringido");
    }
}