package com.tpi.rutas.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad Tramo - Representa cada segmento individual de una ruta
 * Esquema: rutas
 */
@Entity
@Table(name = "tramos", schema = "rutas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tramo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer orden; // Orden del tramo en la ruta

    // Relación con la ruta padre
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ruta_id", nullable = false)
    private Ruta ruta;

    // Ubicaciones de origen
    @Column(name = "origen_latitud", nullable = false, precision = 10, scale = 7)
    private BigDecimal origenLatitud;

    @Column(name = "origen_longitud", nullable = false, precision = 10, scale = 7)
    private BigDecimal origenLongitud;

    @Column(name = "origen_descripcion", nullable = false, length = 500)
    private String origenDescripcion;

    // ID del depósito de origen (si aplica)
    @Column(name = "origen_deposito_id")
    private Long origenDepositoId;

    // Ubicaciones de destino
    @Column(name = "destino_latitud", nullable = false, precision = 10, scale = 7)
    private BigDecimal destinoLatitud;

    @Column(name = "destino_longitud", nullable = false, precision = 10, scale = 7)
    private BigDecimal destinoLongitud;

    @Column(name = "destino_descripcion", nullable = false, length = 500)
    private String destinoDescripcion;

    // ID del depósito de destino (si aplica)
    @Column(name = "destino_deposito_id")
    private Long destinoDepositoId;

    // Tipo y características del tramo
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_tramo", nullable = false, length = 20)
    private TipoTramo tipoTramo;

    @Column(name = "distancia_km", precision = 10, scale = 2)
    private BigDecimal distanciaKm;

    // Tiempos
    @Column(name = "tiempo_estimado_minutos")
    private Integer tiempoEstimadoMinutos;

    @Column(name = "tiempo_real_minutos")
    private Integer tiempoRealMinutos;

    // Fechas de ejecución
    @Column(name = "fecha_hora_inicio_programada")
    private LocalDateTime fechaHoraInicioProgramada;

    @Column(name = "fecha_hora_fin_programada")
    private LocalDateTime fechaHoraFinProgramada;

    @Column(name = "fecha_hora_inicio")
    private LocalDateTime fechaHoraInicio;

    @Column(name = "fecha_hora_fin")
    private LocalDateTime fechaHoraFin;

    // Estado del tramo
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    @Builder.Default
    private EstadoTramo estado = EstadoTramo.ESTIMADO;

    // Costos
    @Column(name = "costo_estimado", precision = 10, scale = 2)
    private BigDecimal costoEstimado;

    @Column(name = "costo_real", precision = 10, scale = 2)
    private BigDecimal costoReal;

    // Asignación de camión y transportista
    @Column(name = "camion_dominio", length = 20)
    private String camionDominio; // Referencia al camión asignado

    @Column(name = "transportista_id")
    private Long transportistaId; // Referencia al transportista

    // Información adicional
    @Column(name = "observaciones", length = 1000)
    private String observaciones;

    @Column(name = "temperatura_requerida")
    private BigDecimal temperaturaRequerida; // Para contenedores refrigerados

    @Column(name = "requiere_equipo_especial")
    @Builder.Default
    private Boolean requiereEquipoEspecial = false;

    // Seguimiento de combustible y peajes
    @Column(name = "combustible_consumido", precision = 8, scale = 2)
    private BigDecimal combustibleConsumido; // litros

    @Column(name = "costo_peajes", precision = 8, scale = 2)
    private BigDecimal costoPeajes;

    @Column(name = "costo_combustible", precision = 8, scale = 2)
    private BigDecimal costoCombustible;

    // Incidencias
    @Column(name = "tiene_incidencias")
    @Builder.Default
    private Boolean tieneIncidencias = false;

    @Column(name = "descripcion_incidencias", length = 1000)
    private String descripcionIncidencias;

    // Auditoría
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Enums
    public enum TipoTramo {
        ORIGEN_DESTINO,     // Directo desde origen a destino final
        ORIGEN_DEPOSITO,    // Desde origen a depósito intermedio
        DEPOSITO_DEPOSITO,  // Entre depósitos
        DEPOSITO_DESTINO    // Desde depósito a destino final
    }

    public enum EstadoTramo {
        ESTIMADO,           // Solo estimación, sin asignación
        ASIGNADO,          // Camión y transportista asignados
        INICIADO,          // Tramo en ejecución
        FINALIZADO,        // Completado exitosamente
        CANCELADO,         // Cancelado
        SUSPENDIDO,        // Temporalmente suspendido
        REPROGRAMADO      // Reprogramado por incidencias
    }

    // Métodos de conveniencia
    public void asignarCamionYTransportista(String dominoCamion, Long idTransportista) {
        this.camionDominio = dominoCamion;
        this.transportistaId = idTransportista;
        if (this.estado == EstadoTramo.ESTIMADO) {
            this.estado = EstadoTramo.ASIGNADO;
        }
    }

    public void iniciarTramo() {
        if (estado == EstadoTramo.ASIGNADO) {
            estado = EstadoTramo.INICIADO;
            fechaHoraInicio = LocalDateTime.now();
        }
    }

    public void finalizarTramo() {
        if (estado == EstadoTramo.INICIADO) {
            estado = EstadoTramo.FINALIZADO;
            fechaHoraFin = LocalDateTime.now();
            calcularTiempoReal();
        }
    }

    public void cancelarTramo(String motivo) {
        estado = EstadoTramo.CANCELADO;
        observaciones = (observaciones != null ? observaciones + " | " : "") + 
                       "CANCELADO: " + motivo;
    }

    public void reprogramarTramo(LocalDateTime nuevaFechaInicio, String motivo) {
        estado = EstadoTramo.REPROGRAMADO;
        fechaHoraInicioProgramada = nuevaFechaInicio;
        observaciones = (observaciones != null ? observaciones + " | " : "") + 
                       "REPROGRAMADO: " + motivo;
    }

    public void registrarIncidencia(String descripcion) {
        tieneIncidencias = true;
        descripcionIncidencias = (descripcionIncidencias != null ? descripcionIncidencias + " | " : "") + 
                                descripcion;
    }

    private void calcularTiempoReal() {
        if (fechaHoraInicio != null && fechaHoraFin != null) {
            long minutos = java.time.Duration.between(fechaHoraInicio, fechaHoraFin).toMinutes();
            this.tiempoRealMinutos = (int) minutos;
        }
    }

    public void calcularCostoReal(BigDecimal precioCombustible) {
        BigDecimal costoTotal = BigDecimal.ZERO;

        // Costo de combustible
        if (combustibleConsumido != null && precioCombustible != null) {
            costoCombustible = combustibleConsumido.multiply(precioCombustible);
            costoTotal = costoTotal.add(costoCombustible);
        }

        // Costo de peajes
        if (costoPeajes != null) {
            costoTotal = costoTotal.add(costoPeajes);
        }

        // Agregar costo base del camión si se tiene
        if (costoEstimado != null) {
            costoTotal = costoTotal.add(costoEstimado);
        }

        this.costoReal = costoTotal;
    }

    // Validaciones y métodos de negocio
    public boolean estaCompletado() {
        return estado == EstadoTramo.FINALIZADO;
    }

    public boolean estaEnEjecucion() {
        return estado == EstadoTramo.INICIADO;
    }

    public boolean puedeIniciar() {
        return estado == EstadoTramo.ASIGNADO && 
               camionDominio != null && 
               transportistaId != null;
    }

    public boolean puedeSerAsignado() {
        return estado == EstadoTramo.ESTIMADO;
    }

    public int calcularDemoraMinutos() {
        if (tiempoEstimadoMinutos == null || tiempoRealMinutos == null) return 0;
        return tiempoRealMinutos - tiempoEstimadoMinutos;
    }

    public BigDecimal calcularDesviacionCosto() {
        if (costoEstimado == null || costoReal == null) return BigDecimal.ZERO;
        return costoReal.subtract(costoEstimado);
    }

    public double calcularDistancia() {
        if (origenLatitud == null || origenLongitud == null || 
            destinoLatitud == null || destinoLongitud == null) {
            return 0.0;
        }

        double lat1 = Math.toRadians(origenLatitud.doubleValue());
        double lon1 = Math.toRadians(origenLongitud.doubleValue());
        double lat2 = Math.toRadians(destinoLatitud.doubleValue());
        double lon2 = Math.toRadians(destinoLongitud.doubleValue());

        double dLat = lat2 - lat1;
        double dLon = lon2 - lon1;

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(lat1) * Math.cos(lat2) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        final double RADIO_TIERRA_KM = 6371.0;
        double distancia = RADIO_TIERRA_KM * c;
        
        // Actualizar la distancia calculada
        this.distanciaKm = new BigDecimal(distancia).setScale(2, BigDecimal.ROUND_HALF_UP);
        
        return distancia;
    }

    public String getResumenTramo() {
        return String.format("Tramo %d: %s → %s (%.2f km) - %s", 
                           orden, 
                           origenDescripcion, 
                           destinoDescripcion,
                           distanciaKm != null ? distanciaKm : BigDecimal.ZERO,
                           estado);
    }

    public boolean esTramoDeDeposito() {
        return tipoTramo == TipoTramo.ORIGEN_DEPOSITO ||
               tipoTramo == TipoTramo.DEPOSITO_DEPOSITO ||
               tipoTramo == TipoTramo.DEPOSITO_DESTINO;
    }

    public boolean esTramoFinal() {
        return tipoTramo == TipoTramo.DEPOSITO_DESTINO ||
               tipoTramo == TipoTramo.ORIGEN_DESTINO;
    }

    public boolean requiereCondicionesEspeciales() {
        return requiereEquipoEspecial || temperaturaRequerida != null;
    }

    // Método para obtener el ID del depósito involucrado en el tramo
    public Long getDepositoInvolucrado() {
        if (origenDepositoId != null) return origenDepositoId;
        if (destinoDepositoId != null) return destinoDepositoId;
        return null;
    }

    public boolean validarSecuencia(Tramo tramoAnterior) {
        if (tramoAnterior == null) return true; // Primer tramo
        
        // El origen de este tramo debe coincidir con el destino del anterior
        return origenLatitud.equals(tramoAnterior.getDestinoLatitud()) &&
               origenLongitud.equals(tramoAnterior.getDestinoLongitud());
    }
}