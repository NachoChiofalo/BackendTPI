package com.tpi.precios.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "Tarifa")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tarifa {

    @Id
    @Column(name = "tarifa_id")
    private Integer tarifaId;

    @Column(name = "precio_combustible_litro", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioCombustibleLitro;

    @Column(name = "precio_km_kg", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioKmKg;

    @Column(name = "precio_km_m3", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioKmM3;

    @Column(name = "fecha_vigencia_inicio", nullable = false)
    private LocalDate fechaVigenciaInicio;

    @Column(name = "fecha_vigencia_fin", nullable = false)
    private LocalDate fechaVigenciaFin;

    @Column(name = "precio_tramo", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioTramo;

    @Enumerated(EnumType.STRING)
    @Transient
    private TipoTarifa tipoTarifa;

    @Enumerated(EnumType.STRING)
    @Transient
    private ModalidadCalculo modalidadCalculo;

    public enum TipoTarifa {
        BASICA,
        PREMIUM,
        EXPRESS,
        ECONOMICA
    }

    public enum ModalidadCalculo {
        POR_PESO,
        POR_VOLUMEN,
        POR_DISTANCIA,
        MIXTA
    }

    // Método para verificar si la tarifa está vigente
    public boolean esVigente() {
        LocalDate hoy = LocalDate.now();
        return !hoy.isBefore(fechaVigenciaInicio) && !hoy.isAfter(fechaVigenciaFin);
    }

    // Método para verificar si la tarifa está vigente en una fecha específica
    public boolean esVigenteEn(LocalDate fecha) {
        return !fecha.isBefore(fechaVigenciaInicio) && !fecha.isAfter(fechaVigenciaFin);
    }
}
