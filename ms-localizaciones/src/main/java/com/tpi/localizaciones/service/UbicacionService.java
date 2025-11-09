package com.tpi.localizaciones.service;

import com.tpi.localizaciones.entity.TipoUbicacion;
import com.tpi.localizaciones.entity.Ubicacion;
import com.tpi.localizaciones.repository.UbicacionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UbicacionService {

    private final UbicacionRepository ubicacionRepository;

    public List<Ubicacion> obtenerTodas() {
        log.info("Obteniendo todas las ubicaciones");
        return ubicacionRepository.findAll();
    }

    public Optional<Ubicacion> obtenerPorId(Long id) {
        log.info("Obteniendo ubicación por id: {}", id);
        return ubicacionRepository.findById(id);
    }

    public List<Ubicacion> buscarPorNombre(String nombre) {
        log.info("Buscando ubicaciones por nombre: {}", nombre);
        return ubicacionRepository.findByNombreContainingIgnoreCase(nombre);
    }

    public List<Ubicacion> buscarPorTipo(TipoUbicacion tipo) {
        log.info("Buscando ubicaciones por tipo: {}", tipo);
        return ubicacionRepository.findByTipoUbicacion(tipo);
    }

    public List<Ubicacion> buscarPorCiudad(Long ciudadId) {
        log.info("Buscando ubicaciones por ciudad: {}", ciudadId);
        return ubicacionRepository.findByCiudadId(ciudadId);
    }

    public List<Ubicacion> buscarPorRadioYTipo(BigDecimal latitud, BigDecimal longitud, 
                                              Double radioKm, TipoUbicacion tipo) {
        log.info("Buscando ubicaciones - Lat: {}, Lon: {}, Radio: {}km, Tipo: {}", 
                latitud, longitud, radioKm, tipo);
        return ubicacionRepository.findByRadioAndTipo(latitud, longitud, radioKm, tipo);
    }

    public List<Ubicacion> buscarDepositosConCapacidad(BigDecimal pesoMaximo, BigDecimal alturaMaxima) {
        log.info("Buscando depósitos con capacidad - Peso máx: {}, Altura máx: {}", 
                pesoMaximo, alturaMaxima);
        return ubicacionRepository.findDepositosConCapacidad(pesoMaximo, alturaMaxima);
    }

    @Transactional
    public Ubicacion guardar(Ubicacion ubicacion) {
        log.info("Guardando ubicación: {}", ubicacion.getNombre());
        return ubicacionRepository.save(ubicacion);
    }

    @Transactional
    public Ubicacion actualizar(Long id, Ubicacion actualizada) {
        log.info("Actualizando ubicación: {}", id);
        return ubicacionRepository.findById(id)
                .map(u -> {
                    u.setNombre(actualizada.getNombre());
                    u.setDireccion(actualizada.getDireccion());
                    u.setLatitud(actualizada.getLatitud());
                    u.setLongitud(actualizada.getLongitud());
                    u.setTipoUbicacion(actualizada.getTipoUbicacion());
                    u.setCodigoPostal(actualizada.getCodigoPostal());
                    u.setNumeroPuerta(actualizada.getNumeroPuerta());
                    u.setPiso(actualizada.getPiso());
                    u.setDepartamento(actualizada.getDepartamento());
                    u.setEntreCalles(actualizada.getEntreCalles());
                    u.setReferencias(actualizada.getReferencias());
                    u.setContactoNombre(actualizada.getContactoNombre());
                    u.setContactoTelefono(actualizada.getContactoTelefono());
                    u.setContactoEmail(actualizada.getContactoEmail());
                    u.setHorarioAtencion(actualizada.getHorarioAtencion());
                    u.setInstruccionesAcceso(actualizada.getInstruccionesAcceso());
                    u.setTieneMuelleCarga(actualizada.getTieneMuelleCarga());
                    u.setTieneGrua(actualizada.getTieneGrua());
                    u.setAlturaMaximaMetros(actualizada.getAlturaMaximaMetros());
                    u.setPesoMaximoToneladas(actualizada.getPesoMaximoToneladas());
                    u.setEspacioManiobraMetros(actualizada.getEspacioManiobraMetros());
                    return ubicacionRepository.save(u);
                })
                .orElseThrow(() -> new RuntimeException("Ubicación no encontrada con id: " + id));
    }

    @Transactional
    public void eliminar(Long id) {
        log.info("Eliminando ubicación: {}", id);
        if (ubicacionRepository.existsById(id)) {
            ubicacionRepository.deleteById(id);
        } else {
            throw new RuntimeException("Ubicación no encontrada con id: " + id);
        }
    }
}