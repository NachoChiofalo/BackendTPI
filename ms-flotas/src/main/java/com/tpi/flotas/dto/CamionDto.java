package com.tpi.flotas.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CamionDto {

    @NotBlank(message = "El dominio es obligatorio")
    @Size(max = 20, message = "El dominio no puede exceder 20 caracteres")
    private String dominio;

    @NotBlank(message = "La marca es obligatoria")
    @Size(max = 50, message = "La marca no puede exceder 50 caracteres")
    private String marca;

    @NotBlank(message = "El modelo es obligatorio")
    @Size(max = 50, message = "El modelo no puede exceder 50 caracteres")
    private String modelo;

    private Integer añoFabricacion;

    @NotNull(message = "El tipo de camión es obligatorio")
    private String tipoCamion; // RIGIDO_PEQUEÑO, RIGIDO_MEDIANO, etc.

    @NotNull(message = "La capacidad de peso es obligatoria")
    @DecimalMin(value = "0.1", message = "La capacidad de peso debe ser mayor a 0")
    private BigDecimal capacidadPeso;

    @NotNull(message = "La capacidad de volumen es obligatoria")
    @DecimalMin(value = "0.1", message = "La capacidad de volumen debe ser mayor a 0")
    private BigDecimal capacidadVolumen;

    @NotNull(message = "El consumo de combustible es obligatorio")
    @DecimalMin(value = "0.1", message = "El consumo de combustible debe ser mayor a 0")
    private BigDecimal consumoCombustible;

    @NotNull(message = "El costo base por km es obligatorio")
    @DecimalMin(value = "0.1", message = "El costo base por km debe ser mayor a 0")
    private BigDecimal costoBaseKm;

    private BigDecimal costoMantenimientoDiario;

    private String estado; // DISPONIBLE, EN_VIAJE, etc.

    private String condicionMecanica; // EXCELENTE, BUENA, etc.

    private String numeroSeguro;

    private LocalDateTime fechaVencimientoSeguro;

    private LocalDateTime fechaUltimoService;

    private Long kilometrajeActual;

    private Long transportistaId;

    private Long depositoBaseId;

    private BigDecimal ubicacionActualLat;

    private BigDecimal ubicacionActualLng;

    private String ubicacionDescripcion;

    private Boolean activo;

    // DTO anidado para información del transportista
    private TransportistaInfoDto transportista;
}
