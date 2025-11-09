package com.tpi.flotas.repository;

import com.tpi.flotas.entity.Transportista;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransportistaRepository extends JpaRepository<Transportista, Integer> {

    // Buscar por teléfono
    Optional<Transportista> findByTelefono(Long telefono);

    // Buscar transportistas por texto (nombre o apellido)
    @Query("SELECT t FROM Transportista t WHERE " +
           "(LOWER(t.nombre) LIKE LOWER(CONCAT('%', :texto, '%')) OR " +
           "LOWER(t.apellido) LIKE LOWER(CONCAT('%', :texto, '%')))")
    List<Transportista> buscarPorTexto(@Param("texto") String texto);

    // Buscar por nombre completo
    List<Transportista> findByNombreContainingIgnoreCaseOrApellidoContainingIgnoreCase(String nombre, String apellido);

    // Contar todos los transportistas
    long count();
}
