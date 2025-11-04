-- Tablas del esquema de localizaciones
CREATE TABLE localizaciones.ciudades (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    provincia VARCHAR(100) NOT NULL,
    pais VARCHAR(100) NOT NULL,
    latitud DECIMAL(10,8) NOT NULL,
    longitud DECIMAL(11,8) NOT NULL,
    codigo_postal VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(nombre, provincia, pais)
);

CREATE TABLE localizaciones.depositos (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    direccion VARCHAR(200) NOT NULL,
    ciudad_id BIGINT NOT NULL REFERENCES localizaciones.ciudades(id),
    latitud DECIMAL(10,8) NOT NULL,
    longitud DECIMAL(11,8) NOT NULL,
    capacidad_maxima DECIMAL(10,2),
    horario_apertura TIME,
    horario_cierre TIME,
    telefono VARCHAR(20),
    email VARCHAR(100),
    activo BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Índices para mejorar rendimiento en búsquedas geográficas
CREATE INDEX idx_ciudades_location ON localizaciones.ciudades(latitud, longitud);
CREATE INDEX idx_depositos_location ON localizaciones.depositos(latitud, longitud);
CREATE INDEX idx_depositos_ciudad ON localizaciones.depositos(ciudad_id);
CREATE INDEX idx_ciudades_nombre ON localizaciones.ciudades(nombre);
CREATE INDEX idx_ciudades_provincia ON localizaciones.ciudades(provincia);
