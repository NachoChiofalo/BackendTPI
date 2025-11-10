package com.tpi.solicitudes.repository;

import com.tpi.solicitudes.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    List<Cliente> findByNombreContainingIgnoreCase(String nombre);
    List<Cliente> findByEmail(String email);
    List<Cliente> findByActivoTrue();
}