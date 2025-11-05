-- Crear esquemas para organizar las tablas por microservicio
CREATE SCHEMA IF NOT EXISTS solicitudes;
CREATE SCHEMA IF NOT EXISTS flotas;
CREATE SCHEMA IF NOT EXISTS rutas;
CREATE SCHEMA IF NOT EXISTS precios;
CREATE SCHEMA IF NOT EXISTS localizaciones;

-- Crear usuario para la aplicación si no existe
DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_user WHERE usename = 'tpi_user') THEN
        CREATE USER tpi_user WITH PASSWORD 'tpi_pass';
    END IF;
END
$$;

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
