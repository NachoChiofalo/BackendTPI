package com.tpi.flotas.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Entity
@Table(name = "camiones", schema = "flotas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Camion {

    @Id
    @Column(length = 20)
    private String dominio;

    @Column(nullable = false, length = 50)
    private String marca;

    @Column(nullable = false, length = 50)
    private String modelo;

    @Column(name = "año_fabricacion")
    private Integer añoFabricacion;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_camion", nullable = false, length = 20)
    private TipoCamion tipoCamion;

    @Column(name = "capacidad_peso", nullable = false, precision = 10, scale = 2)
    private BigDecimal capacidadPeso;

    @Column(name = "capacidad_volumen", nullable = false, precision = 10, scale = 2)
    private BigDecimal capacidadVolumen;

    @Column(name = "consumo_combustible", nullable = false, precision = 8, scale = 2)
    private BigDecimal consumoCombustible;

    @Column(name = "costo_base_km", nullable = false, precision = 8, scale = 2)
    private BigDecimal costoBaseKm;

    @Column(name = "costo_mantenimiento_diario", precision = 8, scale = 2)
    private BigDecimal costoMantenimientoDiario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private EstadoCamion estado = EstadoCamion.DISPONIBLE;

    @Enumerated(EnumType.STRING)
    @Column(name = "condicion_mecanica", length = 15)
    private CondicionMecanica condicionMecanica = CondicionMecanica.BUENA;

    @Column(name = "numero_seguro", length = 50)
    private String numeroSeguro;

    @Column(name = "fecha_vencimiento_seguro")
    private LocalDateTime fechaVencimientoSeguro;

    @Column(name = "fecha_ultimo_service")
    private LocalDateTime fechaUltimoService;

    @Column(name = "kilometraje_actual")
    private Long kilometrajeActual = 0L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transportista_id")
    private Transportista transportista;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deposito_base_id")
    private Deposito depositoBase;

    @Column(name = "deposito_base_id", insertable = false, updatable = false)
    private Long depositoBaseId;

    @Column(name = "ubicacion_actual_lat", precision = 10, scale = 7)
    private BigDecimal ubicacionActualLat;

    @Column(name = "ubicacion_actual_lng", precision = 10, scale = 7)
    private BigDecimal ubicacionActualLng;

    @Column(name = "ubicacion_descripcion", length = 200)
    private String ubicacionDescripcion;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Enums
    public enum TipoCamion {
        RIGIDO_PEQUEÑO, RIGIDO_MEDIANO, RIGIDO_GRANDE,
        SEMI_REMOLQUE, CAMION_REMOLQUE, ESPECIAL
    }

    public enum EstadoCamion {
        DISPONIBLE, EN_VIAJE, EN_MANTENIMIENTO, AVERIADO, INACTIVO
    }

    public enum CondicionMecanica {
        EXCELENTE, BUENA, REGULAR, MALA, CRITICA
    }
}
