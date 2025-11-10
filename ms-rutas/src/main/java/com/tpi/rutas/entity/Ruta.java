package com.tpi.rutas.entity;

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
 * Entidad Ruta - Representa la planificación completa de un traslado
 * Esquema: rutas
 */
@Entity
@Table(name = "rutas", schema = "rutas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ruta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String codigo; // Código único de la ruta

    // Relación con solicitud (desde otro microservicio)
    @Column(name = "solicitud_id", nullable = false)
    private Long solicitudId;

    @Column(name = "cantidad_tramos", nullable = false)
    private Integer cantidadTramos = 0;

    @Column(name = "cantidad_depositos", nullable = false)
    @Builder.Default
    private Integer cantidadDepositos = 0;

    // Distancias y tiempos totales
    @Column(name = "distancia_total_km", precision = 10, scale = 2)
    private BigDecimal distanciaTotalKm;

    @Column(name = "tiempo_estimado_total") // en minutos
    private Integer tiempoEstimadoTotal;

    @Column(name = "tiempo_real_total") // en minutos
    private Integer tiempoRealTotal;

    // Costos
    @Column(name = "costo_estimado_total", precision = 12, scale = 2)
    private BigDecimal costoEstimadoTotal;

    @Column(name = "costo_real_total", precision = 12, scale = 2)
    private BigDecimal costoRealTotal;

    // Estado de la ruta
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EstadoRuta estado = EstadoRuta.PLANIFICADA;

    // Fechas de ejecución
    @Column(name = "fecha_inicio_programada")
    private LocalDateTime fechaInicioProgramada;

    @Column(name = "fecha_fin_programada")
    private LocalDateTime fechaFinProgramada;

    @Column(name = "fecha_inicio_real")
    private LocalDateTime fechaInicioReal;

    @Column(name = "fecha_fin_real")
    private LocalDateTime fechaFinReal;

    // Prioridad y tipo de ruta
    @Enumerated(EnumType.STRING)
    @Column(length = 15)
    @Builder.Default
    private PrioridadRuta prioridad = PrioridadRuta.NORMAL;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_ruta", length = 20)
    private TipoRuta tipoRuta;

    // Observaciones y notas
    @Column(length = 1000)
    private String observaciones;

    // Relaciones
    @OneToMany(mappedBy = "ruta", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @OrderBy("orden ASC")
    @Builder.Default
    private List<Tramo> tramos = new ArrayList<>();

    @Column(name = "activa")
    @Builder.Default
    private Boolean activa = true;

    // Auditoría
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Enums
    public enum EstadoRuta {
        PLANIFICADA,        // Creada pero no iniciada
        EN_EJECUCION,      // En proceso de ejecución
        COMPLETADA,        // Todos los tramos completados
        CANCELADA,         // Cancelada antes de completar
        SUSPENDIDA,        // Temporalmente suspendida
        REPROGRAMADA      // Reprogramada por incidencias
    }

    public enum PrioridadRuta {
        BAJA,
        NORMAL,
        ALTA,
        URGENTE
    }

    public enum TipoRuta {
        DIRECTA,           // Origen → Destino
        CON_DEPOSITOS,     // Origen → Depósito(s) → Destino
        MULTIPLE_DESTINOS, // Un origen, múltiples destinos
        CONSOLIDADA,       // Múltiples orígenes, un destino
        CIRCULAR          // Ruta que vuelve al origen
    }

    // Métodos de conveniencia
    public void addTramo(Tramo tramo) {
        tramos.add(tramo);
        tramo.setRuta(this);
        actualizarContadores();
    }

    public void removeTramo(Tramo tramo) {
        tramos.remove(tramo);
        tramo.setRuta(null);
        actualizarContadores();
    }

    private void actualizarContadores() {
        this.cantidadTramos = tramos.size();
        this.cantidadDepositos = (int) tramos.stream()
                .filter(t -> t.getTipoTramo() == Tramo.TipoTramo.DEPOSITO_DEPOSITO ||
                        t.getTipoTramo() == Tramo.TipoTramo.ORIGEN_DEPOSITO ||
                        t.getTipoTramo() == Tramo.TipoTramo.DEPOSITO_DESTINO)
                .count();
    }

    public void iniciarRuta() {
        if (estado == EstadoRuta.PLANIFICADA) {
            estado = EstadoRuta.EN_EJECUCION;
            fechaInicioReal = LocalDateTime.now();
        }
    }

    public void completarRuta() {
        if (estado == EstadoRuta.EN_EJECUCION) {
            estado = EstadoRuta.COMPLETADA;
            fechaFinReal = LocalDateTime.now();
            calcularTiempoReal();
        }
    }

    public void cancelarRuta(String motivo) {
        estado = EstadoRuta.CANCELADA;
        observaciones = (observaciones != null ? observaciones + " | " : "") +
                "CANCELADA: " + motivo;
    }

    private void calcularTiempoReal() {
        if (fechaInicioReal != null && fechaFinReal != null) {
            long minutos = java.time.Duration.between(fechaInicioReal, fechaFinReal).toMinutes();
            this.tiempoRealTotal = (int) minutos;
        }
    }

    public void recalcularTotales() {
        // Recalcular distancia total
        distanciaTotalKm = tramos.stream()
                .map(Tramo::getDistanciaKm)
                .filter(dist -> dist != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Recalcular tiempo estimado total
        tiempoEstimadoTotal = tramos.stream()
                .map(Tramo::getTiempoEstimadoMinutos)
                .filter(tiempo -> tiempo != null)
                .reduce(0, Integer::sum);

        // Recalcular costo estimado total
        costoEstimadoTotal = tramos.stream()
                .map(Tramo::getCostoEstimado)
                .filter(costo -> costo != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Recalcular costo real total
        costoRealTotal = tramos.stream()
                .map(Tramo::getCostoReal)
                .filter(costo -> costo != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public double calcularPorcentajeCompletado() {
        if (tramos.isEmpty()) return 0.0;

        long tramosCompletados = tramos.stream()
                .filter(t -> t.getEstado() == Tramo.EstadoTramo.FINALIZADO)
                .count();

        return (tramosCompletados * 100.0) / tramos.size();
    }

    public Tramo getTramoActual() {
        return tramos.stream()
                .filter(t -> t.getEstado() == Tramo.EstadoTramo.INICIADO)
                .findFirst()
                .orElse(null);
    }

    public Tramo getSiguienteTramo() {
        return tramos.stream()
                .filter(t -> t.getEstado() == Tramo.EstadoTramo.ASIGNADO)
                .min((t1, t2) -> Integer.compare(t1.getOrden(), t2.getOrden()))
                .orElse(null);
    }

    public boolean estaCompletada() {
        return estado == EstadoRuta.COMPLETADA ||
                tramos.stream().allMatch(t -> t.getEstado() == Tramo.EstadoTramo.FINALIZADO);
    }

    public boolean puedeSerModificada() {
        return estado == EstadoRuta.PLANIFICADA;
    }

    public boolean estaEnEjecucion() {
        return estado == EstadoRuta.EN_EJECUCION;
    }

    public String getResumenRuta() {
        return String.format("Ruta %s: %d tramos, %.2f km, %d min estimados (%s)",
                codigo, cantidadTramos,
                distanciaTotalKm != null ? distanciaTotalKm.doubleValue() : 0.0,
                tiempoEstimadoTotal != null ? tiempoEstimadoTotal : 0,
                estado);
    }

    // Validaciones de negocio
    public boolean validarSecuenciaTramos() {
        if (tramos.isEmpty()) return false;

        for (int i = 0; i < tramos.size() - 1; i++) {
            Tramo actual = tramos.get(i);
            Tramo siguiente = tramos.get(i + 1);

            // El destino del tramo actual debe coincidir con el origen del siguiente
            if (!actual.getDestinoLatitud().equals(siguiente.getOrigenLatitud()) ||
                    !actual.getDestinoLongitud().equals(siguiente.getOrigenLongitud())) {
                return false;
            }
        }
        return true;
    }

    public int calcularDemoraMinutos() {
        if (tiempoEstimadoTotal == null || tiempoRealTotal == null) return 0;
        return tiempoRealTotal - tiempoEstimadoTotal;
    }

    public BigDecimal calcularDesviacionCosto() {
        if (costoEstimadoTotal == null || costoRealTotal == null) return BigDecimal.ZERO;
        return costoRealTotal.subtract(costoEstimadoTotal);
    }
}