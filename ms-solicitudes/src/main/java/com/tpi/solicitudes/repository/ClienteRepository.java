package com.tpi.solicitudes.repository;

import com.tpi.solicitudes.entity.Cliente;
import com.tpi.solicitudes.entity.ClienteId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, ClienteId> {
    List<Cliente> findByNombresContainingIgnoreCase(String nombres);
    List<Cliente> findByApellidosContainingIgnoreCase(String apellidos);
}