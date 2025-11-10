package com.tpi.solicitudes.repository;

import com.tpi.solicitudes.entity.Contenedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContenedorRepository extends JpaRepository<Contenedor, String> {
    List<Contenedor> findByClienteId(Long clienteId);
    List<Contenedor> findByEstado(String estado);
}