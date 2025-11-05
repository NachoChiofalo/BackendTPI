-- ================================================
-- ESQUEMA SOLICITUDES - Clientes, Contenedores y Solicitudes
-- ================================================

-- Tabla de Clientes
CREATE TABLE solicitudes.clientes (
    id BIGSERIAL PRIMARY KEY,
    razon_social VARCHAR(200) NOT NULL,
    cuit VARCHAR(15) UNIQUE NOT NULL,
    email VARCHAR(100),
    telefono VARCHAR(20),
    direccion TEXT,
    ciudad_id BIGINT REFERENCES localizaciones.ciudades(id),
    activo BOOLEAN DEFAULT true,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de Tipos de Contenedor
CREATE TABLE solicitudes.tipos_contenedor (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE,
    descripcion TEXT,
    peso_maximo DECIMAL(10,2) NOT NULL,
    volumen_maximo DECIMAL(10,2) NOT NULL,
    dimensiones VARCHAR(100), -- ej: "20x8x8 pies"
    activo BOOLEAN DEFAULT true
);

-- Tabla de Contenedores
CREATE TABLE solicitudes.contenedores (
    id BIGSERIAL PRIMARY KEY,
    numero_contenedor VARCHAR(50) UNIQUE NOT NULL,
    tipo_contenedor_id BIGINT NOT NULL REFERENCES solicitudes.tipos_contenedor(id),
    peso_actual DECIMAL(10,2) DEFAULT 0,
    volumen_actual DECIMAL(10,2) DEFAULT 0,
    estado VARCHAR(50) DEFAULT 'DISPONIBLE', -- DISPONIBLE, EN_TRANSITO, EN_DEPOSITO
    ubicacion_actual VARCHAR(200),
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de Solicitudes de Transporte
CREATE TABLE solicitudes.solicitudes (
    id BIGSERIAL PRIMARY KEY,
    cliente_id BIGINT NOT NULL REFERENCES solicitudes.clientes(id),
    ciudad_origen_id BIGINT NOT NULL REFERENCES localizaciones.ciudades(id),
    ciudad_destino_id BIGINT NOT NULL REFERENCES localizaciones.ciudades(id),
    fecha_solicitud TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_retiro_deseada DATE NOT NULL,
    fecha_entrega_deseada DATE NOT NULL,
    direccion_retiro TEXT NOT NULL,
    direccion_entrega TEXT NOT NULL,
    peso_total DECIMAL(10,2) NOT NULL,
    volumen_total DECIMAL(10,2) NOT NULL,
    descripcion_carga TEXT,
    estado VARCHAR(50) DEFAULT 'PENDIENTE', -- PENDIENTE, COTIZADA, ACEPTADA, EN_TRANSITO, ENTREGADA, CANCELADA
    observaciones TEXT,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de Detalle de Solicitudes (productos/contenedores)
CREATE TABLE solicitudes.detalle_solicitudes (
    id BIGSERIAL PRIMARY KEY,
    solicitud_id BIGINT NOT NULL REFERENCES solicitudes.solicitudes(id) ON DELETE CASCADE,
    contenedor_id BIGINT REFERENCES solicitudes.contenedores(id),
    descripcion_producto VARCHAR(200) NOT NULL,
    peso DECIMAL(10,2) NOT NULL,
    volumen DECIMAL(10,2) NOT NULL,
    cantidad INTEGER DEFAULT 1,
    valor_declarado DECIMAL(12,2),
    fragil BOOLEAN DEFAULT false,
    peligroso BOOLEAN DEFAULT false,
    temperatura_controlada BOOLEAN DEFAULT false
);

-- Tabla de Historial de Estados de Solicitudes
CREATE TABLE solicitudes.historial_estados (
    id BIGSERIAL PRIMARY KEY,
    solicitud_id BIGINT NOT NULL REFERENCES solicitudes.solicitudes(id) ON DELETE CASCADE,
    estado_anterior VARCHAR(50),
    estado_nuevo VARCHAR(50) NOT NULL,
    observaciones TEXT,
    usuario VARCHAR(100),
    fecha_cambio TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Índices para optimizar consultas
CREATE INDEX idx_clientes_cuit ON solicitudes.clientes(cuit);
CREATE INDEX idx_clientes_activo ON solicitudes.clientes(activo);
CREATE INDEX idx_clientes_ciudad ON solicitudes.clientes(ciudad_id);
CREATE INDEX idx_contenedores_numero ON solicitudes.contenedores(numero_contenedor);
CREATE INDEX idx_contenedores_estado ON solicitudes.contenedores(estado);
CREATE INDEX idx_contenedores_tipo ON solicitudes.contenedores(tipo_contenedor_id);
CREATE INDEX idx_solicitudes_cliente ON solicitudes.solicitudes(cliente_id);
CREATE INDEX idx_solicitudes_estado ON solicitudes.solicitudes(estado);
CREATE INDEX idx_solicitudes_fecha_retiro ON solicitudes.solicitudes(fecha_retiro_deseada);
CREATE INDEX idx_solicitudes_origen_destino ON solicitudes.solicitudes(ciudad_origen_id, ciudad_destino_id);
CREATE INDEX idx_detalle_solicitud ON solicitudes.detalle_solicitudes(solicitud_id);
CREATE INDEX idx_historial_solicitud ON solicitudes.historial_estados(solicitud_id);
CREATE INDEX idx_historial_fecha ON solicitudes.historial_estados(fecha_cambio);
