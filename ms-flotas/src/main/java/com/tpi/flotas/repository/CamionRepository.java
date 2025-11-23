package com.tpi.flotas.repository;

import com.tpi.flotas.entity.Camion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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

    // Versión ordenada por mejor ajuste (menor capacidadPeso primero) para intentar reservar el mejor candidato
    @Query("SELECT c FROM Camion c WHERE c.disponible = true AND c.capacidadPeso >= :pesoMin AND c.capacidadVolumen >= :volumenMin ORDER BY c.capacidadPeso ASC, c.capacidadVolumen ASC")
    List<Camion> findDisponiblesConCapacidadMinimaOrderByAjuste(@Param("pesoMin") BigDecimal pesoMin, @Param("volumenMin") BigDecimal volumenMin);

    // Encontrar por dominio (ID)
    Camion findByDominio(String dominio);

    // Reservar atómicamente: solo actualizará si sigue disponible
    @Modifying
    @Query("UPDATE Camion c SET c.disponible = false WHERE c.dominio = :dominio AND c.disponible = true")
    int reservarIfDisponible(@Param("dominio") String dominio);

    // Liberar camión
    @Modifying
    @Query("UPDATE Camion c SET c.disponible = true WHERE c.dominio = :dominio")
    int liberarCamion(@Param("dominio") String dominio);
}
