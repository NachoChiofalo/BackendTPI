package com.tpi.flotas.repository;

import com.tpi.flotas.entity.Camion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CamionRepository extends JpaRepository<Camion, String> {

    List<Camion> findByActivoTrue();

    List<Camion> findByEstado(Camion.EstadoCamion estado);

    List<Camion> findByTipoCamion(Camion.TipoCamion tipoCamion);

    List<Camion> findByTransportistaId(Long transportistaId);

    List<Camion> findByDepositoBaseId(Long depositoId);

    @Query("SELECT c FROM Camion c WHERE c.activo = true AND c.estado = 'DISPONIBLE'")
    List<Camion> findDisponibles();

    @Query("SELECT c FROM Camion c WHERE c.capacidadPeso >= :pesoMinimo AND c.capacidadVolumen >= :volumenMinimo AND c.activo = true AND c.estado = 'DISPONIBLE'")
    List<Camion> findByCapacidadMinimaDisponibles(@Param("pesoMinimo") Double pesoMinimo, @Param("volumenMinimo") Double volumenMinimo);

    @Query("SELECT c FROM Camion c WHERE c.kilometrajeActual > :kilometraje")
    List<Camion> findByKilometrajeSuperiora(@Param("kilometraje") Long kilometraje);
}
