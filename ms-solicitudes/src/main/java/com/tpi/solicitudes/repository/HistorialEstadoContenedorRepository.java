package com.tpi.solicitudes.repository;

import com.tpi.solicitudes.entity.HistorialEstadoContenedor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistorialEstadoContenedorRepository extends JpaRepository<HistorialEstadoContenedor, Integer> {
    List<HistorialEstadoContenedor> findByIdContenedorOrderByFechaInicioAsc(Integer idContenedor);
}
