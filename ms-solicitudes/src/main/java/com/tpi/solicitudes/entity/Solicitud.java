package com.tpi.solicitudes.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Entidad Solicitud - Representa las solicitudes de transporte de contenedores
 * Mapea a la tabla 'solicitud' en el esquema 'public'
 */
@Entity
@Table(name = "solicitud", schema = "public")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Solicitud {

    @Id
    @Column(name = "solicitud_id")
    private Integer solicitudId;

    @Column(name = "tipo_doc_cliente", nullable = false)
    private Integer tipoDocCliente;

    @Column(name = "num_doc_cliente", nullable = false)
    private Long numDocCliente;

    @Column(name = "estado_solicitud", nullable = false)
    private Integer estadoSolicitud;

    @Column(name = "id_contenedor", nullable = false)
    private Integer idContenedor;

    @Column(name = "id_ruta", nullable = false)
    private Integer idRuta;

    @Column(name = "id_ubicacion_origen", nullable = false)
    private Integer idUbicacionOrigen;

    @Column(name = "id_ubicacion_destino", nullable = false)
    private Integer idUbicacionDestino;

    @Column(name = "costo_estimado", precision = 10, scale = 2)
    private BigDecimal costoEstimado;

    @Column(name = "costo_real", precision = 10, scale = 2)
    private BigDecimal costoReal;

    @Column(name = "fecha_hora_fin")
    private LocalDate fechaHoraFin;

    @Column(name = "fecha_hora_estimada_fin")
    private LocalDate fechaHoraEstimadaFin;

    @Column(name = "fecha_hora_inicio")
    private LocalDate fechaHoraInicio;

    @Column(name = "texto_adicional", length = 100)
    private String textoAdicional;
}