package com.tpi.solicitudes.entity;

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
 * Entidad Solicitud - Representa las solicitudes de transporte de contenedores
 * Esquema: solicitudes
 */
@Entity
@Table(name = "solicitudes", schema = "solicitudes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Solicitud {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String numero; // Número único de solicitud

    // Relaciones
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contenedor_id", nullable = false)
    private Contenedor contenedor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    // Origen
    @Column(name = "origen_latitud", nullable = false, precision = 10, scale = 7)
    private BigDecimal origenLatitud;

    @Column(name = "origen_longitud", nullable = false, precision = 10, scale = 7)
    private BigDecimal origenLongitud;

    @Column(name = "origen_direccion", nullable = false, length = 500)
    private String origenDireccion;

    // Destino
    @Column(name = "destino_latitud", nullable = false, precision = 10, scale = 7)
    private BigDecimal destinoLatitud;

    @Column(name = "destino_longitud", nullable = false, precision = 10, scale = 7)
    private BigDecimal destinoLongitud;

    @Column(name = "destino_direccion", nullable = false, length = 500)
    private String destinoDireccion;

    // Estado y seguimiento
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EstadoSolicitud estado = EstadoSolicitud.BORRADOR;

    @Column(length = 1000)
    private String observaciones;

    // Fechas importantes
    @Column(name = "fecha_retiro_programada")
    private LocalDateTime fechaRetiroProgramada;

    @Column(name = "fecha_entrega_programada")
    private LocalDateTime fechaEntregaProgramada;

    @Column(name = "fecha_retiro_real")
    private LocalDateTime fechaRetiroReal;

    @Column(name = "fecha_entrega_real")
    private LocalDateTime fechaEntregaReal;

    // Costos y tiempos
    @Column(name = "costo_estimado", precision = 12, scale = 2)
    private BigDecimal costoEstimado;

    @Column(name = "tiempo_estimado") // en horas
    private Integer tiempoEstimado;

    @Column(name = "costo_final", precision = 12, scale = 2)
    private BigDecimal costoFinal;

    @Column(name = "tiempo_real") // en horas
    private Integer tiempoReal;

    // Prioridad de la solicitud
    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    @Builder.Default
    private PrioridadSolicitud prioridad = PrioridadSolicitud.NORMAL;

    // Auditoría
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Enums
    public enum EstadoSolicitud {
        BORRADOR,        // Recién creada, en edición
        PROGRAMADA,      // Ruta asignada, esperando inicio
        EN_TRANSITO,     // En proceso de traslado
        ENTREGADA,       // Completada exitosamente
        CANCELADA,       // Cancelada por el cliente
        RECHAZADA       // Rechazada por falta de disponibilidad
    }

    public enum PrioridadSolicitud {
        BAJA,
        NORMAL,
        ALTA,
        URGENTE
    }

    // Métodos de conveniencia
    public void programarRetiro(LocalDateTime fechaRetiro) {
        this.fechaRetiroProgramada = fechaRetiro;
        if (this.estado == EstadoSolicitud.BORRADOR) {
            this.estado = EstadoSolicitud.PROGRAMADA;
        }
    }

    public void programarEntrega(LocalDateTime fechaEntrega) {
        this.fechaEntregaProgramada = fechaEntrega;
    }

    public void registrarInicioRetiro() {
        this.fechaRetiroReal = LocalDateTime.now();
        this.estado = EstadoSolicitud.EN_TRANSITO;
        if (this.contenedor != null) {
            this.contenedor.setEstado(Contenedor.EstadoContenedor.EN_TRANSITO);
        }
    }

    public void registrarEntrega() {
        this.fechaEntregaReal = LocalDateTime.now();
        this.estado = EstadoSolicitud.ENTREGADA;
        if (this.contenedor != null) {
            this.contenedor.setEstado(Contenedor.EstadoContenedor.ENTREGADO);
        }
    }

    public void calcularTiempoReal() {
        if (fechaRetiroReal != null && fechaEntregaReal != null) {
            long horas = java.time.Duration.between(fechaRetiroReal, fechaEntregaReal).toHours();
            this.tiempoReal = (int) horas;
        }
    }

    public void cancelar(String motivo) {
        this.estado = EstadoSolicitud.CANCELADA;
        this.observaciones = (this.observaciones != null ? this.observaciones + " | " : "") + 
                           "CANCELADA: " + motivo;
        if (this.contenedor != null) {
            this.contenedor.setEstado(Contenedor.EstadoContenedor.CANCELADO);
        }
    }

    // Validaciones de negocio
    public boolean puedeSerModificada() {
        return estado == EstadoSolicitud.BORRADOR;
    }

    public boolean estaEnProceso() {
        return estado == EstadoSolicitud.EN_TRANSITO;
    }

    public boolean estaCompletada() {
        return estado == EstadoSolicitud.ENTREGADA;
    }

    public boolean estaCancelada() {
        return estado == EstadoSolicitud.CANCELADA || estado == EstadoSolicitud.RECHAZADA;
    }

    public double calcularDistanciaTotal() {
        // Cálculo básico de distancia entre origen y destino usando fórmula haversine
        if (origenLatitud == null || origenLongitud == null || 
            destinoLatitud == null || destinoLongitud == null) {
            return 0.0;
        }

        double lat1 = Math.toRadians(origenLatitud.doubleValue());
        double lon1 = Math.toRadians(origenLongitud.doubleValue());
        double lat2 = Math.toRadians(destinoLatitud.doubleValue());
        double lon2 = Math.toRadians(destinoLongitud.doubleValue());

        double dLat = lat2 - lat1;
        double dLon = lon2 - lon1;

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(lat1) * Math.cos(lat2) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        // Radio de la Tierra en kilómetros
        final double RADIO_TIERRA_KM = 6371.0;
        
        return RADIO_TIERRA_KM * c;
    }
}