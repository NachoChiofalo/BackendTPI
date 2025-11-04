package com.tpi.localizaciones.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Entidad Ciudad - Representa ciudades donde se encuentran las ubicaciones
 * Esquema: localizaciones
 */
@Entity
@Table(name = "ciudades", schema = "localizaciones")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ciudad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 100)
    private String provincia;

    @Column(length = 100)
    private String pais;

    @Column(length = 10)
    private String codigoPostal;

    // Coordenadas aproximadas del centro de la ciudad
    @Column(name = "latitud", precision = 10, scale = 7)
    private BigDecimal latitud;

    @Column(name = "longitud", precision = 10, scale = 7)
    private BigDecimal longitud;

    @Column(name = "zona_horaria", length = 50)
    private String zonaHoraria;

    @Column(name = "activa")
    @Builder.Default
    private Boolean activa = true;

    // Metadatos de auditoría
    @CreationTimestamp
    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    @UpdateTimestamp
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    // Relación con ubicaciones
    @OneToMany(mappedBy = "ciudad", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Ubicacion> ubicaciones;

    /**
     * Método para obtener el nombre completo de la ciudad
     *
     * @return String con formato "Nombre, Provincia, País"
     */
    public String getNombreCompleto() {
        StringBuilder sb = new StringBuilder();

        if (nombre != null && !nombre.trim().isEmpty()) {
            sb.append(nombre);
        }

        if (provincia != null && !provincia.trim().isEmpty()) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(provincia);
        }

        if (pais != null && !pais.trim().isEmpty()) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(pais);
        }

        return sb.toString();
    }
}
