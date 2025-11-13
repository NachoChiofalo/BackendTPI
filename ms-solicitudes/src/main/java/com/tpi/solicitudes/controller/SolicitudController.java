package com.tpi.solicitudes.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/solicitudes")
@Tag(name = "Solicitudes", description = "API para gestión de solicitudes de transporte")
public class SolicitudController {

    @GetMapping
    @Operation(
        summary = "Obtener todas las solicitudes",
        description = "Retorna una lista de todas las solicitudes de transporte registradas en el sistema"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de solicitudes obtenida exitosamente",
                    content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<List<String>> getAllSolicitudes() {
        // Implementación del método
        return ResponseEntity.ok(List.of("Solicitud 1", "Solicitud 2"));
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Obtener solicitud por ID",
        description = "Retorna una solicitud específica basada en su identificador único"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Solicitud encontrada",
                    content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "404", description = "Solicitud no encontrada"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<String> getSolicitudById(
            @Parameter(description = "ID único de la solicitud", required = true)
            @PathVariable Long id) {
        // Implementación del método
        return ResponseEntity.ok("Solicitud " + id);
    }

    @PostMapping
    @Operation(
        summary = "Crear nueva solicitud",
        description = "Crea una nueva solicitud de transporte en el sistema"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Solicitud creada exitosamente",
                    content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "400", description = "Datos de solicitud inválidos"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<String> createSolicitud(
            @Parameter(description = "Datos de la nueva solicitud", required = true)
            @RequestBody String solicitudRequest) {
        // Implementación del método
        return ResponseEntity.status(HttpStatus.CREATED).body("Solicitud creada");
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Actualizar solicitud",
        description = "Actualiza una solicitud existente en el sistema"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Solicitud actualizada exitosamente",
                    content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "404", description = "Solicitud no encontrada"),
        @ApiResponse(responseCode = "400", description = "Datos de solicitud inválidos"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<String> updateSolicitud(
            @Parameter(description = "ID único de la solicitud a actualizar", required = true)
            @PathVariable Long id,
            @Parameter(description = "Datos actualizados de la solicitud", required = true)
            @RequestBody String solicitudRequest) {
        // Implementación del método
        return ResponseEntity.ok("Solicitud " + id + " actualizada");
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Eliminar solicitud",
        description = "Elimina una solicitud del sistema"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Solicitud eliminada exitosamente"),
        @ApiResponse(responseCode = "404", description = "Solicitud no encontrada"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<Void> deleteSolicitud(
            @Parameter(description = "ID único de la solicitud a eliminar", required = true)
            @PathVariable Long id) {
        // Implementación del método
        return ResponseEntity.noContent().build();
    }
}
