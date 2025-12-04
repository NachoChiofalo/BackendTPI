package com.tpi.solicitudes.repository;

import com.tpi.solicitudes.entity.Solicitud;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SolicitudRepository extends JpaRepository<Solicitud, Integer> {
    List<Solicitud> findByTipoDocClienteAndNumDocCliente(Integer tipoDoc, Long numDoc);
    List<Solicitud> findByEstadoSolicitudNot(Integer estadoSolicitud);
    List<Solicitud> findByIdUbicacionDestino(Integer ubicacionId);

    // Nuevo: obtener la solicitud más reciente que involucra un contenedor y pertenece a un cliente
    Optional<Solicitud> findTopByIdContenedorAndTipoDocClienteAndNumDocClienteOrderByFechaHoraInicioDesc(
            Integer idContenedor, Integer tipoDocCliente, Long numDocCliente);

    // Nuevo: obtener todas las solicitudes de un cliente ordenadas por fecha de inicio descendente
    List<Solicitud> findByTipoDocClienteAndNumDocClienteOrderByFechaHoraInicioDesc(Integer tipoDoc, Long numDoc);

    // Obtener la solicitud asociada a un contenedor específico
    Optional<Solicitud> findByIdContenedor(Integer idContenedor);

    // Obtener solicitudes asociadas a una ruta específica
    List<Solicitud> findByIdRuta(Integer idRuta);

    // Obtener la primera solicitud por rutaId (para validaciones de capacidad)
    Optional<Solicitud> findFirstByIdRuta(Integer idRuta);
}