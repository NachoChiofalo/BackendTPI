-- ================================================
-- SCRIPT DE INICIALIZACIÓN COMPLETA DE BASE DE DATOS
-- Sistema de Logística TPI - 2025
-- ================================================

-- ================================================
-- PASO 1: CREAR ESQUEMAS
-- ================================================
CREATE SCHEMA IF NOT EXISTS solicitudes;
CREATE SCHEMA IF NOT EXISTS flotas;
CREATE SCHEMA IF NOT EXISTS rutas;
CREATE SCHEMA IF NOT EXISTS precios;
CREATE SCHEMA IF NOT EXISTS localizaciones;

-- Otorgar permisos al usuario
GRANT USAGE ON SCHEMA solicitudes, flotas, rutas, precios, localizaciones TO tpi_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA solicitudes, flotas, rutas, precios, localizaciones TO tpi_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA solicitudes, flotas, rutas, precios, localizaciones TO tpi_user;

-- Otorgar permisos futuros
ALTER DEFAULT PRIVILEGES IN SCHEMA solicitudes GRANT ALL ON TABLES TO tpi_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA flotas GRANT ALL ON TABLES TO tpi_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA rutas GRANT ALL ON TABLES TO tpi_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA precios GRANT ALL ON TABLES TO tpi_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA localizaciones GRANT ALL ON TABLES TO tpi_user;

ALTER DEFAULT PRIVILEGES IN SCHEMA solicitudes GRANT ALL ON SEQUENCES TO tpi_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA flotas GRANT ALL ON SEQUENCES TO tpi_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA rutas GRANT ALL ON SEQUENCES TO tpi_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA precios GRANT ALL ON SEQUENCES TO tpi_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA localizaciones GRANT ALL ON SEQUENCES TO tpi_user;

-- ================================================
-- PASO 2: ESQUEMA LOCALIZACIONES
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

-- Índices para optimizar consultas - Localizaciones
CREATE INDEX idx_provincias_pais ON localizaciones.provincias(pais_id);
CREATE INDEX idx_ciudades_provincia ON localizaciones.ciudades(provincia_id);
CREATE INDEX idx_ciudades_coordenadas ON localizaciones.ciudades(latitud, longitud);
CREATE INDEX idx_distancias_origen ON localizaciones.distancias(ciudad_origen_id);
CREATE INDEX idx_distancias_destino ON localizaciones.distancias(ciudad_destino_id);

-- ================================================
-- PASO 3: ESQUEMA FLOTAS
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

-- Índices para optimizar consultas - Flotas
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

-- ================================================
-- PASO 4: ESQUEMA SOLICITUDES
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
    dimensiones VARCHAR(100),
    activo BOOLEAN DEFAULT true
);

-- Tabla de Contenedores
CREATE TABLE solicitudes.contenedores (
    id BIGSERIAL PRIMARY KEY,
    numero_contenedor VARCHAR(50) UNIQUE NOT NULL,
    tipo_contenedor_id BIGINT NOT NULL REFERENCES solicitudes.tipos_contenedor(id),
    peso_actual DECIMAL(10,2) DEFAULT 0,
    volumen_actual DECIMAL(10,2) DEFAULT 0,
    estado VARCHAR(50) DEFAULT 'DISPONIBLE',
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
    estado VARCHAR(50) DEFAULT 'PENDIENTE',
    observaciones TEXT,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de Detalle de Solicitudes
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

-- Índices para optimizar consultas - Solicitudes
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

-- ================================================
-- PASO 5: ESQUEMA RUTAS
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
    estado VARCHAR(50) DEFAULT 'PLANIFICADA',
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
    estado VARCHAR(50) DEFAULT 'PENDIENTE',
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
    estado_vehiculo VARCHAR(50),
    observaciones TEXT,
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de Eventos de Ruta
CREATE TABLE rutas.eventos_ruta (
    id BIGSERIAL PRIMARY KEY,
    ruta_id BIGINT NOT NULL REFERENCES rutas.rutas(id),
    tramo_id BIGINT REFERENCES rutas.tramos(id),
    tipo_evento VARCHAR(50) NOT NULL,
    descripcion TEXT NOT NULL,
    latitud DECIMAL(10,8),
    longitud DECIMAL(11,8),
    timestamp_evento TIMESTAMP NOT NULL,
    impacto_tiempo_horas DECIMAL(5,2) DEFAULT 0,
    impacto_costo DECIMAL(10,2) DEFAULT 0,
    resuelto BOOLEAN DEFAULT false,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de Optimización de Rutas
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

-- Índices para optimizar consultas - Rutas
CREATE INDEX idx_rutas_solicitud ON rutas.rutas(solicitud_id);
CREATE INDEX idx_rutas_camion ON rutas.rutas(camion_dominio);
CREATE INDEX idx_rutas_estado ON rutas.rutas(estado);
CREATE INDEX idx_rutas_fecha_inicio ON rutas.rutas(fecha_inicio_planificada);
CREATE INDEX idx_rutas_origen_destino ON rutas.rutas(ciudad_origen_id, ciudad_destino_id);
CREATE INDEX idx_tramos_ruta ON rutas.tramos(ruta_id, orden_tramo);
CREATE INDEX idx_tramos_estado ON rutas.tramos(estado);

-- ================================================
-- PASO 6: ESQUEMA PRECIOS
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
    tipo_precio VARCHAR(20) NOT NULL,
    valor DECIMAL(10,4) NOT NULL,
    unidad VARCHAR(20),
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
    estado VARCHAR(50) DEFAULT 'PENDIENTE',
    observaciones TEXT,
    valida_hasta DATE,
    usuario_creacion VARCHAR(100),
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CHECK (subtotal >= 0),
    CHECK (total >= 0)
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
    CHECK (precio_unitario >= 0),
    CHECK (subtotal >= 0)
);

-- Índices para optimizar consultas - Precios
CREATE INDEX idx_tarifas_distancia_vigencia ON precios.tarifas_distancia(fecha_vigencia_desde, fecha_vigencia_hasta);
CREATE INDEX idx_tarifas_tipo_camion_vigencia ON precios.tarifas_tipo_camion(tipo_camion_id, fecha_vigencia_desde);
CREATE INDEX idx_tarifas_zona_ciudades ON precios.tarifas_zona(ciudad_origen_id, ciudad_destino_id);
CREATE INDEX idx_cotizaciones_solicitud ON precios.cotizaciones(solicitud_id);
CREATE INDEX idx_cotizaciones_estado ON precios.cotizaciones(estado);
CREATE INDEX idx_detalle_cotizacion ON precios.detalle_cotizaciones(cotizacion_id);

-- ================================================
-- PASO 7: DATOS INICIALES
-- ================================================

-- Insertar países
INSERT INTO localizaciones.paises (nombre, codigo_iso) VALUES
('Argentina', 'ARG'),
('Brasil', 'BRA'),
('Chile', 'CHL'),
('Uruguay', 'URY');

-- Insertar provincias argentinas principales
INSERT INTO localizaciones.provincias (pais_id, nombre, codigo) VALUES
((SELECT id FROM localizaciones.paises WHERE codigo_iso = 'ARG'), 'Buenos Aires', 'BA'),
((SELECT id FROM localizaciones.paises WHERE codigo_iso = 'ARG'), 'Córdoba', 'CB'),
((SELECT id FROM localizaciones.paises WHERE codigo_iso = 'ARG'), 'Santa Fe', 'SF'),
((SELECT id FROM localizaciones.paises WHERE codigo_iso = 'ARG'), 'Mendoza', 'MZ'),
((SELECT id FROM localizaciones.paises WHERE codigo_iso = 'ARG'), 'Tucumán', 'TM');

-- Insertar ciudades principales
INSERT INTO localizaciones.ciudades (provincia_id, nombre, codigo_postal, latitud, longitud) VALUES
((SELECT id FROM localizaciones.provincias WHERE codigo = 'BA'), 'Buenos Aires', 'C1000', -34.6118, -58.3960),
((SELECT id FROM localizaciones.provincias WHERE codigo = 'BA'), 'La Plata', '1900', -34.9215, -57.9545),
((SELECT id FROM localizaciones.provincias WHERE codigo = 'BA'), 'Mar del Plata', '7600', -38.0055, -57.5426),
((SELECT id FROM localizaciones.provincias WHERE codigo = 'CB'), 'Córdoba', '5000', -31.4201, -64.1888),
((SELECT id FROM localizaciones.provincias WHERE codigo = 'SF'), 'Rosario', '2000', -32.9442, -60.6505),
((SELECT id FROM localizaciones.provincias WHERE codigo = 'SF'), 'Santa Fe', '3000', -31.6333, -60.7000),
((SELECT id FROM localizaciones.provincias WHERE codigo = 'MZ'), 'Mendoza', '5500', -32.8895, -68.8458),
((SELECT id FROM localizaciones.provincias WHERE codigo = 'TM'), 'San Miguel de Tucumán', '4000', -26.8083, -65.2176);

-- Insertar distancias entre ciudades principales
INSERT INTO localizaciones.distancias (ciudad_origen_id, ciudad_destino_id, distancia_km, tiempo_estimado_horas) VALUES
((SELECT id FROM localizaciones.ciudades WHERE nombre = 'Buenos Aires'), (SELECT id FROM localizaciones.ciudades WHERE nombre = 'Córdoba'), 695, 8.5),
((SELECT id FROM localizaciones.ciudades WHERE nombre = 'Buenos Aires'), (SELECT id FROM localizaciones.ciudades WHERE nombre = 'Rosario'), 300, 3.5),
((SELECT id FROM localizaciones.ciudades WHERE nombre = 'Buenos Aires'), (SELECT id FROM localizaciones.ciudades WHERE nombre = 'Mendoza'), 1037, 12.0),
((SELECT id FROM localizaciones.ciudades WHERE nombre = 'Buenos Aires'), (SELECT id FROM localizaciones.ciudades WHERE nombre = 'La Plata'), 56, 1.0),
((SELECT id FROM localizaciones.ciudades WHERE nombre = 'Córdoba'), (SELECT id FROM localizaciones.ciudades WHERE nombre = 'Rosario'), 395, 4.5),
((SELECT id FROM localizaciones.ciudades WHERE nombre = 'Córdoba'), (SELECT id FROM localizaciones.ciudades WHERE nombre = 'Mendoza'), 515, 6.0),
((SELECT id FROM localizaciones.ciudades WHERE nombre = 'Rosario'), (SELECT id FROM localizaciones.ciudades WHERE nombre = 'Santa Fe'), 170, 2.0);

-- Insertar tipos de camión
INSERT INTO flotas.tipos_camion (nombre, descripcion, capacidad_peso_min, capacidad_peso_max, capacidad_volumen_min, capacidad_volumen_max) VALUES
('Utilitario', 'Camioneta o furgón pequeño', 500, 3500, 5, 15),
('Camión Liviano', 'Camión de hasta 8 toneladas', 3000, 8000, 12, 35),
('Camión Mediano', 'Camión de 8 a 16 toneladas', 8000, 16000, 30, 60),
('Camión Pesado', 'Camión de más de 16 toneladas', 16000, 30000, 55, 100),
('Semi-remolque', 'Camión con acoplado', 25000, 45000, 80, 150);

-- Insertar depósitos
INSERT INTO flotas.depositos (nombre, direccion, ciudad_id, latitud, longitud, capacidad_maxima, costo_estadia_diario) VALUES
('Depósito Central Buenos Aires', 'Av. Gral. Paz 5000, Villa Lugano', (SELECT id FROM localizaciones.ciudades WHERE nombre = 'Buenos Aires'), -34.6692, -58.4689, 100, 2500.00),
('Depósito Córdoba Norte', 'Ruta 9 Km 695', (SELECT id FROM localizaciones.ciudades WHERE nombre = 'Córdoba'), -31.3800, -64.1500, 50, 1800.00),
('Depósito Rosario Puerto', 'Puerto de Rosario, Zona Franca', (SELECT id FROM localizaciones.ciudades WHERE nombre = 'Rosario'), -32.9520, -60.6420, 75, 2000.00),
('Depósito Mendoza Centro', 'Parque Industrial Mendoza', (SELECT id FROM localizaciones.ciudades WHERE nombre = 'Mendoza'), -32.8700, -68.8200, 40, 1500.00);

-- Insertar transportistas
INSERT INTO flotas.transportistas (nombre, apellido, dni, telefono, email, licencia_conducir, fecha_vencimiento_licencia) VALUES
('Juan Carlos', 'Pérez', '12345678', '+54-11-1234-5678', 'jc.perez@email.com', 'B1234567', '2025-12-31'),
('María Elena', 'González', '23456789', '+54-11-2345-6789', 'me.gonzalez@email.com', 'B2345678', '2026-06-30'),
('Roberto', 'Martínez', '34567890', '+54-341-345-6789', 'r.martinez@email.com', 'C3456789', '2025-09-15'),
('Ana Sofía', 'López', '45678901', '+54-261-456-7890', 'as.lopez@email.com', 'C4567890', '2026-03-20'),
('Carlos Eduardo', 'Ramírez', '56789012', '+54-351-567-8901', 'ce.ramirez@email.com', 'E5678901', '2025-11-10');

-- Insertar camiones
INSERT INTO flotas.camiones (dominio, tipo_camion_id, marca, modelo, anio, capacidad_peso, capacidad_volumen, consumo_combustible, costo_km, transportista_id, deposito_actual_id) VALUES
('ABC123', (SELECT id FROM flotas.tipos_camion WHERE nombre = 'Utilitario'), 'Ford', 'Transit', 2022, 3000, 12, 12.5, 150.00, (SELECT id FROM flotas.transportistas WHERE dni = '12345678'), (SELECT id FROM flotas.depositos WHERE nombre = 'Depósito Central Buenos Aires')),
('DEF456', (SELECT id FROM flotas.tipos_camion WHERE nombre = 'Camión Liviano'), 'Mercedes-Benz', 'Accelo', 2021, 7500, 30, 18.0, 200.00, (SELECT id FROM flotas.transportistas WHERE dni = '23456789'), (SELECT id FROM flotas.depositos WHERE nombre = 'Depósito Central Buenos Aires')),
('GHI789', (SELECT id FROM flotas.tipos_camion WHERE nombre = 'Camión Mediano'), 'Volvo', 'VM', 2020, 15000, 55, 25.0, 280.00, (SELECT id FROM flotas.transportistas WHERE dni = '34567890'), (SELECT id FROM flotas.depositos WHERE nombre = 'Depósito Rosario Puerto')),
('JKL012', (SELECT id FROM flotas.tipos_camion WHERE nombre = 'Camión Pesado'), 'Scania', 'P-Series', 2023, 28000, 90, 35.0, 350.00, (SELECT id FROM flotas.transportistas WHERE dni = '45678901'), (SELECT id FROM flotas.depositos WHERE nombre = 'Depósito Mendoza Centro')),
('MNO345', (SELECT id FROM flotas.tipos_camion WHERE nombre = 'Semi-remolque'), 'Iveco', 'Stralis', 2022, 42000, 140, 45.0, 420.00, (SELECT id FROM flotas.transportistas WHERE dni = '56789012'), (SELECT id FROM flotas.depositos WHERE nombre = 'Depósito Córdoba Norte'));

-- Insertar tipos de contenedor
INSERT INTO solicitudes.tipos_contenedor (nombre, descripcion, peso_maximo, volumen_maximo, dimensiones) VALUES
('Contenedor 20 pies', 'Contenedor estándar de 20 pies', 28000, 33.2, '20x8x8.5 pies'),
('Contenedor 40 pies', 'Contenedor estándar de 40 pies', 30000, 67.7, '40x8x8.5 pies'),
('Contenedor 40 HC', 'Contenedor de 40 pies alta capacidad', 30000, 76.4, '40x8x9.5 pies'),
('Pallet Standard', 'Pallet estándar europeo', 1500, 2.4, '1.2x0.8x2.5 metros'),
('Caja Pequeña', 'Caja para productos pequeños', 50, 0.125, '0.5x0.5x0.5 metros');

-- Insertar clientes
INSERT INTO solicitudes.clientes (razon_social, cuit, email, telefono, direccion, ciudad_id) VALUES
('Distribuidora El Rápido S.A.', '30-12345678-9', 'ventas@elrapido.com.ar', '+54-11-4000-1000', 'Av. Rivadavia 1000', (SELECT id FROM localizaciones.ciudades WHERE nombre = 'Buenos Aires')),
('Logística Córdoba S.R.L.', '30-23456789-0', 'info@logisticacordoba.com', '+54-351-400-2000', 'Bv. San Juan 500', (SELECT id FROM localizaciones.ciudades WHERE nombre = 'Córdoba')),
('Transportes del Litoral', '30-34567890-1', 'operaciones@translitoral.com', '+54-341-400-3000', 'Av. Francia 1200', (SELECT id FROM localizaciones.ciudades WHERE nombre = 'Rosario')),
('Mendoza Cargas Express', '30-45678901-2', 'contacto@mendozacargas.com', '+54-261-400-4000', 'Ruta 40 Km 25', (SELECT id FROM localizaciones.ciudades WHERE nombre = 'Mendoza'));

-- Insertar tarifas base por distancia
INSERT INTO precios.tarifas_distancia (distancia_min_km, distancia_max_km, precio_por_km, fecha_vigencia_desde) VALUES
(0, 100, 180.0000, '2024-01-01'),
(101, 300, 165.0000, '2024-01-01'),
(301, 600, 155.0000, '2024-01-01'),
(601, 1000, 145.0000, '2024-01-01'),
(1001, 9999, 135.0000, '2024-01-01');

-- Insertar tarifas por tipo de camión
INSERT INTO precios.tarifas_tipo_camion (tipo_camion_id, factor_precio, precio_base_km, fecha_vigencia_desde) VALUES
((SELECT id FROM flotas.tipos_camion WHERE nombre = 'Utilitario'), 0.8000, 120.0000, '2024-01-01'),
((SELECT id FROM flotas.tipos_camion WHERE nombre = 'Camión Liviano'), 1.0000, 150.0000, '2024-01-01'),
((SELECT id FROM flotas.tipos_camion WHERE nombre = 'Camión Mediano'), 1.3000, 195.0000, '2024-01-01'),
((SELECT id FROM flotas.tipos_camion WHERE nombre = 'Camión Pesado'), 1.6000, 240.0000, '2024-01-01'),
((SELECT id FROM flotas.tipos_camion WHERE nombre = 'Semi-remolque'), 2.0000, 300.0000, '2024-01-01');

-- Insertar servicios adicionales
INSERT INTO precios.servicios_adicionales (nombre, descripcion, tipo_precio, valor, unidad) VALUES
('Seguro de Carga', 'Seguro contra robo y daños', 'PORCENTUAL', 2.5000, 'PORCENTAJE'),
('Carga y Descarga', 'Servicio de manipuleo', 'FIJO', 5000.0000, 'PESOS'),
('Estadia en Depósito', 'Costo por día en depósito', 'POR_DIA', 1500.0000, 'PESOS_POR_DIA'),
('Transporte Urgente', 'Entrega en menos de 24hs', 'PORCENTUAL', 50.0000, 'PORCENTAJE'),
('Mercadería Frágil', 'Cuidado especial para productos frágiles', 'PORCENTUAL', 15.0000, 'PORCENTAJE');

-- ================================================
-- FIN DE LA INICIALIZACIÓN
-- ================================================

