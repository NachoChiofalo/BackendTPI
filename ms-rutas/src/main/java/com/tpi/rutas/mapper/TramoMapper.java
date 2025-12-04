package com.tpi.rutas.mapper;

import com.tpi.rutas.dto.TramoResponseDTO;
import com.tpi.rutas.entity.Tramo;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper para convertir entre entidad Tramo y TramoResponseDTO
 */
@Component
public class TramoMapper {

    /**
     * Convierte una entidad Tramo a TramoResponseDTO
     * @param tramo La entidad Tramo
     * @return TramoResponseDTO
     */
    public TramoResponseDTO toResponseDTO(Tramo tramo) {
        if (tramo == null) {
            return null;
        }

        return TramoResponseDTO.builder()
                .tramoId(tramo.getTramoId())
                .rutaId(tramo.getRutaId())
                .tipoTramoId(tramo.getTipoTramoId())
                .dominio(tramo.getDominio())
                .ubicacionOrigenId(tramo.getUbicacionOrigenId())
                .transportistaId(tramo.getTransportistaId())
                .ubicacionDestinoId(tramo.getUbicacionDestinoId())
                .distancia(tramo.getCostoAproximado()) // costoAproximado se mapea como distancia
                .fechaHoraInicio(tramo.getFechaHoraInicio())
                .fechaHoraFin(tramo.getFechaHoraFin())
                .fechaHoraEstimadaFin(tramo.getFechaHoraEstimadaFin())
                .build();
    }

    /**
     * Convierte una lista de entidades Tramo a una lista de TramoResponseDTO
     * @param tramos Lista de entidades Tramo
     * @return Lista de TramoResponseDTO
     */
    public List<TramoResponseDTO> toResponseDTOList(List<Tramo> tramos) {
        if (tramos == null) {
            return null;
        }

        return tramos.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }


}

