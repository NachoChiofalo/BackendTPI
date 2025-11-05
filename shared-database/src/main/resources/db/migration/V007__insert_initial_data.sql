-- ================================================
-- DATOS INICIALES - Información básica para testing
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
