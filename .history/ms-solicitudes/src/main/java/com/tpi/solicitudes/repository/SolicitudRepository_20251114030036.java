package com.tpi.solicitudes.repository;

import com.tpi.solicitudes.entity.Solicitud;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SolicitudRepository extends JpaRepository<Solicitud, Integer> {
    List<Solicitud> findByTipoDocClienteAndNumDocCliente(Integer tipoDoc, Long numDoc);
    List<Solicitud> findByEstadoSolicitudNot(Integer estadoSolicitud);
    List<Solicitud> findByIdUbicacionDestino(Integer ubicacionId);
}