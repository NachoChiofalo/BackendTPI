package com.tpi.flotas.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad Deposito - Representa los puntos intermedios de almacenamiento
 * Esquema: flotas
 */
@Entity
@Table(name = "depositos", schema = "flotas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Deposito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String codigo; // Código identificador único

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 500)
    private String direccion;

    // Geolocalización
    @Column(name = "latitud", nullable = false, precision = 10, scale = 7)
    private BigDecimal latitud;

    @Column(name = "longitud", nullable = false, precision = 10, scale = 7)
    private BigDecimal longitud;

    // Capacidades del depósito
    @Column(name = "capacidad_maxima_contenedores", nullable = false)
    private Integer capacidadMaximaContenedores;

    @Column(name = "capacidad_peso_total", precision = 12, scale = 2)
    private BigDecimal capacidadPesoTotal; // en toneladas

    @Column(name = "capacidad_volumen_total", precision = 12, scale = 2)
    private BigDecimal capacidadVolumenTotal; // en metros cúbicos

    // Ocupación actual
    @Column(name = "contenedores_actuales")
    @Builder.Default
    private Integer contenedoresActuales = 0;

    @Column(name = "peso_actual", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal pesoActual = BigDecimal.ZERO;

    @Column(name = "volumen_actual", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal volumenActual = BigDecimal.ZERO;

    // Costos y operación
    @Column(name = "costo_estadía_diario", nullable = false, precision = 8, scale = 2)
    private BigDecimal costoEstadiaDiario; // por contenedor por día

    @Column(name = "tiempo_operacion_carga", nullable = false)
    private Integer tiempoOperacionCarga; // minutos para carga/descarga

    // Horarios de operación
    @Column(name = "hora_apertura", length = 5)
    private String horaApertura; // formato HH:MM

    @Column(name = "hora_cierre", length = 5)
    private String horaCierre; // formato HH:MM

    @Column(name = "opera_fines_semana")
    @Builder.Default
    private Boolean operaFinesSemana = false;

    // Estado y configuración
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    @Builder.Default
    private EstadoDeposito estado = EstadoDeposito.OPERATIVO;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_deposito", length = 20)
    private TipoDeposito tipoDeposito;

    // Servicios disponibles
    @Column(name = "tiene_grua")
    @Builder.Default
    private Boolean tieneGrua = false;

    @Column(name = "tiene_bascula")
    @Builder.Default
    private Boolean tieneBascula = false;

    @Column(name = "tiene_refrigeracion")
    @Builder.Default
    private Boolean tieneRefrigeracion = false;

    @Column(name = "tiene_seguridad_24h")
    @Builder.Default
    private Boolean tieneSeguridad24h = false;

    // Contacto y responsable
    @Column(name = "responsable_nombre", length = 100)
    private String responsableNombre;

    @Column(name = "responsable_telefono", length = 20)
    private String responsableTelefono;

    @Column(name = "responsable_email", length = 150)
    private String responsableEmail;

    // Relaciones
    @OneToMany(mappedBy = "depositoBase", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Camion> camionesBase = new ArrayList<>();

    @Column(name = "activo")
    @Builder.Default
    private Boolean activo = true;

    // Auditoría
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Enums
    public enum EstadoDeposito {
        OPERATIVO,          // Funcionando normalmente
        MANTENIMIENTO,      // En mantenimiento
        SATURADO,          // A capacidad máxima
        CERRADO_TEMPORAL,  // Cerrado temporalmente
        FUERA_SERVICIO     // Fuera de servicio
    }

    public enum TipoDeposito {
        PRINCIPAL,         // Depósito principal/central
        SECUNDARIO,        // Depósito de apoyo
        ESPECIAILIZADO,   // Para contenedores especiales
        TEMPORAL,         // Depósito temporal
        CROSS_DOCKING     // Solo para transferencias
    }

    // Métodos de conveniencia
    public boolean puedeAlmacenar(Integer cantidadContenedores, BigDecimal peso, BigDecimal volumen) {
        if (!activo || estado != EstadoDeposito.OPERATIVO) {
            return false;
        }

        boolean capacidadContenedores = (contenedoresActuales + cantidadContenedores) <= capacidadMaximaContenedores;
        boolean capacidadPeso = capacidadPesoTotal == null || 
                               (pesoActual.add(peso != null ? peso : BigDecimal.ZERO)).compareTo(capacidadPesoTotal) <= 0;
        boolean capacidadVolumen = capacidadVolumenTotal == null || 
                                  (volumenActual.add(volumen != null ? volumen : BigDecimal.ZERO)).compareTo(capacidadVolumenTotal) <= 0;

        return capacidadContenedores && capacidadPeso && capacidadVolumen;
    }

    public void ingresarContenedor(BigDecimal peso, BigDecimal volumen) {
        contenedoresActuales++;
        if (peso != null) {
            pesoActual = pesoActual.add(peso);
        }
        if (volumen != null) {
            volumenActual = volumenActual.add(volumen);
        }
    }

    public void retirarContenedor(BigDecimal peso, BigDecimal volumen) {
        if (contenedoresActuales > 0) {
            contenedoresActuales--;
        }
        if (peso != null && pesoActual.compareTo(peso) >= 0) {
            pesoActual = pesoActual.subtract(peso);
        }
        if (volumen != null && volumenActual.compareTo(volumen) >= 0) {
            volumenActual = volumenActual.subtract(volumen);
        }
    }

    public double calcularPorcentajeOcupacion() {
        if (capacidadMaximaContenedores == 0) return 0.0;
        return (contenedoresActuales.doubleValue() / capacidadMaximaContenedores.doubleValue()) * 100.0;
    }

    public boolean estaOperativoEnHorario(LocalDateTime fecha) {
        if (!activo || estado != EstadoDeposito.OPERATIVO) {
            return false;
        }

        // Verificar si opera en fines de semana
        int diaSemana = fecha.getDayOfWeek().getValue();
        if ((diaSemana == 6 || diaSemana == 7) && !operaFinesSemana) {
            return false;
        }

        // Verificar horario (implementación básica)
        if (horaApertura != null && horaCierre != null) {
            int hora = fecha.getHour();
            int horaApert = Integer.parseInt(horaApertura.split(":")[0]);
            int horaCierr = Integer.parseInt(horaCierre.split(":")[0]);
            
            return hora >= horaApert && hora < horaCierr;
        }

        return true; // Si no hay horarios definidos, asume 24h
    }

    public BigDecimal calcularCostoEstadia(Integer dias) {
        if (dias == null || dias <= 0) return BigDecimal.ZERO;
        return costoEstadiaDiario.multiply(new BigDecimal(dias));
    }

    public String getInfoCompleta() {
        return String.format("%s - %s (%s) - Ocupación: %d/%d contenedores (%.1f%%)", 
                            codigo, nombre, estado, 
                            contenedoresActuales, capacidadMaximaContenedores,
                            calcularPorcentajeOcupacion());
    }

    public boolean tieneCapacidadParaTipo(String tipoContenedor) {
        if (tipoContenedor == null) return true;
        
        switch (tipoDeposito) {
            case ESPECIAILIZADO:
                return tieneRefrigeracion || tieneGrua; // Acepta contenedores especiales
            case CROSS_DOCKING:
                return true; // Solo transferencias, acepta todos
            case TEMPORAL:
                return !tipoContenedor.contains("REFRIGERADO"); // No acepta refrigerados
            default:
                return tieneRefrigeracion || !tipoContenedor.contains("REFRIGERADO");
        }
    }

    public double calcularDistanciaA(BigDecimal latDestino, BigDecimal lngDestino) {
        if (latDestino == null || lngDestino == null) return 0.0;

        double lat1 = Math.toRadians(this.latitud.doubleValue());
        double lon1 = Math.toRadians(this.longitud.doubleValue());
        double lat2 = Math.toRadians(latDestino.doubleValue());
        double lon2 = Math.toRadians(lngDestino.doubleValue());

        double dLat = lat2 - lat1;
        double dLon = lon2 - lon1;

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(lat1) * Math.cos(lat2) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        final double RADIO_TIERRA_KM = 6371.0;
        return RADIO_TIERRA_KM * c;
    }
}