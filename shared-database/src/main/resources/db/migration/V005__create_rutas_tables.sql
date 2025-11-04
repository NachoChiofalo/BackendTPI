-- Tablas del esquema de rutas
CREATE TABLE rutas.rutas (
    id BIGSERIAL PRIMARY KEY,
    ciudad_origen_id BIGINT NOT NULL REFERENCES localizaciones.ciudades(id),
    ciudad_destino_id BIGINT NOT NULL REFERENCES localizaciones.ciudades(id),
    distancia_km DECIMAL(8,2) NOT NULL CHECK (distancia_km > 0),
    tiempo_estimado_horas DECIMAL(6,2) NOT NULL CHECK (tiempo_estimado_horas > 0),
    dificultad VARCHAR(10) DEFAULT 'MEDIA' CHECK (dificultad IN ('BAJA', 'MEDIA', 'ALTA')),
    tipo_ruta VARCHAR(20) DEFAULT 'DIRECTA' CHECK (tipo_ruta IN ('DIRECTA', 'CON_PARADAS', 'ALTERNATIVA')),
    peajes_cantidad INTEGER DEFAULT 0,
    costo_peajes DECIMAL(8,2) DEFAULT 0,
    estado_ruta VARCHAR(20) DEFAULT 'ACTIVA' CHECK (estado_ruta IN ('ACTIVA', 'INACTIVA', 'EN_MANTENIMIENTO')),
    observaciones TEXT,
    activa BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(ciudad_origen_id, ciudad_destino_id),
    CHECK (ciudad_origen_id != ciudad_destino_id)
);

-- Tabla para puntos intermedios de las rutas
CREATE TABLE rutas.puntos_ruta (
    id BIGSERIAL PRIMARY KEY,
    ruta_id BIGINT NOT NULL REFERENCES rutas.rutas(id) ON DELETE CASCADE,
    orden_punto INTEGER NOT NULL,
    ciudad_id BIGINT REFERENCES localizaciones.ciudades(id),
    latitud DECIMAL(10,7),
    longitud DECIMAL(10,7),
    descripcion VARCHAR(200),
    es_parada_obligatoria BOOLEAN DEFAULT FALSE,
    tiempo_estimado_parada INTEGER DEFAULT 0, -- en minutos
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(ruta_id, orden_punto)
);

-- Tabla para restricciones de rutas por tipo de vehículo
CREATE TABLE rutas.restricciones_ruta (
    id BIGSERIAL PRIMARY KEY,
    ruta_id BIGINT NOT NULL REFERENCES rutas.rutas(id) ON DELETE CASCADE,
    tipo_camion VARCHAR(20) NOT NULL,
    peso_maximo_permitido DECIMAL(10,2),
    altura_maxima_permitida DECIMAL(5,2),
    ancho_maximo_permitido DECIMAL(5,2),
    restriccion_horaria_inicio TIME,
    restriccion_horaria_fin TIME,
    dias_semana_restriccion VARCHAR(20), -- formato: 'L,M,X,J,V,S,D'
    activa BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla para historial de viajes realizados
CREATE TABLE rutas.viajes (
    id BIGSERIAL PRIMARY KEY,
    ruta_id BIGINT NOT NULL REFERENCES rutas.rutas(id),
    solicitud_id BIGINT REFERENCES solicitudes.solicitudes(id),
    camion_dominio VARCHAR(20) NOT NULL REFERENCES flotas.camiones(dominio),
    transportista_id BIGINT NOT NULL REFERENCES flotas.transportistas(id),
    fecha_inicio TIMESTAMP NOT NULL,
    fecha_fin TIMESTAMP,
    distancia_real_km DECIMAL(8,2),
    tiempo_real_horas DECIMAL(6,2),
    combustible_consumido_litros DECIMAL(8,2),
    costo_combustible DECIMAL(10,2),
    costo_peajes DECIMAL(8,2),
    estado_viaje VARCHAR(20) DEFAULT 'EN_CURSO' CHECK (estado_viaje IN (
        'PLANIFICADO', 'EN_CURSO', 'COMPLETADO', 'CANCELADO', 'INTERRUMPIDO'
    )),
    observaciones TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Índices para mejorar rendimiento
CREATE INDEX idx_rutas_origen ON rutas.rutas(ciudad_origen_id);
CREATE INDEX idx_rutas_destino ON rutas.rutas(ciudad_destino_id);
CREATE INDEX idx_rutas_distancia ON rutas.rutas(distancia_km);
CREATE INDEX idx_rutas_activa ON rutas.rutas(activa);
CREATE INDEX idx_puntos_ruta_orden ON rutas.puntos_ruta(ruta_id, orden_punto);
CREATE INDEX idx_restricciones_tipo ON rutas.restricciones_ruta(tipo_camion);
CREATE INDEX idx_viajes_fecha_inicio ON rutas.viajes(fecha_inicio);
CREATE INDEX idx_viajes_camion ON rutas.viajes(camion_dominio);
CREATE INDEX idx_viajes_transportista ON rutas.viajes(transportista_id);
CREATE INDEX idx_viajes_estado ON rutas.viajes(estado_viaje);
