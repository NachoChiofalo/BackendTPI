-- Modificar la columna dominio para permitir NULL
-- Esto permitirá crear tramos sin camión asignado inicialmente

-- Paso 1: Eliminar la constraint de FK si existe
ALTER TABLE public.tramo DROP CONSTRAINT IF EXISTS fk_tramo_camion;

-- Paso 2: Modificar la columna para permitir NULL
ALTER TABLE public.tramo ALTER COLUMN dominio DROP NOT NULL;

-- Paso 3: Recrear la constraint de FK pero permitiendo NULL
ALTER TABLE public.tramo
ADD CONSTRAINT fk_tramo_camion
FOREIGN KEY (dominio) REFERENCES public.camion(dominio)
ON DELETE SET NULL
ON UPDATE CASCADE;

-- Verificar el cambio
SELECT column_name, is_nullable, data_type
FROM information_schema.columns
WHERE table_name = 'tramo' AND column_name = 'dominio';

