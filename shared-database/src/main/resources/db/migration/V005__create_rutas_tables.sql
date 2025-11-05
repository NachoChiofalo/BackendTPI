-- ================================================
-- ESQUEMA RUTAS - Rutas, Tramos y Planificación
-- ================================================

-- Tabla de Rutas
CREATE TABLE rutas.rutas (
    id BIGSERIAL PRIMARY KEY,
    solicitud_id BIGINT NOT NULL REFERENCES solicitudes.solicitudes(id),
    camion_dominio VARCHAR(10) NOT NULL REFERENCES flotas.camiones(dominio),
    ciudad_origen_id BIGINT NOT NULL REFERENCES localizaciones.ciudades(id),
    ciudad_destino_id BIGINT NOT NULL REFERENCES localizaciones.ciudades(id),
    distancia_total_km DECIMAL(10,2) NOT NULL,
    tiempo_estimado_horas DECIMAL(5,2) NOT NULL,
    costo_total DECIMAL(12,2) NOT NULL,
    fecha_inicio_planificada TIMESTAMP NOT NULL,
    fecha_fin_planificada TIMESTAMP NOT NULL,
    fecha_inicio_real TIMESTAMP,
    fecha_fin_real TIMESTAMP,
    estado VARCHAR(50) DEFAULT 'PLANIFICADA', -- PLANIFICADA, EN_CURSO, COMPLETADA, CANCELADA
    observaciones TEXT,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de Tramos de Ruta
CREATE TABLE rutas.tramos (
    id BIGSERIAL PRIMARY KEY,
    ruta_id BIGINT NOT NULL REFERENCES rutas.rutas(id) ON DELETE CASCADE,
    orden_tramo INTEGER NOT NULL,
    ciudad_origen_id BIGINT NOT NULL REFERENCES localizaciones.ciudades(id),
    ciudad_destino_id BIGINT NOT NULL REFERENCES localizaciones.ciudades(id),
    deposito_intermedio_id BIGINT REFERENCES flotas.depositos(id),
    distancia_km DECIMAL(10,2) NOT NULL,
    tiempo_estimado_horas DECIMAL(5,2) NOT NULL,
    costo_combustible DECIMAL(10,2) NOT NULL,
    costo_peajes DECIMAL(10,2) DEFAULT 0,
    costo_estadia DECIMAL(10,2) DEFAULT 0,
    fecha_inicio_planificada TIMESTAMP NOT NULL,
    fecha_fin_planificada TIMESTAMP NOT NULL,
    fecha_inicio_real TIMESTAMP,
    fecha_fin_real TIMESTAMP,
    estado VARCHAR(50) DEFAULT 'PENDIENTE', -- PENDIENTE, EN_TRANSITO, COMPLETADO
    observaciones TEXT,
    UNIQUE(ruta_id, orden_tramo)
);

-- Tabla de Seguimiento de Ubicación
CREATE TABLE rutas.seguimiento_ubicacion (
    id BIGSERIAL PRIMARY KEY,
    ruta_id BIGINT NOT NULL REFERENCES rutas.rutas(id),
    tramo_id BIGINT REFERENCES rutas.tramos(id),
    latitud DECIMAL(10,8) NOT NULL,
    longitud DECIMAL(11,8) NOT NULL,
    velocidad_kmh DECIMAL(5,2),
    direccion VARCHAR(100),
    timestamp_ubicacion TIMESTAMP NOT NULL,
    estado_vehiculo VARCHAR(50), -- EN_MOVIMIENTO, PARADO, DESCARGANDO, CARGANDO
    observaciones TEXT,
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de Eventos de Ruta
CREATE TABLE rutas.eventos_ruta (
    id BIGSERIAL PRIMARY KEY,
    ruta_id BIGINT NOT NULL REFERENCES rutas.rutas(id),
    tramo_id BIGINT REFERENCES rutas.tramos(id),
    tipo_evento VARCHAR(50) NOT NULL, -- INICIO, FIN, PARADA, DEMORA, INCIDENTE
    descripcion TEXT NOT NULL,
    latitud DECIMAL(10,8),
    longitud DECIMAL(11,8),
    timestamp_evento TIMESTAMP NOT NULL,
    impacto_tiempo_horas DECIMAL(5,2) DEFAULT 0,
    impacto_costo DECIMAL(10,2) DEFAULT 0,
    resuelto BOOLEAN DEFAULT false,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de Optimización de Rutas (para algoritmos futuros)
CREATE TABLE rutas.optimizacion_rutas (
    id BIGSERIAL PRIMARY KEY,
    ruta_original_id BIGINT NOT NULL REFERENCES rutas.rutas(id),
    algoritmo_usado VARCHAR(50) NOT NULL,
    distancia_original_km DECIMAL(10,2) NOT NULL,
    distancia_optimizada_km DECIMAL(10,2) NOT NULL,
    tiempo_original_horas DECIMAL(5,2) NOT NULL,
    tiempo_optimizado_horas DECIMAL(5,2) NOT NULL,
    costo_original DECIMAL(12,2) NOT NULL,
    costo_optimizado DECIMAL(12,2) NOT NULL,
    porcentaje_mejora DECIMAL(5,2),
    aplicada BOOLEAN DEFAULT false,
    fecha_calculo TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Índices para optimizar consultas
CREATE INDEX idx_rutas_solicitud ON rutas.rutas(solicitud_id);
CREATE INDEX idx_rutas_camion ON rutas.rutas(camion_dominio);
CREATE INDEX idx_rutas_estado ON rutas.rutas(estado);
CREATE INDEX idx_rutas_fecha_inicio ON rutas.rutas(fecha_inicio_planificada);
CREATE INDEX idx_rutas_origen_destino ON rutas.rutas(ciudad_origen_id, ciudad_destino_id);
CREATE INDEX idx_tramos_ruta ON rutas.tramos(ruta_id, orden_tramo);
CREATE INDEX idx_tramos_estado ON rutas.tramos(estado);
CREATE INDEX idx_seguimiento_ruta_timestamp ON rutas.seguimiento_ubicacion(ruta_id, timestamp_ubicacion);
CREATE INDEX idx_seguimiento_ubicacion ON rutas.seguimiento_ubicacion(latitud, longitud);
CREATE INDEX idx_eventos_ruta_timestamp ON rutas.eventos_ruta(ruta_id, timestamp_evento);
CREATE INDEX idx_eventos_tipo ON rutas.eventos_ruta(tipo_evento);
CREATE INDEX idx_optimizacion_ruta_original ON rutas.optimizacion_rutas(ruta_original_id);
