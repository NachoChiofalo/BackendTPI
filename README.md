# 🚛 TPI Backend Logística — Sistema de Transporte de Contenedores

Sistema backend basado en **microservicios** para la gestión logística de transporte de contenedores. Desarrollado como Trabajo Práctico Integrador (TPI) utilizando **Java 17**, **Spring Boot 3.2** y **Spring Cloud**.

---

## 📋 Tabla de Contenidos

- [Descripción General](#-descripción-general)
- [Arquitectura](#-arquitectura)
- [Stack Tecnológico](#-stack-tecnológico)
- [Estructura del Proyecto](#-estructura-del-proyecto)
- [Microservicios](#-microservicios)
  - [API Gateway](#api-gateway-puerto-8090)
  - [ms-solicitudes](#ms-solicitudes-puerto-8084)
  - [ms-flotas](#ms-flotas-puerto-8081)
  - [ms-rutas](#ms-rutas-puerto-8085)
  - [ms-precios](#ms-precios-puerto-8083)
  - [ms-localizaciones](#ms-localizaciones-puerto-8087)
- [Librerías Compartidas](#-librerías-compartidas)
- [Seguridad](#-seguridad)
- [Requisitos Previos](#-requisitos-previos)
- [Instalación y Ejecución](#-instalación-y-ejecución)
- [Endpoints Principales](#-endpoints-principales)
- [Testing](#-testing)
- [Variables de Entorno](#-variables-de-entorno)

---

## 📝 Descripción General

El sistema gestiona el ciclo de vida completo del transporte de contenedores:

1. **Registro de solicitudes**: Un cliente registra una solicitud de transporte con un contenedor asociado.
2. **Cálculo de rutas**: Se generan rutas tentativas con estimaciones de costo y tiempo, utilizando OSRM para cálculos geográficos reales.
3. **Asignación de ruta**: El operador asigna una ruta a la solicitud.
4. **Gestión de flota**: Se asignan camiones disponibles verificando capacidad de peso y volumen.
5. **Seguimiento**: Se registra el estado cronológico del envío hasta la entrega.
6. **Cálculo de costos**: Se calculan los costos según tarifas vigentes, distancia, peso y volumen.

---

## 🏗 Arquitectura

```
                         ┌──────────────────┐
                         │     Cliente       │
                         └────────┬─────────┘
                                  │
                                  ▼
                    ┌─────────────────────────┐
                    │   API Gateway (:8090)    │
                    │  JWT / Circuit Breaker   │
                    └──┬───┬───┬───┬───┬──────┘
                       │   │   │   │   │
          ┌────────────┘   │   │   │   └─────────────┐
          ▼                ▼   │   ▼                  ▼
   ┌─────────────┐ ┌──────────┐│ ┌──────────┐ ┌──────────────┐
   │ ms-flotas   │ │ms-rutas  ││ │ms-precios│ │ms-localizac. │
   │   (:8081)   │ │ (:8085)  ││ │ (:8083)  │ │   (:8087)    │
   └─────────────┘ └────┬─────┘│ └──────────┘ └───────┬──────┘
                         │      │                      │
                         │      ▼                      │
                         │ ┌──────────────┐            │
                         │ │ms-solicitudes│            │
                         │ │   (:8084)    │            │
                         │ └──────────────┘            │
                         │                             │
                         └──────────┬──────────────────┘
                                    ▼
                             ┌─────────────┐
                             │  OSRM (:5000)│
                             │  (Routing)   │
                             └─────────────┘

          ┌──────────────────────────────────────────┐
          │          PostgreSQL 15 (:5432)            │
          │           tpi_logistica                   │
          └──────────────────────────────────────────┘

          ┌──────────────────────────────────────────┐
          │          Keycloak (:8080)                 │
          │       OAuth2 / OpenID Connect             │
          └──────────────────────────────────────────┘
```

### Comunicación entre Servicios

| Origen | Destino | Método |
|--------|---------|--------|
| `ms-rutas` | `ms-localizaciones` | REST (RestTemplate) |
| `ms-rutas` | `ms-precios` | REST (RestTemplate) |
| `ms-solicitudes` | `ms-localizaciones` | REST |
| `ms-solicitudes` | `ms-rutas` | REST |
| `ms-localizaciones` | OSRM | HTTP (OpenFeign) |
| API Gateway | Todos los microservicios | Spring Cloud Gateway |

---

## 🛠 Stack Tecnológico

| Tecnología | Versión | Uso |
|---|---|---|
| **Java** | 17 | Lenguaje principal |
| **Spring Boot** | 3.2.0 | Framework de microservicios |
| **Spring Cloud** | 2023.0.0 | Gateway, OpenFeign |
| **Spring Security** | — | OAuth2 Resource Server / JWT |
| **Spring Data JPA** | — | Acceso a datos |
| **PostgreSQL** | 15 | Base de datos relacional |
| **Keycloak** | 23.0 | Servidor de identidad (OAuth2/OIDC) |
| **OSRM** | — | Motor de cálculo de rutas geográficas |
| **Resilience4j** | — | Circuit Breaker en el Gateway |
| **Lombok** | 1.18.30 | Reducción de boilerplate |
| **MapStruct** | 1.5.5 | Mapeo de objetos (DTO ↔ Entity) |
| **SpringDoc OpenAPI** | 2.2.0 | Documentación Swagger |
| **Micrometer + Prometheus** | — | Observabilidad y métricas |
| **TestContainers** | 1.19.3 | Testing con contenedores |
| **Docker Compose** | — | Orquestación de servicios |
| **Maven** | — | Gestión de dependencias y build |

---

## 📁 Estructura del Proyecto

```
BackendTPI/
├── api-gateway/                   # API Gateway (Spring Cloud Gateway)
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/
├── ms-flotas/                     # Microservicio de Flotas
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/
├── ms-localizaciones/             # Microservicio de Localizaciones
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/
├── ms-precios/                    # Microservicio de Precios
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/
├── ms-rutas/                      # Microservicio de Rutas
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/
├── ms-solicitudes/                # Microservicio de Solicitudes
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/
├── shared-libs/                   # Librerías compartidas
│   ├── database-common/           #   Utilidades de base de datos
│   ├── common-dto/                #   DTOs compartidos
│   └── security-common/           #   Configuración de seguridad
├── infrastructure/
│   └── docker/
│       ├── docker-compose.yml     # Orquestación de todos los servicios
│       ├── postgres/              # Scripts de inicialización de BD
│       └── osrm/                  # Configuración de OSRM
├── tests/
│   └── integration/               # Tests de integración end-to-end
└── pom.xml                        # POM padre (multi-módulo Maven)
```

---

## 🔧 Microservicios

### API Gateway (Puerto 8090)

Punto de entrada único para todas las peticiones. Enruta el tráfico a los microservicios correspondientes, aplica seguridad JWT y patrón Circuit Breaker.

**Rutas configuradas:**

| Ruta | Microservicio destino |
|---|---|
| `/api/flotas/**` | ms-flotas (:8081) |
| `/api/solicitudes/**` | ms-solicitudes (:8084) |
| `/api/rutas/**` | ms-rutas (:8085) |
| `/api/precios/**` | ms-precios (:8083) |
| `/api/localizaciones/**` | ms-localizaciones (:8087) |

**Características:**
- Validación JWT con Keycloak
- Circuit Breaker (Resilience4j): umbral de fallos del 50%, ventana de 10 llamadas
- Reintentos automáticos (3 intentos por petición fallida)
- Logging de peticiones/respuestas

---

### ms-solicitudes (Puerto 8084)

Gestiona las solicitudes de transporte de contenedores y su seguimiento.

**Entidades principales:**
- `Solicitud` — Solicitud de transporte (estados: Creada → En Progreso → Completada)
- `Cliente` — Datos del cliente (documento, nombre, contacto)
- `Contenedor` — Contenedor a transportar (tipo, peso, volumen)
- `EstadoContenedor` — Historial de estados del contenedor

**Endpoints:**

| Método | Ruta | Descripción |
|---|---|---|
| `POST` | `/api/solicitudes` | Crear nueva solicitud de transporte |
| `GET` | `/api/solicitudes/cliente` | Consultar solicitudes de un cliente |
| `GET` | `/api/solicitudes/cliente/contenedores` | Listar contenedores del cliente con estado |
| `GET` | `/api/solicitudes/{id}/seguimiento` | Seguimiento cronológico del envío |
| `GET` | `/api/solicitudes/pendientes` | Entregas pendientes (vista operador) |
| `PUT` | `/api/solicitudes/{id}/ruta/{rutaId}` | Asignar ruta a solicitud |
| `POST` | `/api/solicitudes/{id}/finalizar` | Finalizar solicitud y calcular costos |

---

### ms-flotas (Puerto 8081)

Administra camiones, transportistas y control de capacidad vehicular.

**Entidades principales:**
- `Camion` — Camión (dominio/patente, capacidad peso/volumen, costo por km)
- `Transportista` — Conductor (documento, nombre, teléfono)

**Endpoints:**

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/api/camiones` | Listar todos los camiones |
| `GET` | `/api/camiones/disponibles` | Camiones disponibles |
| `GET` | `/api/camiones/capacidad` | Filtrar por capacidad (peso/volumen) |
| `POST` | `/api/camiones` | Registrar camión (rol OPERADOR) |
| `POST` | `/api/camiones/asignar` | Asignar camión a tramo de ruta |
| `POST` | `/api/camiones/{dominio}/disponibilidad` | Cambiar disponibilidad |
| `GET` | `/api/transportistas` | Listar transportistas |

---

### ms-rutas (Puerto 8085)

Calcula rutas óptimas de entrega con múltiples paradas intermedias (depósitos).

**Entidades principales:**
- `Ruta` — Ruta completa (cantidad de tramos y depósitos intermedios)
- `Tramo` — Segmento de ruta (origen, destino, distancia, tiempo, costo)

**Endpoints:**

| Método | Ruta | Descripción |
|---|---|---|
| `POST` | `/api/rutas/tentativas` | Generar 3 rutas tentativas con estimaciones |
| `GET` | `/api/rutas` | Listar todas las rutas |
| `GET` | `/api/tramos` | Listar tramos de rutas |

> El cálculo de rutas tentativas consulta a `ms-localizaciones` para distancias (OSRM) y a `ms-precios` para costos.

---

### ms-precios (Puerto 8083)

Motor de cálculo de tarifas y costos de transporte.

**Entidades principales:**
- `Tarifa` — Tarifa configurable (precio combustible, precio por km/kg, precio por km/m³, precio por estadía)
  - Tipos: BASICA, PREMIUM, EXPRESS, ECONOMICA
  - Modalidades: POR_PESO, POR_VOLUMEN, POR_DISTANCIA, MIXTA
- `CalculoPrecio` — Cotización calculada

**Endpoints:**

| Método | Ruta | Descripción |
|---|---|---|
| `POST` | `/api/precios/cotizacion` | Obtener cotización de costo |
| `GET` | `/api/tarifas` | Listar tarifas vigentes |

---

### ms-localizaciones (Puerto 8087)

Gestiona ubicaciones geográficas y cálculo de distancias reales vía OSRM.

**Entidades principales:**
- `Ubicacion` — Ubicación geográfica (ciudad, dirección, latitud, longitud)
- `Deposito` — Depósito/almacén (ubicación, nombre, capacidad)

**Endpoints:**

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/api/ubicaciones` | Listar ubicaciones |
| `POST` | `/api/ubicaciones/por-coordenadas` | Crear ubicación desde coordenadas |
| `GET` | `/api/ubicaciones/{id}` | Detalle de ubicación |
| `GET` | `/api/depositos` | Listar depósitos |
| `POST` | `/api/geo/distancia` | Calcular distancia entre coordenadas |

---

## 📚 Librerías Compartidas

| Librería | Descripción |
|---|---|
| **database-common** | Configuración base de JPA, driver PostgreSQL, utilidades de repositorio y soporte TestContainers |
| **common-dto** | DTOs compartidos entre microservicios, anotaciones de validación, configuración de serialización JSON y soporte MapStruct |
| **security-common** | Configuración de Spring Security, OAuth2 Resource Server, manejo de JWT y control de acceso basado en roles (RBAC) |

---

## 🔐 Seguridad

El sistema utiliza **OAuth2 con JWT** mediante **Keycloak** como servidor de identidad.

### Roles

| Rol | Permisos |
|---|---|
| `ROLE_CLIENTE` | Crear solicitudes, consultar sus contenedores y seguimiento |
| `ROLE_OPERADOR` | Gestionar camiones, asignar rutas, finalizar solicitudes |
| *(público)* | Lectura de datos (GET) en todos los endpoints |

### Flujo de Autenticación

1. El cliente obtiene un token JWT desde Keycloak (`realm: TPI-BACKEND`).
2. El token se envía en el header `Authorization: Bearer <token>`.
3. El API Gateway valida el JWT contra Keycloak (JWK Set URI).
4. Los roles se extraen del claim `realm_access.roles`.
5. Las rutas protegidas verifican el rol requerido.

---

## 📦 Requisitos Previos

- **Java** 17+
- **Maven** 3.8+
- **Docker** y **Docker Compose**
- **Git**

---

## 🚀 Instalación y Ejecución

### 1. Clonar el repositorio

```bash
git clone https://github.com/NachoChiofalo/BackendTPI.git
cd BackendTPI
```

### 2. Compilar el proyecto

```bash
mvn clean package -DskipTests
```

### 3. Levantar todos los servicios con Docker Compose

```bash
docker compose -f infrastructure/docker/docker-compose.yml up --build
```

Esto levantará:
- **PostgreSQL** en el puerto `5432`
- **Keycloak** en el puerto `8080`
- **OSRM** en el puerto `5000`
- **API Gateway** en el puerto `8090`
- Los **5 microservicios** en sus respectivos puertos

### 4. Acceder a los servicios

| Servicio | URL |
|---|---|
| API Gateway | `http://localhost:8090` |
| Keycloak (admin) | `http://localhost:8080` (admin/admin) |
| Swagger UI (por microservicio) | `http://localhost:{puerto}/swagger-ui.html` |
| Health Check (ejemplo) | `http://localhost:8090/actuator/health` |

---

## 📡 Endpoints Principales

Los endpoints se acceden a través del **API Gateway** (`http://localhost:8090`). El gateway enruta las peticiones según el prefijo del path (ver [tabla de rutas](#api-gateway-puerto-8090)) y reescribe la URL antes de reenviarla al microservicio correspondiente.

A continuación se listan los endpoints internos de cada microservicio:

```
# Solicitudes (ms-solicitudes)
POST   /api/solicitudes                          # Crear solicitud
GET    /api/solicitudes/cliente                   # Solicitudes del cliente
GET    /api/solicitudes/{id}/seguimiento          # Seguimiento de envío
GET    /api/solicitudes/pendientes                # Entregas pendientes

# Flotas (ms-flotas)
GET    /api/camiones                              # Listar camiones
GET    /api/camiones/disponibles                  # Camiones disponibles
POST   /api/camiones                              # Registrar camión

# Rutas (ms-rutas)
POST   /api/rutas/tentativas                      # Generar rutas tentativas
GET    /api/rutas                                 # Listar rutas
GET    /api/tramos                                # Listar tramos

# Precios (ms-precios)
POST   /api/precios/cotizacion                    # Obtener cotización
GET    /api/tarifas                               # Tarifas vigentes

# Localizaciones (ms-localizaciones)
GET    /api/ubicaciones                           # Listar ubicaciones
POST   /api/geo/distancia                         # Calcular distancia
GET    /api/depositos                             # Listar depósitos
```

---

## 🧪 Testing

### Tests unitarios

```bash
mvn test
```

### Tests de integración

```bash
mvn verify -Pintegration-tests
```

Los tests de integración utilizan **TestContainers** (PostgreSQL), **WireMock** (mocks de APIs externas) y **REST Assured** (testing de endpoints).

---

## ⚙ Variables de Entorno

| Variable | Descripción | Valor por defecto (Docker) |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | Perfil de Spring activo | `docker` |
| `SPRING_DATASOURCE_URL` | URL de conexión a PostgreSQL | `jdbc:postgresql://tpi-postgres:5432/tpi_logistica` |
| `SPRING_DATASOURCE_USERNAME` | Usuario de base de datos | `tpi_user` |
| `SPRING_DATASOURCE_PASSWORD` | Contraseña de base de datos | `tpi_pass` |
| `KEYCLOAK_AUTH_SERVER_URL` | URL del servidor Keycloak | `http://tpi-keycloak:8080` |
| `OSRM_URL` | URL del servicio OSRM | `http://tpi-osrm:5000` |
| `MS_LOCALIZACIONES_URL` | URL de ms-localizaciones | `http://tpi-ms-localizaciones:8087` |
| `MS_RUTAS_URL` | URL de ms-rutas | `http://tpi-ms-rutas:8085` |
| `TZ` | Zona horaria | `UTC` |

### Perfiles de Maven

| Perfil | Descripción |
|---|---|
| `dev` | Desarrollo local (activo por defecto) |
| `test` | Entorno de testing |
| `prod` | Producción |
| `integration-tests` | Ejecución de tests de integración |
