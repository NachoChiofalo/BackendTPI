package com.tpi.localizaciones.repository;

import com.tpi.localizaciones.entity.TipoUbicacion;
import com.tpi.localizaciones.entity.Ubicacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface UbicacionRepository extends JpaRepository<Ubicacion, Long> {
    
    List<Ubicacion> findByTipoUbicacion(TipoUbicacion tipo);
    
    List<Ubicacion> findByCiudadId(Long ciudadId);
    
    List<Ubicacion> findByNombreContainingIgnoreCase(String nombre);
    
    @Query("SELECT u FROM Ubicacion u WHERE " +
           "(:lat IS NULL OR ABS(u.latitud - :lat) <= :radioKm / 111.12) AND " +
           "(:lon IS NULL OR ABS(u.longitud - :lon) <= :radioKm / (111.12 * COS(RADIANS(:lat)))) AND " +
           "(:tipo IS NULL OR u.tipoUbicacion = :tipo)")
    List<Ubicacion> findByRadioAndTipo(
            @Param("lat") BigDecimal latitud,
            @Param("lon") BigDecimal longitud,
            @Param("radioKm") Double radioKm,
            @Param("tipo") TipoUbicacion tipo);
            
    @Query("SELECT u FROM Ubicacion u WHERE " +
           "u.tieneGrua = true AND " +
           "(:pesoMax IS NULL OR u.pesoMaximoToneladas >= :pesoMax) AND " +
           "(:alturaMax IS NULL OR u.alturaMaximaMetros >= :alturaMax)")
    List<Ubicacion> findDepositosConCapacidad(
            @Param("pesoMax") BigDecimal pesoMaximo,
            @Param("alturaMax") BigDecimal alturaMaxima);
}