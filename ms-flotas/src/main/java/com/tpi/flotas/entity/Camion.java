package com.tpi.flotas.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;

@Entity
@Table(name = "Camion")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Camion {

    @Id
    @Column(name = "dominio", length = 10)
    private String dominio;

    @Column(name = "disponible", nullable = false)
    private Boolean disponible;

    @Column(name = "capacidad_peso", nullable = false, precision = 10, scale = 2)
    private BigDecimal capacidadPeso;

    @Column(name = "capacidad_volumen", nullable = false, precision = 10, scale = 2)
    private BigDecimal capacidadVolumen;

    @Column(name = "costo_base_km", nullable = false, precision = 10, scale = 2)
    private BigDecimal costoBaseKm;

    @Column(name = "consumo_promedio", nullable = false, precision = 10, scale = 2)
    private BigDecimal consumoPromedio;
}
