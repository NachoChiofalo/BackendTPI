package com.tpi.flotas.service;

import com.tpi.flotas.entity.Transportista;
import com.tpi.flotas.repository.TransportistaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class TransportistaService {

    private final TransportistaRepository transportistaRepository;

    public List<Transportista> obtenerTodos() {
        log.info("Obteniendo todos los transportistas activos");
        return transportistaRepository.findByActivoTrue();
    }

    public Optional<Transportista> obtenerPorId(Long id) {
        log.info("Obteniendo transportista por ID: {}", id);
        return transportistaRepository.findById(id);
    }

    public Optional<Transportista> obtenerPorDni(String dni) {
        log.info("Obteniendo transportista por DNI: {}", dni);
        return transportistaRepository.findByDniAndActivoTrue(dni);
    }

    public List<Transportista> buscarPorTexto(String texto) {
        log.info("Buscando transportistas por texto: {}", texto);
        return transportistaRepository.buscarPorTexto(texto);
    }

    @Transactional
    public Transportista guardar(Transportista transportista) {
        log.info("Guardando transportista: {} {}", transportista.getNombre(), transportista.getApellido());
        return transportistaRepository.save(transportista);
    }

    @Transactional
    public Transportista actualizar(Long id, Transportista transportistaActualizado) {
        log.info("Actualizando transportista: {}", id);
        return transportistaRepository.findById(id)
                .map(transportista -> {
                    // Actualizar campos permitidos
                    if (transportistaActualizado.getNombre() != null) {
                        transportista.setNombre(transportistaActualizado.getNombre());
                    }
                    if (transportistaActualizado.getApellido() != null) {
                        transportista.setApellido(transportistaActualizado.getApellido());
                    }
                    if (transportistaActualizado.getTelefono() != null) {
                        transportista.setTelefono(transportistaActualizado.getTelefono());
                    }
                    if (transportistaActualizado.getEmail() != null) {
                        transportista.setEmail(transportistaActualizado.getEmail());
                    }
                    if (transportistaActualizado.getFechaVencimientoLicencia() != null) {
                        transportista.setFechaVencimientoLicencia(transportistaActualizado.getFechaVencimientoLicencia());
                    }
                    return transportistaRepository.save(transportista);
                })
                .orElseThrow(() -> new RuntimeException("Transportista no encontrado: " + id));
    }

    @Transactional
    public void eliminar(Long id) {
        log.info("Marcando como inactivo el transportista: {}", id);
        transportistaRepository.findById(id)
                .ifPresentOrElse(
                        transportista -> {
                            transportista.setActivo(false);
                            transportistaRepository.save(transportista);
                        },
                        () -> {
                            throw new RuntimeException("Transportista no encontrado: " + id);
                        }
                );
    }

    public long contarTransportistas() {
        return transportistaRepository.countByActivoTrue();
    }
}
