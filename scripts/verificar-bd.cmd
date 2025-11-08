@echo off
REM Script para verificar el estado de la base de datos PostgreSQL
REM Sistema de Logística TPI - 2025

echo ================================================
echo Verificacion de Base de Datos PostgreSQL
echo ================================================
echo.

echo [1/4] Verificando que el contenedor este corriendo...
docker ps --filter "name=tpi-postgres" --format "table {{.Names}}\t{{.Status}}"
echo.

echo [2/4] Verificando esquemas creados...
docker exec -it tpi-postgres psql -U tpi_user -d tpi_logistica -c "\dn"
echo.

echo [3/4] Contando tablas por esquema...
docker exec -it tpi-postgres psql -U tpi_user -d tpi_logistica -c "SELECT schemaname, COUNT(*) as total_tablas FROM pg_tables WHERE schemaname IN ('localizaciones', 'flotas', 'solicitudes', 'rutas', 'precios') GROUP BY schemaname ORDER BY schemaname;"
echo.

echo [4/4] Verificando datos iniciales...
docker exec -it tpi-postgres psql -U tpi_user -d tpi_logistica -c "SELECT 'paises' as tabla, COUNT(*) as registros FROM localizaciones.paises UNION ALL SELECT 'provincias', COUNT(*) FROM localizaciones.provincias UNION ALL SELECT 'ciudades', COUNT(*) FROM localizaciones.ciudades UNION ALL SELECT 'distancias', COUNT(*) FROM localizaciones.distancias UNION ALL SELECT 'tipos_camion', COUNT(*) FROM flotas.tipos_camion UNION ALL SELECT 'depositos', COUNT(*) FROM flotas.depositos UNION ALL SELECT 'transportistas', COUNT(*) FROM flotas.transportistas UNION ALL SELECT 'camiones', COUNT(*) FROM flotas.camiones UNION ALL SELECT 'tipos_contenedor', COUNT(*) FROM solicitudes.tipos_contenedor UNION ALL SELECT 'clientes', COUNT(*) FROM solicitudes.clientes UNION ALL SELECT 'tarifas_distancia', COUNT(*) FROM precios.tarifas_distancia UNION ALL SELECT 'servicios_adicionales', COUNT(*) FROM precios.servicios_adicionales ORDER BY tabla;"
echo.

echo ================================================
echo Verificacion completada
echo ================================================
echo.
echo Datos de conexion:
echo   Host: localhost
echo   Puerto: 5432
echo   Base de datos: tpi_logistica
echo   Usuario: tpi_user
echo   Password: tpi_pass
echo.
echo Para conectarte manualmente:
echo   docker exec -it tpi-postgres psql -U tpi_user -d tpi_logistica
echo.
pause

