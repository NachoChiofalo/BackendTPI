package com.tpi.flotas.repository;

import com.tpi.flotas.entity.Camion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface CamionRepository extends JpaRepository<Camion, String> {

    // Buscar camiones disponibles
    List<Camion> findByDisponibleTrue();

    // Buscar camiones no disponibles
    List<Camion> findByDisponibleFalse();

    // Buscar por capacidad mínima
    @Query("SELECT c FROM Camion c WHERE c.capacidadPeso >= :pesoMin AND c.capacidadVolumen >= :volumenMin")
    List<Camion> findByCapacidadMinima(@Param("pesoMin") BigDecimal pesoMin, @Param("volumenMin") BigDecimal volumenMin);

    // Buscar camiones disponibles con capacidad mínima
    @Query("SELECT c FROM Camion c WHERE c.disponible = true AND c.capacidadPeso >= :pesoMin AND c.capacidadVolumen >= :volumenMin")
    List<Camion> findDisponiblesConCapacidadMinima(@Param("pesoMin") BigDecimal pesoMin, @Param("volumenMin") BigDecimal volumenMin);
}
