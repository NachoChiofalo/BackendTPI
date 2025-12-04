package com.tpi.rutas.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad Tramo - Representa cada segmento individual de una ruta
 * Mapea a la tabla 'Tramo' en el esquema 'public'
 */
@Entity
@Table(name = "tramo", schema = "public")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tramo {

    @Id
    @Column(name = "tramo_id")
    private Integer tramoId;

    @Column(name = "ruta_id", nullable = false)
    private Integer rutaId;

    @Column(name = "tipo_tramo_id", nullable = false)
    private Integer tipoTramoId;

    @Column(name = "dominio", length = 7)  // Permite null para tramos sin camión asignado
    private String dominio;

    @Column(name = "ubicacion_origen_id", nullable = false)
    private Integer ubicacionOrigenId;

    @Column(name = "transportista_id")  // Permite null hasta que se asigne manualmente
    private Integer transportistaId;

    @Column(name = "ubicacion_destino_id", nullable = false)
    private Integer ubicacionDestinoId;

    @Column(name = "costo_aproximado", precision = 10, scale = 2)
    private BigDecimal costoAproximado;

    @Column(name = "costo_real", precision = 10, scale = 2)
    private BigDecimal costoReal;

    @Column(name = "fecha_hora_inicio")
    private LocalDateTime fechaHoraInicio;

    @Column(name = "fecha_hora_fin")
    private LocalDateTime fechaHoraFin;

    @Column(name = "fecha_hora_estimada_fin")
    private LocalDateTime fechaHoraEstimadaFin;
}


