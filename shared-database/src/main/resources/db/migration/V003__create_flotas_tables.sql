-- ================================================
-- ESQUEMA FLOTAS - Transportistas, Camiones y Depósitos
-- ================================================

-- Tabla de Transportistas
CREATE TABLE flotas.transportistas (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    dni VARCHAR(20) UNIQUE NOT NULL,
    telefono VARCHAR(20),
    email VARCHAR(100),
    licencia_conducir VARCHAR(50) NOT NULL,
    fecha_vencimiento_licencia DATE,
    activo BOOLEAN DEFAULT true,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de Depósitos
CREATE TABLE flotas.depositos (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    direccion TEXT NOT NULL,
    ciudad_id BIGINT NOT NULL REFERENCES localizaciones.ciudades(id),
    latitud DECIMAL(10,8) NOT NULL,
    longitud DECIMAL(11,8) NOT NULL,
    capacidad_maxima INTEGER,
    costo_estadia_diario DECIMAL(10,2) NOT NULL,
    activo BOOLEAN DEFAULT true,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de Tipos de Camión
CREATE TABLE flotas.tipos_camion (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE,
    descripcion TEXT,
    capacidad_peso_min DECIMAL(10,2) NOT NULL,
    capacidad_peso_max DECIMAL(10,2) NOT NULL,
    capacidad_volumen_min DECIMAL(10,2) NOT NULL,
    capacidad_volumen_max DECIMAL(10,2) NOT NULL,
    activo BOOLEAN DEFAULT true
);

-- Tabla de Camiones
CREATE TABLE flotas.camiones (
    dominio VARCHAR(10) PRIMARY KEY,
    tipo_camion_id BIGINT NOT NULL REFERENCES flotas.tipos_camion(id),
    marca VARCHAR(50),
    modelo VARCHAR(50),
    anio INTEGER,
    capacidad_peso DECIMAL(10,2) NOT NULL,
    capacidad_volumen DECIMAL(10,2) NOT NULL,
    consumo_combustible DECIMAL(5,2) NOT NULL,
    costo_km DECIMAL(10,2) NOT NULL,
    transportista_id BIGINT REFERENCES flotas.transportistas(id),
    deposito_actual_id BIGINT REFERENCES flotas.depositos(id),
    disponible BOOLEAN DEFAULT true,
    activo BOOLEAN DEFAULT true,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de Mantenimientos de Camiones
CREATE TABLE flotas.mantenimientos (
    id BIGSERIAL PRIMARY KEY,
    camion_dominio VARCHAR(10) NOT NULL REFERENCES flotas.camiones(dominio),
    tipo_mantenimiento VARCHAR(50) NOT NULL,
    descripcion TEXT,
    fecha_inicio DATE NOT NULL,
    fecha_fin_estimada DATE,
    fecha_fin_real DATE,
    costo DECIMAL(10,2),
    estado VARCHAR(20) DEFAULT 'PROGRAMADO',
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Índices para optimizar consultas
CREATE INDEX idx_transportistas_activo ON flotas.transportistas(activo);
CREATE INDEX idx_transportistas_dni ON flotas.transportistas(dni);
CREATE INDEX idx_depositos_ciudad ON flotas.depositos(ciudad_id);
CREATE INDEX idx_depositos_activo ON flotas.depositos(activo);
CREATE INDEX idx_camiones_disponible ON flotas.camiones(disponible, activo);
CREATE INDEX idx_camiones_capacidad ON flotas.camiones(capacidad_peso, capacidad_volumen);
CREATE INDEX idx_camiones_transportista ON flotas.camiones(transportista_id);
CREATE INDEX idx_camiones_deposito ON flotas.camiones(deposito_actual_id);
CREATE INDEX idx_mantenimientos_camion ON flotas.mantenimientos(camion_dominio);
CREATE INDEX idx_mantenimientos_estado ON flotas.mantenimientos(estado);
