# Microservicio de Precios (ms-precios)

## Descripción
Microservicio encargado de la gestión de tarifas y cálculo de precios para servicios de transporte logístico.

## Funcionalidades Principales

### 1. Gestión de Tarifas
- **CRUD de tarifas**: Crear, leer, actualizar y eliminar tarifas
- **Consulta de tarifas vigentes**: Obtener tarifas activas por fecha
- **Historial de tarifas**: Gestión de versiones de tarifas con fechas de vigencia

### 2. Cálculo de Precios
- **Cotización automática**: Cálculo de precios basado en peso, volumen y distancia
- **Factores de ajuste**: Aplicación de factores por urgencia y tipo de servicio
- **Integración con rutas**: Obtención de distancias reales desde el microservicio de rutas

## Endpoints Principales

### Tarifas (`/api/tarifas`)
- `GET /api/tarifas` - Obtener todas las tarifas
- `GET /api/tarifas/vigentes` - Obtener tarifas vigentes
- `GET /api/tarifas/vigente-actual` - Obtener tarifa vigente más reciente
- `GET /api/tarifas/{id}` - Obtener tarifa por ID
- `POST /api/tarifas` - Crear nueva tarifa
- `PUT /api/tarifas/{id}` - Actualizar tarifa
- `DELETE /api/tarifas/{id}` - Eliminar tarifa

### Cotizaciones (`/api/cotizaciones`)
- `POST /api/cotizaciones/calcular` - Calcular precio para cotización
- `POST /api/cotizaciones/calcular-con-tarifa` - Calcular precio con tarifa específica

## Estructura del Proyecto

```
src/main/java/com/tpi/precios/
├── PreciosApplication.java          # Clase principal
├── client/                          # Clientes Feign
│   ├── RutasClient.java
│   └── RutasClientFallback.java
├── config/                          # Configuraciones
│   └── WebConfig.java
├── controller/                      # Controladores REST
│   ├── TarifaController.java
│   └── CotizacionController.java
├── dto/                            # Data Transfer Objects
│   ├── TarifaDto.java
│   ├── CalculoPrecioDto.java
│   └── SolicitudCotizacionDto.java
├── entity/                         # Entidades JPA
│   ├── Tarifa.java
│   └── CalculoPrecio.java
├── exception/                      # Manejo de excepciones
│   └── GlobalExceptionHandler.java
├── repository/                     # Repositorios JPA
│   └── TarifaRepository.java
├── service/                        # Servicios de negocio
│   ├── TarifaService.java
│   └── CalculoPrecioService.java
└── utils/                          # Utilidades
    └── PrecioUtils.java
```

## Configuración

### Base de Datos
- **Motor**: PostgreSQL
- **Tabla principal**: `Tarifa`
- **Puerto**: 8082

### Dependencias Externas
- **ms-rutas**: Para obtener distancias entre ubicaciones
- **Eureka Server**: Para descubrimiento de servicios

## Algoritmo de Cálculo de Precios

1. **Obtención de tarifa vigente**: Se busca la tarifa más reciente que esté vigente
2. **Cálculo de distancia**: Se obtiene la distancia real desde el microservicio de rutas
3. **Cálculo base**: Se calcula el precio por peso y volumen, tomando el mayor
4. **Precio del tramo**: Se agrega el costo base del tramo
5. **Factores adicionales**: Se aplican factores por urgencia y tipo de servicio
6. **Resultado final**: Se redondea y retorna el precio calculado

### Factores de Ajuste
- **Factor urgencia**: 1.5x (50% adicional)
- **Factor premium**: 1.2x (20% adicional)
- **Factor económico**: 0.8x (20% descuento)

## Ejemplos de Uso

### Crear una nueva tarifa
```json
POST /api/tarifas
{
    "precioCombustibleLitro": 150.50,
    "precioKmKg": 0.25,
    "precioKmM3": 0.30,
    "fechaVigenciaInicio": "2025-01-01",
    "fechaVigenciaFin": "2025-12-31",
    "precioTramo": 5000.00
}
```

### Solicitar cotización
```json
POST /api/cotizaciones/calcular
{
    "ubicacionOrigenId": 1,
    "ubicacionDestinoId": 5,
    "pesoKg": 1500.00,
    "volumenM3": 25.5,
    "tipoServicio": "PREMIUM",
    "esUrgente": true
}
```

## Logging
Los logs se almacenan en `logs/ms-precios.log` con información detallada de:
- Cálculos de precios realizados
- Consultas a tarifas
- Errores de integración con otros servicios
- Activación de fallbacks

## Health Check
- **Endpoint**: `/actuator/health`
- **Puerto**: 8082
- **Contexto**: `/ms-precios`
