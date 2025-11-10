package com.tpi.localizaciones.service;

import com.tpi.localizaciones.client.OsrmClient;
import com.tpi.localizaciones.dto.osrm.OsrmDistanciaRequest;
import com.tpi.localizaciones.dto.osrm.OsrmDistanciaResponse;
import com.tpi.localizaciones.entity.DistanciaCalculada;
import com.tpi.localizaciones.entity.EstadoValidacion;
import com.tpi.localizaciones.repository.DistanciaCalculadaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Servicio para cálculo de distancias usando OSRM
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class OsrmDistanciaService {

    private final OsrmClient osrmClient;
    private final DistanciaCalculadaRepository distanciaRepository;

    /**
     * Calcula la distancia entre dos puntos usando OSRM
     * Si existe en caché y está vigente, la retorna; sino calcula nueva
     */
    @Transactional
    public OsrmDistanciaResponse calcularDistancia(BigDecimal latOrigen, BigDecimal lonOrigen,
                                                    BigDecimal latDestino, BigDecimal lonDestino) {
        return calcularDistancia(latOrigen, lonOrigen, latDestino, lonDestino, false);
    }

    /**
     * Calcula la distancia entre dos puntos usando OSRM
     *
     * @param latOrigen Latitud de origen
     * @param lonOrigen Longitud de origen
     * @param latDestino Latitud de destino
     * @param lonDestino Longitud de destino
     * @param forzarRecalculo Si se debe forzar el recálculo ignorando caché
     * @return Respuesta con distancia y duración
     */
    @Transactional
    public OsrmDistanciaResponse calcularDistancia(BigDecimal latOrigen, BigDecimal lonOrigen,
                                                    BigDecimal latDestino, BigDecimal lonDestino,
                                                    boolean forzarRecalculo) {
        log.info("Calculando distancia desde ({}, {}) hasta ({}, {}). Forzar recálculo: {}",
                latOrigen, lonOrigen, latDestino, lonDestino, forzarRecalculo);

        // Buscar en caché si no se fuerza recálculo
        if (!forzarRecalculo) {
            DistanciaCalculada enCache = buscarEnCache(latOrigen, lonOrigen, latDestino, lonDestino);
            if (enCache != null && enCache.estaVigente()) {
                log.info("Distancia encontrada en caché válida, ID: {}", enCache.getId());
                enCache.incrementarUso();
                distanciaRepository.save(enCache);
                return convertirAResponse(enCache);
            }
        }

        // Calcular usando OSRM
        OsrmDistanciaRequest request = OsrmDistanciaRequest.builder()
                .latitudOrigen(latOrigen)
                .longitudOrigen(lonOrigen)
                .latitudDestino(latDestino)
                .longitudDestino(lonDestino)
                .incluirGeometria(false)
                .incluirPasos(false)
                .build();

        OsrmDistanciaResponse response = osrmClient.calcularDistancia(request);

        if (response.getExitoso()) {
            // Guardar en caché
            guardarEnCache(latOrigen, lonOrigen, latDestino, lonDestino, response);
        }

        return response;
    }

    /**
     * Calcula distancia con detalles de ruta completa
     */
    @Transactional
    public OsrmDistanciaResponse calcularDistanciaConRuta(BigDecimal latOrigen, BigDecimal lonOrigen,
                                                           BigDecimal latDestino, BigDecimal lonDestino) {
        log.info("Calculando distancia CON RUTA desde ({}, {}) hasta ({}, {})",
                latOrigen, lonOrigen, latDestino, lonDestino);

        OsrmDistanciaRequest request = OsrmDistanciaRequest.builder()
                .latitudOrigen(latOrigen)
                .longitudOrigen(lonOrigen)
                .latitudDestino(latDestino)
                .longitudDestino(lonDestino)
                .incluirGeometria(true)
                .incluirPasos(true)
                .incluirOverview(true)
                .build();

        OsrmDistanciaResponse response = osrmClient.calcularDistancia(request);

        if (response.getExitoso()) {
            guardarEnCache(latOrigen, lonOrigen, latDestino, lonDestino, response);
        }

        return response;
    }

    /**
     * Busca una distancia calculada en caché
     */
    private DistanciaCalculada buscarEnCache(BigDecimal latOrigen, BigDecimal lonOrigen,
                                             BigDecimal latDestino, BigDecimal lonDestino) {
        return distanciaRepository.findByCoordenadasExactas(
                latOrigen, lonOrigen, latDestino, lonDestino
        ).orElse(null);
    }

    /**
     * Guarda el resultado de OSRM en caché
     */
    private void guardarEnCache(BigDecimal latOrigen, BigDecimal lonOrigen,
                                BigDecimal latDestino, BigDecimal lonDestino,
                                OsrmDistanciaResponse response) {
        try {
            // Buscar si ya existe para actualizar en lugar de duplicar
            DistanciaCalculada distancia = buscarEnCache(latOrigen, lonOrigen, latDestino, lonDestino);

            if (distancia == null) {
                distancia = new DistanciaCalculada();
                distancia.setLatitudOrigen(latOrigen);
                distancia.setLongitudOrigen(lonOrigen);
                distancia.setLatitudDestino(latDestino);
                distancia.setLongitudDestino(lonDestino);
                distancia.setNumeroUsos(0);
            }

            distancia.setDistanciaKm(response.getDistanciaKm());
            distancia.setDuracionMinutos(response.getDuracionMinutos());
            distancia.setFuenteCalculo(DistanciaCalculada.FuenteCalculo.API_EXTERNA);
            distancia.setProveedorApi("OSRM");
            distancia.setTipoTransporte(DistanciaCalculada.TipoTransporte.CAMION);
            distancia.setMomentoCalculo(LocalDateTime.now());
            distancia.setEstadoValidacion(EstadoValidacion.VALIDADA);
            distancia.setRutaJson(response.getRutaJson());
            distancia.setValidezHoras(24); // 24 horas de validez
            distancia.establecerVigencia();
            distancia.calcularDistanciaLineal();

            distanciaRepository.save(distancia);
            log.info("Distancia guardada en caché: {} km, {} min",
                    response.getDistanciaKm(), response.getDuracionMinutos());
        } catch (Exception e) {
            log.error("Error al guardar distancia en caché: {}", e.getMessage(), e);
        }
    }

    /**
     * Convierte una entidad DistanciaCalculada a OsrmDistanciaResponse
     */
    private OsrmDistanciaResponse convertirAResponse(DistanciaCalculada distancia) {
        return OsrmDistanciaResponse.builder()
                .distanciaKm(distancia.getDistanciaKm())
                .duracionMinutos(distancia.getDuracionMinutos())
                .distanciaMetros(distancia.getDistanciaKm().multiply(BigDecimal.valueOf(1000)).doubleValue())
                .duracionSegundos(distancia.getDuracionMinutos().multiply(BigDecimal.valueOf(60)).doubleValue())
                .codigo("Ok")
                .exitoso(true)
                .rutaJson(distancia.getRutaJson())
                .build();
    }

    /**
     * Verifica si OSRM está disponible
     */
    public boolean verificarDisponibilidad() {
        return osrmClient.verificarDisponibilidad();
    }

    /**
     * Limpia distancias expiradas del caché
     */
    @Transactional
    public int limpiarCacheExpirado() {
        log.info("Limpiando caché de distancias expiradas");
        int eliminadas = distanciaRepository.eliminarExpiradas(LocalDateTime.now());
        log.info("Distancias expiradas eliminadas: {}", eliminadas);
        return eliminadas;
    }
}

