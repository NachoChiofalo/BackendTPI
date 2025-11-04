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
 * Entidad Tarifa - Gestiona las tarifas y costos del sistema
 * Esquema: precios
 */
@Entity
@Table(name = "tarifas", schema = "precios")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tarifa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String codigo; // Código identificador de la tarifa

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 500)
    private String descripcion;

    // Tarifas base por kilómetro
    @Column(name = "costo_base_km", nullable = false, precision = 8, scale = 4)
    private BigDecimal costoBaseKm; // Costo base por kilómetro

    @Column(name = "costo_km_contenedor_20", precision = 8, scale = 4)
    private BigDecimal costoKmContenedor20; // Costo adicional por km para contenedores 20'

    @Column(name = "costo_km_contenedor_40", precision = 8, scale = 4)
    private BigDecimal costoKmContenedor40; // Costo adicional por km para contenedores 40'

    // Multiplicadores por tipo de contenedor
    @Column(name = "multiplicador_refrigerado", precision = 6, scale = 4)
    @Builder.Default
    private BigDecimal multiplicadorRefrigerado = new BigDecimal("1.5"); // +50%

    @Column(name = "multiplicador_especial", precision = 6, scale = 4)
    @Builder.Default
    private BigDecimal multiplicadorEspecial = new BigDecimal("2.0"); // +100%

    // Tarifas por peso y volumen
    @Column(name = "costo_tonelada", precision = 8, scale = 4)
    private BigDecimal costoTonelada; // Costo por tonelada transportada

    @Column(name = "costo_metro_cubico", precision = 8, scale = 4)
    private BigDecimal costoMetroCubico; // Costo por metro cúbico

    // Precios de combustible
    @Column(name = "precio_combustible_litro", nullable = false, precision = 6, scale = 3)
    private BigDecimal precioCombustibleLitro;

    // Cargo fijo por gestión
    @Column(name = "cargo_gestion_fijo", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal cargoGestionFijo = new BigDecimal("500.00");

    @Column(name = "cargo_gestion_por_tramo", precision = 8, scale = 2)
    @Builder.Default
    private BigDecimal cargoGestionPorTramo = new BigDecimal("150.00");

    // Costos de depósito
    @Column(name = "costo_estadia_deposito_dia", precision = 8, scale = 2)
    private BigDecimal costoEstadiaDepositoDia; // Costo base por día en depósito

    // Multiplicadores por urgencia/prioridad
    @Column(name = "multiplicador_urgente", precision = 6, scale = 4)
    @Builder.Default
    private BigDecimal multiplicadorUrgente = new BigDecimal("1.8"); // +80%

    @Column(name = "multiplicador_alta_prioridad", precision = 6, scale = 4)
    @Builder.Default
    private BigDecimal multiplicadorAltaPrioridad = new BigDecimal("1.3"); // +30%

    // Costos adicionales
    @Column(name = "costo_carga_descarga", precision = 8, scale = 2)
    @Builder.Default
    private BigDecimal costoCargaDescarga = new BigDecimal("200.00");

    @Column(name = "costo_demora_hora", precision = 8, scale = 2)
    @Builder.Default
    private BigDecimal costoDemoraHora = new BigDecimal("50.00");

    @Column(name = "costo_fin_semana_extra", precision = 8, scale = 2)
    @Builder.Default
    private BigDecimal costoFinSemanaExtra = new BigDecimal("300.00");

    // Vigencia de la tarifa
    @Column(name = "fecha_vigencia_desde", nullable = false)
    private LocalDateTime fechaVigenciaDesde;

    @Column(name = "fecha_vigencia_hasta")
    private LocalDateTime fechaVigenciaHasta;

    @Column(name = "activa")
    @Builder.Default
    private Boolean activa = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_tarifa", length = 20)
    @Builder.Default
    private TipoTarifa tipoTarifa = TipoTarifa.GENERAL;

    @Enumerated(EnumType.STRING)
    @Column(name = "modalidad_calculo", length = 20)
    @Builder.Default
    private ModalidadCalculo modalidadCalculo = ModalidadCalculo.POR_DISTANCIA;

    // Auditoría
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Enums
    public enum TipoTarifa {
        GENERAL,        // Tarifa general para todos los clientes
        CORPORATIVA,    // Tarifa especial para clientes corporativos
        PROMOCIONAL,    // Tarifa promocional temporal
        VIP,           // Tarifa para clientes VIP
        URGENTE       // Tarifa especial para servicios urgentes
    }

    public enum ModalidadCalculo {
        POR_DISTANCIA,      // Cálculo basado en distancia
        POR_PESO,           // Cálculo basado en peso
        POR_VOLUMEN,        // Cálculo basado en volumen
        MIXTO,             // Combinación de factores
        TARIFA_FIJA       // Tarifa fija independiente de factores
    }

    // Métodos de negocio
    public boolean estaVigente() {
        LocalDateTime ahora = LocalDateTime.now();
        return activa && 
               ahora.isAfter(fechaVigenciaDesde) && 
               (fechaVigenciaHasta == null || ahora.isBefore(fechaVigenciaHasta));
    }

    public void activar() {
        this.activa = true;
        if (this.fechaVigenciaDesde == null) {
            this.fechaVigenciaDesde = LocalDateTime.now();
        }
    }

    public void desactivar() {
        this.activa = false;
        this.fechaVigenciaHasta = LocalDateTime.now();
    }

    public BigDecimal calcularCostoBase(BigDecimal distanciaKm, String tipoContenedor, 
                                      BigDecimal peso, BigDecimal volumen) {
        BigDecimal costo = BigDecimal.ZERO;

        switch (modalidadCalculo) {
            case POR_DISTANCIA:
                costo = calcularPorDistancia(distanciaKm, tipoContenedor);
                break;
            case POR_PESO:
                costo = calcularPorPeso(peso);
                break;
            case POR_VOLUMEN:
                costo = calcularPorVolumen(volumen);
                break;
            case MIXTO:
                costo = calcularMixto(distanciaKm, tipoContenedor, peso, volumen);
                break;
            case TARIFA_FIJA:
                costo = cargoGestionFijo;
                break;
        }

        return costo;
    }

    private BigDecimal calcularPorDistancia(BigDecimal distanciaKm, String tipoContenedor) {
        BigDecimal costo = costoBaseKm.multiply(distanciaKm);
        
        if (tipoContenedor != null) {
            if (tipoContenedor.contains("20")) {
                costo = costo.add(costoKmContenedor20.multiply(distanciaKm));
            } else if (tipoContenedor.contains("40")) {
                costo = costo.add(costoKmContenedor40.multiply(distanciaKm));
            }
            
            if (tipoContenedor.contains("REFRIGERADO")) {
                costo = costo.multiply(multiplicadorRefrigerado);
            } else if (tipoContenedor.contains("ESPECIAL") || 
                      tipoContenedor.contains("OPEN_TOP") || 
                      tipoContenedor.contains("FLAT_RACK")) {
                costo = costo.multiply(multiplicadorEspecial);
            }
        }
        
        return costo;
    }

    private BigDecimal calcularPorPeso(BigDecimal peso) {
        if (peso == null || costoTonelada == null) return BigDecimal.ZERO;
        return costoTonelada.multiply(peso);
    }

    private BigDecimal calcularPorVolumen(BigDecimal volumen) {
        if (volumen == null || costoMetroCubico == null) return BigDecimal.ZERO;
        return costoMetroCubico.multiply(volumen);
    }

    private BigDecimal calcularMixto(BigDecimal distanciaKm, String tipoContenedor, 
                                   BigDecimal peso, BigDecimal volumen) {
        BigDecimal costoPorDistancia = calcularPorDistancia(distanciaKm, tipoContenedor);
        BigDecimal costoPorPeso = calcularPorPeso(peso);
        BigDecimal costoPorVolumen = calcularPorVolumen(volumen);
        
        // Tomar el mayor de los tres factores
        return costoPorDistancia.max(costoPorPeso).max(costoPorVolumen);
    }

    public BigDecimal aplicarMultiplicadorPrioridad(BigDecimal costoBase, String prioridad) {
        if (prioridad == null) return costoBase;
        
        switch (prioridad.toUpperCase()) {
            case "URGENTE":
                return costoBase.multiply(multiplicadorUrgente);
            case "ALTA":
                return costoBase.multiply(multiplicadorAltaPrioridad);
            default:
                return costoBase;
        }
    }

    public BigDecimal calcularCostoCombustible(BigDecimal consumoLitros) {
        if (consumoLitros == null) return BigDecimal.ZERO;
        return precioCombustibleLitro.multiply(consumoLitros);
    }

    public BigDecimal calcularCostoGestion(Integer cantidadTramos) {
        BigDecimal costo = cargoGestionFijo;
        if (cantidadTramos != null && cantidadTramos > 0) {
            costo = costo.add(cargoGestionPorTramo.multiply(new BigDecimal(cantidadTramos)));
        }
        return costo;
    }

    public String getResumenTarifa() {
        return String.format("Tarifa %s (%s): $%.2f/km base - %s", 
                           codigo, nombre, costoBaseKm, 
                           estaVigente() ? "VIGENTE" : "NO VIGENTE");
    }

    // Validaciones
    public boolean validarRangosFecha() {
        if (fechaVigenciaDesde == null) return false;
        if (fechaVigenciaHasta != null) {
            return fechaVigenciaHasta.isAfter(fechaVigenciaDesde);
        }
        return true;
    }

    public boolean validarCostos() {
        return costoBaseKm != null && costoBaseKm.compareTo(BigDecimal.ZERO) > 0 &&
               precioCombustibleLitro != null && precioCombustibleLitro.compareTo(BigDecimal.ZERO) > 0;
    }
}