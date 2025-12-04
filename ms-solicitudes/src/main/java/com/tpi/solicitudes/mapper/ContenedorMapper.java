package com.tpi.solicitudes.mapper;

import com.tpi.solicitudes.dto.ContenedorDto;
import com.tpi.solicitudes.entity.Contenedor;
import com.tpi.solicitudes.entity.Solicitud;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Mapper para convertir entidades Contenedor a DTOs
 */
@Component
@RequiredArgsConstructor
public class ContenedorMapper {

    public ContenedorDto toDto(Contenedor contenedor, Solicitud solicitud) {
        if (contenedor == null) {
            return null;
        }

        return ContenedorDto.builder()
                .idContenedor(contenedor.getIdContenedor())
                .idEstadoContenedor(contenedor.getIdEstadoContenedor())
                .nombreEstado(contenedor.getEstadoContenedor() != null ?
                    contenedor.getEstadoContenedor().getNombre() : null)
                .volumenM3(contenedor.getVolumenM3())
                .pesoKg(contenedor.getPesoKg())
                .tipoDocCliente(solicitud != null ? solicitud.getTipoDocCliente() : null)
                .numDocCliente(solicitud != null ? solicitud.getNumDocCliente() : null)
                .build();
    }

    public ContenedorDto toDto(Contenedor contenedor) {
        if (contenedor == null) {
            return null;
        }

        return ContenedorDto.builder()
                .idContenedor(contenedor.getIdContenedor())
                .idEstadoContenedor(contenedor.getIdEstadoContenedor())
                .nombreEstado(contenedor.getEstadoContenedor() != null ?
                    contenedor.getEstadoContenedor().getNombre() : null)
                .volumenM3(contenedor.getVolumenM3())
                .pesoKg(contenedor.getPesoKg())
                .build();
    }
}
