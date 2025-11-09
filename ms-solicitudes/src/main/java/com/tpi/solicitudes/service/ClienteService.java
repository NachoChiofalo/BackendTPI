package com.tpi.solicitudes.service;

import com.tpi.solicitudes.entity.Cliente;
import com.tpi.solicitudes.repository.ClienteRepository;
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
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public List<Cliente> obtenerTodos() {
        log.info("Obteniendo todos los clientes");
        return clienteRepository.findAll();
    }

    public Optional<Cliente> obtenerPorId(Long id) {
        log.info("Obteniendo cliente por id: {}", id);
        return clienteRepository.findById(id);
    }

    public List<Cliente> buscarPorNombre(String nombre) {
        log.info("Buscando clientes por nombre: {}", nombre);
        return clienteRepository.findByNombreContainingIgnoreCase(nombre);
    }

    public Optional<Cliente> buscarPorEmail(String email) {
        log.info("Buscando cliente por email: {}", email);
        List<Cliente> res = clienteRepository.findByEmail(email);
        return res.isEmpty() ? Optional.empty() : Optional.of(res.get(0));
    }

    @Transactional
    public Cliente guardar(Cliente cliente) {
        log.info("Guardando cliente: {}", cliente.getNombre());
        return clienteRepository.save(cliente);
    }

    @Transactional
    public Cliente actualizar(Long id, Cliente actualizado) {
        log.info("Actualizando cliente: {}", id);
        return clienteRepository.findById(id)
                .map(c -> {
                    c.setNombre(actualizado.getNombre());
                    c.setEmail(actualizado.getEmail());
                    c.setTelefono(actualizado.getTelefono());
                    c.setDireccion(actualizado.getDireccion());
                    c.setNumeroDocumento(actualizado.getNumeroDocumento());
                    c.setTipoDocumento(actualizado.getTipoDocumento());
                    c.setActivo(actualizado.getActivo());
                    return clienteRepository.save(c);
                })
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con id: " + id));
    }

    @Transactional
    public void eliminar(Long id) {
        log.info("Eliminando cliente: {}", id);
        if (clienteRepository.existsById(id)) {
            clienteRepository.deleteById(id);
        } else {
            throw new RuntimeException("Cliente no encontrado con id: " + id);
        }
    }
}