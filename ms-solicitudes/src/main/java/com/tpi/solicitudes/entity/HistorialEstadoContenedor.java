package com.tpi.solicitudes.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "historialestadocontenedor", schema = "public")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistorialEstadoContenedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "historial_contenedor_id")
    private Integer historialContenedorId;

    @Column(name = "id_estado_contenedor", nullable = false)
    private Integer idEstadoContenedor;

    @Column(name = "id_contenedor")
    private Integer idContenedor;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;
}
