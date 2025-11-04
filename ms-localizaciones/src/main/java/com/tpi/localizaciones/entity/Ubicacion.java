package com.tpi.localizaciones.entity;

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
 * Entidad Ubicacion - Representa ubicaciones específicas con coordenadas
 * Esquema: localizaciones
 */
@Entity
@Table(name = "ubicaciones", schema = "localizaciones")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ubicacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 500)
    private String direccion;

    // Coordenadas precisas
    @Column(name = "latitud", nullable = false, precision = 10, scale = 7)
    private BigDecimal latitud;

    @Column(name = "longitud", nullable = false, precision = 10, scale = 7)
    private BigDecimal longitud;

    // Tipo de ubicación
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_ubicacion", nullable = false, length = 20)
    private TipoUbicacion tipoUbicacion;

    // Información adicional
    @Column(name = "codigo_postal", length = 10)
    private String codigoPostal;

    @Column(name = "numero_puerta", length = 20)
    private String numeroPuerta;

    @Column(name = "piso", length = 10)
    private String piso;

    @Column(name = "departamento", length = 10)
    private String departamento;

    @Column(name = "entre_calles", length = 200)
    private String entreCalles;

    @Column(name = "referencias", length = 500)
    private String referencias;

    // Contacto en la ubicación
    @Column(name = "contacto_nombre", length = 100)
    private String contactoNombre;

    @Column(name = "contacto_telefono", length = 20)
    private String contactoTelefono;

    @Column(name = "contacto_email", length = 150)
    private String contactoEmail;

    // Horarios de acceso
    @Column(name = "horario_atencion", length = 200)
    private String horarioAtencion;

    @Column(name = "instrucciones_acceso", length = 1000)
    private String instruccionesAcceso;

    // Características físicas del lugar
    @Column(name = "tiene_muelle_carga")
    @Builder.Default
    private Boolean tieneMuelleCarga = false;

    @Column(name = "tiene_grua")
    @Builder.Default
    private Boolean tieneGrua = false;

    @Column(name = "altura_maxima_metros", precision = 5, scale = 2)
    private BigDecimal alturaMaximaMetros;

    @Column(name = "peso_maximo_toneladas", precision = 8, scale = 2)
    private BigDecimal pesoMaximoToneladas;

    @Column(name = "espacio_maniobra_metros", precision = 6, scale = 2)
    private BigDecimal espacioManiobraMetros;

    // Restricciones y limitaciones
    @Column(name = "restricciones_vehiculos", length = 500)
    private String restriccionesVehiculos;

    @Column(name = "requiere_cita_previa")
    @Builder.Default
    private Boolean requiereCitaPrevia = false;

    @Column(name = "tiempo_carga_estimado_minutos")
    private Integer tiempoCargaEstimadoMinutos;

    // Relación con ciudad
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ciudad_id")
    private Ciudad ciudad;

    // Estado y validación
    @Enumerated(EnumType.STRING)
    @Column(name = "estado_validacion", length = 15)
    @Builder.Default
    private EstadoValidacion estadoValidacion = EstadoValidacion.PENDIENTE;

    @Column(name = "fecha_ultima_validacion")
    private LocalDateTime fechaUltimaValidacion;

    @Column(name = "validado_por", length = 100)
    private String validadoPor;

    @Column(name = "observaciones_validacion", length = 1000)
    private String observacionesValidacion;

    @Column(name = "activa")
    @Builder.Default
    private Boolean activa = true;

    // Auditoría
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Enums
    public enum TipoUbicacion {
        CLIENTE_ORIGEN,     // Punto de recogida del cliente
        CLIENTE_DESTINO,    // Punto de entrega al cliente
        DEPOSITO,          // Depósito/almacén
        PUERTO,            // Puerto marítimo/fluvial
        AEROPUERTO,        // Aeropuerto
        TERMINAL_CARGAS,   // Terminal de cargas
        ZONA_INDUSTRIAL,   // Zona industrial
        CENTRO_LOGISTICO,  // Centro logístico
        OTRO              // Otro tipo de ubicación
    }

    public enum EstadoValidacion {
        PENDIENTE,         // Pendiente de validación
        VALIDADA,         // Validada y verificada
        RECHAZADA,        // Rechazada por errores
        REQUIERE_REVISION // Requiere revisión adicional
    }

    // Métodos de conveniencia
    public String getDireccionCompleta() {
        StringBuilder sb = new StringBuilder(direccion);
        
        if (numeroPuerta != null && !numeroPuerta.trim().isEmpty()) {
            sb.append(" ").append(numeroPuerta);
        }
        
        if (piso != null && !piso.trim().isEmpty()) {
            sb.append(", Piso ").append(piso);
        }
        
        if (departamento != null && !departamento.trim().isEmpty()) {
            sb.append(", Dpto. ").append(departamento);
        }
        
        if (ciudad != null) {
            sb.append(", ").append(ciudad.getNombreCompleto());
        }
        
        if (codigoPostal != null && !codigoPostal.trim().isEmpty()) {
            sb.append(" (").append(codigoPostal).append(")");
        }
        
        return sb.toString();
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

    public void validar(String validadoPor, String observaciones) {
        this.estadoValidacion = EstadoValidacion.VALIDADA;
        this.fechaUltimaValidacion = LocalDateTime.now();
        this.validadoPor = validadoPor;
        this.observacionesValidacion = observaciones;
    }

    public void rechazar(String validadoPor, String motivo) {
        this.estadoValidacion = EstadoValidacion.RECHAZADA;
        this.fechaUltimaValidacion = LocalDateTime.now();
        this.validadoPor = validadoPor;
        this.observacionesValidacion = "RECHAZADA: " + motivo;
    }

    public void marcarParaRevision(String motivo) {
        this.estadoValidacion = EstadoValidacion.REQUIERE_REVISION;
        this.observacionesValidacion = (this.observacionesValidacion != null ? 
                                      this.observacionesValidacion + " | " : "") + 
                                     "REQUIERE REVISIÓN: " + motivo;
    }

    public boolean estaValidada() {
        return estadoValidacion == EstadoValidacion.VALIDADA;
    }

    public boolean puedeRecibirCamion(String tipoCamion, BigDecimal alturaVehiculo, BigDecimal pesoVehiculo) {
        // Verificar restricciones de altura
        if (alturaMaximaMetros != null && alturaVehiculo != null && 
            alturaVehiculo.compareTo(alturaMaximaMetros) > 0) {
            return false;
        }
        
        // Verificar restricciones de peso
        if (pesoMaximoToneladas != null && pesoVehiculo != null && 
            pesoVehiculo.compareTo(pesoMaximoToneladas) > 0) {
            return false;
        }
        
        // Verificar restricciones específicas de vehículos
        if (restriccionesVehiculos != null && tipoCamion != null) {
            String restricciones = restriccionesVehiculos.toLowerCase();
            String tipo = tipoCamion.toLowerCase();
            
            if (restricciones.contains("prohibido " + tipo) || 
                restricciones.contains("no permitido " + tipo)) {
                return false;
            }
        }
        
        return true;
    }

    public boolean requiereEquipoEspecial() {
        return !tieneMuelleCarga || !tieneGrua;
    }

    public String getResumenLogistico() {
        StringBuilder sb = new StringBuilder();
        
        sb.append("Capacidades: ");
        if (tieneMuelleCarga) sb.append("Muelle de carga, ");
        if (tieneGrua) sb.append("Grúa, ");
        
        if (alturaMaximaMetros != null) {
            sb.append(String.format("Altura máx: %.2fm, ", alturaMaximaMetros));
        }
        
        if (pesoMaximoToneladas != null) {
            sb.append(String.format("Peso máx: %.1ft, ", pesoMaximoToneladas));
        }
        
        if (tiempoCargaEstimadoMinutos != null) {
            sb.append(String.format("Tiempo carga: %d min", tiempoCargaEstimadoMinutos));
        }
        
        String resultado = sb.toString();
        return resultado.endsWith(", ") ? resultado.substring(0, resultado.length() - 2) : resultado;
    }

    public String getInfoContacto() {
        StringBuilder sb = new StringBuilder();
        
        if (contactoNombre != null && !contactoNombre.trim().isEmpty()) {
            sb.append("Contacto: ").append(contactoNombre);
        }
        
        if (contactoTelefono != null && !contactoTelefono.trim().isEmpty()) {
            if (sb.length() > 0) sb.append(" - ");
            sb.append("Tel: ").append(contactoTelefono);
        }
        
        if (contactoEmail != null && !contactoEmail.trim().isEmpty()) {
            if (sb.length() > 0) sb.append(" - ");
            sb.append("Email: ").append(contactoEmail);
        }
        
        return sb.length() > 0 ? sb.toString() : "Sin información de contacto";
    }

    // Validaciones
    public boolean validarCoordenadas() {
        if (latitud == null || longitud == null) return false;
        
        // Validar rangos válidos de coordenadas (mundial)
        double lat = latitud.doubleValue();
        double lng = longitud.doubleValue();
        
        return lat >= -90.0 && lat <= 90.0 && lng >= -180.0 && lng <= 180.0;
    }

    public boolean validarDatosCompletos() {
        return nombre != null && !nombre.trim().isEmpty() &&
               direccion != null && !direccion.trim().isEmpty() &&
               validarCoordenadas() &&
               tipoUbicacion != null;
    }

    public String getInfoCompleta() {
        return String.format("%s (%s) - %s - %s - Estado: %s", 
                           nombre, 
                           tipoUbicacion,
                           getDireccionCompleta(),
                           getResumenLogistico(),
                           estadoValidacion);
    }

    public boolean esAccesible24h() {
        return horarioAtencion != null && 
               (horarioAtencion.toLowerCase().contains("24") || 
                horarioAtencion.toLowerCase().contains("siempre"));
    }

    public boolean necesitaCitaPrevia() {
        return requiereCitaPrevia != null && requiereCitaPrevia;
    }

    // Método para obtener coordenadas como String para APIs externas
    public String getCoordenadaCompleta() {
        return latitud + "," + longitud;
    }

    public boolean estaEnRangoDeOperacion(BigDecimal latitudCentral, BigDecimal longitudCentral, double radioKm) {
        double distancia = calcularDistanciaA(latitudCentral, longitudCentral);
        return distancia <= radioKm;
    }
}