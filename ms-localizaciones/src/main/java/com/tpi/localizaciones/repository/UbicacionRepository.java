package com.tpi.localizaciones.repository;

import com.tpi.localizaciones.entity.Ubicacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UbicacionRepository extends JpaRepository<Ubicacion, Integer> {
    Optional<Ubicacion> findByLatitudAndLongitud(String latitud, String longitud);
}

