package com.tpi.precios.repository;

import com.tpi.precios.entity.Tarifa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TarifaRepository extends JpaRepository<Tarifa, Integer> {

    // Buscar tarifas vigentes en la fecha actual
    @Query("SELECT t FROM Tarifa t WHERE :fecha BETWEEN t.fechaVigenciaInicio AND t.fechaVigenciaFin")
    List<Tarifa> findTarifasVigentesEn(@Param("fecha") LocalDate fecha);

    // Buscar tarifas vigentes actualmente
    @Query("SELECT t FROM Tarifa t WHERE CURRENT_DATE BETWEEN t.fechaVigenciaInicio AND t.fechaVigenciaFin")
    List<Tarifa> findTarifasVigentes();

    // Buscar la tarifa más reciente vigente
    @Query("SELECT t FROM Tarifa t WHERE CURRENT_DATE BETWEEN t.fechaVigenciaInicio AND t.fechaVigenciaFin ORDER BY t.fechaVigenciaInicio DESC")
    Optional<Tarifa> findTarifaVigenteMasReciente();

    // Buscar tarifas por rango de fechas
    @Query("SELECT t FROM Tarifa t WHERE t.fechaVigenciaInicio >= :fechaInicio AND t.fechaVigenciaFin <= :fechaFin")
    List<Tarifa> findByRangoFechas(@Param("fechaInicio") LocalDate fechaInicio, @Param("fechaFin") LocalDate fechaFin);

    // Buscar tarifas que vencen pronto
    @Query("SELECT t FROM Tarifa t WHERE t.fechaVigenciaFin BETWEEN :fechaInicio AND :fechaFin")
    List<Tarifa> findTarifasQueVencenEntre(@Param("fechaInicio") LocalDate fechaInicio, @Param("fechaFin") LocalDate fechaFin);

    // Buscar tarifas futuras
    @Query("SELECT t FROM Tarifa t WHERE t.fechaVigenciaInicio > CURRENT_DATE ORDER BY t.fechaVigenciaInicio ASC")
    List<Tarifa> findTarifasFuturas();
}
