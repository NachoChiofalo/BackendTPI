-- Creación de esquemas para los diferentes microservicios
CREATE SCHEMA IF NOT EXISTS flotas;
CREATE SCHEMA IF NOT EXISTS solicitudes;
CREATE SCHEMA IF NOT EXISTS rutas;
CREATE SCHEMA IF NOT EXISTS precios;
CREATE SCHEMA IF NOT EXISTS localizaciones;

-- Configurar el search_path para incluir todos los esquemas
SET search_path TO public, flotas, solicitudes, rutas, precios, localizaciones;

-- Crear extensiones necesarias
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "postgis" SCHEMA public;
