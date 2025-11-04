package com.tpi.flotas.entity;

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
 * Entidad Camion - Representa los vehículos de transporte
 * Esquema: flotas
 */
@Entity
@Table(name = "camiones", schema = "flotas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Camion {

    @Id
    @Column(name = "dominio", length = 20)
    private String dominio; // Patente del camión (PK personalizada)

    @Column(nullable = false, length = 50)
    private String marca;

    @Column(nullable = false, length = 50)
    private String modelo;

    @Column(name = "año_fabricacion")
    private Integer añoFabricacion;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_camion", nullable = false, length = 20)
    private TipoCamion tipoCamion;

    // Capacidades
    @Column(name = "capacidad_peso", nullable = false, precision = 10, scale = 2)
    private BigDecimal capacidadPeso; // en toneladas

    @Column(name = "capacidad_volumen", nullable = false, precision = 10, scale = 2)
    private BigDecimal capacidadVolumen; // en metros cúbicos

    // Consumo y costos
    @Column(name = "consumo_combustible", nullable = false, precision = 8, scale = 2)
    private BigDecimal consumoCombustible; // litros por km

    @Column(name = "costo_base_km", nullable = false, precision = 8, scale = 2)
    private BigDecimal costoBaseKm; // costo base por kilómetro

    @Column(name = "costo_mantenimiento_diario", precision = 8, scale = 2)
    private BigDecimal costoMantenimientoDiario;

    // Estado y disponibilidad
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    @Builder.Default
    private EstadoCamion estado = EstadoCamion.DISPONIBLE;

    @Enumerated(EnumType.STRING)
    @Column(name = "condicion_mecanica", length = 15)
    @Builder.Default
    private CondicionMecanica condicionMecanica = CondicionMecanica.BUENA;

    // Seguros y documentación
    @Column(name = "numero_seguro", length = 50)
    private String numeroSeguro;

    @Column(name = "fecha_vencimiento_seguro")
    private LocalDateTime fechaVencimientoSeguro;

    @Column(name = "fecha_ultimo_service")
    private LocalDateTime fechaUltimoService;

    @Column(name = "kilometraje_actual")
    private Long kilometrajeActual;

    // Relaciones
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transportista_id")
    private Transportista transportista;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deposito_base_id")
    private Deposito depositoBase; // Depósito donde opera habitualmente

    // Ubicación actual (para seguimiento)
    @Column(name = "ubicacion_actual_lat", precision = 10, scale = 7)
    private BigDecimal ubicacionActualLatitud;

    @Column(name = "ubicacion_actual_lng", precision = 10, scale = 7)
    private BigDecimal ubicacionActualLongitud;

    @Column(name = "ubicacion_descripcion")
    private String ubicacionDescripcion;

    @Column(name = "activo")
    @Builder.Default
    private Boolean activo = true;

    // Auditoría
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Enums
    public enum TipoCamion {
        RIGIDO_PEQUEÑO,     // Hasta 7 toneladas
        RIGIDO_MEDIANO,     // 7-18 toneladas
        RIGIDO_GRANDE,      // 18-26 toneladas
        SEMI_REMOLQUE,      // Tractocamión con semi
        CAMION_REMOLQUE,    // Camión con remolque
        ESPECIAL           // Equipos especiales
    }

    public enum EstadoCamion {
        DISPONIBLE,         // Libre para asignaciones
        EN_VIAJE,          // Transportando carga
        EN_MANTENIMIENTO,  // En taller
        AVERIADO,          // Fuera de servicio por avería
        INACTIVO          // Temporalmente fuera de servicio
    }

    public enum CondicionMecanica {
        EXCELENTE,
        BUENA,
        REGULAR,
        MALA,
        CRITICA
    }

    // Métodos de conveniencia
    public boolean estaDisponibleParaViaje() {
        return activo && 
               estado == EstadoCamion.DISPONIBLE && 
               condicionMecanica != CondicionMecanica.CRITICA &&
               condicionMecanica != CondicionMecanica.MALA;
    }

    public boolean puedeTransportar(BigDecimal peso, BigDecimal volumen) {
        if (peso == null || volumen == null) return false;
        return capacidadPeso.compareTo(peso) >= 0 && 
               capacidadVolumen.compareTo(volumen) >= 0;
    }

    public BigDecimal calcularCostoCombustible(BigDecimal distanciaKm, BigDecimal precioCombustible) {
        if (distanciaKm == null || precioCombustible == null) return BigDecimal.ZERO;
        return consumoCombustible.multiply(distanciaKm).multiply(precioCombustible);
    }

    public BigDecimal calcularCostoTotal(BigDecimal distanciaKm, BigDecimal precioCombustible, Integer diasEstadia) {
        BigDecimal costoKm = costoBaseKm.multiply(distanciaKm);
        BigDecimal costoCombustible = calcularCostoCombustible(distanciaKm, precioCombustible);
        BigDecimal costoEstadia = BigDecimal.ZERO;
        
        if (diasEstadia != null && diasEstadia > 0 && costoMantenimientoDiario != null) {
            costoEstadia = costoMantenimientoDiario.multiply(new BigDecimal(diasEstadia));
        }
        
        return costoKm.add(costoCombustible).add(costoEstadia);
    }

    public void actualizarUbicacion(BigDecimal latitud, BigDecimal longitud, String descripcion) {
        this.ubicacionActualLatitud = latitud;
        this.ubicacionActualLongitud = longitud;
        this.ubicacionDescripcion = descripcion;
    }

    public void registrarKilometraje(Long kilometros) {
        if (kilometros != null && kilometros > 0) {
            this.kilometrajeActual = (this.kilometrajeActual != null ? this.kilometrajeActual : 0L) + kilometros;
        }
    }

    public boolean necesitaMantenimiento() {
        if (fechaUltimoService == null) return true;
        
        // Mantenimiento cada 6 meses o 10,000 km
        LocalDateTime fechaLimite = fechaUltimoService.plusMonths(6);
        Long kmLimite = 10000L;
        
        return LocalDateTime.now().isAfter(fechaLimite) || 
               (kilometrajeActual != null && kilometrajeActual > kmLimite);
    }

    public boolean tieneVigenteSeguro() {
        return fechaVencimientoSeguro != null && 
               fechaVencimientoSeguro.isAfter(LocalDateTime.now());
    }

    public String getInfoCompleta() {
        return String.format("%s - %s %s (%d) - %s", 
                            dominio, marca, modelo, añoFabricacion, tipoCamion);
    }

    // Validaciones de negocio específicas para tipos de contenedores
    public boolean puedeTransportarTipoContenedor(String tipoContenedor) {
        if (tipoContenedor == null) return false;
        
        switch (tipoCamion) {
            case RIGIDO_PEQUEÑO:
                return tipoContenedor.contains("20") && !tipoContenedor.contains("REFRIGERADO");
            case RIGIDO_MEDIANO:
                return !tipoContenedor.contains("40") || tipoContenedor.contains("REFRIGERADO");
            case RIGIDO_GRANDE:
            case SEMI_REMOLQUE:
            case CAMION_REMOLQUE:
                return true; // Pueden transportar cualquier tipo
            case ESPECIAL:
                return tipoContenedor.contains("REFRIGERADO") || 
                       tipoContenedor.contains("OPEN_TOP") || 
                       tipoContenedor.contains("FLAT_RACK");
            default:
                return false;
        }
    }
}