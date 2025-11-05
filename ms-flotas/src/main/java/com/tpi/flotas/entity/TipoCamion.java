package com.tpi.flotas.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;

@Entity
@Table(name = "tipos_camion", schema = "flotas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TipoCamion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false, unique = true, length = 50)
    private String nombre;

    @Column(name = "descripcion")
    private String descripcion;

    @Column(name = "capacidad_peso_min", nullable = false, precision = 10, scale = 2)
    private BigDecimal capacidadPesoMin;

    @Column(name = "capacidad_peso_max", nullable = false, precision = 10, scale = 2)
    private BigDecimal capacidadPesoMax;

    @Column(name = "capacidad_volumen_min", nullable = false, precision = 10, scale = 2)
    private BigDecimal capacidadVolumenMin;

    @Column(name = "capacidad_volumen_max", nullable = false, precision = 10, scale = 2)
    private BigDecimal capacidadVolumenMax;

    @Column(name = "activo")
    @Builder.Default
    private Boolean activo = true;

    @PrePersist
    protected void onCreate() {
        if (activo == null) activo = true;
    }
}
