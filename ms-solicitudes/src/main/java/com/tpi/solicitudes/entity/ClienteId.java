package com.tpi.solicitudes.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Clase para la clave primaria compuesta de Cliente
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClienteId implements Serializable {
    private Integer tipoDocClienteId;
    private Long numDocCliente;
}

