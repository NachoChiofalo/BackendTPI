package com.tpi.precios.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CalculoPrecio {

    private Long calculoId;
    private Integer tarifaId;
    private BigDecimal distanciaKm;
    private BigDecimal pesoKg;
    private BigDecimal volumenM3;
    private BigDecimal precioBase;
    private BigDecimal precioTotal;
    private BigDecimal precioFinal;
    private LocalDateTime fechaCalculo;
    private TipoCalculo tipoCalculo;
    private EstadoCalculo estadoCalculo;
    private String observaciones;

    public enum TipoCalculo {
        ESTIMACION,
        COTIZACION,
        FACTURACION
    }

    public enum EstadoCalculo {
        CALCULADO,
        APROBADO,
        RECHAZADO,
        VENCIDO
    }

    // Método para calcular el precio por peso
    public BigDecimal calcularPorPeso(BigDecimal precioKmKg) {
        if (distanciaKm != null && pesoKg != null && precioKmKg != null) {
            return distanciaKm.multiply(pesoKg).multiply(precioKmKg);
        }
        return BigDecimal.ZERO;
    }

    // Método para calcular el precio por volumen
    public BigDecimal calcularPorVolumen(BigDecimal precioKmM3) {
        if (distanciaKm != null && volumenM3 != null && precioKmM3 != null) {
            return distanciaKm.multiply(volumenM3).multiply(precioKmM3);
        }
        return BigDecimal.ZERO;
    }
}
