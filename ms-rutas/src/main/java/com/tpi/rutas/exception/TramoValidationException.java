package com.tpi.rutas.exception;

import lombok.Getter;

@Getter
public class TramoValidationException extends RuntimeException {
    private final String codigo;

    public TramoValidationException(String codigo, String mensaje) {
        super(mensaje);
        this.codigo = codigo;
    }
}
