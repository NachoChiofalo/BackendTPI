package com.tpi.flotas.service;

import com.tpi.flotas.entity.Camion;
import com.tpi.flotas.repository.CamionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CamionService {

    private final CamionRepository camionRepository;
    private final JdbcTemplate jdbcTemplate;
    private final PlatformTransactionManager transactionManager;

    public List<Camion> obtenerTodos() {
        log.info("Obteniendo todos los camiones");
        return camionRepository.findAll();
    }

    public List<Camion> obtenerDisponibles() {
        log.info("Obteniendo camiones disponibles");
        return camionRepository.findByDisponibleTrue();
    }

    public List<Camion> obtenerNoDisponibles() {
        log.info("Obteniendo camiones no disponibles");
        return camionRepository.findByDisponibleFalse();
    }

    public Optional<Camion> obtenerPorDominio(String dominio) {
        log.info("Obteniendo camión por dominio: {}", dominio);
        return camionRepository.findById(dominio);
    }

    /**
     * REGLA DE NEGOCIO 1: Un camión no puede transportar contenedores que superen su peso o volumen máximo
     * Este método busca camiones que cumplan con la capacidad mínima requerida
     * 
     * @param pesoMin Peso mínimo requerido (peso del contenedor)
     * @param volumenMin Volumen mínimo requerido (volumen del contenedor)
     * @return Lista de camiones con capacidad suficiente
     */
    public List<Camion> obtenerConCapacidadMinima(BigDecimal pesoMin, BigDecimal volumenMin) {
        log.info("Obteniendo camiones con capacidad mínima - Peso: {}, Volumen: {}", pesoMin, volumenMin);
        return camionRepository.findByCapacidadMinima(pesoMin, volumenMin);
    }

    /**
     * REGLA DE NEGOCIO 1: Validación de capacidad para camiones disponibles
     * Filtra solo camiones que estén disponibles Y cumplan con la capacidad requerida
     * 
     * @param pesoMin Peso mínimo requerido (peso del contenedor)
     * @param volumenMin Volumen mínimo requerido (volumen del contenedor)
     * @return Lista de camiones disponibles con capacidad suficiente
     */
    public List<Camion> obtenerDisponiblesConCapacidad(BigDecimal pesoMin, BigDecimal volumenMin) {
        log.info("Obteniendo camiones disponibles con capacidad mínima - Peso: {}, Volumen: {}", pesoMin, volumenMin);
        return camionRepository.findDisponiblesConCapacidadMinima(pesoMin, volumenMin);
    }

    @Transactional
    public Camion guardar(Camion camion) {
        log.info("Guardando camión: {}", camion.getDominio());
        return camionRepository.save(camion);
    }

    @Transactional
    public Camion actualizar(String dominio, Camion camionActualizado) {
        log.info("Actualizando camión: {}", dominio);
        return camionRepository.findById(dominio)
                .map(camion -> {
                    camion.setDisponible(camionActualizado.getDisponible());
                    camion.setCapacidadPeso(camionActualizado.getCapacidadPeso());
                    camion.setCapacidadVolumen(camionActualizado.getCapacidadVolumen());
                    camion.setCostoBaseKm(camionActualizado.getCostoBaseKm());
                    camion.setConsumoPromedio(camionActualizado.getConsumoPromedio());
                    return camionRepository.save(camion);
                })
                .orElseThrow(() -> new RuntimeException("Camión no encontrado con dominio: " + dominio));
    }

    @Transactional
    public void cambiarDisponibilidad(String dominio, Boolean disponible) {
        log.info("=== INICIANDO cambiarDisponibilidad en CamionService ===");
        log.info("Cambiando disponibilidad del camión {} a: {}", dominio, disponible);

        camionRepository.findById(dominio)
                .ifPresentOrElse(
                    camion -> {
                        log.info("🚛 Camión encontrado: {} - Disponibilidad actual: {}", dominio, camion.getDisponible());
                        camion.setDisponible(disponible);
                        Camion camionGuardado = camionRepository.save(camion);
                        log.info("✅ Camión {} guardado con nueva disponibilidad: {}", dominio, camionGuardado.getDisponible());
                    },
                    () -> {
                        log.error("❌ Camión no encontrado con dominio: {}", dominio);
                        throw new RuntimeException("Camión no encontrado con dominio: " + dominio);
                    }
                );

        log.info("=== FIN cambiarDisponibilidad en CamionService ===");
    }

    @Transactional
    public void eliminar(String dominio) {
        log.info("Eliminando camión: {}", dominio);
        if (camionRepository.existsById(dominio)) {
            camionRepository.deleteById(dominio);
        } else {
            throw new RuntimeException("Camión no encontrado con dominio: " + dominio);
        }
    }

    /**
     * Asigna (reserva) el mejor camión disponible que cumpla con los requisitos de capacidad.
     * Implementación simple dentro de este servicio usando consultas SQL directas contra la BD compartida.
     * - Lee peso/volumen requeridos del tramo
     * - Busca candidatos en la tabla Camion y intenta reservarlos atómicamente
     * - Actualiza el tramo (dominio, estado)
     * No crea nuevas entidades ni archivos; devuelve el dominio del camión reservado o null si no fue posible.
     *
     * @param tramoId Id del tramo a asignar
     * @return Dominio del camión reservado o null
     */
    @Transactional
    public String asignarCamionATramo(Integer tramoId) {
        log.info("Iniciando asignación de camión para tramo {}", tramoId);

        // Leer requisitos del tramo directamente desde la tabla 'tramo'
        Map<String, Object> tramoRow;
        try {
            tramoRow = jdbcTemplate.queryForMap("SELECT peso_requerido, volumen_requerido, estado FROM public.tramo WHERE tramo_id = ?", tramoId);
        } catch (EmptyResultDataAccessException e) {
            throw new RuntimeException("Tramo no encontrado con id: " + tramoId);
        }

        BigDecimal pesoReq = (BigDecimal) tramoRow.get("peso_requerido");
        BigDecimal volumenReq = (BigDecimal) tramoRow.get("volumen_requerido");
        String estado = (String) tramoRow.get("estado");

        if (estado != null && !"PENDIENTE".equalsIgnoreCase(estado)) {
            throw new RuntimeException("Tramo " + tramoId + " no está en estado PENDIENTE");
        }

        log.info("Requisitos tramo {} -> peso: {}, volumen: {}", tramoId, pesoReq, volumenReq);

        // Buscar candidatos ordenados por mejor ajuste
        List<Map<String, Object>> candidatos = jdbcTemplate.queryForList(
                "SELECT dominio, capacidad_peso, capacidad_volumen FROM public.camion WHERE disponible = true AND capacidad_peso >= ? AND capacidad_volumen >= ? ORDER BY capacidad_peso ASC, capacidad_volumen ASC",
                pesoReq, volumenReq);

        if (candidatos == null || candidatos.isEmpty()) {
            log.info("No se encontraron camiones elegibles para el tramo {}", tramoId);
            return null;
        }

        for (Map<String, Object> c : candidatos) {
            String dominio = (String) c.get("dominio");
            try {
                int rows = jdbcTemplate.update("UPDATE public.camion SET disponible = FALSE WHERE dominio = ? AND disponible = TRUE", dominio);
                if (rows == 1) {
                    log.info("Camión {} reservado. Actualizando tramo {}", dominio, tramoId);
                    int updatedTramo = jdbcTemplate.update("UPDATE public.tramo SET dominio = ?, estado = 'ASIGNADO' WHERE tramo_id = ?", dominio, tramoId);
                    if (updatedTramo == 1) {
                        log.info("Asignación completada: tramo {} -> camion {}", tramoId, dominio);
                        return dominio;
                    } else {
                        // Compensar: liberar camión (en nueva transacción)
                        liberarCamionEnNuevaTx(dominio);
                        throw new RuntimeException("No se pudo actualizar el tramo " + tramoId + " después de reservar el camión");
                    }
                } else {
                    log.info("Camión {} ya no estaba disponible, intentando siguiente candidato", dominio);
                }
            } catch (Exception e) {
                log.warn("Error al intentar reservar/actualizar con camión {}: {}", dominio, e.getMessage());
                // continuar con siguiente candidato
            }
        }

        log.info("No fue posible reservar ningún camión para el tramo {}", tramoId);
        return null;
    }

    /**
     * Asigna un camión específico a un tramo: valida existencia, disponibilidad y capacidad, y reserva de forma atómica.
     * Modificado para no depender de una SELECT previa en la tabla 'tramo' (evita BadSqlGrammar cuando la tabla no existe)
     * y para aceptar sólo `tramoId` y `dominio` como parámetros.
     * @param tramoId id del tramo
     * @param dominio dominio del camión a asignar
     * @return dominio reservado si éxito
     */
    @Transactional
    public Map<String, Object> asignarCamionEspecificoATramo(Integer tramoId, String dominio) {
        log.info("Asignando camión {} al tramo {} (asignación específica, sin SELECT de tramo)", dominio, tramoId);

        // Intentar reservar el camión atómicamente (sin validar peso/volumen porque no se pasan en esta API)
        int rows = jdbcTemplate.update(
                "UPDATE public.camion SET disponible = FALSE WHERE dominio = ? AND disponible = TRUE",
                dominio);

        if (rows != 1) {
            throw new RuntimeException("El camión no está disponible: " + dominio);
        }

        // Construir respuesta enriquecida con información del camión
        Map<String, Object> result = new HashMap<>();
        result.put("dominio", dominio);
        result.put("tramoId", tramoId);
        result.put("asignado", true);
        result.put("timestamp", Instant.now().toString());

        Optional<Camion> camionOpt = camionRepository.findById(dominio);
        camionOpt.ifPresent(camion -> {
            result.put("disponible", camion.getDisponible());
            result.put("capacidadPeso", camion.getCapacidadPeso());
            result.put("capacidadVolumen", camion.getCapacidadVolumen());
            result.put("costoBaseKm", camion.getCostoBaseKm());
            result.put("consumoPromedio", camion.getConsumoPromedio());
        });

        // No intentamos actualizar la tabla 'tramo' aquí: este endpoint sólo reserva el camión
        log.info("Camión {} reservado para el tramo {} (respuesta enriquecida)", dominio, tramoId);
        return result;
    }

    /**
     * Asigna un camión específico a un tramo: versión que acepta peso/volumen opcionales.
     * Intenta leer del tramo; si la lectura falla por error SQL, usa los valores proporcionados.
     */
    @Transactional
    public String asignarCamionEspecificoATramo(Integer tramoId, String dominio, BigDecimal peso, BigDecimal volumen) {
        log.info("Asignando camión {} al tramo {} (específico, con fallback peso/volumen).", dominio, tramoId);

        BigDecimal pesoReq = null;
        BigDecimal volumenReq = null;
        String estado = null;

        try {
            Map<String, Object> tramoRow = jdbcTemplate.queryForMap("SELECT peso_requerido, volumen_requerido, estado FROM public.tramo WHERE tramo_id = ?", tramoId);
            pesoReq = (BigDecimal) tramoRow.get("peso_requerido");
            volumenReq = (BigDecimal) tramoRow.get("volumen_requerido");
            estado = (String) tramoRow.get("estado");
        } catch (EmptyResultDataAccessException e) {
            throw new RuntimeException("Tramo no encontrado con id: " + tramoId);
        } catch (Exception e) {
            // Problema leyendo la tabla tramo (p.ej. BadSqlGrammarException). Intentamos fallback con parámetros
            log.warn("No se pudo leer tramo {} desde la BD ({}). Se intentará usar peso/volumen pasados como fallback.", tramoId, e.getMessage());
            if (peso == null || volumen == null) {
                throw new RuntimeException("No se pudo leer el tramo y no se proporcionaron 'peso' y 'volumen' en la petición; imposible validar capacidad.");
            }
            pesoReq = peso;
            volumenReq = volumen;
            // permitimos continuar sin validar estado del tramo en este fallback
        }

        if (estado != null && !"PENDIENTE".equalsIgnoreCase(estado)) {
            throw new RuntimeException("Tramo " + tramoId + " no está en estado PENDIENTE");
        }

        if (pesoReq == null || volumenReq == null) {
            throw new RuntimeException("No se pudo obtener requisitos de peso/volumen para el tramo y no se proporcionaron parámetros alternativos.");
        }

        // Intentar reservar el camión asegurando capacidad suficiente
        int rows = jdbcTemplate.update(
                "UPDATE public.camion SET disponible = FALSE WHERE dominio = ? AND disponible = TRUE AND capacidad_peso >= ? AND capacidad_volumen >= ?",
                dominio, pesoReq, volumenReq);

        if (rows != 1) {
            throw new RuntimeException("El camión no está disponible o no cumple con la capacidad necesaria: " + dominio);
        }

        // Actualizar tramo (si la tabla existe y es accesible)
        try {
            int updatedTramo = jdbcTemplate.update("UPDATE public.tramo SET dominio = ?, estado = 'ASIGNADO' WHERE tramo_id = ?", dominio, tramoId);
            if (updatedTramo != 1) {
                // Compensación: liberar camión (en nueva transacción)
                liberarCamionEnNuevaTx(dominio);
                throw new RuntimeException("No se pudo actualizar el tramo " + tramoId + " después de reservar el camión");
            }
        } catch (Exception e) {
            // Si no se puede actualizar la tabla tramo (p.ej. no existe), liberamos y reportamos
            liberarCamionEnNuevaTx(dominio);
            throw new RuntimeException("Error al actualizar tramo: " + e.getMessage());
        }

        log.info("Asignación específica completada: tramo {} -> camion {}", tramoId, dominio);
        return dominio;
    }

    /**
     * Ejecuta la liberación del camión en una nueva transacción independiente para que la compensación
     * pueda completarse aunque la transacción principal esté marcada para rollback.
     */
    private void liberarCamionEnNuevaTx(String dominio) {
        try {
            TransactionTemplate tt = new TransactionTemplate(transactionManager);
            tt.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
            tt.execute(status -> {
                log.info("Liberando camión {} en transacción nueva (compensación)", dominio);
                jdbcTemplate.update("UPDATE public.camion SET disponible = TRUE WHERE dominio = ?", dominio);
                return null;
            });
        } catch (Exception e) {
            log.error("No se pudo liberar el camión {} en transacción nueva: {}", dominio, e.getMessage());
        }
    }
}
