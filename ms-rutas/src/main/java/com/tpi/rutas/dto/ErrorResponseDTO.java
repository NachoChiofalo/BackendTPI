package com.tpi.rutas.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponseDTO {
    private String error;
    private String mensaje;
    private String codigo;
    private LocalDateTime timestamp;
    private String path;
}
