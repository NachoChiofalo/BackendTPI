package com.tpi.flotas.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "camiones", schema = "flotas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Camion {

    @Id
    @Column(name = "dominio", length = 10)
    private String dominio;

    @Column(name = "tipo_camion_id", nullable = false)
    private Long tipoCamionId;

    @Column(name = "marca", length = 50)
    private String marca;

    @Column(name = "modelo", length = 50)
    private String modelo;

    @Column(name = "anio")
    private Integer anio;

    @Column(name = "capacidad_peso", nullable = false, precision = 10, scale = 2)
    private BigDecimal capacidadPeso;

    @Column(name = "capacidad_volumen", nullable = false, precision = 10, scale = 2)
    private BigDecimal capacidadVolumen;

    @Column(name = "consumo_combustible", nullable = false, precision = 5, scale = 2)
    private BigDecimal consumoCombustible;

    @Column(name = "costo_km", nullable = false, precision = 10, scale = 2)
    private BigDecimal costoKm;

    @Column(name = "transportista_id")
    private Long transportistaId;

    @Column(name = "deposito_actual_id")
    private Long depositoActualId;

    @Column(name = "disponible")
    @Builder.Default
    private Boolean disponible = true;

    @Column(name = "activo")
    @Builder.Default
    private Boolean activo = true;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        fechaActualizacion = LocalDateTime.now();
        if (disponible == null) disponible = true;
        if (activo == null) activo = true;
    }

    @PreUpdate
    protected void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }
}
