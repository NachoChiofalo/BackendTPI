package com.tpi.solicitudes.repository;

import com.tpi.solicitudes.entity.Contenedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface ContenedorRepository extends JpaRepository<Contenedor, Integer> {
    // Removido findByClienteId y findByEstado ya que esos campos no existen en la tabla real

    Optional<Contenedor> findTopByOrderByIdContenedorDesc();
}