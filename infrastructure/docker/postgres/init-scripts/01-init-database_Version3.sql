-- ================================================
-- SCRIPT DE INICIALIZACIÓN DE BASE DE DATOS
-- Sistema de Logística - Base de Datos Compartida
-- ================================================

-- ================================================
-- CREACIÓN DE SCHEMAS
-- ================================================
CREATE SCHEMA IF NOT EXISTS localizaciones;
CREATE SCHEMA IF NOT EXISTS flotas;
CREATE SCHEMA IF NOT EXISTS precios;
CREATE SCHEMA IF NOT EXISTS rutas;
CREATE SCHEMA IF NOT EXISTS solicitudes;

-- ================================================
-- CONFIGURACIÓN DE PERMISOS PARA BASE DE DATOS COMPARTIDA
-- ================================================
-- Otorgar permisos completos al usuario para el esquema público
GRANT ALL PRIVILEGES ON SCHEMA public TO tpi_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO tpi_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO tpi_user;
GRANT ALL PRIVILEGES ON ALL FUNCTIONS IN SCHEMA public TO tpi_user;

-- Permisos por defecto para tablas futuras en el esquema público
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO tpi_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO tpi_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON FUNCTIONS TO tpi_user;

-- Otorgar permisos para los schemas de microservicios
GRANT ALL PRIVILEGES ON SCHEMA localizaciones TO tpi_user;
GRANT ALL PRIVILEGES ON SCHEMA flotas TO tpi_user;
GRANT ALL PRIVILEGES ON SCHEMA precios TO tpi_user;
GRANT ALL PRIVILEGES ON SCHEMA rutas TO tpi_user;
GRANT ALL PRIVILEGES ON SCHEMA solicitudes TO tpi_user;

-- Permisos por defecto para tablas futuras en cada schema
ALTER DEFAULT PRIVILEGES IN SCHEMA localizaciones GRANT ALL ON TABLES TO tpi_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA localizaciones GRANT ALL ON SEQUENCES TO tpi_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA localizaciones GRANT ALL ON FUNCTIONS TO tpi_user;

ALTER DEFAULT PRIVILEGES IN SCHEMA flotas GRANT ALL ON TABLES TO tpi_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA flotas GRANT ALL ON SEQUENCES TO tpi_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA flotas GRANT ALL ON FUNCTIONS TO tpi_user;

ALTER DEFAULT PRIVILEGES IN SCHEMA precios GRANT ALL ON TABLES TO tpi_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA precios GRANT ALL ON SEQUENCES TO tpi_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA precios GRANT ALL ON FUNCTIONS TO tpi_user;

ALTER DEFAULT PRIVILEGES IN SCHEMA rutas GRANT ALL ON TABLES TO tpi_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA rutas GRANT ALL ON SEQUENCES TO tpi_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA rutas GRANT ALL ON FUNCTIONS TO tpi_user;

ALTER DEFAULT PRIVILEGES IN SCHEMA solicitudes GRANT ALL ON TABLES TO tpi_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA solicitudes GRANT ALL ON SEQUENCES TO tpi_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA solicitudes GRANT ALL ON FUNCTIONS TO tpi_user;

-- ================================================
-- TABLA: Clientes
-- ================================================
CREATE TABLE Clientes (
    tipo_doc_cliente_id INTEGER NOT NULL,
    num_doc_cliente BIGINT NOT NULL,
    nombres VARCHAR(50) NOT NULL,
    apellidos VARCHAR(50) NOT NULL,
    domicilio VARCHAR(50) NOT NULL,
    telefono VARCHAR(50) NOT NULL,
    PRIMARY KEY (tipo_doc_cliente_id, num_doc_cliente)
);

-- ================================================
-- TABLA: TipoUsuario
-- ================================================
CREATE TABLE TipoUsuario (
    tipo_usuario_id INTEGER NOT NULL,
    nombre VARCHAR(50) NOT NULL,
    descripcion VARCHAR(200),
    PRIMARY KEY (tipo_usuario_id)
);

-- ================================================
-- TABLA: Usuario
-- ================================================
CREATE TABLE Usuario (
    usuario_id INTEGER NOT NULL,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(50) NOT NULL,
    id_cliente BIGINT,
    tipo_usuario_id INTEGER NOT NULL,
    transportista_id INTEGER,
    PRIMARY KEY (usuario_id)
);

-- ================================================
-- TABLA: Ubicacion
-- ================================================
CREATE TABLE Ubicacion (
    ubicacion_id INTEGER NOT NULL,
    ciudad VARCHAR(100) NOT NULL,
    direccion VARCHAR(50),
    latitud VARCHAR(50) NOT NULL,
    longitud VARCHAR(50) NOT NULL,
    nombre VARCHAR(30),
    PRIMARY KEY (ubicacion_id)
);

-- ================================================
-- TABLA: Deposito
-- ================================================
CREATE TABLE Deposito (
    deposito_id INTEGER NOT NULL,
    ubicacion_id INTEGER NOT NULL,
    nombre VARCHAR(50) NOT NULL,
    PRIMARY KEY (deposito_id)
);

-- ================================================
-- TABLA: Transportista
-- ================================================
CREATE TABLE Transportista (
    transportista_id INTEGER NOT NULL,
    nombre VARCHAR(50) NOT NULL,
    apellido VARCHAR(50) NOT NULL,
    telefono BIGINT NOT NULL,
    PRIMARY KEY (transportista_id)
);

-- ================================================
-- TABLA: Camion
-- ================================================
CREATE TABLE Camion (
    dominio VARCHAR(10) NOT NULL,
    disponible BOOLEAN NOT NULL,
    capacidad_peso DECIMAL(10,2) NOT NULL,
    capacidad_volumen DECIMAL(10,2) NOT NULL,
    costo_base_km DECIMAL(10,2) NOT NULL,
    consumo_promedio DECIMAL(10,2) NOT NULL,
    PRIMARY KEY (dominio)
);

-- ================================================
-- TABLA: TipoTramo
-- ================================================
CREATE TABLE TipoTramo (
    tipo_tramo_id INTEGER NOT NULL,
    nombre VARCHAR(50) NOT NULL,
    descripcion VARCHAR(200),
    PRIMARY KEY (tipo_tramo_id)
);

-- ================================================
-- TABLA: Ruta
-- ================================================
CREATE TABLE Ruta (
    ruta_id INTEGER NOT NULL,
    cantidad_tramos INTEGER NOT NULL,
    cantidad_depositos INTEGER NOT NULL,
    PRIMARY KEY (ruta_id)
);

-- ================================================
-- TABLA: EstadoTramo
-- ================================================
CREATE TABLE EstadoTramo (
    estado_tramo_id INTEGER NOT NULL,
    nombre VARCHAR(50) NOT NULL,
    descripcion VARCHAR(100),
    PRIMARY KEY (estado_tramo_id)
);

-- ================================================
-- TABLA: Tramo
-- ================================================
CREATE TABLE Tramo (
    tramo_id INTEGER NOT NULL,
    ruta_id INTEGER NOT NULL,
    tipo_tramo_id INTEGER NOT NULL,
    dominio VARCHAR(7) NOT NULL,
    ubicacion_origen_id INTEGER NOT NULL,
    transportista_id INTEGER NOT NULL,
    ubicacion_destino_id INTEGER NOT NULL,
    costo_aproximado DECIMAL(10,2),
    costo_real DECIMAL(10,2),
    fecha_hora_inicio DATE NOT NULL,
    fecha_hora_fin DATE,
    fecha_hora_estimada_fin DATE,
    PRIMARY KEY (tramo_id)
);

-- ================================================
-- TABLA: HistorialEstadoTramo
-- ================================================
CREATE TABLE HistorialEstadoTramo (
    historial_tramo_id INTEGER NOT NULL,
    estado_tramo_id INTEGER NOT NULL,
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE,
    tramo_id INTEGER NOT NULL,
    PRIMARY KEY (historial_tramo_id)
);

-- ================================================
-- TABLA: Tarifa
-- ================================================
CREATE TABLE Tarifa (
    tarifa_id INTEGER NOT NULL,
    precio_combustible_litro DECIMAL(10,2) NOT NULL,
    precio_km_kg DECIMAL(10,2) NOT NULL,
    precio_km_m3 DECIMAL(10,2) NOT NULL,
    fecha_vigencia_inicio DATE NOT NULL,
    fecha_vigencia_fin DATE NOT NULL,
    precio_tramo DECIMAL(10,2) NOT NULL,
    PRIMARY KEY (tarifa_id)
);


-- ================================================
-- TABLA: EstadoSolicitud
-- ================================================
CREATE TABLE EstadoSolicitud (
    id_estado_solicitud INTEGER NOT NULL,
    nombre VARCHAR(50) NOT NULL,
    texto_adicional VARCHAR(100),
    PRIMARY KEY (id_estado_solicitud)
);

-- ================================================
-- TABLA: EstadoContenedor
-- ================================================
CREATE TABLE EstadoContenedor (
    id_estado_contenedor INTEGER NOT NULL,
    nombre VARCHAR(30),
    texto_adicional VARCHAR(200),
    PRIMARY KEY (id_estado_contenedor)
);

-- ================================================
-- TABLA: Contenedor
-- ================================================
CREATE TABLE Contenedor (
    id_contenedor INTEGER NOT NULL,
    id_estado_contenedor INTEGER NOT NULL,
    volumen_m3 DECIMAL(10,2) NOT NULL,
    peso_kg DECIMAL(10,2) NOT NULL,
    PRIMARY KEY (id_contenedor)
);

-- ================================================
-- TABLA: Solicitud
-- ================================================
CREATE TABLE Solicitud (
    solicitud_id INTEGER NOT NULL,
    tipo_doc_cliente INTEGER NOT NULL,
    num_doc_cliente BIGINT NOT NULL,
    estado_solicitud INTEGER NOT NULL,
    id_contenedor INTEGER NOT NULL,
    id_ruta INTEGER NOT NULL,
    id_ubicacion_origen INTEGER NOT NULL,
    id_ubicacion_destino INTEGER NOT NULL,
    costo_estimado DECIMAL(10,2),
    costo_real DECIMAL(10,2),
    fecha_hora_fin DATE,
    fecha_hora_estimada_fin DATE,
    fecha_hora_inicio DATE,
    texto_adicional VARCHAR(100),
    PRIMARY KEY (solicitud_id)
);

-- ================================================
-- TABLA: HistorialEstadoContenedor
-- ================================================
CREATE TABLE HistorialEstadoContenedor (
    historial_contenedor_id SERIAL PRIMARY KEY,
    id_estado_contenedor INTEGER NOT NULL,
    id_contenedor INTEGER,
    fecha_inicio DATE NOT NULL
);


-- ================================================
-- CLAVES FORÁNEAS
-- ================================================

-- Usuario
ALTER TABLE Usuario
ADD CONSTRAINT fk_usuario_tipo_usuario
FOREIGN KEY (tipo_usuario_id) REFERENCES TipoUsuario(tipo_usuario_id);

ALTER TABLE Usuario
ADD CONSTRAINT fk_usuario_transportista
FOREIGN KEY (transportista_id) REFERENCES Transportista(transportista_id);

-- Deposito
ALTER TABLE Deposito
ADD CONSTRAINT fk_deposito_ubicacion
FOREIGN KEY (ubicacion_id) REFERENCES Ubicacion(ubicacion_id);

-- Contenedor
ALTER TABLE Contenedor
ADD CONSTRAINT fk_contenedor_estado
FOREIGN KEY (id_estado_contenedor) REFERENCES EstadoContenedor(id_estado_contenedor);

-- Solicitud
ALTER TABLE Solicitud
ADD CONSTRAINT fk_solicitud_cliente
FOREIGN KEY (tipo_doc_cliente, num_doc_cliente) REFERENCES Clientes(tipo_doc_cliente_id, num_doc_cliente);

ALTER TABLE Solicitud
ADD CONSTRAINT fk_solicitud_contenedor
FOREIGN KEY (id_contenedor) REFERENCES Contenedor(id_contenedor);

ALTER TABLE Solicitud
ADD CONSTRAINT fk_solicitud_ruta
FOREIGN KEY (id_ruta) REFERENCES Ruta(ruta_id);

ALTER TABLE Solicitud
ADD CONSTRAINT fk_solicitud_ubicacion_origen
FOREIGN KEY (id_ubicacion_origen) REFERENCES Ubicacion(ubicacion_id);

ALTER TABLE Solicitud
ADD CONSTRAINT fk_solicitud_ubicacion_destino
FOREIGN KEY (id_ubicacion_destino) REFERENCES Ubicacion(ubicacion_id);

ALTER TABLE Solicitud
ADD CONSTRAINT fk_solicitud_estado
FOREIGN KEY (estado_solicitud) REFERENCES EstadoSolicitud(id_estado_solicitud);

-- Tramo
ALTER TABLE Tramo
ADD CONSTRAINT fk_tramo_ruta
FOREIGN KEY (ruta_id) REFERENCES Ruta(ruta_id);

ALTER TABLE Tramo
ADD CONSTRAINT fk_tramo_tipo
FOREIGN KEY (tipo_tramo_id) REFERENCES TipoTramo(tipo_tramo_id);

ALTER TABLE Tramo
ADD CONSTRAINT fk_tramo_camion
FOREIGN KEY (dominio) REFERENCES Camion(dominio);

ALTER TABLE Tramo
ADD CONSTRAINT fk_tramo_ubicacion_origen
FOREIGN KEY (ubicacion_origen_id) REFERENCES Ubicacion(ubicacion_id);

ALTER TABLE Tramo
ADD CONSTRAINT fk_tramo_ubicacion_destino
FOREIGN KEY (ubicacion_destino_id) REFERENCES Ubicacion(ubicacion_id);

ALTER TABLE Tramo
ADD CONSTRAINT fk_tramo_transportista
FOREIGN KEY (transportista_id) REFERENCES Transportista(transportista_id);

-- Historial Estado Contenedor
ALTER TABLE HistorialEstadoContenedor
ADD CONSTRAINT fk_historial_contenedor_estado
FOREIGN KEY (id_estado_contenedor) REFERENCES EstadoContenedor(id_estado_contenedor);

ALTER TABLE HistorialEstadoContenedor
ADD CONSTRAINT fk_historial_contenedor
FOREIGN KEY (id_contenedor) REFERENCES Contenedor(id_contenedor);

-- Historial Estado Tramo
ALTER TABLE HistorialEstadoTramo
ADD CONSTRAINT fk_historial_estado_tramo
FOREIGN KEY (estado_tramo_id) REFERENCES EstadoTramo(estado_tramo_id);

ALTER TABLE HistorialEstadoTramo
ADD CONSTRAINT fk_historial_tramo
FOREIGN KEY (tramo_id) REFERENCES Tramo(tramo_id);

-- ================================================
-- INSERCIÓN DE DATOS
-- ================================================

-- Insertar TipoUsuario
INSERT INTO TipoUsuario (tipo_usuario_id, nombre, descripcion) VALUES
(1, 'Administrador', 'Usuario con permisos completos del sistema'),
(2, 'Cliente', 'Cliente que realiza solicitudes de transporte'),
(3, 'Transportista', 'Conductor de vehículos de transporte'),
(4, 'Operador', 'Usuario operativo del sistema');

-- Insertar Clientes
INSERT INTO Clientes (tipo_doc_cliente_id, num_doc_cliente, nombres, apellidos, domicilio, telefono) VALUES
(1, 12345678, 'Juan Carlos', 'Pérez González', 'Av. Corrientes 1234, CABA', '+54-11-1234-5678'),
(1, 23456789, 'María Elena', 'López Martínez', 'Calle San Martín 567, La Plata', '+54-221-2345-6789'),
(2, 30123456789, 'Empresa Logística', 'S.R.L.', 'Parque Industrial Norte, Córdoba', '+54-351-345-6789'),
(1, 34567890, 'Roberto', 'García Silva', 'Av. Belgrano 890, Rosario', '+54-341-456-7890'),
(2, 30234567890, 'Transportes del Sur', 'S.A.', 'Zona Franca, Mendoza', '+54-261-567-8901');

-- Insertar Ubicacion
INSERT INTO Ubicacion (ubicacion_id, ciudad, direccion, latitud, longitud, nombre) VALUES
(1, 'Buenos Aires', 'Av. Corrientes 1234', '-34.6037', '-58.3816', 'Centro CABA'),
(2, 'La Plata', 'Calle 7 y 50', '-34.9215', '-57.9545', 'Centro La Plata'),
(3, 'Córdoba', 'Av. Colón 1500', '-31.4201', '-64.1888', 'Centro Córdoba'),
(4, 'Rosario', 'Av. Pellegrini 2000', '-32.9442', '-60.6505', 'Centro Rosario'),
(5, 'Mendoza', 'San Martín 1100', '-32.8895', '-68.8458', 'Centro Mendoza'),
(6, 'Mar del Plata', 'Av. Constitución 5500', '-38.0055', '-57.5426', 'Puerto MDP'),
(7, 'Tucumán', 'Av. Soldati 300', '-26.8083', '-65.2176', 'Parque Industrial'),
(8, 'Salta', 'Ruta Nacional 9 Km 5', '-24.7859', '-65.4117', 'Zona Norte');

-- Insertar Deposito
INSERT INTO Deposito (deposito_id, ubicacion_id, nombre) VALUES
(1, 1, 'Depósito Central Buenos Aires'),
(2, 3, 'Depósito Córdoba Norte'),
(3, 4, 'Depósito Puerto Rosario'),
(4, 5, 'Depósito Mendoza Centro'),
(5, 6, 'Depósito Mar del Plata');

-- Insertar Transportista
INSERT INTO Transportista (transportista_id, nombre, apellido, telefono) VALUES
(1, 'Carlos Eduardo', 'Ramírez', 1145678901),
(2, 'Ana Sofía', 'Fernández', 1156789012),
(3, 'Miguel Ángel', 'González', 3514567890),
(4, 'Laura Patricia', 'Martín', 3415678901),
(5, 'Diego Alejandro', 'Vázquez', 2614567890);

-- Insertar Usuario
INSERT INTO Usuario (usuario_id, username, password, id_cliente, tipo_usuario_id, transportista_id) VALUES
(1, 'admin', 'admin123', NULL, 1, NULL),
(2, 'jperez', 'cliente123', 12345678, 2, NULL),
(3, 'mlopez', 'cliente456', 23456789, 2, NULL),
(4, 'cramirez', 'trans123', NULL, 3, 1),
(5, 'afernandez', 'trans456', NULL, 3, 2),
(6, 'operador1', 'oper123', NULL, 4, NULL);

-- Insertar TipoTramo
INSERT INTO TipoTramo (tipo_tramo_id, nombre, descripcion) VALUES
(1, 'Urbano', 'Tramo dentro de la ciudad'),
(2, 'Interurbano', 'Tramo entre ciudades cercanas'),
(3, 'Interprovincial', 'Tramo entre provincias'),
(4, 'Con Depósito', 'Tramo que incluye parada en depósito'),
(5, 'Directo', 'Tramo directo sin paradas intermedias');

-- Insertar Tarifa
INSERT INTO Tarifa (tarifa_id, precio_combustible_litro, precio_km_kg, precio_km_m3, fecha_vigencia_inicio, fecha_vigencia_fin, precio_tramo) VALUES
(1, 150.50, 2.75, 1.85, '2024-01-01', '2024-12-31', 5000.00),
(2, 155.75, 2.80, 1.90, '2024-07-01', '2024-12-31', 5200.00),
(3, 148.25, 2.70, 1.80, '2024-01-01', '2024-06-30', 4800.00);

-- Insertar Camion
INSERT INTO Camion (dominio, disponible, capacidad_peso, capacidad_volumen, costo_base_km, consumo_promedio) VALUES
('ABC123', TRUE, 8000.00, 25.50, 180.00, 12.5),
('DEF456', TRUE, 12000.00, 38.75, 220.00, 18.2),
('GHI789', FALSE, 15000.00, 45.20, 280.00, 22.8),
('JKL012', TRUE, 25000.00, 65.00, 350.00, 28.5),
('MNO345', TRUE, 6000.00, 20.30, 150.00, 10.8);

-- Insertar EstadoSolicitud
INSERT INTO EstadoSolicitud (id_estado_solicitud, nombre, texto_adicional) VALUES
(1, 'Pendiente', 'Solicitud recibida, pendiente de procesamiento'),
(2, 'En Proceso', 'Solicitud siendo procesada y asignada'),
(3, 'Asignada', 'Solicitud asignada a transportista y vehículo'),
(4, 'En Tránsito', 'Carga en movimiento hacia destino'),
(5, 'Entregada', 'Carga entregada exitosamente'),
(6, 'Cancelada', 'Solicitud cancelada por el cliente'),
(7, 'Rechazada', 'Solicitud rechazada por falta de recursos');

-- Insertar EstadoContenedor
INSERT INTO EstadoContenedor (id_estado_contenedor, nombre, texto_adicional) VALUES
(1, 'Disponible', 'Contenedor libre para nueva carga'),
(2, 'Cargando', 'Contenedor en proceso de carga'),
(3, 'Cargado', 'Contenedor completamente cargado'),
(4, 'En Tránsito', 'Contenedor en movimiento'),
(5, 'Descargando', 'Contenedor en proceso de descarga'),
(6, 'Mantenimiento', 'Contenedor fuera de servicio por mantenimiento');

-- Insertar Contenedor
INSERT INTO Contenedor (id_contenedor, id_estado_contenedor, volumen_m3, peso_kg) VALUES
(1, 1, 15.50, 2500.00),
(2, 3, 22.75, 4800.00),
(3, 4, 18.30, 3200.00),
(4, 1, 28.60, 1800.00),
(5, 2, 12.45, 5500.00);

-- Insertar Ruta
INSERT INTO Ruta (ruta_id, cantidad_tramos, cantidad_depositos) VALUES
(1, 2, 1),
(2, 3, 2),
(3, 1, 0),
(4, 4, 3),
(5, 2, 1);

-- Insertar Solicitud
INSERT INTO Solicitud (solicitud_id, tipo_doc_cliente, num_doc_cliente, estado_solicitud, id_contenedor, id_ruta, id_ubicacion_origen, id_ubicacion_destino, costo_estimado, costo_real, fecha_hora_fin, fecha_hora_estimada_fin, fecha_hora_inicio, texto_adicional) VALUES
(1, 1, 12345678, 3, 1, 1, 1, 3, 15000.00, NULL, NULL, '2024-11-15', '2024-11-10', 'Mercadería frágil - manipular con cuidado'),
(2, 2, 30123456789, 4, 2, 2, 3, 5, 22500.00, 21800.00, NULL, '2024-11-12', '2024-11-08', 'Carga industrial pesada'),
(3, 1, 23456789, 5, 3, 3, 2, 4, 8500.00, 8200.00, '2024-11-05', '2024-11-05', '2024-11-03', 'Entrega urgente completada'),
(4, 1, 34567890, 1, 4, 4, 4, 7, 28000.00, NULL, NULL, '2024-11-20', NULL, 'Solicitud de transporte interprovincial'),
(5, 2, 30234567890, 2, 5, 5, 5, 6, 12000.00, NULL, NULL, '2024-11-18', '2024-11-11', 'Productos perecederos - refrigeración requerida');

-- Insertar EstadoTramo
INSERT INTO EstadoTramo (estado_tramo_id, nombre, descripcion) VALUES
(1, 'Planificado', 'Tramo planificado pero no iniciado'),
(2, 'En Curso', 'Tramo actualmente en ejecución'),
(3, 'Completado', 'Tramo finalizado exitosamente'),
(4, 'Pausado', 'Tramo temporalmente pausado'),
(5, 'Cancelado', 'Tramo cancelado'),
(6, 'Con Demora', 'Tramo con retraso respecto a lo planificado');

-- Insertar Tramo
INSERT INTO Tramo (tramo_id, ruta_id, tipo_tramo_id, dominio, ubicacion_origen_id, transportista_id, ubicacion_destino_id, costo_aproximado, costo_real, fecha_hora_inicio, fecha_hora_fin, fecha_hora_estimada_fin) VALUES
(1, 1, 3, 'ABC123', 1, 1, 2, 8000.00, 7800.00, '2024-11-10', '2024-11-10', '2024-11-10'),
(2, 1, 3, 'ABC123', 2, 1, 3, 7000.00, NULL, '2024-11-11', NULL, '2024-11-11'),
(3, 2, 4, 'DEF456', 3, 2, 1, 12000.00, 11500.00, '2024-11-08', '2024-11-09', '2024-11-09'),
(4, 2, 2, 'DEF456', 1, 2, 4, 6500.00, NULL, '2024-11-10', NULL, '2024-11-10'),
(5, 2, 3, 'DEF456', 4, 2, 5, 8000.00, NULL, '2024-11-11', NULL, '2024-11-12'),
(6, 3, 5, 'MNO345', 2, 5, 4, 8500.00, 8200.00, '2024-11-03', '2024-11-05', '2024-11-05'),
(7, 4, 1, 'JKL012', 4, 4, 6, 15000.00, NULL, '2024-11-15', NULL, '2024-11-16'),
(8, 5, 2, 'GHI789', 5, 3, 6, 12000.00, NULL, '2024-11-11', NULL, '2024-11-13');

-- Insertar HistorialEstadoContenedor
INSERT INTO HistorialEstadoContenedor (id_estado_contenedor, id_contenedor, fecha_inicio) VALUES
(1, 1, '2024-11-01'),
(2, 2, '2024-11-08'),
(3, 2, '2024-11-08'),
(4, 3, '2024-11-03'),
(1, 4, '2024-11-01'),
(2, 5, '2024-11-11');

-- Insertar HistorialEstadoTramo
INSERT INTO HistorialEstadoTramo (historial_tramo_id, estado_tramo_id, fecha_inicio, fecha_fin, tramo_id) VALUES
(1, 1, '2024-11-09', '2024-11-10', 1),
(2, 2, '2024-11-10', '2024-11-10', 1),
(3, 3, '2024-11-10', NULL, 1),
(4, 1, '2024-11-10', '2024-11-11', 2),
(5, 2, '2024-11-11', NULL, 2),
(6, 3, '2024-11-03', '2024-11-05', 6),
(7, 1, '2024-11-08', '2024-11-08', 3),
(8, 2, '2024-11-08', '2024-11-09', 3),
(9, 3, '2024-11-09', NULL, 3);

-- ================================================
-- ÍNDICES PARA OPTIMIZACIÓN
-- ================================================

-- Índices en claves foráneas
CREATE INDEX idx_usuario_tipo ON Usuario(tipo_usuario_id);
CREATE INDEX idx_usuario_transportista ON Usuario(transportista_id);
CREATE INDEX idx_deposito_ubicacion ON Deposito(ubicacion_id);
CREATE INDEX idx_solicitud_cliente ON Solicitud(tipo_doc_cliente, num_doc_cliente);
CREATE INDEX idx_solicitud_contenedor ON Solicitud(id_contenedor);
CREATE INDEX idx_solicitud_ruta ON Solicitud(id_ruta);
CREATE INDEX idx_solicitud_estado ON Solicitud(estado_solicitud);
CREATE INDEX idx_contenedor_estado ON Contenedor(id_estado_contenedor);
CREATE INDEX idx_tramo_ruta ON Tramo(ruta_id);
CREATE INDEX idx_tramo_camion ON Tramo(dominio);
CREATE INDEX idx_tramo_transportista ON Tramo(transportista_id);
CREATE INDEX idx_historial_tramo ON HistorialEstadoTramo(tramo_id);

-- ================================================
-- FIN DEL SCRIPT
-- ================================================