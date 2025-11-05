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

    // Buscar camiones activos
    List<Camion> findByActivoTrue();

    // Buscar camiones disponibles
    List<Camion> findByActivoTrueAndDisponibleTrue();

    // Buscar por transportista
    List<Camion> findByTransportistaIdAndActivoTrue(Long transportistaId);

    // Buscar por tipo de camión
    List<Camion> findByTipoCamionIdAndActivoTrue(Long tipoCamionId);

    // Buscar por depósito actual
    List<Camion> findByDepositoActualIdAndActivoTrue(Long depositoId);

    // Buscar por capacidad mínima
    @Query("SELECT c FROM Camion c WHERE c.activo = true AND c.capacidadPeso >= :pesoMin AND c.capacidadVolumen >= :volumenMin")
    List<Camion> findByCapacidadMinima(@Param("pesoMin") BigDecimal pesoMin, @Param("volumenMin") BigDecimal volumenMin);

    // Buscar camiones disponibles con capacidad suficiente
    @Query("SELECT c FROM Camion c WHERE c.activo = true AND c.disponible = true AND c.capacidadPeso >= :pesoMin AND c.capacidadVolumen >= :volumenMin")
    List<Camion> findDisponiblesConCapacidad(@Param("pesoMin") BigDecimal pesoMin, @Param("volumenMin") BigDecimal volumenMin);
}
