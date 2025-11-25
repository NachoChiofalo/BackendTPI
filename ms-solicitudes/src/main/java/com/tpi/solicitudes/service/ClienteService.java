package com.tpi.solicitudes.service;

import com.tpi.solicitudes.entity.Cliente;
import com.tpi.solicitudes.entity.ClienteId;
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

    public Optional<Cliente> obtenerPorId(Integer tipoDocClienteId, Long numDocCliente) {
        log.info("Obteniendo cliente por tipo doc: {} y num doc: {}", tipoDocClienteId, numDocCliente);
        ClienteId id = new ClienteId(tipoDocClienteId, numDocCliente);
        return clienteRepository.findById(id);
    }

    public List<Cliente> buscarPorNombres(String nombres) {
        log.info("Buscando clientes por nombres: {}", nombres);
        return clienteRepository.findByNombresContainingIgnoreCase(nombres);
    }

    public List<Cliente> buscarPorApellidos(String apellidos) {
        log.info("Buscando clientes por apellidos: {}", apellidos);
        return clienteRepository.findByApellidosContainingIgnoreCase(apellidos);
    }

    @Transactional
    public Cliente guardar(Cliente cliente) {
        log.info("Guardando cliente: {} {}", cliente.getNombres(), cliente.getApellidos());
        return clienteRepository.save(cliente);
    }

    @Transactional
    public Cliente actualizar(Integer tipoDocClienteId, Long numDocCliente, Cliente actualizado) {
        log.info("Actualizando cliente tipo doc: {} num doc: {}", tipoDocClienteId, numDocCliente);
        ClienteId id = new ClienteId(tipoDocClienteId, numDocCliente);
        return clienteRepository.findById(id)
                .map(c -> {
                    c.setNombres(actualizado.getNombres());
                    c.setApellidos(actualizado.getApellidos());
                    c.setTelefono(actualizado.getTelefono());
                    c.setDomicilio(actualizado.getDomicilio());
                    return clienteRepository.save(c);
                })
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
    }

    @Transactional
    public void eliminar(Integer tipoDocClienteId, Long numDocCliente) {
        log.info("Eliminando cliente tipo doc: {} num doc: {}", tipoDocClienteId, numDocCliente);
        ClienteId id = new ClienteId(tipoDocClienteId, numDocCliente);
        if (clienteRepository.existsById(id)) {
            clienteRepository.deleteById(id);
        } else {
            throw new RuntimeException("Cliente no encontrado");
        }
    }

    @Transactional
    public Cliente guardarSiNoExiste(Integer tipoDocClienteId, Long numDocCliente, Cliente clienteACrear) {
        log.info("guardarSiNoExiste: {} - {}", tipoDocClienteId, numDocCliente);
        Optional<Cliente> existente = clienteRepository.findByTipoDocClienteIdAndNumDocCliente(tipoDocClienteId, numDocCliente);
        if (existente.isPresent()) {
            log.info("Cliente ya existe: {}-{}", tipoDocClienteId, numDocCliente);
            return existente.get();
        }

        if (clienteACrear == null) {
            throw new RuntimeException("Datos insuficientes para crear cliente");
        }

        // Validar nombres/apellidos antes de crear
        if (clienteACrear.getNombres() == null || clienteACrear.getNombres().trim().isEmpty()
                || clienteACrear.getApellidos() == null || clienteACrear.getApellidos().trim().isEmpty()) {
            log.warn("Intento de crear cliente sin nombres/apellidos: {} - {}", tipoDocClienteId, numDocCliente);
            // Permitimos creación con placeholders para no bloquear flujo; pero registramos la advertencia
            clienteACrear.setNombres(clienteACrear.getNombres() == null ? "SIN_NOMBRE" : clienteACrear.getNombres());
            clienteACrear.setApellidos(clienteACrear.getApellidos() == null ? "SIN_APELLIDO" : clienteACrear.getApellidos());
        }

        clienteACrear.setTipoDocClienteId(tipoDocClienteId);
        clienteACrear.setNumDocCliente(numDocCliente);
        log.info("Cliente no existente: creando nuevo cliente {} {}", clienteACrear.getNombres(), clienteACrear.getApellidos());
        return clienteRepository.save(clienteACrear);
    }
}