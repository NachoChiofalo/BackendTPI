-- Script para agregar un depósito intermedio entre Buenos Aires y Mendoza
-- Esto permitirá que se generen rutas con múltiples tramos

-- Primero, agregar una nueva ubicación intermedia (por ejemplo, San Luis)
-- San Luis está geográficamente entre Buenos Aires y Mendoza
INSERT INTO Ubicacion (ubicacion_id, latitud, longitud, denominacion, codigo_postal, pais, provincia, partido_localidad, calle, altura, piso, depto)
VALUES (
    8,
    -33.301, -- Latitud de San Luis capital
    -66.337, -- Longitud de San Luis capital
    'San Luis Capital',
    '5700',
    'Argentina',
    'San Luis',
    'San Luis',
    'Avenida Illia',
    '500',
    NULL,
    NULL
);

-- Ahora crear un depósito en esa ubicación
INSERT INTO Deposito (deposito_id, ubicacion_id, nombre)
VALUES (6, 8, 'Depósito San Luis Centro');

-- Actualizar la secuencia de deposito_id
SELECT setval('deposito_deposito_id_seq', (SELECT MAX(deposito_id) FROM Deposito));

-- Verificar los depósitos
SELECT d.deposito_id, d.nombre, u.ubicacion_id, u.denominacion, u.latitud, u.longitud
FROM Deposito d
INNER JOIN Ubicacion u ON d.ubicacion_id = u.ubicacion_id
ORDER BY d.deposito_id;

