package com.tpi.localizaciones.repository;

import com.tpi.localizaciones.entity.DistanciaCalculada;
import com.tpi.localizaciones.entity.EstadoValidacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
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

    /**
     * Busca por coordenadas exactas
     */
    @Query("SELECT d FROM DistanciaCalculada d " +
          "WHERE d.latitudOrigen = :latOrigen " +
          "AND d.longitudOrigen = :lonOrigen " +
          "AND d.latitudDestino = :latDestino " +
          "AND d.longitudDestino = :lonDestino " +
          "ORDER BY d.momentoCalculo DESC")
    Optional<DistanciaCalculada> findByCoordenadasExactas(
            @Param("latOrigen") BigDecimal latOrigen,
            @Param("lonOrigen") BigDecimal lonOrigen,
            @Param("latDestino") BigDecimal latDestino,
            @Param("lonDestino") BigDecimal lonDestino);

    /**
     * Elimina distancias expiradas
     */
    @Modifying
    @Query("DELETE FROM DistanciaCalculada d WHERE d.fechaExpiracion < :fecha")
    int eliminarExpiradas(@Param("fecha") LocalDateTime fecha);
}