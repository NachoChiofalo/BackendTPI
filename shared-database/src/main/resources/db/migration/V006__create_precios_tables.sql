-- ================================================
-- ESQUEMA PRECIOS - Tarifas, Cotizaciones y Precios
-- ================================================

-- Tabla de Tarifas Base por Distancia
CREATE TABLE precios.tarifas_distancia (
    id BIGSERIAL PRIMARY KEY,
    distancia_min_km INTEGER NOT NULL,
    distancia_max_km INTEGER NOT NULL,
    precio_por_km DECIMAL(8,4) NOT NULL,
    activo BOOLEAN DEFAULT true,
    fecha_vigencia_desde DATE NOT NULL,
    fecha_vigencia_hasta DATE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CHECK (distancia_min_km < distancia_max_km),
    CHECK (precio_por_km > 0)
);

-- Tabla de Tarifas por Tipo de Camión
CREATE TABLE precios.tarifas_tipo_camion (
    id BIGSERIAL PRIMARY KEY,
    tipo_camion_id BIGINT NOT NULL REFERENCES flotas.tipos_camion(id),
    factor_precio DECIMAL(6,4) NOT NULL DEFAULT 1.0000,
    precio_base_km DECIMAL(8,4) NOT NULL,
    activo BOOLEAN DEFAULT true,
    fecha_vigencia_desde DATE NOT NULL,
    fecha_vigencia_hasta DATE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CHECK (factor_precio > 0),
    CHECK (precio_base_km > 0)
);

-- Tabla de Tarifas por Zona Geográfica
CREATE TABLE precios.tarifas_zona (
    id BIGSERIAL PRIMARY KEY,
    ciudad_origen_id BIGINT NOT NULL REFERENCES localizaciones.ciudades(id),
    ciudad_destino_id BIGINT NOT NULL REFERENCES localizaciones.ciudades(id),
    factor_zona DECIMAL(6,4) NOT NULL DEFAULT 1.0000,
    precio_fijo DECIMAL(10,2) DEFAULT 0,
    activo BOOLEAN DEFAULT true,
    fecha_vigencia_desde DATE NOT NULL,
    fecha_vigencia_hasta DATE,
    observaciones TEXT,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(ciudad_origen_id, ciudad_destino_id, fecha_vigencia_desde),
    CHECK (factor_zona > 0),
    CHECK (precio_fijo >= 0)
);

-- Tabla de Tarifas por Peso y Volumen
CREATE TABLE precios.tarifas_carga (
    id BIGSERIAL PRIMARY KEY,
    peso_min_kg DECIMAL(10,2) NOT NULL,
    peso_max_kg DECIMAL(10,2) NOT NULL,
    volumen_min_m3 DECIMAL(10,2) NOT NULL,
    volumen_max_m3 DECIMAL(10,2) NOT NULL,
    factor_peso DECIMAL(6,4) NOT NULL DEFAULT 1.0000,
    factor_volumen DECIMAL(6,4) NOT NULL DEFAULT 1.0000,
    precio_adicional DECIMAL(10,2) DEFAULT 0,
    activo BOOLEAN DEFAULT true,
    fecha_vigencia_desde DATE NOT NULL,
    fecha_vigencia_hasta DATE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CHECK (peso_min_kg < peso_max_kg),
    CHECK (volumen_min_m3 < volumen_max_m3),
    CHECK (factor_peso > 0),
    CHECK (factor_volumen > 0)
);

-- Tabla de Servicios Adicionales
CREATE TABLE precios.servicios_adicionales (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    descripcion TEXT,
    tipo_precio VARCHAR(20) NOT NULL, -- FIJO, PORCENTUAL, POR_KM, POR_DIA
    valor DECIMAL(10,4) NOT NULL,
    unidad VARCHAR(20), -- PESOS, PORCENTAJE, PESOS_POR_KM, PESOS_POR_DIA
    obligatorio BOOLEAN DEFAULT false,
    activo BOOLEAN DEFAULT true,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CHECK (valor >= 0)
);

-- Tabla de Cotizaciones
CREATE TABLE precios.cotizaciones (
    id BIGSERIAL PRIMARY KEY,
    solicitud_id BIGINT NOT NULL REFERENCES solicitudes.solicitudes(id),
    numero_cotizacion VARCHAR(50) UNIQUE NOT NULL,
    fecha_cotizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_vencimiento DATE NOT NULL,
    subtotal DECIMAL(12,2) NOT NULL,
    descuento DECIMAL(10,2) DEFAULT 0,
    impuestos DECIMAL(10,2) DEFAULT 0,
    total DECIMAL(12,2) NOT NULL,
    moneda VARCHAR(3) DEFAULT 'ARS',
    estado VARCHAR(50) DEFAULT 'PENDIENTE', -- PENDIENTE, ENVIADA, ACEPTADA, RECHAZADA, VENCIDA
    observaciones TEXT,
    valida_hasta DATE,
    usuario_creacion VARCHAR(100),
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CHECK (subtotal >= 0),
    CHECK (total >= 0),
    CHECK (descuento >= 0),
    CHECK (impuestos >= 0)
);

-- Tabla de Detalle de Cotizaciones
CREATE TABLE precios.detalle_cotizaciones (
    id BIGSERIAL PRIMARY KEY,
    cotizacion_id BIGINT NOT NULL REFERENCES precios.cotizaciones(id) ON DELETE CASCADE,
    concepto VARCHAR(200) NOT NULL,
    descripcion TEXT,
    cantidad DECIMAL(10,2) DEFAULT 1,
    precio_unitario DECIMAL(10,4) NOT NULL,
    subtotal DECIMAL(12,2) NOT NULL,
    tipo_concepto VARCHAR(50), -- TRANSPORTE, SEGURO, ESTADIA, SERVICIO_ADICIONAL
    referencia_id BIGINT, -- ID de la tarifa o servicio usado
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CHECK (cantidad > 0),
    CHECK (precio_unitario >= 0),
    CHECK (subtotal >= 0)
);

-- Tabla de Historial de Precios
CREATE TABLE precios.historial_precios (
    id BIGSERIAL PRIMARY KEY,
    tabla_referencia VARCHAR(50) NOT NULL, -- tarifas_distancia, tarifas_tipo_camion, etc.
    registro_id BIGINT NOT NULL,
    campo_modificado VARCHAR(50) NOT NULL,
    valor_anterior TEXT,
    valor_nuevo TEXT,
    usuario VARCHAR(100),
    motivo TEXT,
    fecha_cambio TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Índices para optimizar consultas
CREATE INDEX idx_tarifas_distancia_rango ON precios.tarifas_distancia(distancia_min_km, distancia_max_km);
CREATE INDEX idx_tarifas_distancia_vigencia ON precios.tarifas_distancia(fecha_vigencia_desde, fecha_vigencia_hasta);
CREATE INDEX idx_tarifas_tipo_camion ON precios.tarifas_tipo_camion(tipo_camion_id);
CREATE INDEX idx_tarifas_tipo_vigencia ON precios.tarifas_tipo_camion(fecha_vigencia_desde, fecha_vigencia_hasta);
CREATE INDEX idx_tarifas_zona_origen_destino ON precios.tarifas_zona(ciudad_origen_id, ciudad_destino_id);
CREATE INDEX idx_tarifas_zona_vigencia ON precios.tarifas_zona(fecha_vigencia_desde, fecha_vigencia_hasta);
CREATE INDEX idx_tarifas_carga_peso ON precios.tarifas_carga(peso_min_kg, peso_max_kg);
CREATE INDEX idx_tarifas_carga_volumen ON precios.tarifas_carga(volumen_min_m3, volumen_max_m3);
CREATE INDEX idx_cotizaciones_solicitud ON precios.cotizaciones(solicitud_id);
CREATE INDEX idx_cotizaciones_numero ON precios.cotizaciones(numero_cotizacion);
CREATE INDEX idx_cotizaciones_estado ON precios.cotizaciones(estado);
CREATE INDEX idx_cotizaciones_fecha ON precios.cotizaciones(fecha_cotizacion);
CREATE INDEX idx_detalle_cotizacion ON precios.detalle_cotizaciones(cotizacion_id);
CREATE INDEX idx_historial_referencia ON precios.historial_precios(tabla_referencia, registro_id);
