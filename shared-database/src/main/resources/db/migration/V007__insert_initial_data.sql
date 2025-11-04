-- Datos iniciales para el sistema
-- Ciudades principales de Argentina
INSERT INTO localizaciones.ciudades (nombre, provincia, pais, latitud, longitud, codigo_postal) VALUES
('Buenos Aires', 'Buenos Aires', 'Argentina', -34.6118, -58.3960, 'C1000'),
('Córdoba', 'Córdoba', 'Argentina', -31.4201, -64.1888, 'X5000'),
('Rosario', 'Santa Fe', 'Argentina', -32.9442, -60.6505, 'S2000'),
('Mendoza', 'Mendoza', 'Argentina', -32.8908, -68.8272, 'M5500'),
('Tucumán', 'Tucumán', 'Argentina', -26.8241, -65.2226, 'T4000'),
('La Plata', 'Buenos Aires', 'Argentina', -34.9214, -57.9544, 'B1900'),
('Mar del Plata', 'Buenos Aires', 'Argentina', -38.0023, -57.5575, 'B7600'),
('Salta', 'Salta', 'Argentina', -24.7821, -65.4232, 'A4400'),
('Santa Fe', 'Santa Fe', 'Argentina', -31.6333, -60.7000, 'S3000'),
('San Juan', 'San Juan', 'Argentina', -31.5375, -68.5364, 'J5400'),
('Neuquén', 'Neuquén', 'Argentina', -38.9516, -68.0591, 'Q8300'),
('Bahía Blanca', 'Buenos Aires', 'Argentina', -38.7183, -62.2663, 'B8000'),
('Resistencia', 'Chaco', 'Argentina', -27.4514, -58.9867, 'H3500'),
('Posadas', 'Misiones', 'Argentina', -27.3621, -55.8969, 'N3300'),
('San Luis', 'San Luis', 'Argentina', -33.2950, -66.3356, 'D5700');

-- Depósitos principales
INSERT INTO localizaciones.depositos (nombre, direccion, ciudad_id, latitud, longitud, capacidad_maxima, horario_apertura, horario_cierre, telefono, email) VALUES
('Depósito Central Buenos Aires', 'Av. del Puerto 1234, Puerto Madero', 1, -34.6200, -58.3700, 15000.00, '06:00', '22:00', '+541143211234', 'buenosaires@tpilogistica.com'),
('Depósito Córdoba Norte', 'Ruta Nacional 9 Km 15', 2, -31.4000, -64.1700, 10000.00, '07:00', '20:00', '+543514567890', 'cordoba@tpilogistica.com'),
('Depósito Rosario Sur', 'Circunvalación 2500', 3, -32.9600, -60.6300, 12000.00, '06:30', '21:30', '+543414567890', 'rosario@tpilogistica.com'),
('Depósito Mendoza Centro', 'Acceso Este 890', 4, -32.8800, -68.8100, 8000.00, '07:00', '19:00', '+542614567890', 'mendoza@tpilogistica.com'),
('Depósito Tucumán', 'Av. Circunvalación 1500', 5, -26.8100, -65.2100, 6000.00, '07:30', '18:30', '+543814567890', 'tucuman@tpilogistica.com');

-- Transportistas de ejemplo
INSERT INTO flotas.transportistas (username, email, nombre, apellido, telefono, licencia_conducir, categoria_licencia, fecha_vencimiento_licencia, fecha_ingreso) VALUES
('jperez', 'juan.perez@email.com', 'Juan Carlos', 'Pérez', '+54911234567', 'BA123456789', 'A2', '2025-12-31', '2023-01-15'),
('mgonzalez', 'maria.gonzalez@email.com', 'María Elena', 'González', '+54911234568', 'CB987654321', 'A3', '2026-06-30', '2023-03-20'),
('crojas', 'carlos.rojas@email.com', 'Carlos Alberto', 'Rojas', '+54911234569', 'SF456789123', 'A2', '2025-09-15', '2023-02-10'),
('lmartinez', 'luis.martinez@email.com', 'Luis Fernando', 'Martínez', '+54911234570', 'MD789123456', 'A3', '2026-03-20', '2023-04-05'),
('asanchez', 'ana.sanchez@email.com', 'Ana Patricia', 'Sánchez', '+54911234571', 'TM654321987', 'A2', '2025-11-10', '2023-05-12');

-- Camiones de ejemplo
INSERT INTO flotas.camiones (dominio, marca, modelo, año_fabricacion, tipo_camion, capacidad_peso, capacidad_volumen, consumo_combustible, costo_base_km, costo_mantenimiento_diario, transportista_id, deposito_base_id, numero_seguro, fecha_vencimiento_seguro, fecha_ultimo_service, kilometraje_actual) VALUES
('ABC123', 'Mercedes-Benz', 'Atego 1719', 2020, 'RIGIDO_MEDIANO', 8.50, 35.00, 0.35, 12.50, 150.00, 1, 1, 'SEG001234567', '2025-08-15', '2024-10-15', 45000),
('DEF456', 'Scania', 'R450', 2019, 'SEMI_REMOLQUE', 25.00, 85.00, 0.42, 18.75, 200.00, 2, 2, 'SEG001234568', '2025-11-20', '2024-09-20', 67000),
('GHI789', 'Volvo', 'FH460', 2021, 'CAMION_REMOLQUE', 30.00, 95.00, 0.45, 22.00, 250.00, 3, 3, 'SEG001234569', '2026-01-30', '2024-11-01', 32000),
('JKL012', 'Ford', 'Cargo 1722', 2018, 'RIGIDO_PEQUEÑO', 5.50, 25.00, 0.28, 10.00, 120.00, 4, 4, 'SEG001234570', '2025-07-10', '2024-08-10', 78000),
('MNO345', 'Iveco', 'Stralis 570', 2022, 'SEMI_REMOLQUE', 28.00, 90.00, 0.40, 20.50, 220.00, 5, 5, 'SEG001234571', '2026-04-25', '2024-10-25', 15000);

-- Rutas principales entre ciudades
INSERT INTO rutas.rutas (ciudad_origen_id, ciudad_destino_id, distancia_km, tiempo_estimado_horas, dificultad, peajes_cantidad, costo_peajes) VALUES
(1, 2, 695, 8.5, 'MEDIA', 3, 2500.00),  -- Buenos Aires - Córdoba
(1, 3, 305, 3.5, 'BAJA', 2, 1200.00),   -- Buenos Aires - Rosario
(1, 4, 1037, 12.0, 'MEDIA', 4, 3800.00), -- Buenos Aires - Mendoza
(2, 3, 395, 4.5, 'BAJA', 1, 800.00),    -- Córdoba - Rosario
(2, 4, 520, 6.0, 'MEDIA', 2, 1500.00),  -- Córdoba - Mendoza
(3, 1, 305, 3.5, 'BAJA', 2, 1200.00),   -- Rosario - Buenos Aires
(4, 1, 1037, 12.0, 'MEDIA', 4, 3800.00), -- Mendoza - Buenos Aires
(1, 5, 1311, 15.0, 'ALTA', 5, 4500.00), -- Buenos Aires - Tucumán
(1, 6, 56, 1.0, 'BAJA', 0, 0.00),       -- Buenos Aires - La Plata
(1, 7, 404, 5.0, 'BAJA', 1, 600.00);    -- Buenos Aires - Mar del Plata

-- Tarifas base por tipo de camión
INSERT INTO precios.tarifas_base (tipo_camion, precio_base_km, factor_peso, factor_volumen, factor_distancia, precio_minimo, precio_maximo) VALUES
('RIGIDO_PEQUEÑO', 8.50, 1.0, 1.0, 1.0, 2500.00, 50000.00),
('RIGIDO_MEDIANO', 12.50, 1.2, 1.1, 1.0, 3500.00, 75000.00),
('RIGIDO_GRANDE', 16.75, 1.4, 1.2, 1.0, 4500.00, 100000.00),
('SEMI_REMOLQUE', 18.75, 1.5, 1.3, 0.95, 5000.00, 150000.00),
('CAMION_REMOLQUE', 22.00, 1.6, 1.4, 0.90, 6000.00, 200000.00),
('ESPECIAL', 25.00, 1.8, 1.5, 1.2, 8000.00, 300000.00);

-- Factores de ajuste dinámicos
INSERT INTO precios.factores_ajuste (nombre, descripcion, tipo_factor, factor_multiplicador, aplicable_tipos_camion) VALUES
('Combustible Alto', 'Ajuste por precio elevado del combustible', 'COMBUSTIBLE', 1.15, 'TODOS'),
('Demanda Alta', 'Recargo por alta demanda en la ruta', 'DEMANDA', 1.25, 'TODOS'),
('Temporada Baja', 'Descuento por temporada de baja demanda', 'TEMPORADA', 0.90, 'TODOS'),
('Entrega Urgente', 'Recargo por solicitud de entrega urgente', 'URGENCIA', 1.50, 'TODOS'),
('Carga Asegurada', 'Recargo por seguro adicional', 'SEGURO', 1.10, 'TODOS'),
('Ruta con Peajes', 'Ajuste por rutas con múltiples peajes', 'PEAJES', 1.08, 'TODOS');

-- Promociones activas
INSERT INTO precios.promociones (codigo_promocion, nombre, descripcion, tipo_descuento, valor_descuento, descuento_maximo, peso_minimo_kg, monto_minimo, usos_maximos, fecha_inicio, fecha_fin) VALUES
('NUEVO2025', 'Descuento Cliente Nuevo', '20% de descuento para clientes nuevos', 'PORCENTAJE', 20.00, 10000.00, NULL, 5000.00, 100, '2025-01-01', '2025-12-31'),
('VOLUMEN500', 'Descuento por Volumen', 'Descuento para cargas superiores a 500kg', 'PORCENTAJE', 15.00, 15000.00, 500.00, NULL, NULL, '2025-01-01', '2025-12-31'),
('ENVIOGRATIS', 'Envío Gratis Premium', 'Envío gratuito para compras mayores a $50000', 'ENVIO_GRATIS', 0.00, NULL, NULL, 50000.00, 50, '2025-01-01', '2025-06-30');
