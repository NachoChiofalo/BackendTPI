-- Tablas del esquema de precios
CREATE TABLE precios.tarifas_base (
    id BIGSERIAL PRIMARY KEY,
    tipo_camion VARCHAR(20) NOT NULL,
    precio_base_km DECIMAL(8,2) NOT NULL CHECK (precio_base_km > 0),
    factor_peso DECIMAL(6,4) DEFAULT 1.0 CHECK (factor_peso > 0),
    factor_volumen DECIMAL(6,4) DEFAULT 1.0 CHECK (factor_volumen > 0),
    factor_distancia DECIMAL(6,4) DEFAULT 1.0 CHECK (factor_distancia > 0),
    precio_minimo DECIMAL(8,2) DEFAULT 0,
    precio_maximo DECIMAL(8,2),
    activa BOOLEAN DEFAULT TRUE,
    fecha_vigencia_desde DATE DEFAULT CURRENT_DATE,
    fecha_vigencia_hasta DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CHECK (fecha_vigencia_hasta IS NULL OR fecha_vigencia_hasta > fecha_vigencia_desde)
);

-- Tabla para tarifas especiales por ruta
CREATE TABLE precios.tarifas_ruta (
    id BIGSERIAL PRIMARY KEY,
    ruta_id BIGINT NOT NULL REFERENCES rutas.rutas(id),
    tipo_camion VARCHAR(20) NOT NULL,
    precio_fijo DECIMAL(10,2),
    descuento_porcentaje DECIMAL(5,2) DEFAULT 0 CHECK (descuento_porcentaje >= 0 AND descuento_porcentaje <= 100),
    recargo_porcentaje DECIMAL(5,2) DEFAULT 0 CHECK (recargo_porcentaje >= 0),
    motivo_ajuste VARCHAR(200),
    activa BOOLEAN DEFAULT TRUE,
    fecha_vigencia_desde DATE DEFAULT CURRENT_DATE,
    fecha_vigencia_hasta DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(ruta_id, tipo_camion, fecha_vigencia_desde)
);

-- Tabla para factores de ajuste dinámicos
CREATE TABLE precios.factores_ajuste (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE,
    descripcion VARCHAR(200),
    tipo_factor VARCHAR(20) NOT NULL CHECK (tipo_factor IN (
        'COMBUSTIBLE', 'DEMANDA', 'TEMPORADA', 'URGENCIA', 'SEGURO', 'PEAJES'
    )),
    factor_multiplicador DECIMAL(6,4) NOT NULL CHECK (factor_multiplicador > 0),
    aplicable_tipos_camion VARCHAR(200), -- JSON o CSV de tipos aplicables
    activo BOOLEAN DEFAULT TRUE,
    fecha_vigencia_desde DATE DEFAULT CURRENT_DATE,
    fecha_vigencia_hasta DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla para historial de cotizaciones
CREATE TABLE precios.cotizaciones (
    id BIGSERIAL PRIMARY KEY,
    solicitud_id BIGINT REFERENCES solicitudes.solicitudes(id),
    cliente_username VARCHAR(50) NOT NULL,
    ciudad_origen_id BIGINT NOT NULL REFERENCES localizaciones.ciudades(id),
    ciudad_destino_id BIGINT NOT NULL REFERENCES localizaciones.ciudades(id),
    peso_kg DECIMAL(10,2) NOT NULL,
    volumen_m3 DECIMAL(10,2) NOT NULL,
    tipo_camion_sugerido VARCHAR(20) NOT NULL,
    precio_base DECIMAL(10,2) NOT NULL,
    ajustes_aplicados JSONB, -- Detalles de factores aplicados
    precio_final DECIMAL(10,2) NOT NULL,
    fecha_cotizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_vencimiento TIMESTAMP,
    estado_cotizacion VARCHAR(20) DEFAULT 'VIGENTE' CHECK (estado_cotizacion IN (
        'VIGENTE', 'ACEPTADA', 'RECHAZADA', 'VENCIDA'
    )),
    observaciones TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla para promociones y descuentos
CREATE TABLE precios.promociones (
    id BIGSERIAL PRIMARY KEY,
    codigo_promocion VARCHAR(20) UNIQUE,
    nombre VARCHAR(100) NOT NULL,
    descripcion TEXT,
    tipo_descuento VARCHAR(20) NOT NULL CHECK (tipo_descuento IN (
        'PORCENTAJE', 'MONTO_FIJO', 'ENVIO_GRATIS'
    )),
    valor_descuento DECIMAL(8,2) NOT NULL,
    descuento_maximo DECIMAL(8,2),
    peso_minimo_kg DECIMAL(8,2),
    monto_minimo DECIMAL(8,2),
    usos_maximos INTEGER,
    usos_actuales INTEGER DEFAULT 0,
    activa BOOLEAN DEFAULT TRUE,
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CHECK (fecha_fin IS NULL OR fecha_fin > fecha_inicio)
);

-- Índices para mejorar rendimiento
CREATE INDEX idx_tarifas_base_tipo ON precios.tarifas_base(tipo_camion);
CREATE INDEX idx_tarifas_base_activa ON precios.tarifas_base(activa);
CREATE INDEX idx_tarifas_base_vigencia ON precios.tarifas_base(fecha_vigencia_desde, fecha_vigencia_hasta);
CREATE INDEX idx_tarifas_ruta_ruta ON precios.tarifas_ruta(ruta_id);
CREATE INDEX idx_tarifas_ruta_tipo ON precios.tarifas_ruta(tipo_camion);
CREATE INDEX idx_factores_tipo ON precios.factores_ajuste(tipo_factor);
CREATE INDEX idx_factores_activo ON precios.factores_ajuste(activo);
CREATE INDEX idx_cotizaciones_cliente ON precios.cotizaciones(cliente_username);
CREATE INDEX idx_cotizaciones_fecha ON precios.cotizaciones(fecha_cotizacion);
CREATE INDEX idx_cotizaciones_estado ON precios.cotizaciones(estado_cotizacion);
CREATE INDEX idx_promociones_codigo ON precios.promociones(codigo_promocion);
CREATE INDEX idx_promociones_activa ON precios.promociones(activa);
CREATE INDEX idx_promociones_vigencia ON precios.promociones(fecha_inicio, fecha_fin);
