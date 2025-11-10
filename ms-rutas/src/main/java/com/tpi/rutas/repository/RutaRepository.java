package com.tpi.rutas.repository;

import com.tpi.rutas.entity.Ruta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RutaRepository extends JpaRepository<Ruta, Integer> {
    // Los campos codigo y solicitudId no existen en la tabla real
}
