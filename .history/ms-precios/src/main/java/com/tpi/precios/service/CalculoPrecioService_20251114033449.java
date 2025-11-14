package com.tpi.precios.service;

import com.tpi.precios.entity.CalculoPrecio;
import com.tpi.precios.entity.Tarifa;
import com.tpi.precios.dto.CalculoPrecioDto;
import com.tpi.precios.dto.SolicitudCotizacionDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CalculoPrecioService {

    private final TarifaService tarifaService;

    // Factores de ajuste
    private static final BigDecimal FACTOR_URGENCIA = new BigDecimal("1.5");
    private static final BigDecimal FACTOR_PREMIUM = new BigDecimal("1.2");
    private static final BigDecimal FACTOR_ECONOMICO = new BigDecimal("0.8");

    /**
     * REGLA DE NEGOCIO 2 y 4: Calcular tarifa aproximada del envío
     * - La tarifa se calcula en base a valores promedio de camiones elegibles
     * - Usa tarifa vigente con costo por km según peso y volumen
     * - Incluye precio base del tramo (cargo de gestión)
     * - Integra con OSRM para obtener distancia real
     */
    public CalculoPrecioDto calcularPrecio(SolicitudCotizacionDto solicitud) {
        log.info("Calculando precio para solicitud de cotización");

        Optional<Tarifa> tarifaOpt = tarifaService.obtenerTarifaVigenteMasReciente();
        if (tarifaOpt.isEmpty()) {
            throw new RuntimeException("No hay tarifas vigentes disponibles");
        }

        Tarifa tarifa = tarifaOpt.get();

        BigDecimal distanciaEstimada = obtenerDistanciaReal(
            solicitud.getUbicacionOrigenId(),
            solicitud.getUbicacionDestinoId()
        );

        return realizarCalculo(solicitud, tarifa, distanciaEstimada);
    }

    public CalculoPrecioDto calcularPrecioConTarifa(CalculoPrecioDto calculoDto) {
        log.info("Calculando precio con tarifa específica: {}", calculoDto.getTarifaId());

        Optional<Tarifa> tarifaOpt = tarifaService.obtenerPorId(calculoDto.getTarifaId());
        if (tarifaOpt.isEmpty()) {
            throw new RuntimeException("Tarifa no encontrada con ID: " + calculoDto.getTarifaId());
        }

        Tarifa tarifa = tarifaOpt.get();

        CalculoPrecio calculo = CalculoPrecio.builder()
                .tarifaId(tarifa.getTarifaId())
                .distanciaKm(calculoDto.getDistanciaKm())
                .pesoKg(calculoDto.getPesoKg())
                .volumenM3(calculoDto.getVolumenM3())
                .tipoCalculo(CalculoPrecio.TipoCalculo.valueOf(calculoDto.getTipoCalculo()))
                .estadoCalculo(CalculoPrecio.EstadoCalculo.CALCULADO)
                .fechaCalculo(LocalDateTime.now())
                .observaciones(calculoDto.getObservaciones())
                .build();

        return realizarCalculoCompleto(calculo, tarifa, calculoDto);
    }

    /**
     * REGLA DE NEGOCIO 2: Calcular tarifa final del envío
     * Incluye:
     * - Cargo de gestión (valor fijo por cantidad de tramos = precioTramo)
     * - Costo por kilómetro (según peso y volumen del contenedor)
     * - Costo de combustible estimado (consumo promedio × distancia × precio litro)
     * 
     * REGLA DE NEGOCIO 3: Costos diferenciados por camión
     * - Usa valores promedio de la tarifa para cotización
     * - El cálculo final real usará costoBaseKm específico del camión asignado
     */
    private CalculoPrecioDto realizarCalculo(SolicitudCotizacionDto solicitud, Tarifa tarifa, BigDecimal distancia) {
        BigDecimal precioPorPeso = distancia
                .multiply(solicitud.getPesoKg())
                .multiply(tarifa.getPrecioKmKg());

        BigDecimal precioPorVolumen = distancia
                .multiply(solicitud.getVolumenM3())
                .multiply(tarifa.getPrecioKmM3());

        BigDecimal precioBase = precioPorPeso.max(precioPorVolumen);

        BigDecimal costoCombustible = calcularCostoCombustible(distancia, tarifa);

        BigDecimal precioTotal = precioBase
                .add(tarifa.getPrecioTramo())
                .add(costoCombustible);

        BigDecimal precioFinal = aplicarFactores(precioTotal, solicitud);

        return CalculoPrecioDto.builder()
                .tarifaId(tarifa.getTarifaId())
                .distanciaKm(distancia)
                .pesoKg(solicitud.getPesoKg())
                .volumenM3(solicitud.getVolumenM3())
                .precioBase(precioBase.setScale(2, RoundingMode.HALF_UP))
                .precioTotal(precioTotal.setScale(2, RoundingMode.HALF_UP))
                .precioFinal(precioFinal.setScale(2, RoundingMode.HALF_UP))
                .fechaCalculo(LocalDateTime.now())
                .tipoCalculo("COTIZACION")
                .estadoCalculo("CALCULADO")
                .ubicacionOrigenId(solicitud.getUbicacionOrigenId())
                .ubicacionDestinoId(solicitud.getUbicacionDestinoId())
                .tipoServicio(solicitud.getTipoServicio())
                .esUrgente(solicitud.getEsUrgente())
                .observaciones(solicitud.getObservaciones())
                .build();
    }

    private CalculoPrecioDto realizarCalculoCompleto(CalculoPrecio calculo, Tarifa tarifa, CalculoPrecioDto dto) {
        // Calcular precio base por peso y volumen
        BigDecimal precioPorPeso = calculo.calcularPorPeso(tarifa.getPrecioKmKg());
        BigDecimal precioPorVolumen = calculo.calcularPorVolumen(tarifa.getPrecioKmM3());

        // Tomar el mayor entre peso y volumen
        BigDecimal precioBase = precioPorPeso.max(precioPorVolumen);

        // Agregar precio base del tramo
        BigDecimal precioTotal = precioBase.add(tarifa.getPrecioTramo());

        // Aplicar factores adicionales si corresponde
        BigDecimal precioFinal = precioTotal;
        if (dto.getEsUrgente() != null && dto.getEsUrgente()) {
            precioFinal = precioFinal.multiply(FACTOR_URGENCIA);
        }

        calculo.setPrecioBase(precioBase);
        calculo.setPrecioTotal(precioTotal);
        calculo.setPrecioFinal(precioFinal);

        return CalculoPrecioDto.builder()
                .calculoId(calculo.getCalculoId())
                .tarifaId(calculo.getTarifaId())
                .distanciaKm(calculo.getDistanciaKm())
                .pesoKg(calculo.getPesoKg())
                .volumenM3(calculo.getVolumenM3())
                .precioBase(precioBase.setScale(2, RoundingMode.HALF_UP))
                .precioTotal(precioTotal.setScale(2, RoundingMode.HALF_UP))
                .precioFinal(precioFinal.setScale(2, RoundingMode.HALF_UP))
                .fechaCalculo(calculo.getFechaCalculo())
                .tipoCalculo(calculo.getTipoCalculo().name())
                .estadoCalculo(calculo.getEstadoCalculo().name())
                .observaciones(calculo.getObservaciones())
                .esUrgente(dto.getEsUrgente())
                .factorUrgencia(dto.getEsUrgente() != null && dto.getEsUrgente() ? FACTOR_URGENCIA : BigDecimal.ONE)
                .build();
    }

    private BigDecimal aplicarFactores(BigDecimal precioBase, SolicitudCotizacionDto solicitud) {
        BigDecimal precioFinal = precioBase;

        // Factor de urgencia
        if (solicitud.getEsUrgente() != null && solicitud.getEsUrgente()) {
            log.info("Aplicando factor de urgencia");
            precioFinal = precioFinal.multiply(FACTOR_URGENCIA);
        }

        // Factor por tipo de servicio
        if (solicitud.getTipoServicio() != null) {
            switch (solicitud.getTipoServicio().toUpperCase()) {
                case "PREMIUM":
                    log.info("Aplicando factor premium");
                    precioFinal = precioFinal.multiply(FACTOR_PREMIUM);
                    break;
                case "ECONOMICO":
                    log.info("Aplicando factor económico");
                    precioFinal = precioFinal.multiply(FACTOR_ECONOMICO);
                    break;
                default:
                    // Servicio estándar, no se aplica factor adicional
                    break;
            }
        }

        return precioFinal;
    }

    /**
     * Obtiene la distancia estimada entre dos ubicaciones
     * Nota: En una implementación completa, este servicio llamaría a ms-localizaciones
     * para obtener la distancia real vía OSRM. Por ahora usa cálculo estimado.
     */
    private BigDecimal obtenerDistanciaReal(Integer origenId, Integer destinoId) {
        log.info("Calculando distancia estimada entre ubicación {} y {}", origenId, destinoId);
        return calcularDistanciaEstimada(origenId, destinoId);
    }

    /**
     * REGLA DE NEGOCIO 2: Cálculo de costo de combustible
     * Fórmula: (distancia × consumo promedio estimado) × precio por litro
     * Consumo promedio estimado: 0.35 litros/km (típico para camiones de carga)
     */
    private BigDecimal calcularCostoCombustible(BigDecimal distanciaKm, Tarifa tarifa) {
        BigDecimal consumoPromedioLitrosPorKm = new BigDecimal("0.35");
        BigDecimal litrosNecesarios = distanciaKm.multiply(consumoPromedioLitrosPorKm);
        BigDecimal costo = litrosNecesarios.multiply(tarifa.getPrecioCombustibleLitro());
        
        log.info("Costo combustible: {} km × {} l/km × ${}/l = ${}", 
                distanciaKm, consumoPromedioLitrosPorKm, tarifa.getPrecioCombustibleLitro(), costo);
        
        return costo;
    }

    /**
     * Cálculo estimado de distancia basado en IDs de ubicaciones
     * Nota: En producción, este cálculo debería ser más sofisticado o
     * integrarse con ms-localizaciones para obtener distancias reales vía OSRM
     */
    private BigDecimal calcularDistanciaEstimada(Integer origenId, Integer destinoId) {
        if (origenId.equals(destinoId)) {
            return BigDecimal.ZERO;
        }

        // Cálculo simplificado: diferencia de IDs × 150 km
        int diferencia = Math.abs(origenId - destinoId);
        BigDecimal distanciaEstimada = new BigDecimal(diferencia * 150);

        log.info("Distancia estimada calculada: {} km (origen: {}, destino: {})", 
                distanciaEstimada, origenId, destinoId);
        return distanciaEstimada;
    }
}

