package com.tpi.rutas.repository;

import com.tpi.rutas.entity.Tramo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TramoRepository extends JpaRepository<Tramo, Long> {
    List<Tramo> findByRutaIdOrderByOrdenAsc(Long rutaId);
    List<Tramo> findByEstado(String estado);
}
