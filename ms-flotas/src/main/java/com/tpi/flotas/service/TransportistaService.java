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
        log.info("Obteniendo todos los transportistas");
        return transportistaRepository.findAll();
    }

    public Optional<Transportista> obtenerPorId(Integer id) {
        log.info("Obteniendo transportista por ID: {}", id);
        return transportistaRepository.findById(id);
    }

    public Optional<Transportista> obtenerPorTelefono(Long telefono) {
        log.info("Obteniendo transportista por teléfono: {}", telefono);
        return transportistaRepository.findByTelefono(telefono);
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
    public Transportista actualizar(Integer id, Transportista transportistaActualizado) {
        log.info("Actualizando transportista: {}", id);
        return transportistaRepository.findById(id)
                .map(transportista -> {
                    transportista.setNombre(transportistaActualizado.getNombre());
                    transportista.setApellido(transportistaActualizado.getApellido());
                    transportista.setTelefono(transportistaActualizado.getTelefono());
                    return transportistaRepository.save(transportista);
                })
                .orElseThrow(() -> new RuntimeException("Transportista no encontrado con ID: " + id));
    }

    @Transactional
    public void eliminar(Integer id) {
        log.info("Eliminando transportista: {}", id);
        if (transportistaRepository.existsById(id)) {
            transportistaRepository.deleteById(id);
        } else {
            throw new RuntimeException("Transportista no encontrado con ID: " + id);
        }
    }
}
