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

echo [2/4] Verificando conexion a la base de datos...
docker exec -it tpi-postgres psql -U tpi_user -d tpi_logistica -c "SELECT current_database(), current_user;"
echo.

echo [3/4] Contando tablas creadas...
docker exec -it tpi-postgres psql -U tpi_user -d tpi_logistica -c "SELECT 'public' as esquema, COUNT(*) as total_tablas FROM pg_tables WHERE schemaname = 'public';"
echo.

echo [4/4] Verificando datos iniciales...
docker exec -it tpi-postgres psql -U tpi_user -d tpi_logistica -c "SELECT 'Clientes' as tabla, COUNT(*) as registros FROM Clientes UNION ALL SELECT 'TipoUsuario', COUNT(*) FROM TipoUsuario UNION ALL SELECT 'Usuario', COUNT(*) FROM Usuario UNION ALL SELECT 'Ubicacion', COUNT(*) FROM Ubicacion UNION ALL SELECT 'Deposito', COUNT(*) FROM Deposito UNION ALL SELECT 'Transportista', COUNT(*) FROM Transportista UNION ALL SELECT 'Camion', COUNT(*) FROM Camion UNION ALL SELECT 'TipoTramo', COUNT(*) FROM TipoTramo UNION ALL SELECT 'Ruta', COUNT(*) FROM Ruta UNION ALL SELECT 'EstadoTramo', COUNT(*) FROM EstadoTramo UNION ALL SELECT 'Tramo', COUNT(*) FROM Tramo UNION ALL SELECT 'Tarifa', COUNT(*) FROM Tarifa UNION ALL SELECT 'EstadoSolicitud', COUNT(*) FROM EstadoSolicitud UNION ALL SELECT 'EstadoContenedor', COUNT(*) FROM EstadoContenedor UNION ALL SELECT 'Contenedor', COUNT(*) FROM Contenedor UNION ALL SELECT 'Solicitud', COUNT(*) FROM Solicitud UNION ALL SELECT 'HistorialEstadoContenedor', COUNT(*) FROM HistorialEstadoContenedor UNION ALL SELECT 'HistorialEstadoTramo', COUNT(*) FROM HistorialEstadoTramo ORDER BY tabla;"
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

