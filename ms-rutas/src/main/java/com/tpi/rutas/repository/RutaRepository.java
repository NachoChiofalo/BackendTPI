package com.tpi.rutas.repository;

import com.tpi.rutas.entity.Ruta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RutaRepository extends JpaRepository<Ruta, Long> {

    List<Ruta> findByCodigoContainingIgnoreCase(String codigo);

    List<Ruta> findBySolicitudId(Long solicitudId);
}
