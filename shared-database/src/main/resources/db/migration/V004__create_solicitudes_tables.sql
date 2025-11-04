-- Tablas del esquema de solicitudes
CREATE TABLE solicitudes.solicitudes (
    id BIGSERIAL PRIMARY KEY,
    cliente_username VARCHAR(50) NOT NULL,
    ciudad_origen_id BIGINT NOT NULL REFERENCES localizaciones.ciudades(id),
    ciudad_destino_id BIGINT NOT NULL REFERENCES localizaciones.ciudades(id),
    direccion_retiro VARCHAR(200),
    direccion_entrega VARCHAR(200),
    fecha_retiro TIMESTAMP NOT NULL,
    fecha_entrega_estimada TIMESTAMP,
    fecha_entrega_real TIMESTAMP,
    peso_kg DECIMAL(10,2) NOT NULL CHECK (peso_kg > 0),
    volumen_m3 DECIMAL(10,2) NOT NULL CHECK (volumen_m3 > 0),
    tipo_carga VARCHAR(50),
    valor_declarado DECIMAL(12,2),
    requiere_seguro BOOLEAN DEFAULT FALSE,
    estado VARCHAR(20) DEFAULT 'PENDIENTE' CHECK (estado IN (
        'PENDIENTE', 'CONFIRMADA', 'EN_TRANSITO', 'ENTREGADA', 'CANCELADA'
    )),
    precio_calculado DECIMAL(10,2),
    precio_final DECIMAL(10,2),
    observaciones TEXT,
    camion_asignado VARCHAR(20) REFERENCES flotas.camiones(dominio),
    transportista_asignado BIGINT REFERENCES flotas.transportistas(id),
    fecha_asignacion TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CHECK (ciudad_origen_id != ciudad_destino_id),
    CHECK (fecha_retiro > CURRENT_TIMESTAMP)
);

-- Tabla para seguimiento de estados de solicitud
CREATE TABLE solicitudes.seguimiento_solicitudes (
    id BIGSERIAL PRIMARY KEY,
    solicitud_id BIGINT NOT NULL REFERENCES solicitudes.solicitudes(id),
    estado_anterior VARCHAR(20),
    estado_nuevo VARCHAR(20) NOT NULL,
    fecha_cambio TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    observaciones TEXT,
    usuario_cambio VARCHAR(50),
    ubicacion_lat DECIMAL(10,7),
    ubicacion_lng DECIMAL(10,7),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla para documentos asociados a solicitudes
CREATE TABLE solicitudes.documentos_solicitud (
    id BIGSERIAL PRIMARY KEY,
    solicitud_id BIGINT NOT NULL REFERENCES solicitudes.solicitudes(id),
    tipo_documento VARCHAR(50) NOT NULL,
    nombre_archivo VARCHAR(200) NOT NULL,
    ruta_archivo VARCHAR(500) NOT NULL,
    tamaño_bytes BIGINT,
    tipo_mime VARCHAR(100),
    subido_por VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Índices para mejorar rendimiento
CREATE INDEX idx_solicitudes_cliente ON solicitudes.solicitudes(cliente_username);
CREATE INDEX idx_solicitudes_estado ON solicitudes.solicitudes(estado);
CREATE INDEX idx_solicitudes_fecha_retiro ON solicitudes.solicitudes(fecha_retiro);
CREATE INDEX idx_solicitudes_origen ON solicitudes.solicitudes(ciudad_origen_id);
CREATE INDEX idx_solicitudes_destino ON solicitudes.solicitudes(ciudad_destino_id);
CREATE INDEX idx_solicitudes_camion ON solicitudes.solicitudes(camion_asignado);
CREATE INDEX idx_solicitudes_transportista ON solicitudes.solicitudes(transportista_asignado);
CREATE INDEX idx_seguimiento_solicitud ON solicitudes.seguimiento_solicitudes(solicitud_id);
CREATE INDEX idx_seguimiento_fecha ON solicitudes.seguimiento_solicitudes(fecha_cambio);
CREATE INDEX idx_documentos_solicitud ON solicitudes.documentos_solicitud(solicitud_id);
