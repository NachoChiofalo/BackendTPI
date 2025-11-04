package com.tpi.localizaciones.entity;

/**
 * Enum que representa los estados de validación de ubicaciones
 */
public enum EstadoValidacion {
    PENDIENTE("Pendiente de validación"),
    VALIDADA("Validada y aprobada"),
    RECHAZADA("Rechazada por validación"),
    EN_REVISION("En proceso de revisión");

    private final String descripcion;

    EstadoValidacion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
