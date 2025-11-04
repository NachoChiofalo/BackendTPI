package com.tpi.flotas.repository;

import com.tpi.flotas.entity.Transportista;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransportistaRepository extends JpaRepository<Transportista, Long> {

    Optional<Transportista> findByUsername(String username);

    Optional<Transportista> findByEmail(String email);

    List<Transportista> findByActivoTrue();

    @Query("SELECT t FROM Transportista t WHERE t.fechaVencimientoLicencia < CURRENT_TIMESTAMP AND t.activo = true")
    List<Transportista> findConLicenciaVencida();

    @Query("SELECT t FROM Transportista t WHERE " +
           "LOWER(t.nombre) LIKE LOWER(CONCAT('%', :texto, '%')) OR " +
           "LOWER(t.apellido) LIKE LOWER(CONCAT('%', :texto, '%')) OR " +
           "LOWER(t.username) LIKE LOWER(CONCAT('%', :texto, '%'))")
    List<Transportista> buscarPorTexto(@Param("texto") String texto);
}
