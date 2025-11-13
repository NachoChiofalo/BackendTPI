package com.tpi.localizaciones.dto;

import lombok.Data;

@Data
public class DistanciaDTO {
    private String origen;
    private String destino;
    private double kilometros;
    private String duracionTexto;
}