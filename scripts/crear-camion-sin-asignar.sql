-- Crear un camión especial para representar tramos sin asignar
-- Este script se ejecuta una sola vez para preparar la base de datos

-- Verificar si existe un modelo de camión (necesario para la FK)
DO $$
BEGIN
    -- Si no existe ningún modelo, crear uno genérico
    IF NOT EXISTS (SELECT 1 FROM public.modelo WHERE id_modelo = 1) THEN
        INSERT INTO public.modelo (id_modelo, nombre, id_marca)
        VALUES (1, 'GENERICO', 1)
        ON CONFLICT DO NOTHING;
    END IF;
END $$;

-- Crear el camión "SIN_ASG" para tramos no asignados
INSERT INTO public.camion (dominio, patente, id_modelo, anio)
VALUES ('SIN_ASG', 'SIN-ASG', 1, 2000)
ON CONFLICT (dominio) DO NOTHING;

-- Verificar que se creó correctamente
SELECT * FROM public.camion WHERE dominio = 'SIN_ASG';

