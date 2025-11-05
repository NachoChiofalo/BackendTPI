@echo off
echo 🚀 Iniciando entorno de desarrollo TPI Backend con Flyway...
echo.

REM Verificar si Docker está ejecutándose
docker info >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Docker no está ejecutándose. Por favor inicia Docker Desktop.
    pause
    exit /b 1
)

echo ✅ Docker está ejecutándose
echo.

REM Cambiar al directorio de Docker
cd /d "%~dp0..\infrastructure\docker"

echo 📦 Deteniendo contenedores existentes...
docker-compose down

echo.
echo 🔧 Construyendo y levantando servicios...
docker-compose up --build -d

echo.
echo ⏳ Esperando que los servicios estén listos...
timeout /t 15 /nobreak >nul

echo.
echo ✅ Servicios iniciados:
echo    📊 PostgreSQL: localhost:5432
echo    🗃️  PgAdmin: http://localhost:8082
echo    🔄 Flyway: Migraciones ejecutadas automáticamente
echo    🔑 Keycloak: http://localhost:8080

echo.
echo 📋 Verificando estado de contenedores...
docker-compose ps

echo.
echo 🔍 Verificando logs de Flyway...
docker logs tpi-flyway

echo.
echo 🎉 ¡Entorno listo para desarrollo!
echo.
echo 📝 Próximos pasos:
echo    1. Conectar a PgAdmin: admin@tpilogistica.com / admin123
echo    2. Agregar servidor PostgreSQL: postgres:5432, usuario: tpi_user, contraseña: tpi_pass
echo    3. Verificar que existen los esquemas: flotas, solicitudes, rutas, precios, localizaciones
echo    4. Revisar que las tablas fueron creadas con datos iniciales
echo.
pause
