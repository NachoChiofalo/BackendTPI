package com.tpi.flotas.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransportistaDto {

    private Long id;

    @NotBlank(message = "El username es obligatorio")
    @Size(max = 50, message = "El username no puede exceder 50 caracteres")
    private String username;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe tener un formato válido")
    @Size(max = 100, message = "El email no puede exceder 100 caracteres")
    private String email;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(max = 100, message = "El apellido no puede exceder 100 caracteres")
    private String apellido;

    @Size(max = 20, message = "El teléfono no puede exceder 20 caracteres")
    private String telefono;

    @NotBlank(message = "La licencia de conducir es obligatoria")
    @Size(max = 20, message = "La licencia de conducir no puede exceder 20 caracteres")
    private String licenciaConducir;

    @NotBlank(message = "La categoría de licencia es obligatoria")
    @Size(max = 10, message = "La categoría de licencia no puede exceder 10 caracteres")
    private String categoriaLicencia;

    @NotNull(message = "La fecha de vencimiento de licencia es obligatoria")
    @Future(message = "La fecha de vencimiento debe ser futura")
    private LocalDateTime fechaVencimientoLicencia;

    private LocalDateTime fechaIngreso;

    private Boolean activo;
}

// DTO simplificado para información anidada
@Data
@NoArgsConstructor
@AllArgsConstructor
class TransportistaInfoDto {
    private Long id;
    private String username;
    private String nombre;
    private String apellido;
    private String telefono;
    private String categoriaLicencia;
}
