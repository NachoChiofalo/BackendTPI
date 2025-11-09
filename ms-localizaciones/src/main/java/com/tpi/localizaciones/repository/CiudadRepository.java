package com.tpi.localizaciones.repository;

import com.tpi.localizaciones.entity.Ciudad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CiudadRepository extends JpaRepository<Ciudad, Long> {
    
    List<Ciudad> findByNombreContainingIgnoreCase(String nombre);
    
    List<Ciudad> findByProvinciaContainingIgnoreCase(String provincia);
    
    List<Ciudad> findByPaisContainingIgnoreCase(String pais);
    
    List<Ciudad> findByActivaTrue();
    
    List<Ciudad> findByCodigoPostal(String codigoPostal);
}