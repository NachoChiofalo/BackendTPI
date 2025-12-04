package com.tpi.solicitudes.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Entidad Contenedor - Representa los contenedores a transportar
 * Mapea a la tabla 'Contenedor' en el esquema 'public'
 */
@Entity
@Table(name = "contenedor", schema = "public")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Contenedor {

    @Id
    @Column(name = "id_contenedor")
    private Integer idContenedor;

    @Column(name = "id_estado_contenedor", nullable = false)
    private Integer idEstadoContenedor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_estado_contenedor", insertable = false, updatable = false)
    private EstadoContenedor estadoContenedor;

    @Column(name = "volumen_m3", nullable = false, precision = 10, scale = 2)
    private BigDecimal volumenM3;

    @Column(name = "peso_kg", nullable = false, precision = 10, scale = 2)
    private BigDecimal pesoKg;
}