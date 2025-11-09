package com.tpi.localizaciones.dto;
import com.tpi.localizaciones.entity.TipoUbicacion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UbicacionDto {

    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String nombre;

    @NotBlank(message = "La dirección es obligatoria")
    @Size(max = 500, message = "La dirección no puede exceder 500 caracteres")
    private String direccion;

    @NotNull(message = "La latitud es obligatoria")
    @DecimalMin(value = "-90.0000000", message = "La latitud debe estar entre -90 y 90")
    @DecimalMax(value = "90.0000000", message = "La latitud debe estar entre -90 y 90")
    private BigDecimal latitud;

    @NotNull(message = "La longitud es obligatoria")
    @DecimalMin(value = "-180.0000000", message = "La longitud debe estar entre -180 y 180")
    @DecimalMax(value = "180.0000000", message = "La longitud debe estar entre -180 y 180")
    private BigDecimal longitud;

    @NotNull(message = "El tipo de ubicación es obligatorio")
    private TipoUbicacion tipoUbicacion;

    @Size(max = 10, message = "El código postal no puede exceder 10 caracteres")
    private String codigoPostal;

    @Size(max = 20, message = "El número de puerta no puede exceder 20 caracteres")
    private String numeroPuerta;

    @Size(max = 10, message = "El piso no puede exceder 10 caracteres")
    private String piso;

    @Size(max = 10, message = "El departamento no puede exceder 10 caracteres")
    private String departamento;

    @Size(max = 200, message = "La información de entre calles no puede exceder 200 caracteres")
    private String entreCalles;

    @Size(max = 500, message = "Las referencias no pueden exceder 500 caracteres")
    private String referencias;

    @Size(max = 100, message = "El nombre de contacto no puede exceder 100 caracteres")
    private String contactoNombre;

    @Size(max = 20, message = "El teléfono de contacto no puede exceder 20 caracteres")
    private String contactoTelefono;

    @Email(message = "El email de contacto debe ser válido")
    @Size(max = 150, message = "El email de contacto no puede exceder 150 caracteres")
    private String contactoEmail;

    @Size(max = 200, message = "El horario de atención no puede exceder 200 caracteres")
    private String horarioAtencion;

    @Size(max = 1000, message = "Las instrucciones de acceso no pueden exceder 1000 caracteres")
    private String instruccionesAcceso;

    private Boolean tieneMuelleCarga;

    private Boolean tieneGrua;

    @DecimalMin(value = "0.0", message = "La altura máxima debe ser mayor o igual a 0")
    private BigDecimal alturaMaximaMetros;

    @DecimalMin(value = "0.0", message = "El peso máximo debe ser mayor o igual a 0")
    private BigDecimal pesoMaximoToneladas;

    @DecimalMin(value = "0.0", message = "El espacio de maniobra debe ser mayor o igual a 0")
    private BigDecimal espacioManiobraMetros;

    private Long ciudadId;
    private String ciudadNombre;
}