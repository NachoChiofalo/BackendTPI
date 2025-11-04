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
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad Contenedor - Representa los contenedores a transportar
 * Esquema: solicitudes
 */
@Entity
@Table(name = "contenedores", schema = "solicitudes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Contenedor {

    @Id
    @Column(name = "identificacion", length = 50)
    private String identificacion; // PK personalizada

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal peso; // en toneladas

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal volumen; // en metros cúbicos

    @Column(length = 200)
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EstadoContenedor estado = EstadoContenedor.REGISTRADO;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_contenedor", length = 20)
    private TipoContenedor tipoContenedor;

    // Relaciones
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @OneToMany(mappedBy = "contenedor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Solicitud> solicitudes = new ArrayList<>();

    // Ubicación actual (opcional - para seguimiento)
    @Column(name = "ubicacion_actual_lat", precision = 10, scale = 7)
    private BigDecimal ubicacionActualLatitud;

    @Column(name = "ubicacion_actual_lng", precision = 10, scale = 7)
    private BigDecimal ubicacionActualLongitud;

    @Column(name = "ubicacion_descripcion")
    private String ubicacionDescripcion;

    // Auditoría
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Enums
    public enum EstadoContenedor {
        REGISTRADO,      // Recién registrado
        EN_ORIGEN,       // En punto de origen esperando retiro
        EN_TRANSITO,     // Siendo transportado
        EN_DEPOSITO,     // En depósito intermedio
        EN_DESTINO,      // En punto de destino
        ENTREGADO,       // Entregado al cliente
        CANCELADO        // Solicitud cancelada
    }

    public enum TipoContenedor {
        ESTANDAR_20,     // 20 pies estándar
        ESTANDAR_40,     // 40 pies estándar
        HIGH_CUBE_40,    // 40 pies high cube
        REFRIGERADO_20,  // 20 pies refrigerado
        REFRIGERADO_40,  // 40 pies refrigerado
        OPEN_TOP_20,     // 20 pies open top
        OPEN_TOP_40,     // 40 pies open top
        FLAT_RACK       // Flat rack
    }

    // Métodos de conveniencia
    public void addSolicitud(Solicitud solicitud) {
        solicitudes.add(solicitud);
        solicitud.setContenedor(this);
    }

    public void removeSolicitud(Solicitud solicitud) {
        solicitudes.remove(solicitud);
        solicitud.setContenedor(null);
    }

    public void actualizarUbicacion(BigDecimal latitud, BigDecimal longitud, String descripcion) {
        this.ubicacionActualLatitud = latitud;
        this.ubicacionActualLongitud = longitud;
        this.ubicacionDescripcion = descripcion;
    }

    // Validaciones de negocio
    public boolean puedeSerTransportadoPor(BigDecimal capacidadPesoCamion, BigDecimal capacidadVolumenCamion) {
        return this.peso.compareTo(capacidadPesoCamion) <= 0 && 
               this.volumen.compareTo(capacidadVolumenCamion) <= 0;
    }

    public boolean estaDisponibleParaTransporte() {
        return estado == EstadoContenedor.REGISTRADO || 
               estado == EstadoContenedor.EN_ORIGEN || 
               estado == EstadoContenedor.EN_DEPOSITO;
    }
}