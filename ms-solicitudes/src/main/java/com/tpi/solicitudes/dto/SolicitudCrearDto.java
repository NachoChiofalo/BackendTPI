package com.tpi.solicitudes.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO para la creación de una solicitud incluyendo datos embebidos de cliente y contenedor.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitudCrearDto {

    @NotBlank
    private String descripcion;

    @NotNull
    @Positive
    private Double peso;

    private String tipoContenedor;

    private String observaciones;

    @NotNull
    private Double latitudOrigen;

    @NotNull
    private Double longitudOrigen;

    @NotNull
    private Double latitudDestino;

    @NotNull
    private Double longitudDestino;

    private Integer idRuta;

    @Valid
    @NotNull
    private ClienteInfo cliente;

    @Valid
    @NotNull
    private ContenedorInfo contenedor;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ClienteInfo {
        @NotNull
        private Integer tipoDocumento; // mapea a Cliente.tipoDocClienteId

        @NotNull
        private Long numDocumento; // mapea a Cliente.numDocCliente

        private String nombres;
        private String apellidos;
        private String domicilio;
        private String telefono;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ContenedorInfo {
        private Integer idContenedor; // opcional: si se provee se intentará reusar

        private BigDecimal peso; // requerido si se crea nuevo
        private BigDecimal volumen; // requerido si se crea nuevo

        private Integer idEstadoContenedor; // opcional
    }
}

