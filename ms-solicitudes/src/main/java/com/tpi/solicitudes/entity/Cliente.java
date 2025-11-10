package com.tpi.solicitudes.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Entidad Cliente - Representa los clientes que solicitan traslados de contenedores
 * Mapea a la tabla 'Clientes' en el esquema 'public'
 */
@Entity
@Table(name = "clientes", schema = "public")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(ClienteId.class)
public class Cliente implements Serializable {

    @Id
    @Column(name = "tipo_doc_cliente_id", nullable = false)
    private Integer tipoDocClienteId;

    @Id
    @Column(name = "num_doc_cliente", nullable = false)
    private Long numDocCliente;

    @Column(name = "nombres", nullable = false, length = 50)
    private String nombres;

    @Column(name = "apellidos", nullable = false, length = 50)
    private String apellidos;

    @Column(name = "domicilio", nullable = false, length = 50)
    private String domicilio;

    @Column(name = "telefono", nullable = false, length = 50)
    private String telefono;
}
