-- Tablas del esquema de flotas
CREATE TABLE flotas.transportistas (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    telefono VARCHAR(20),
    licencia_conducir VARCHAR(20) NOT NULL,
    categoria_licencia VARCHAR(10) NOT NULL,
    fecha_vencimiento_licencia DATE NOT NULL,
    fecha_ingreso DATE DEFAULT CURRENT_DATE,
    activo BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE flotas.camiones (
    dominio VARCHAR(20) PRIMARY KEY,
    marca VARCHAR(50) NOT NULL,
    modelo VARCHAR(50) NOT NULL,
    año_fabricacion INTEGER,
    tipo_camion VARCHAR(20) NOT NULL CHECK (tipo_camion IN (
        'RIGIDO_PEQUEÑO', 'RIGIDO_MEDIANO', 'RIGIDO_GRANDE',
        'SEMI_REMOLQUE', 'CAMION_REMOLQUE', 'ESPECIAL'
    )),
    capacidad_peso DECIMAL(10,2) NOT NULL CHECK (capacidad_peso > 0),
    capacidad_volumen DECIMAL(10,2) NOT NULL CHECK (capacidad_volumen > 0),
    consumo_combustible DECIMAL(8,2) NOT NULL CHECK (consumo_combustible > 0),
    costo_base_km DECIMAL(8,2) NOT NULL CHECK (costo_base_km > 0),
    costo_mantenimiento_diario DECIMAL(8,2),
    estado VARCHAR(15) NOT NULL DEFAULT 'DISPONIBLE' CHECK (estado IN (
        'DISPONIBLE', 'EN_VIAJE', 'EN_MANTENIMIENTO', 'AVERIADO', 'INACTIVO'
    )),
    condicion_mecanica VARCHAR(15) DEFAULT 'BUENA' CHECK (condicion_mecanica IN (
        'EXCELENTE', 'BUENA', 'REGULAR', 'MALA', 'CRITICA'
    )),
    numero_seguro VARCHAR(50),
    fecha_vencimiento_seguro DATE,
    fecha_ultimo_service DATE,
    kilometraje_actual BIGINT DEFAULT 0,
    transportista_id BIGINT REFERENCES flotas.transportistas(id),
    deposito_base_id BIGINT REFERENCES localizaciones.depositos(id),
    ubicacion_actual_lat DECIMAL(10,7),
    ubicacion_actual_lng DECIMAL(10,7),
    ubicacion_descripcion VARCHAR(200),
    activo BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla para historial de mantenimientos
CREATE TABLE flotas.mantenimientos (
    id BIGSERIAL PRIMARY KEY,
    camion_dominio VARCHAR(20) NOT NULL REFERENCES flotas.camiones(dominio),
    tipo_mantenimiento VARCHAR(50) NOT NULL,
    descripcion TEXT,
    fecha_mantenimiento DATE NOT NULL,
    costo DECIMAL(10,2),
    kilometraje BIGINT,
    proximo_mantenimiento_km BIGINT,
    realizado_por VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Índices para mejorar rendimiento
CREATE INDEX idx_camiones_estado ON flotas.camiones(estado);
CREATE INDEX idx_camiones_tipo ON flotas.camiones(tipo_camion);
CREATE INDEX idx_camiones_transportista ON flotas.camiones(transportista_id);
CREATE INDEX idx_camiones_deposito ON flotas.camiones(deposito_base_id);
CREATE INDEX idx_camiones_ubicacion ON flotas.camiones(ubicacion_actual_lat, ubicacion_actual_lng);
CREATE INDEX idx_transportistas_username ON flotas.transportistas(username);
CREATE INDEX idx_mantenimientos_camion ON flotas.mantenimientos(camion_dominio);
CREATE INDEX idx_mantenimientos_fecha ON flotas.mantenimientos(fecha_mantenimiento);
