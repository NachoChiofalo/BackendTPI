package com.tpi.flotas.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import jakarta.validation.constraints.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransportistaDto {

    private Integer transportistaId;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 50, message = "El nombre no puede exceder 50 caracteres")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(max = 50, message = "El apellido no puede exceder 50 caracteres")
    private String apellido;

    @NotNull(message = "El teléfono es obligatorio")
    private Long telefono;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TransportistaInfoDto {
        private Long id;
        private String username;
        private String nombre;
        private String apellido;
        private String telefono;
        private String categoriaLicencia;
    }
}
