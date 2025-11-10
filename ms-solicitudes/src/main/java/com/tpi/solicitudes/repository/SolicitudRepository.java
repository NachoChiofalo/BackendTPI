package com.tpi.solicitudes.repository;

import com.tpi.solicitudes.entity.Solicitud;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SolicitudRepository extends JpaRepository<Solicitud, Long> {
    List<Solicitud> findByNumeroContainingIgnoreCase(String numero);
    List<Solicitud> findByClienteId(Long clienteId);
    List<Solicitud> findByEstado(String estado);
}