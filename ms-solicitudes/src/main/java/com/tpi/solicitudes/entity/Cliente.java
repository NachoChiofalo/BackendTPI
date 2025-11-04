package com.tpi.solicitudes.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad Cliente - Representa los clientes que solicitan traslados de contenedores
 * Esquema: solicitudes
 */
@Entity
@Table(name = "clientes", schema = "solicitudes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = false, length = 20)
    private String telefono;

    @Column(length = 500)
    private String direccion;

    @Column(name = "numero_documento", length = 20)
    private String numeroDocumento;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_documento", length = 10)
    private TipoDocumento tipoDocumento;

    @Column(name = "activo")
    @Builder.Default
    private Boolean activo = true;

    // Relaciones
    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Contenedor> contenedores = new ArrayList<>();

    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Solicitud> solicitudes = new ArrayList<>();

    // Auditoría
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Enums
    public enum TipoDocumento {
        DNI, PASAPORTE, CUIT, CUIL
    }

    // Métodos de conveniencia
    public void addContenedor(Contenedor contenedor) {
        contenedores.add(contenedor);
        contenedor.setCliente(this);
    }

    public void removeContenedor(Contenedor contenedor) {
        contenedores.remove(contenedor);
        contenedor.setCliente(null);
    }

    public void addSolicitud(Solicitud solicitud) {
        solicitudes.add(solicitud);
        solicitud.setCliente(this);
    }

    public void removeSolicitud(Solicitud solicitud) {
        solicitudes.remove(solicitud);
        solicitud.setCliente(null);
    }
}