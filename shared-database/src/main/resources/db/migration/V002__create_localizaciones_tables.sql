-- ================================================
-- ESQUEMA LOCALIZACIONES - Ciudades y ubicaciones
-- ================================================

-- Tabla de Países
CREATE TABLE localizaciones.paises (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    codigo_iso VARCHAR(3) NOT NULL UNIQUE,
    activo BOOLEAN DEFAULT true,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de Provincias/Estados
CREATE TABLE localizaciones.provincias (
    id BIGSERIAL PRIMARY KEY,
    pais_id BIGINT NOT NULL REFERENCES localizaciones.paises(id),
    nombre VARCHAR(100) NOT NULL,
    codigo VARCHAR(10),
    activo BOOLEAN DEFAULT true,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(pais_id, nombre)
);

-- Tabla de Ciudades
CREATE TABLE localizaciones.ciudades (
    id BIGSERIAL PRIMARY KEY,
    provincia_id BIGINT NOT NULL REFERENCES localizaciones.provincias(id),
    nombre VARCHAR(100) NOT NULL,
    codigo_postal VARCHAR(20),
    latitud DECIMAL(10,8),
    longitud DECIMAL(11,8),
    activo BOOLEAN DEFAULT true,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(provincia_id, nombre)
);

-- Tabla de Distancias precalculadas entre ciudades
CREATE TABLE localizaciones.distancias (
    id BIGSERIAL PRIMARY KEY,
    ciudad_origen_id BIGINT NOT NULL REFERENCES localizaciones.ciudades(id),
    ciudad_destino_id BIGINT NOT NULL REFERENCES localizaciones.ciudades(id),
    distancia_km DECIMAL(10,2) NOT NULL,
    tiempo_estimado_horas DECIMAL(5,2),
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(ciudad_origen_id, ciudad_destino_id),
    CHECK (ciudad_origen_id != ciudad_destino_id)
);

-- Índices para optimizar consultas
CREATE INDEX idx_provincias_pais ON localizaciones.provincias(pais_id);
CREATE INDEX idx_ciudades_provincia ON localizaciones.ciudades(provincia_id);
CREATE INDEX idx_ciudades_coordenadas ON localizaciones.ciudades(latitud, longitud);
CREATE INDEX idx_distancias_origen ON localizaciones.distancias(ciudad_origen_id);
CREATE INDEX idx_distancias_destino ON localizaciones.distancias(ciudad_destino_id);
