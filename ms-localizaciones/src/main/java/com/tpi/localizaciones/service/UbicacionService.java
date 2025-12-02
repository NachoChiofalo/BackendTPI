package com.tpi.localizaciones.service;

import com.tpi.localizaciones.entity.Ubicacion;
import com.tpi.localizaciones.repository.UbicacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UbicacionService {

    private final UbicacionRepository ubicacionRepository;

    public List<Ubicacion> findAll() {
        return ubicacionRepository.findAll();
    }

    public Optional<Ubicacion> findById(Integer id) {
        return ubicacionRepository.findById(id);
    }

    public long count() {
        return ubicacionRepository.count();
    }

    @Transactional
    public Ubicacion save(Ubicacion ubicacion) {
        return ubicacionRepository.save(ubicacion);
    }

    /**
     * Busca una ubicación por coordenadas. Si no existe, crea una nueva.
     */
    @Transactional
    public Ubicacion findOrCreateByCoordinates(Double latitud, Double longitud) {
        // Convertir Double a String para comparar
        String latitudStr = String.valueOf(latitud);
        String longitudStr = String.valueOf(longitud);

        return ubicacionRepository.findByLatitudAndLongitud(latitudStr, longitudStr)
                .orElseGet(() -> {
                    Ubicacion nuevaUbicacion = new Ubicacion();
                    // Asignar valores String convertidos desde Double
                    nuevaUbicacion.setLatitud(String.valueOf(latitud));
                    nuevaUbicacion.setLongitud(String.valueOf(longitud));
                    nuevaUbicacion.setCiudad("Desconocida"); // Valor por defecto requerido
                    nuevaUbicacion.setNombre("Ubicación generada automáticamente");
                    return ubicacionRepository.save(nuevaUbicacion);
                });
    }
}

