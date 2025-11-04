package com.tpi.flotas.entity;

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
 * Entidad Transportista - Representa los conductores de camiones
 * Esquema: flotas
 */
@Entity
@Table(name = "transportistas", schema = "flotas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transportista {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 100)
    private String apellido;

    @Column(nullable = false, unique = true, length = 20)
    private String dni;

    @Column(nullable = false, length = 20)
    private String telefono;

    @Column(length = 150)
    private String email;

    @Column(length = 300)
    private String direccion;

    @Column(name = "licencia_conducir", nullable = false, length = 30)
    private String licenciaConducir;

    @Enumerated(EnumType.STRING)
    @Column(name = "categoria_licencia", nullable = false, length = 10)
    private CategoriaLicencia categoriaLicencia;

    @Column(name = "fecha_vencimiento_licencia")
    private LocalDateTime fechaVencimientoLicencia;

    @Column(name = "años_experiencia")
    private Integer añosExperiencia;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    @Builder.Default
    private EstadoTransportista estado = EstadoTransportista.DISPONIBLE;

    @Column(name = "activo")
    @Builder.Default
    private Boolean activo = true;

    // Relaciones
    @OneToMany(mappedBy = "transportista", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Camion> camiones = new ArrayList<>();

    // Auditoría
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Enums
    public enum CategoriaLicencia {
        A,      // Motocicletas
        B,      // Automóviles
        C,      // Camiones hasta 12,000 kg
        D,      // Transporte de pasajeros
        E       // Camiones articulados
    }

    public enum EstadoTransportista {
        DISPONIBLE,     // Libre para asignaciones
        EN_VIAJE,       // Actualmente transportando
        DESCANSO,       // En período de descanso obligatorio
        NO_DISPONIBLE,  // Temporalmente no disponible
        SUSPENDIDO     // Suspendido por infracciones
    }

    // Métodos de conveniencia
    public void addCamion(Camion camion) {
        camiones.add(camion);
        camion.setTransportista(this);
    }

    public void removeCamion(Camion camion) {
        camiones.remove(camion);
        camion.setTransportista(null);
    }

    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }

    public boolean estaDisponibleParaViaje() {
        return activo && estado == EstadoTransportista.DISPONIBLE;
    }

    public boolean tieneVigenteLicencia() {
        return fechaVencimientoLicencia != null && 
               fechaVencimientoLicencia.isAfter(LocalDateTime.now());
    }

    public boolean puedeConducirCategoria(String categoria) {
        if (categoriaLicencia == null) return false;
        
        switch (categoriaLicencia) {
            case E: return true; // Puede conducir todas las categorías
            case D: return categoria.equals("C") || categoria.equals("B") || categoria.equals("A");
            case C: return categoria.equals("B") || categoria.equals("A");
            case B: return categoria.equals("A");
            case A: return categoria.equals("A");
            default: return false;
        }
    }
}