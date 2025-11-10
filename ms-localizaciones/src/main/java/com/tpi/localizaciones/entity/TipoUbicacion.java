package com.tpi.localizaciones.entity;

/**
 * Tipo de ubicacion disponible en el sistema.
 */
public enum TipoUbicacion {
    CLIENTE_ORIGEN,     // Punto de recogida del cliente
    CLIENTE_DESTINO,    // Punto de entrega al cliente
    DEPOSITO,           // Depósito/almacén
    PUERTO,             // Puerto marítimo/fluvial
    AEROPUERTO,         // Aeropuerto
    TERMINAL_CARGAS,    // Terminal de cargas
    ZONA_INDUSTRIAL,    // Zona industrial
    CENTRO_LOGISTICO,   // Centro logístico
    OTRO                // Otro tipo de ubicación
}
