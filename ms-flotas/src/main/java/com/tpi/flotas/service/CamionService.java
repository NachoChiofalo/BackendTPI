package com.tpi.flotas.service;

import com.tpi.flotas.dto.CamionDto;
import com.tpi.flotas.dto.TransportistaDto;
import com.tpi.flotas.entity.Camion;
import com.tpi.flotas.entity.Transportista;
import com.tpi.flotas.repository.CamionRepository;
import com.tpi.flotas.repository.TransportistaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CamionService {

    private final CamionRepository camionRepository;
    private final TransportistaRepository transportistaRepository;

    public List<CamionDto> obtenerTodos() {
        return camionRepository.findByActivoTrue()
                .stream()
                .map(this::convertirADto)
                .collect(Collectors.toList());
    }

    public List<CamionDto> obtenerDisponibles() {
        return camionRepository.findDisponibles()
                .stream()
                .map(this::convertirADto)
                .collect(Collectors.toList());
    }

    public Optional<CamionDto> obtenerPorDominio(String dominio) {
        return camionRepository.findById(dominio)
                .map(this::convertirADto);
    }

    public List<CamionDto> obtenerPorTipo(String tipoCamion) {
        try {
            Camion.TipoCamion tipo = Camion.TipoCamion.valueOf(tipoCamion);
            return camionRepository.findByTipoCamion(tipo)
                    .stream()
                    .map(this::convertirADto)
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            return List.of();
        }
    }

    public List<CamionDto> buscarPorCapacidad(Double pesoMinimo, Double volumenMinimo) {
        return camionRepository.findByCapacidadMinimaDisponibles(pesoMinimo, volumenMinimo)
                .stream()
                .map(this::convertirADto)
                .collect(Collectors.toList());
    }

    public CamionDto crear(CamionDto camionDto) {
        // Verificar que el dominio no exista
        if (camionRepository.existsById(camionDto.getDominio())) {
            throw new RuntimeException("Ya existe un camión con el dominio: " + camionDto.getDominio());
        }

        Camion camion = convertirAEntidad(camionDto);
        Camion camionGuardado = camionRepository.save(camion);
        return convertirADto(camionGuardado);
    }

    public CamionDto actualizar(String dominio, CamionDto camionDto) {
        return camionRepository.findById(dominio)
                .map(camionExistente -> {
                    actualizarCamionExistente(camionExistente, camionDto);
                    Camion camionActualizado = camionRepository.save(camionExistente);
                    return convertirADto(camionActualizado);
                })
                .orElseThrow(() -> new RuntimeException("Camión no encontrado: " + dominio));
    }

    public void eliminar(String dominio) {
        camionRepository.findById(dominio)
                .ifPresentOrElse(
                    camion -> {
                        camion.setActivo(false);
                        camionRepository.save(camion);
                    },
                    () -> {
                        throw new RuntimeException("Camión no encontrado: " + dominio);
                    }
                );
    }

    public CamionDto asignarTransportista(String dominio, Long transportistaId) {
        Camion camion = camionRepository.findById(dominio)
                .orElseThrow(() -> new RuntimeException("Camión no encontrado: " + dominio));

        Transportista transportista = transportistaRepository.findById(transportistaId)
                .orElseThrow(() -> new RuntimeException("Transportista no encontrado: " + transportistaId));

        camion.setTransportista(transportista);
        Camion camionActualizado = camionRepository.save(camion);
        return convertirADto(camionActualizado);
    }

    private CamionDto convertirADto(Camion camion) {
        CamionDto dto = new CamionDto();
        dto.setDominio(camion.getDominio());
        dto.setMarca(camion.getMarca());
        dto.setModelo(camion.getModelo());
        dto.setAñoFabricacion(camion.getAñoFabricacion());
        dto.setTipoCamion(camion.getTipoCamion() != null ? camion.getTipoCamion().name() : null);
        dto.setCapacidadPeso(camion.getCapacidadPeso());
        dto.setCapacidadVolumen(camion.getCapacidadVolumen());
        dto.setConsumoCombustible(camion.getConsumoCombustible());
        dto.setCostoBaseKm(camion.getCostoBaseKm());
        dto.setCostoMantenimientoDiario(camion.getCostoMantenimientoDiario());
        dto.setEstado(camion.getEstado() != null ? camion.getEstado().name() : null);
        dto.setCondicionMecanica(camion.getCondicionMecanica() != null ? camion.getCondicionMecanica().name() : null);
        dto.setNumeroSeguro(camion.getNumeroSeguro());
        dto.setFechaVencimientoSeguro(camion.getFechaVencimientoSeguro());
        dto.setFechaUltimoService(camion.getFechaUltimoService());
        dto.setKilometrajeActual(camion.getKilometrajeActual());
        dto.setTransportistaId(camion.getTransportista() != null ? camion.getTransportista().getId() : null);
        dto.setDepositoBaseId(camion.getDepositoBaseId());
        dto.setUbicacionActualLat(camion.getUbicacionActualLat());
        dto.setUbicacionActualLng(camion.getUbicacionActualLng());
        dto.setUbicacionDescripcion(camion.getUbicacionDescripcion());
        dto.setActivo(camion.getActivo());

        return dto;
    }

    private Camion convertirAEntidad(CamionDto dto) {
        Camion camion = new Camion();
        camion.setDominio(dto.getDominio());
        camion.setMarca(dto.getMarca());
        camion.setModelo(dto.getModelo());
        camion.setAñoFabricacion(dto.getAñoFabricacion());

        if (dto.getTipoCamion() != null) {
            camion.setTipoCamion(Camion.TipoCamion.valueOf(dto.getTipoCamion()));
        }

        camion.setCapacidadPeso(dto.getCapacidadPeso());
        camion.setCapacidadVolumen(dto.getCapacidadVolumen());
        camion.setConsumoCombustible(dto.getConsumoCombustible());
        camion.setCostoBaseKm(dto.getCostoBaseKm());
        camion.setCostoMantenimientoDiario(dto.getCostoMantenimientoDiario());

        if (dto.getEstado() != null) {
            camion.setEstado(Camion.EstadoCamion.valueOf(dto.getEstado()));
        }

        if (dto.getCondicionMecanica() != null) {
            camion.setCondicionMecanica(Camion.CondicionMecanica.valueOf(dto.getCondicionMecanica()));
        }

        camion.setNumeroSeguro(dto.getNumeroSeguro());
        camion.setFechaVencimientoSeguro(dto.getFechaVencimientoSeguro());
        camion.setFechaUltimoService(dto.getFechaUltimoService());
        camion.setKilometrajeActual(dto.getKilometrajeActual());
        camion.setDepositoBaseId(dto.getDepositoBaseId());
        camion.setUbicacionActualLat(dto.getUbicacionActualLat());
        camion.setUbicacionActualLng(dto.getUbicacionActualLng());
        camion.setUbicacionDescripcion(dto.getUbicacionDescripcion());
        camion.setActivo(dto.getActivo() != null ? dto.getActivo() : true);

        return camion;
    }

    private void actualizarCamionExistente(Camion camionExistente, CamionDto dto) {
        if (dto.getMarca() != null) camionExistente.setMarca(dto.getMarca());
        if (dto.getModelo() != null) camionExistente.setModelo(dto.getModelo());
        if (dto.getAñoFabricacion() != null) camionExistente.setAñoFabricacion(dto.getAñoFabricacion());
        if (dto.getTipoCamion() != null) camionExistente.setTipoCamion(Camion.TipoCamion.valueOf(dto.getTipoCamion()));
        if (dto.getCapacidadPeso() != null) camionExistente.setCapacidadPeso(dto.getCapacidadPeso());
        if (dto.getCapacidadVolumen() != null) camionExistente.setCapacidadVolumen(dto.getCapacidadVolumen());
        if (dto.getConsumoCombustible() != null) camionExistente.setConsumoCombustible(dto.getConsumoCombustible());
        if (dto.getCostoBaseKm() != null) camionExistente.setCostoBaseKm(dto.getCostoBaseKm());
        if (dto.getCostoMantenimientoDiario() != null) camionExistente.setCostoMantenimientoDiario(dto.getCostoMantenimientoDiario());
        if (dto.getEstado() != null) camionExistente.setEstado(Camion.EstadoCamion.valueOf(dto.getEstado()));
        if (dto.getCondicionMecanica() != null) camionExistente.setCondicionMecanica(Camion.CondicionMecanica.valueOf(dto.getCondicionMecanica()));
        if (dto.getNumeroSeguro() != null) camionExistente.setNumeroSeguro(dto.getNumeroSeguro());
        if (dto.getFechaVencimientoSeguro() != null) camionExistente.setFechaVencimientoSeguro(dto.getFechaVencimientoSeguro());
        if (dto.getFechaUltimoService() != null) camionExistente.setFechaUltimoService(dto.getFechaUltimoService());
        if (dto.getKilometrajeActual() != null) camionExistente.setKilometrajeActual(dto.getKilometrajeActual());
        if (dto.getDepositoBaseId() != null) camionExistente.setDepositoBaseId(dto.getDepositoBaseId());
        if (dto.getUbicacionActualLat() != null) camionExistente.setUbicacionActualLat(dto.getUbicacionActualLat());
        if (dto.getUbicacionActualLng() != null) camionExistente.setUbicacionActualLng(dto.getUbicacionActualLng());
        if (dto.getUbicacionDescripcion() != null) camionExistente.setUbicacionDescripcion(dto.getUbicacionDescripcion());
        if (dto.getActivo() != null) camionExistente.setActivo(dto.getActivo());
    }
}
