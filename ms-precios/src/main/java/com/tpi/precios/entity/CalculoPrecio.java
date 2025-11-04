package com.tpi.precios.entity;

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
 * Entidad CalculoPrecio - Almacena los cálculos de precio realizados
 * Esquema: precios
 */
@Entity
@Table(name = "calculos_precios", schema = "precios")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CalculoPrecio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String codigo; // Código único del cálculo

    // Referencias a otras entidades
    @Column(name = "solicitud_id", nullable = false)
    private Long solicitudId;

    @Column(name = "ruta_id")
    private Long rutaId;

    @Column(name = "tarifa_id", nullable = false)
    private Long tarifaId;

    // Tipo de cálculo
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_calculo", nullable = false, length = 15)
    private TipoCalculo tipoCalculo;

    // Datos base del cálculo
    @Column(name = "distancia_total_km", precision = 10, scale = 2)
    private BigDecimal distanciaTotalKm;

    @Column(name = "peso_contenedor", precision = 10, scale = 2)
    private BigDecimal pesoContenedor;

    @Column(name = "volumen_contenedor", precision = 10, scale = 2)
    private BigDecimal volumenContenedor;

    @Column(name = "tipo_contenedor", length = 30)
    private String tipoContenedor;

    @Column(name = "cantidad_tramos")
    private Integer cantidadTramos;

    @Column(name = "cantidad_depositos")
    private Integer cantidadDepositos;

    @Column(name = "dias_estimados_estadia")
    private Integer diasEstimadosEstadia;

    @Column(name = "prioridad_solicitud", length = 15)
    private String prioridadSolicitud;

    // Componentes del costo
    @Column(name = "costo_base", precision = 12, scale = 2)
    private BigDecimal costoBase;

    @Column(name = "costo_combustible", precision = 10, scale = 2)
    private BigDecimal costoCombustible;

    @Column(name = "costo_gestion", precision = 10, scale = 2)
    private BigDecimal costoGestion;

    @Column(name = "costo_estadia_depositos", precision = 10, scale = 2)
    private BigDecimal costoEstadiaDepositos;

    @Column(name = "costo_carga_descarga", precision = 8, scale = 2)
    private BigDecimal costoCargaDescarga;

    @Column(name = "costo_demoras", precision = 8, scale = 2)
    private BigDecimal costoDemoras;

    @Column(name = "costo_fin_semana", precision = 8, scale = 2)
    private BigDecimal costoFinSemana;

    @Column(name = "costo_peajes", precision = 8, scale = 2)
    private BigDecimal costoPeajes;

    // Multiplicadores aplicados
    @Column(name = "multiplicador_prioridad", precision = 6, scale = 4)
    @Builder.Default
    private BigDecimal multiplicadorPrioridad = BigDecimal.ONE;

    @Column(name = "multiplicador_tipo_contenedor", precision = 6, scale = 4)
    @Builder.Default
    private BigDecimal multiplicadorTipoContenedor = BigDecimal.ONE;

    // Descuentos y ajustes
    @Column(name = "descuento_cliente", precision = 8, scale = 2)
    @Builder.Default
    private BigDecimal descuentoCliente = BigDecimal.ZERO;

    @Column(name = "ajuste_manual", precision = 8, scale = 2)
    @Builder.Default
    private BigDecimal ajusteManual = BigDecimal.ZERO;

    @Column(name = "motivo_ajuste", length = 500)
    private String motivoAjuste;

    // Totales
    @Column(name = "subtotal_antes_ajustes", precision = 12, scale = 2)
    private BigDecimal subtotalAntesAjustes;

    @Column(name = "total_final", precision = 12, scale = 2)
    private BigDecimal totalFinal;

    @Column(name = "impuestos", precision = 10, scale = 2)
    private BigDecimal impuestos;

    @Column(name = "total_con_impuestos", precision = 12, scale = 2)
    private BigDecimal totalConImpuestos;

    // Estado y validez
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    @Builder.Default
    private EstadoCalculo estado = EstadoCalculo.CALCULADO;

    @Column(name = "fecha_validez_hasta")
    private LocalDateTime fechaValidezHasta;

    @Column(name = "aprobado_por", length = 100)
    private String aprobadoPor;

    @Column(name = "fecha_aprobacion")
    private LocalDateTime fechaAprobacion;

    // Observaciones
    @Column(name = "observaciones", length = 1000)
    private String observaciones;

    // Comparación con cálculo real (si existe)
    @Column(name = "costo_real", precision = 12, scale = 2)
    private BigDecimal costoReal;

    @Column(name = "variacion_real", precision = 10, scale = 2)
    private BigDecimal variacionReal; // diferencia entre estimado y real

    @Column(name = "porcentaje_variacion", precision = 6, scale = 2)
    private BigDecimal porcentajeVariacion;

    // Auditoría
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Enums
    public enum TipoCalculo {
        ESTIMADO,       // Cálculo inicial/estimativo
        COTIZACION,     // Cotización formal para el cliente
        REAL,          // Cálculo final/real tras ejecución
        REPROCESO      // Recálculo por cambios
    }

    public enum EstadoCalculo {
        CALCULADO,      // Recién calculado
        ENVIADO,        // Enviado al cliente
        APROBADO,       // Aprobado por el cliente
        RECHAZADO,      // Rechazado por el cliente
        VENCIDO,        // Validez expirada
        FACTURADO      // Ya facturado
    }

    // Métodos de negocio
    public void calcularTotales() {
        // Calcular subtotal sumando todos los componentes
        subtotalAntesAjustes = BigDecimal.ZERO
            .add(costoBase != null ? costoBase : BigDecimal.ZERO)
            .add(costoCombustible != null ? costoCombustible : BigDecimal.ZERO)
            .add(costoGestion != null ? costoGestion : BigDecimal.ZERO)
            .add(costoEstadiaDepositos != null ? costoEstadiaDepositos : BigDecimal.ZERO)
            .add(costoCargaDescarga != null ? costoCargaDescarga : BigDecimal.ZERO)
            .add(costoDemoras != null ? costoDemoras : BigDecimal.ZERO)
            .add(costoFinSemana != null ? costoFinSemana : BigDecimal.ZERO)
            .add(costoPeajes != null ? costoPeajes : BigDecimal.ZERO);

        // Aplicar multiplicadores
        BigDecimal totalConMultiplicadores = subtotalAntesAjustes
            .multiply(multiplicadorPrioridad)
            .multiply(multiplicadorTipoContenedor);

        // Aplicar descuentos y ajustes
        totalFinal = totalConMultiplicadores
            .subtract(descuentoCliente != null ? descuentoCliente : BigDecimal.ZERO)
            .add(ajusteManual != null ? ajusteManual : BigDecimal.ZERO);

        // Calcular impuestos (21% IVA por ejemplo)
        impuestos = totalFinal.multiply(new BigDecimal("0.21"));
        totalConImpuestos = totalFinal.add(impuestos);
    }

    public void aprobar(String aprobadoPor) {
        this.estado = EstadoCalculo.APROBADO;
        this.aprobadoPor = aprobadoPor;
        this.fechaAprobacion = LocalDateTime.now();
    }

    public void rechazar(String motivo) {
        this.estado = EstadoCalculo.RECHAZADO;
        this.observaciones = (this.observaciones != null ? this.observaciones + " | " : "") + 
                           "RECHAZADO: " + motivo;
    }

    public void establecerValidez(int diasValidez) {
        this.fechaValidezHasta = LocalDateTime.now().plusDays(diasValidez);
    }

    public boolean estaVigente() {
        return estado == EstadoCalculo.CALCULADO || estado == EstadoCalculo.ENVIADO ||
               estado == EstadoCalculo.APROBADO &&
               (fechaValidezHasta == null || LocalDateTime.now().isBefore(fechaValidezHasta));
    }

    public void compararConCostoReal(BigDecimal costoRealEjecutado) {
        this.costoReal = costoRealEjecutado;
        if (totalFinal != null && costoRealEjecutado != null) {
            this.variacionReal = costoRealEjecutado.subtract(totalFinal);
            
            if (totalFinal.compareTo(BigDecimal.ZERO) > 0) {
                this.porcentajeVariacion = variacionReal
                    .divide(totalFinal, 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(new BigDecimal("100"));
            }
        }
    }

    public BigDecimal obtenerCostoUnitarioPorKm() {
        if (totalFinal == null || distanciaTotalKm == null || 
            distanciaTotalKm.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return totalFinal.divide(distanciaTotalKm, 4, BigDecimal.ROUND_HALF_UP);
    }

    public BigDecimal obtenerCostoPorTonelada() {
        if (totalFinal == null || pesoContenedor == null || 
            pesoContenedor.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return totalFinal.divide(pesoContenedor, 2, BigDecimal.ROUND_HALF_UP);
    }

    public String getResumenCalculo() {
        return String.format("Cálculo %s: $%.2f (%s) - %s", 
                           codigo, 
                           totalConImpuestos != null ? totalConImpuestos : BigDecimal.ZERO,
                           tipoCalculo, estado);
    }

    public String getDetalleComponentes() {
        StringBuilder sb = new StringBuilder();
        sb.append("Componentes del cálculo:\n");
        sb.append(String.format("- Costo base: $%.2f\n", costoBase != null ? costoBase : BigDecimal.ZERO));
        sb.append(String.format("- Combustible: $%.2f\n", costoCombustible != null ? costoCombustible : BigDecimal.ZERO));
        sb.append(String.format("- Gestión: $%.2f\n", costoGestion != null ? costoGestion : BigDecimal.ZERO));
        sb.append(String.format("- Estadía: $%.2f\n", costoEstadiaDepositos != null ? costoEstadiaDepositos : BigDecimal.ZERO));
        sb.append(String.format("- Subtotal: $%.2f\n", subtotalAntesAjustes != null ? subtotalAntesAjustes : BigDecimal.ZERO));
        sb.append(String.format("- Total final: $%.2f\n", totalFinal != null ? totalFinal : BigDecimal.ZERO));
        sb.append(String.format("- Con impuestos: $%.2f", totalConImpuestos != null ? totalConImpuestos : BigDecimal.ZERO));
        return sb.toString();
    }

    // Validaciones
    public boolean validarComponentes() {
        return solicitudId != null && 
               tarifaId != null &&
               tipoCalculo != null &&
               totalFinal != null &&
               totalFinal.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean requiereAprobacion() {
        // Requiere aprobación si supera cierto monto o tiene ajustes manuales
        BigDecimal montoAprobacion = new BigDecimal("10000.00"); // $10,000
        return (totalFinal != null && totalFinal.compareTo(montoAprobacion) > 0) ||
               (ajusteManual != null && ajusteManual.compareTo(BigDecimal.ZERO) != 0);
    }

    public double calcularPrecisionEstimacion() {
        if (costoReal == null || totalFinal == null || 
            totalFinal.compareTo(BigDecimal.ZERO) == 0) {
            return 0.0;
        }
        
        BigDecimal diferencia = costoReal.subtract(totalFinal).abs();
        BigDecimal precision = BigDecimal.ONE.subtract(
            diferencia.divide(totalFinal, 4, BigDecimal.ROUND_HALF_UP)
        );
        
        return precision.multiply(new BigDecimal("100")).doubleValue();
    }
}