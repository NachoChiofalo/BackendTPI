# Solución Propuesta - Microservicios TPI Backend 2025

## Arquitectura de Microservicios

La solución propuesta se basa en 5 microservicios principales más un API Gateway central, cada uno con responsabilidades específicas y bien definidas.

## 3.1. Microservicio Solicitudes

Este servicio gestionaría las entidades **Solicitud**, **Cliente** y **Contenedor**.

### Endpoints por Rol

#### Rol Cliente:
- `POST /solicitudes`: Para registrar una nueva solicitud de transporte.
- `GET /solicitudes/cliente/{clienteId}`: Para consultar el estado de sus solicitudes.

#### Rol Operador:
- `GET /solicitudes/pendientes`: Para consultar todos los contenedores pendientes de entrega.
- `GET /solicitudes/{id}`: Ver el detalle de una solicitud específica.
- `GET /clientes`: Listar todos los clientes.
- `GET /contenedores`: Listar todos los contenedores.

## 3.2. Microservicio Flotas

Este servicio gestionaría las entidades **Camión**, **Transportista** y **Depósito**.

### Endpoints por Rol

#### Rol Operador:
- `POST /camiones`: Registrar un nuevo camión.
- `PUT /camiones/{dominio}`: Actualizar un camión.
- `GET /camiones`: Listar todos los camiones.
- `GET /camiones/disponibles`: Para mostrar cuales son los camiones libres.
- `POST /depósitos`: Registrar un nuevo depósito.
- `GET /depósitos`: Listar depósitos.
- `GET /depósitos/{id}/contenedores`: Ver contenedores actualmente en un depósito.
- `POST /transportistas`: Registrar un nuevo transportista.

## 3.3. Microservicio Precios

Este servicio gestionaría la entidad **Tarifa** y realizaría los cálculos de costos.

### Endpoints por Rol

#### Rol Operador:
- `POST /tarifas`: Para registrar o actualizar las tarifas (costo km, combustible, estadía).
- `GET /tarifas/vigente`: Consultar la tarifa activa.

#### Endpoints Internos (usados por otros servicios):
- `POST /precios/estimar`: Calcula el costo estimado para una ruta tentativa.
- `POST /precios/calcular-real`: Calcula el costo real al finalizar una solicitud, usando los tramos, camiones específicos y estadías.

## 3.4. Microservicio Rutas

Este servicio gestionaría las entidades **Ruta**, **Tramo** y sus estados.

### Endpoints por Rol

#### Rol Operador:
- `GET /rutas/tentativas`: Para consultar rutas tentativas, incluyendo tramos y costo/tiempo estimados.
- `POST /solicitudes/{solicitudId}/ruta`: Para asignar la ruta definitiva a una solicitud.
- `PUT /tramos/{tramoId}/asignar-camión`: Para asignar un camión a un tramo específico.

#### Rol Transportista:
- `GET /transportistas/{transportistaId}/tramos`: Para ver los tramos que tiene asignados.
- `POST /tramos/{tramoId}/iniciar`: Para registrar el inicio de un tramo.
- `POST /tramos/{tramoId}/finalizar`: Para registrar el fin de un tramo.

## 3.5. Microservicio Localizaciones

Este servicio gestionaría **Ubicación** y **Ciudad** y se integraría con la API externa de mapas.

### Endpoints por Rol

#### Rol Operador:
- `POST /ciudades`: Para cargar y actualizar ciudades.
- `POST /ubicaciones`: Para registrar nuevas ubicaciones (como depósitos o puntos de clientes).

#### Endpoints Internos (usados por ms-rutas):
- `GET /distancia?origenLat=...&origenLon=...&destinoLat=...&destinoLon=...`: Endpoint que consume la API de Google Maps para calcular la distancia entre dos puntos geográficos.

## Comunicación entre Microservicios

### Flujo Principal de Operaciones

1. **Cliente registra solicitud** → `ms-solicitudes`
2. **Operador consulta rutas tentativas** → `ms-rutas` → `ms-localizaciones` (para distancias) → `ms-precios` (para costos estimados)
3. **Operador asigna ruta** → `ms-rutas`
4. **Operador asigna camiones** → `ms-rutas` ↔ `ms-flotas`
5. **Transportista inicia/finaliza tramos** → `ms-rutas`
6. **Cálculo de costos reales** → `ms-precios` ← `ms-rutas`

### Integración con API Externa

El microservicio `ms-localizaciones` será el único punto de contacto con la API de Google Maps, encapsulando esta dependencia externa y proporcionando una interfaz interna consistente para el cálculo de distancias.

## Consideraciones Técnicas

- Cada microservicio tendrá su propia base de datos independiente
- Comunicación asíncrona donde sea posible
- API Gateway centralizado para enrutamiento y autenticación
- Implementación de circuit breakers para resilencia
- Logs distribuidos para trazabilidad
- Documentación automática con Swagger/OpenAPI en cada servicio