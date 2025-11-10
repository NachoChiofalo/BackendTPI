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

/**
 * Entidad DistanciaCalculada - Cache de distancias calculadas entre ubicaciones
 * Esquema: localizaciones
 */
@Entity
@Table(name = "distancias_calculadas", schema = "localizaciones", 
       uniqueConstraints = {
           @UniqueConstraint(columnNames = {"latitud_origen", "longitud_origen", 
                                          "latitud_destino", "longitud_destino"})
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DistanciaCalculada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Coordenadas de origen
    @Column(name = "latitud_origen", nullable = false, precision = 10, scale = 7)
    private BigDecimal latitudOrigen;

    @Column(name = "longitud_origen", nullable = false, precision = 10, scale = 7)
    private BigDecimal longitudOrigen;

    // Coordenadas de destino
    @Column(name = "latitud_destino", nullable = false, precision = 10, scale = 7)
    private BigDecimal latitudDestino;

    @Column(name = "longitud_destino", nullable = false, precision = 10, scale = 7)
    private BigDecimal longitudDestino;


    // Resultados del cálculo
    @Column(name = "distancia_km", nullable = false, precision = 10, scale = 3)
    private BigDecimal distanciaKm;

    @Column(name = "duracion_minutos", precision = 8, scale = 1)
    private BigDecimal duracionMinutos;


    @Column(name = "distancia_lineal_km", precision = 10, scale = 3)
    private BigDecimal distanciaLinealKm; // Distancia en línea recta (haversine)

    // Información de la fuente del cálculo
    @Enumerated(EnumType.STRING)
    @Column(name = "fuente_calculo", nullable = false, length = 20)
    private FuenteCalculo fuenteCalculo;

    @Column(name = "proveedor_api", length = 50)
    private String proveedorApi; // Google Maps, MapBox, etc.

    // Detalles de la ruta (si disponible)
    @Column(name = "ruta_json", columnDefinition = "TEXT")
    private String rutaJson; // JSON con detalles de la ruta

    @Column(name = "instrucciones_navegacion", columnDefinition = "TEXT")
    private String instruccionesNavegacion;

    // Condiciones del cálculo
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_transporte", length = 15)
    @Builder.Default
    private TipoTransporte tipoTransporte = TipoTransporte.CAMION;

    @Column(name = "evitar_peajes")
    @Builder.Default
    private Boolean evitarPeajes = false;

    @Column(name = "evitar_autopistas")
    @Builder.Default
    private Boolean evitarAutopistas = false;

    @Column(name = "momento_calculo")
    private LocalDateTime momentoCalculo; // Momento específico para traffic-aware

    // Información de tráfico
    @Column(name = "considerado_trafico")
    @Builder.Default
    private Boolean consideradoTrafico = false;

    @Column(name = "factor_trafico", precision = 4, scale = 2)
    private BigDecimal factorTrafico; // Multiplicador por tráfico

    // Costo estimado de combustible
    @Column(name = "consumo_combustible_estimado", precision = 8, scale = 3)
    private BigDecimal consumoCombustibleEstimado; // litros

    @Column(name = "costo_peajes_estimado", precision = 10, scale = 2)
    private BigDecimal costoPeajesEstimado;

    // Validez y confiabilidad del cálculo
    @Column(name = "validez_horas", nullable = false)
    @Builder.Default
    private Integer validezHoras = 24; // Horas que el cálculo se considera válido

    @Column(name = "fecha_expiracion")
    private LocalDateTime fechaExpiracion;

    @Column(name = "precision_estimada", precision = 5, scale = 2)
    private BigDecimal precisionEstimada; // Porcentaje de precisión estimado

    @Column(name = "numero_usos")
    @Builder.Default
    private Integer numeroUsos = 1;

    // Relaciones a Ubicacion (origen/destino)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ubicacion_origen_id")
    private Ubicacion ubicacionOrigen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ubicacion_destino_id")
    private Ubicacion ubicacionDestino;

    // Estado de validación (usa enum compartido)
    @Enumerated(EnumType.STRING)
    @Column(name = "estado_validacion", length = 20)
    private EstadoValidacion estadoValidacion;

    // Información adicional
    @Column(name = "pais_origen", length = 3)
    private String paisOrigen; // Código ISO del país

    @Column(name = "pais_destino", length = 3)
    private String paisDestino;

    @Column(name = "cruza_fronteras")
    @Builder.Default
    private Boolean cruzaFronteras = false;

    @Column(name = "observaciones", length = 500)
    private String observaciones;

    // Auditoría
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Enums
    public enum FuenteCalculo {
        API_EXTERNA,        // Calculado via API externa (Google Maps, etc.)
        CALCULO_INTERNO,    // Calculado internamente (haversine, etc.)
        MANUAL,            // Ingresado manualmente
        IMPORTADO         // Importado desde otra fuente
    }

    public enum TipoTransporte {
        CAMION,           // Optimizado para camiones
        VEHICULO_LIVIANO, // Para vehículos livianos
        CAMINANDO,        // A pie
        BICICLETA        // En bicicleta
    }

    // Métodos de negocio
    public boolean estaVigente() {
        return fechaExpiracion == null || LocalDateTime.now().isBefore(fechaExpiracion);
    }

    

    public void incrementarUso() {
        this.numeroUsos++;
    }

    public void establecerVigencia() {
        this.fechaExpiracion = LocalDateTime.now().plusHours(validezHoras);
    }

    public void calcularDistanciaLineal() {
        if (latitudOrigen != null && longitudOrigen != null && 
            latitudDestino != null && longitudDestino != null) {
            
            double lat1 = Math.toRadians(latitudOrigen.doubleValue());
            double lon1 = Math.toRadians(longitudOrigen.doubleValue());
            double lat2 = Math.toRadians(latitudDestino.doubleValue());
            double lon2 = Math.toRadians(longitudDestino.doubleValue());

            double dLat = lat2 - lat1;
            double dLon = lon2 - lon1;

            double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                       Math.cos(lat1) * Math.cos(lat2) *
                       Math.sin(dLon / 2) * Math.sin(dLon / 2);

            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
            
            final double RADIO_TIERRA_KM = 6371.0;
            double distancia = RADIO_TIERRA_KM * c;
            
            this.distanciaLinealKm = new BigDecimal(distancia).setScale(3, BigDecimal.ROUND_HALF_UP);
        }
    }

    public BigDecimal calcularFactorDesviacion() {
        if (distanciaLinealKm == null || distanciaKm == null || 
            distanciaLinealKm.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ONE;
        }
        
        return distanciaKm.divide(distanciaLinealKm, 3, BigDecimal.ROUND_HALF_UP);
    }

    public BigDecimal calcularVelocidadPromedio() {
        if (distanciaKm == null || duracionMinutos == null || 
            duracionMinutos.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        
        // Convertir minutos a horas y calcular km/h
        BigDecimal horas = duracionMinutos.divide(new BigDecimal("60"), 3, BigDecimal.ROUND_HALF_UP);
        return distanciaKm.divide(horas, 2, BigDecimal.ROUND_HALF_UP);
    }

    // Compatibilidad con DTOs/servicios que usan 'tiempoEstimadoMinutos' como Integer
    public Integer getTiempoEstimadoMinutos() {
        return duracionMinutos == null ? null : duracionMinutos.intValue();
    }

    public void setTiempoEstimadoMinutos(Integer minutos) {
        this.duracionMinutos = minutos == null ? null : new BigDecimal(minutos);
    }

    public boolean esRutaInternacional() {
        return cruzaFronteras || 
               (paisOrigen != null && paisDestino != null && !paisOrigen.equals(paisDestino));
    }

    public boolean requiereActualizacion() {
        return !estaVigente() || 
               (consideradoTrafico && momentoCalculo != null && 
                momentoCalculo.isBefore(LocalDateTime.now().minusHours(2)));
    }

    public String getCoordenadaOrigen() {
        return latitudOrigen + "," + longitudOrigen;
    }

    public String getCoordenadaDestino() {
        return latitudDestino + "," + longitudDestino;
    }

    public String getResumenRuta() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%.2f km", distanciaKm));
        
        if (duracionMinutos != null) {
            int horas = duracionMinutos.intValue() / 60;
            int minutos = duracionMinutos.intValue() % 60;
            if (horas > 0) {
                sb.append(String.format(" (%dh %dm)", horas, minutos));
            } else {
                sb.append(String.format(" (%dm)", minutos));
            }
        }
        
        if (factorTrafico != null && consideradoTrafico) {
            sb.append(String.format(" - Factor tráfico: %.2f", factorTrafico));
        }
        
        return sb.toString();
    }

    public BigDecimal estimarCostoCombustible(BigDecimal consumoPorKm, BigDecimal precioCombustible) {
        if (distanciaKm == null || consumoPorKm == null || precioCombustible == null) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal consumoTotal = distanciaKm.multiply(consumoPorKm);
        return consumoTotal.multiply(precioCombustible);
    }

    public boolean esCalculoConfiable() {
        return fuenteCalculo == FuenteCalculo.API_EXTERNA && 
               estaVigente() && 
               precisionEstimada != null && 
               precisionEstimada.compareTo(new BigDecimal("85")) >= 0;
    }

    // Método estático para crear una clave única
    public static String generarClave(BigDecimal latOrigen, BigDecimal lngOrigen, 
                                    BigDecimal latDestino, BigDecimal lngDestino) {
        return String.format("%.7f,%.7f_%.7f,%.7f", 
                           latOrigen, lngOrigen, latDestino, lngDestino);
    }

    public String getClave() {
        return generarClave(latitudOrigen, longitudOrigen, latitudDestino, longitudDestino);
    }

    // Validaciones
    public boolean validarCoordenadas() {
        return latitudOrigen != null && longitudOrigen != null &&
               latitudDestino != null && longitudDestino != null &&
               latitudOrigen.compareTo(latitudDestino) != 0 ||
               longitudOrigen.compareTo(longitudDestino) != 0; // No son el mismo punto
    }

    public boolean validarResultados() {
        return distanciaKm != null && distanciaKm.compareTo(BigDecimal.ZERO) > 0;
    }

    @PrePersist
    @PreUpdate
    public void prePersistOrUpdate() {
        calcularDistanciaLineal();
        establecerVigencia();
        
        if (momentoCalculo == null) {
            momentoCalculo = LocalDateTime.now();
        }
    }
}