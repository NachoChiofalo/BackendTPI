# 🏗️ Estructura del Proyecto TPI Backend Logística 2025

## 📋 Información del Proyecto
- **Materia:** Backend de Aplicaciones 2025
- **Tipo:** Trabajo Práctico Integrador (TPI)
- **Arquitectura:** Microservicios con Base de Datos Compartida
- **Tecnologías:** Java + Spring Boot + PostgreSQL + Keycloak + Docker

---

## 📁 Estructura Completa del Proyecto

```
tpi-backend-logistica-2025/
├── 📁 api-gateway/                    # Spring Cloud Gateway
│   ├── 📁 src/main/java/
│   │   └── 📁 com/tpi/gateway/
│   │       ├── 📁 config/             # Configuración de rutas y filtros
│   │       │   ├── GatewayConfig.java
│   │       │   └── CorsConfig.java
│   │       ├── 📁 filter/             # Filtros JWT y logging
│   │       │   ├── JwtAuthenticationFilter.java
│   │       │   └── LoggingFilter.java
│   │       ├── 📁 security/           # Configuración Keycloak
│   │       │   └── SecurityConfig.java
│   │       └── GatewayApplication.java
│   ├── 📁 src/main/resources/
│   │   ├── application.yml
│   │   ├── application-dev.yml
│   │   └── application-prod.yml
│   ├── Dockerfile
│   └── pom.xml
│
├── 📁 ms-solicitudes/                 # Microservicio de Solicitudes
│   ├── 📁 src/main/java/
│   │   └── 📁 com/tpi/solicitudes/
│   │       ├── 📁 controller/         
│   │       │   ├── SolicitudController.java
│   │       │   ├── ClienteController.java
│   │       │   └── ContenedorController.java
│   │       ├── 📁 service/            
│   │       │   ├── SolicitudService.java
│   │       │   ├── ClienteService.java
│   │       │   └── ContenedorService.java
│   │       ├── 📁 repository/         
│   │       │   ├── SolicitudRepository.java
│   │       │   ├── ClienteRepository.java
│   │       │   └── ContenedorRepository.java
│   │       ├── 📁 entity/             # Entidades JPA
│   │       │   ├── Solicitud.java
│   │       │   ├── Cliente.java
│   │       │   └── Contenedor.java
│   │       ├── 📁 dto/                
│   │       │   ├── 📁 request/
│   │       │   │   ├── SolicitudRequest.java
│   │       │   │   └── ClienteRequest.java
│   │       │   ├── 📁 response/
│   │       │   │   ├── SolicitudResponse.java
│   │       │   │   └── EstadoSeguimientoResponse.java
│   │       │   └── 📁 mapper/
│   │       │       └── SolicitudMapper.java
│   │       ├── 📁 config/             
│   │       │   ├── DatabaseConfig.java    # ⚠️ MISMO datasource
│   │       │   ├── SecurityConfig.java
│   │       │   └── SwaggerConfig.java
│   │       ├── 📁 exception/          
│   │       │   └── SolicitudNotFoundException.java
│   │       └── 📁 client/             # Comunicación con otros MS
│   │           ├── PreciosClient.java
│   │           └── RutasClient.java
│   ├── 📁 src/main/resources/
│   │   ├── application.yml            # ⚠️ MISMA BD configurada
│   │   └── logback-spring.xml
│   ├── 📁 src/test/
│   ├── Dockerfile
│   └── pom.xml
│
├── 📁 ms-flotas/                      # Microservicio de Flotas
│   ├── 📁 src/main/java/
│   │   └── 📁 com/tpi/flotas/
│   │       ├── 📁 controller/
│   │       │   ├── CamionController.java
│   │       │   ├── TransportistaController.java
│   │       │   └── DepositoController.java
│   │       ├── 📁 service/
│   │       │   ├── CamionService.java
│   │       │   ├── TransportistaService.java
│   │       │   └── DepositoService.java
│   │       ├── 📁 repository/
│   │       │   ├── CamionRepository.java
│   │       │   ├── TransportistaRepository.java
│   │       │   └── DepositoRepository.java
│   │       ├── 📁 entity/             # ⚠️ TODAS en el mismo esquema
│   │       │   ├── Camion.java
│   │       │   ├── Transportista.java
│   │       │   └── Deposito.java
│   │       ├── 📁 dto/
│   │       ├── 📁 config/
│   │       │   └── DatabaseConfig.java    # ⚠️ MISMO datasource
│   │       ├── 📁 exception/
│   │       └── 📁 client/
│   │           └── RutasClient.java
│   ├── 📁 src/main/resources/
│   │   ├── application.yml            # ⚠️ MISMA BD configurada
│   │   └── logback-spring.xml
│   ├── 📁 src/test/
│   ├── Dockerfile
│   └── pom.xml
│
├── 📁 ms-rutas/                       # Microservicio de Rutas
│   ├── 📁 src/main/java/
│   │   └── 📁 com/tpi/rutas/
│   │       ├── 📁 controller/
│   │       │   ├── RutaController.java
│   │       │   └── TramoController.java
│   │       ├── 📁 service/
│   │       │   ├── RutaService.java
│   │       │   ├── TramoService.java
│   │       │   └── OptimizacionRutaService.java  # ✅ Lógica compleja aquí
│   │       ├── 📁 repository/
│   │       │   ├── RutaRepository.java
│   │       │   └── TramoRepository.java
│   │       ├── 📁 entity/             # ⚠️ Acceso directo a otras entidades
│   │       │   ├── Ruta.java
│   │       │   ├── Tramo.java
│   │       │   ├── Solicitud.java     # ✅ Puede hacer JOIN directo
│   │       │   └── Camion.java        # ✅ Puede hacer JOIN directo
│   │       ├── 📁 dto/
│   │       ├── 📁 config/
│   │       │   └── DatabaseConfig.java    # ⚠️ MISMO datasource
│   │       ├── 📁 exception/
│   │       └── 📁 client/
│   │           ├── PreciosClient.java
│   │           └── LocalizacionesClient.java
│   ├── 📁 src/main/resources/
│   │   ├── application.yml            # ⚠️ MISMA BD configurada
│   │   └── logback-spring.xml
│   ├── 📁 src/test/
│   ├── Dockerfile
│   └── pom.xml
│
├── 📁 ms-precios/                     # Microservicio de Precios
│   ├── 📁 src/main/java/
│   │   └── 📁 com/tpi/precios/
│   │       ├── 📁 controller/
│   │       │   ├── TarifaController.java
│   │       │   └── CalculoPrecioController.java
│   │       ├── 📁 service/
│   │       │   ├── TarifaService.java
│   │       │   ├── CalculoPrecioService.java   # ✅ Cálculos complejos
│   │       │   └── EstimacionService.java
│   │       ├── 📁 repository/
│   │       │   ├── TarifaRepository.java
│   │       │   └── CalculoPrecioRepository.java
│   │       ├── 📁 entity/             # ⚠️ Acceso a entidades de otros MS
│   │       │   ├── Tarifa.java
│   │       │   ├── CalculoPrecio.java
│   │       │   ├── Solicitud.java     # ✅ Para cálculos directos
│   │       │   ├── Ruta.java          # ✅ Para distancias
│   │       │   └── Camion.java        # ✅ Para costos específicos
│   │       ├── 📁 dto/
│   │       ├── 📁 config/
│   │       │   └── DatabaseConfig.java    # ⚠️ MISMO datasource
│   │       ├── 📁 exception/
│   │       └── 📁 utils/              
│   │           └── CalculadoraPrecios.java
│   ├── 📁 src/main/resources/
│   │   ├── application.yml            # ⚠️ MISMA BD configurada
│   │   └── logback-spring.xml
│   ├── 📁 src/test/
│   ├── Dockerfile
│   └── pom.xml
│
├── 📁 ms-localizaciones/              # Microservicio de Localizaciones
│   ├── 📁 src/main/java/
│   │   └── 📁 com/tpi/localizaciones/
│   │       ├── 📁 controller/
│   │       │   ├── CiudadController.java
│   │       │   ├── UbicacionController.java
│   │       │   └── DistanciaController.java
│   │       ├── 📁 service/
│   │       │   ├── CiudadService.java
│   │       │   ├── UbicacionService.java
│   │       │   └── GoogleMapsService.java    # ✅ Integración externa
│   │       ├── 📁 repository/
│   │       │   ├── CiudadRepository.java
│   │       │   ├── UbicacionRepository.java
│   │       │   └── DistanciaRepository.java  # ✅ Cache de distancias
│   │       ├── 📁 entity/
│   │       │   ├── Ciudad.java
│   │       │   ├── Ubicacion.java
│   │       │   └── DistanciaCalculada.java   # ✅ Para cache
│   │       ├── 📁 dto/
│   │       ├── 📁 config/
│   │       │   ├── DatabaseConfig.java       # ⚠️ MISMO datasource
│   │       │   ├── GoogleMapsConfig.java
│   │       │   └── CacheConfig.java
│   │       ├── 📁 exception/
│   │       └── 📁 integration/        
│   │           └── 📁 googlemaps/
│   │               ├── GoogleMapsClient.java
│   │               └── DistanceMatrixService.java
│   ├── 📁 src/main/resources/
│   │   ├── application.yml            # ⚠️ MISMA BD configurada
│   │   └── logback-spring.xml
│   ├── 📁 src/test/
│   ├── Dockerfile
│   └── pom.xml
│
├── 📁 shared-database/                # ⚠️ NUEVO: Gestión centralizada de BD
│   ├── 📁 src/main/resources/
│   │   ├── 📁 db/migration/           # ✅ Scripts Flyway centralizados
│   │   │   ├── V001__create_schemas.sql
│   │   │   ├── V002__create_solicitudes_tables.sql
│   │   │   ├── V003__create_flotas_tables.sql
│   │   │   ├── V004__create_rutas_tables.sql
│   │   │   ├── V005__create_precios_tables.sql
│   │   │   ├── V006__create_localizaciones_tables.sql
│   │   │   ├── V007__create_foreign_keys.sql
│   │   │   └── V008__insert_seed_data.sql
│   │   └── 📁 data/                   # Datos de prueba
│   │       ├── ciudades.sql
│   │       ├── depositos.sql
│   │       └── tarifas-iniciales.sql
│   └── README.md                      # Documentación del modelo
│
├── 📁 shared-libs/                    # Librerías compartidas (REDUCIDAS)
│   ├── 📁 common-dto/                 # DTOs para comunicación entre MS
│   │   ├── 📁 src/main/java/
│   │   │   └── 📁 com/tpi/shared/dto/
│   │   │       ├── 📁 internal/       # ✅ Para comunicación interna
│   │   │       │   ├── SolicitudDto.java
│   │   │       │   ├── RutaDto.java
│   │   │       │   └── PrecioDto.java
│   │   │       └── 📁 external/       # ✅ Para clientes
│   │   │           └── SeguimientoDto.java
│   │   └── pom.xml
│   ├── 📁 security-common/            
│   │   ├── 📁 src/main/java/
│   │   │   └── 📁 com/tpi/shared/security/
│   │   │       ├── JwtAuthenticationFilter.java
│   │   │       ├── SecurityUtils.java
│   │   │       └── KeycloakConfig.java
│   │   └── pom.xml
│   └── 📁 database-common/            # ⚠️ NUEVO: Config BD compartida
│       ├── 📁 src/main/java/
│       │   └── 📁 com/tpi/shared/database/
│       │       ├── DatabaseConfiguration.java
│       │       ├── TransactionManagerConfig.java
│       │       └── JpaConfig.java
│       └── pom.xml
│
├── 📁 infrastructure/                 # Infraestructura SIMPLIFICADA
│   ├── 📁 docker/
│   │   ├── 📁 keycloak/
│   │   │   ├── Dockerfile
│   │   │   └── 📁 config/
│   │   ├── 📁 postgres/               # ✅ UNA SOLA BD
│   │   │   ├── Dockerfile
│   │   │   └── 📁 init-scripts/
│   │   │       ├── 01-create-database.sql
│   │   │       ├── 02-create-users.sql
│   │   │       └── 03-grant-permissions.sql
│   │   └── 📁 nginx/
│   │       └── nginx.conf
│   ├── docker-compose.yml             # ✅ Setup simplificado
│   ├── docker-compose.dev.yml
│   └── docker-compose.prod.yml
│
├── 📁 docs/                           # Documentación ACTUALIZADA
│   ├── 📁 api/                        # Documentación de APIs
│   │   ├── solicitudes-api.md
│   │   ├── flotas-api.md
│   │   ├── rutas-api.md
│   │   ├── precios-api.md
│   │   └── localizaciones-api.md
│   ├── 📁 architecture/               # Documentación de arquitectura
│   │   ├── microservices-shared-db.md  # ✅ Justificación de BD compartida
│   │   ├── database-design.md
│   │   ├── security-design.md
│   │   └── transaction-management.md   # ✅ Manejo de transacciones
│   ├── 📁 deployment/                 # Guías de despliegue
│   │   ├── local-setup.md
│   │   └── production-setup.md
│   └── 📁 testing/                    # Documentación de pruebas
│       ├── integration-tests.md
│       └── load-tests.md
│
├── 📁 tests/                          # Tests SIMPLIFICADOS
│   ├── 📁 postman/
│   │   ├── TPI-Backend-Collection.json
│   │   ├── TPI-Backend-Environment.json
│   │   └── 📁 test-data/
│   ├── 📁 k6/                         # Pruebas de carga
│   │   └── load-test-scripts/
│   └── 📁 integration/                # ✅ Tests E2E más fáciles
│       └── 📁 src/test/java/
│           └── com/tpi/integration/
│               ├── SolicitudCompleteFlowTest.java
│               └── RutaAsignacionTest.java
│
├── 📁 scripts/                        # Scripts de utilidad
│   ├── start-dev.sh
│   ├── stop-dev.sh
│   ├── setup-database.sh              # ✅ Setup BD única
│   ├── setup-keycloak.sh
│   ├── migrate-db.sh
│   └── 📁 sql/                        # Scripts SQL iniciales
│       ├── create-database.sql        # ✅ Una sola BD
│       └── seed-data.sql
│
├── 📁 config/                         # Configuraciones globales
│   ├── 📁 keycloak/
│   │   ├── realm-export.json
│   │   └── client-configs/
│   ├── 📁 database/                   # ✅ Config BD centralizada
│   │   ├── database-dev.yml
│   │   ├── database-prod.yml
│   │   └── connection-pool.yml
│   ├── 📁 monitoring/                 # Configuración de monitoreo
│   │   ├── prometheus.yml
│   │   └── grafana/
│   └── 📁 logging/
│       └── logback-common.xml
│
├── .gitignore
├── README.md
├── CONTRIBUTING.md
├── LICENSE
├── ESTRUCTURA_PROYECTO.md             # ✅ Este archivo
├── pom.xml                            # ✅ Gestión unificada
└── 📁 .github/                        # GitHub Actions
    └── 📁 workflows/
        ├── ci-cd.yml
        ├── build-test.yml
        └── deploy-dev.yml
```

---

## 🔑 Características Clave de esta Estructura

### ✅ **Ventajas de BD Compartida**

1. **📊 Transacciones Simples**
   - Todas las operaciones en una sola BD
   - ACID garantizado nativamente
   - No necesitas Saga Pattern

2. **🔍 Queries Complejas Permitidas**
   - JOINs directos entre tablas
   - Reportes agregados fáciles
   - Consultas optimizadas

3. **🛠️ Setup Simplificado**
   - Una sola BD para configurar
   - Una sola migración Flyway
   - Una sola conexión por MS

4. **🧪 Testing Más Fácil**
   - Tests de integración simples
   - Datos consistentes
   - Rollback automático en tests

### ⚠️ **Consideraciones Importantes**

1. **🔒 Aislamiento Lógico**
   - Cada MS solo accede a "sus" tablas
   - Usa prefijos en tablas (ej: `sol_`, `flo_`, `rut_`)
   - Documenta qué MS es "owner" de cada tabla

2. **📝 Configuración BD por MS**
   ```yaml
   # Mismo datasource en todos los application.yml
   spring:
     datasource:
       url: jdbc:postgresql://localhost:5432/tpi_logistica
       username: tpi_user
       password: tpi_pass
   ```

3. **🎯 Disciplina de Desarrollo**
   - Cada MS solo modifica "sus" entidades
   - Comunicación vía API para datos de otros MS
   - Documenta dependencias claramente

---

## 🚀 **Pasos de Implementación Sugeridos**

### **Fase 1: Setup Inicial**
1. Crear estructura de carpetas base
2. Configurar BD PostgreSQL única
3. Setup básico de cada microservicio
4. Configurar API Gateway

### **Fase 2: Implementación Core**
1. Implementar entidades y repositorios
2. Desarrollar servicios básicos
3. Crear controllers con endpoints
4. Configurar comunicación entre MS

### **Fase 3: Integraciones**
1. Integrar con Keycloak para seguridad
2. Implementar Google Maps API
3. Configurar logging y monitoreo
4. Crear tests de integración

### **Fase 4: Finalización**
1. Documentar APIs con Swagger
2. Crear colección de pruebas Postman
3. Configurar Docker Compose
4. Documentación final

---

## 📚 **Tecnologías y Dependencias**

### **Core Technologies**
- **Java 17+**
- **Spring Boot 3.x**
- **Spring Cloud Gateway**
- **PostgreSQL 15+**
- **Keycloak**
- **Docker & Docker Compose**

### **Additional Libraries**
- **Spring Data JPA**
- **Flyway** (migraciones BD)
- **OpenFeign** (comunicación entre MS)
- **Swagger/OpenAPI 3**
- **MapStruct** (mapeo DTOs)
- **Testcontainers** (testing)

---

## 🎯 **Justificación Arquitectónica**

Esta estructura con **BD compartida** fue elegida para:

1. **Cumplir todos los requerimientos funcionales** sin complejidad técnica adicional
2. **Facilitar el desarrollo** en el tiempo disponible del TP
3. **Garantizar consistencia de datos** con transacciones ACID
4. **Simplificar testing e integración**
5. **Permitir queries complejas** requeridas por el negocio

La arquitectura mantiene **separación de responsabilidades** entre microservicios mientras optimiza para **velocidad de desarrollo** y **confiabilidad**.

---

## 📝 **Notas Adicionales**

- Esta estructura está optimizada para **desarrollo académico**
- Permite **escalabilidad futura** a BDs separadas si es necesario
- Mantiene **principios de microservicios** en la lógica de negocio
- Facilita **deployment con Docker Compose**
- Soporta **desarrollo en paralelo** por equipos

---

**Última actualización:** 2025-11-04  
**Autor:** Proyecto TPI Backend 2025  
**Versión:** 1.0