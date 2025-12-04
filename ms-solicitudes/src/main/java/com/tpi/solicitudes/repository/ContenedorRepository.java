package com.tpi.solicitudes.repository;

import com.tpi.solicitudes.entity.Contenedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface ContenedorRepository extends JpaRepository<Contenedor, Integer> {

    Optional<Contenedor> findTopByOrderByIdContenedorDesc();

    @Query("SELECT c FROM Contenedor c LEFT JOIN FETCH c.estadoContenedor WHERE c.idContenedor = :id")
    Optional<Contenedor> findByIdWithEstado(@Param("id") Integer id);

    @Query("SELECT c FROM Contenedor c LEFT JOIN FETCH c.estadoContenedor")
    List<Contenedor> findAllWithEstado();

    @Query("""
        SELECT c FROM Contenedor c 
        LEFT JOIN FETCH c.estadoContenedor 
        WHERE c.idContenedor IN (
            SELECT s.idContenedor FROM Solicitud s 
            WHERE s.numDocCliente = :numDocCliente
        )
    """)
    List<Contenedor> findContenedoresByNumDocCliente(@Param("numDocCliente") Long numDocCliente);
}