package com.tpi.rutas.service;

import com.tpi.rutas.entity.Tramo;
import com.tpi.rutas.repository.TramoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class CalculoRutaService {

    private final TramoRepository tramoRepository;
    private final Random random = new Random();

    /**
     * REGLA DE NEGOCIO 2: Calcular costo por estadía en depósitos
     * Calcula los días de estadía en cada depósito sumando la diferencia
     * entre fechaHoraFin de un tramo y fechaHoraInicio del siguiente tramo
     * 
     * @param rutaId ID de la ruta
     * @param costoPorDia Costo diario de estadía en depósito
     * @return Costo total de estadía
     */
    public BigDecimal calcularCostoEstadia(Integer rutaId, BigDecimal costoPorDia) {
        log.info("Calculando costo de estadía para ruta {}", rutaId);
        
        List<Tramo> tramos = tramoRepository.findByRutaId(rutaId);
        if (tramos.size() < 2) {
            return BigDecimal.ZERO;
        }

        BigDecimal costoTotal = BigDecimal.ZERO;
        
        for (int i = 0; i < tramos.size() - 1; i++) {
            Tramo tramoActual = tramos.get(i);
            Tramo tramoSiguiente = tramos.get(i + 1);
            
            if (tramoActual.getFechaHoraFin() != null && tramoSiguiente.getFechaHoraInicio() != null) {
                long diasEstadia = ChronoUnit.DAYS.between(
                    tramoActual.getFechaHoraFin(),
                    tramoSiguiente.getFechaHoraInicio()
                );
                
                if (diasEstadia > 0) {
                    BigDecimal costoEstadiaTramo = costoPorDia.multiply(BigDecimal.valueOf(diasEstadia));
                    costoTotal = costoTotal.add(costoEstadiaTramo);
                    log.info("Tramo {}: {} días de estadía = ${}", 
                            tramoActual.getTramoId(), diasEstadia, costoEstadiaTramo);
                }
            }
        }
        
        log.info("Costo total de estadía: ${}", costoTotal);
        return costoTotal;
    }

    /**
     * REGLA DE NEGOCIO 5: Tiempo estimado se calcula en base a las distancias
     * Calcula el tiempo estimado total sumando:
     * - Tiempo de viaje basado en distancia (velocidad promedio 60 km/h)
     * - Tiempo de carga/descarga en depósitos (4 horas por depósito)
     * 
     * @param distanciaTotal Distancia total en kilómetros
     * @param cantidadDepositos Cantidad de depósitos intermedios
     * @return Tiempo estimado en horas
     */
    public BigDecimal calcularTiempoEstimado(BigDecimal distanciaTotal, int cantidadDepositos) {
        log.info("Calculando tiempo estimado para {} km con {} depósitos", 
                distanciaTotal, cantidadDepositos);
        
        BigDecimal velocidadPromedio = new BigDecimal("60");
        BigDecimal tiempoViaje = distanciaTotal.divide(velocidadPromedio, 2, RoundingMode.HALF_UP);
        
        BigDecimal horasPorDeposito = new BigDecimal("4");
        BigDecimal tiempoDepositos = horasPorDeposito.multiply(BigDecimal.valueOf(cantidadDepositos));
        
        BigDecimal tiempoTotal = tiempoViaje.add(tiempoDepositos);
        
        log.info("Tiempo estimado: {} horas de viaje + {} horas en depósitos = {} horas total",
                tiempoViaje, tiempoDepositos, tiempoTotal);
        
        return tiempoTotal;
    }

    /**
     * REGLA DE NEGOCIO 7: Calcular tiempo real transcurrido
     * Suma la diferencia entre fechaHoraFin e fechaHoraInicio de todos los tramos
     * 
     * @param rutaId ID de la ruta
     * @return Días reales transcurridos
     */
    public long calcularTiempoReal(Integer rutaId) {
        log.info("Calculando tiempo real para ruta {}", rutaId);
        
        List<Tramo> tramos = tramoRepository.findByRutaId(rutaId);
        
        LocalDateTime fechaInicio = tramos.stream()
                .map(Tramo::getFechaHoraInicio)
                .filter(fecha -> fecha != null)
                .min(LocalDateTime::compareTo)
                .orElse(null);
        
        LocalDateTime fechaFin = tramos.stream()
                .map(Tramo::getFechaHoraFin)
                .filter(fecha -> fecha != null)
                .max(LocalDateTime::compareTo)
                .orElse(null);
        
        if (fechaInicio != null && fechaFin != null) {
            long dias = ChronoUnit.DAYS.between(fechaInicio, fechaFin);
            log.info("Tiempo real: {} días", dias);
            return dias;
        }
        
        return 0;
    }

    /**
     * REGLA DE NEGOCIO 2: Calcular costo real completo de un traslado
     * Incluye: costos de tramos + combustible + estadía
     * 
     * @param rutaId ID de la ruta
     * @param costoPorDiaDeposito Costo diario de estadía
     * @return Costo real total
     */
    public BigDecimal calcularCostoRealCompleto(Integer rutaId, BigDecimal costoPorDiaDeposito) {
        log.info("Calculando costo real completo para ruta {}", rutaId);
        
        List<Tramo> tramos = tramoRepository.findByRutaId(rutaId);
        
        BigDecimal costoTramos = tramos.stream()
                .map(Tramo::getCostoReal)
                .filter(costo -> costo != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal costoEstadia = calcularCostoEstadia(rutaId, costoPorDiaDeposito);
        
        BigDecimal costoTotal = costoTramos.add(costoEstadia);
        
        log.info("Costo real total: ${} (tramos) + ${} (estadía) = ${}",
                costoTramos, costoEstadia, costoTotal);
        
        return costoTotal;
    }

    /**
     * Calcula una distancia aleatoria para un tramo (entre 50 y 500 km)
     *
     * @return Distancia en kilómetros
     */
    public BigDecimal calcularDistanciaAleatoria() {
        double distancia = 50 + (random.nextDouble() * 450); // Entre 50 y 500 km
        return BigDecimal.valueOf(distancia).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calcula un costo aproximado basado en la distancia
     * Usa una tarifa base de $50 por km más un componente aleatorio
     *
     * @param distanciaKm Distancia en kilómetros
     * @return Costo aproximado
     */
    public BigDecimal calcularCostoAproximado(BigDecimal distanciaKm) {
        BigDecimal tarifaBasePorKm = new BigDecimal("50");
        BigDecimal costoBase = distanciaKm.multiply(tarifaBasePorKm);

        // Agregar variación aleatoria del ±20%
        double variacion = 0.8 + (random.nextDouble() * 0.4); // Entre 0.8 y 1.2
        BigDecimal costoFinal = costoBase.multiply(BigDecimal.valueOf(variacion));

        return costoFinal.setScale(2, RoundingMode.HALF_UP);
    }
}
