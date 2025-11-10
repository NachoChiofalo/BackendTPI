package com.tpi.rutas.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad Ruta - Representa la planificación completa de un traslado
 * Mapea a la tabla 'Ruta' en el esquema 'public'
 */
@Entity
@Table(name = "ruta", schema = "public")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ruta {

    @Id
    @Column(name = "ruta_id")
    private Integer rutaId;

    @Column(name = "cantidad_tramos", nullable = false)
    private Integer cantidadTramos;

    @Column(name = "cantidad_depositos", nullable = false)
    private Integer cantidadDepositos;
}

