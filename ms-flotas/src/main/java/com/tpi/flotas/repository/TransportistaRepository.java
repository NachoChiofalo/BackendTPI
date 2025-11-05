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

    // Buscar transportistas activos
    List<Transportista> findByActivoTrue();

    // Buscar por DNI
    Optional<Transportista> findByDniAndActivoTrue(String dni);

    // Buscar por email
    Optional<Transportista> findByEmailAndActivoTrue(String email);

    // Buscar transportistas por texto (nombre, apellido, dni o email)
    @Query("SELECT t FROM Transportista t WHERE t.activo = true AND " +
           "(LOWER(t.nombre) LIKE LOWER(CONCAT('%', :texto, '%')) OR " +
           "LOWER(t.apellido) LIKE LOWER(CONCAT('%', :texto, '%')) OR " +
           "LOWER(t.dni) LIKE LOWER(CONCAT('%', :texto, '%')) OR " +
           "LOWER(t.email) LIKE LOWER(CONCAT('%', :texto, '%')))")
    List<Transportista> buscarPorTexto(@Param("texto") String texto);

    // Buscar por licencia de conducir
    Optional<Transportista> findByLicenciaConducirAndActivoTrue(String licenciaConducir);

    // Contar transportistas activos
    long countByActivoTrue();
}
