package com.tpi.solicitudes.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad EstadoContenedor - Representa los posibles estados de un contenedor
 * Mapea a la tabla 'EstadoContenedor' en el esquema 'public'
 */
@Entity
@Table(name = "estadocontenedor", schema = "public")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstadoContenedor {

    @Id
    @Column(name = "id_estado_contenedor")
    private Integer idEstadoContenedor;

    @Column(name = "nombre", length = 30)
    private String nombre;

    @Column(name = "texto_adicional", length = 200)
    private String textoAdicional;
}
