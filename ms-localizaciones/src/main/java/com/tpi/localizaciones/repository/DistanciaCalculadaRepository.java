package com.tpi.localizaciones.repository;

import com.tpi.localizaciones.entity.DistanciaCalculada;
import com.tpi.localizaciones.entity.EstadoValidacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DistanciaCalculadaRepository extends JpaRepository<DistanciaCalculada, Long> {
    
    @Query("SELECT d FROM DistanciaCalculada d " +
          "WHERE d.ubicacionOrigen.id = :origenId AND d.ubicacionDestino.id = :destinoId " +
          "AND d.estadoValidacion = 'VALIDADA' " +
          "AND d.momentoCalculo > :fecha " +
          "ORDER BY d.momentoCalculo DESC")
    Optional<DistanciaCalculada> findUltimaDistanciaValidada(
            @Param("origenId") Long origenId,
            @Param("destinoId") Long destinoId,
            @Param("fecha") LocalDateTime fecha);
    
    List<DistanciaCalculada> findByEstadoValidacion(EstadoValidacion estado);
    
    List<DistanciaCalculada> findByUbicacionOrigenId(Long origenId);
    
    List<DistanciaCalculada> findByUbicacionDestinoId(Long destinoId);
    
    @Query("SELECT d FROM DistanciaCalculada d " +
          "WHERE d.ubicacionOrigen.id = :origenId " +
          "AND d.ubicacionDestino.id = :destinoId")
    Optional<DistanciaCalculada> findByOrigenYDestino(
            @Param("origenId") Long origenId,
            @Param("destinoId") Long destinoId);
}