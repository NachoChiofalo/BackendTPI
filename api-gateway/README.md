# API Gateway - Sistema de Logística

## Descripción
API Gateway centralizado para el sistema de logística que maneja el enrutamiento, autenticación, autorización y circuit breakers para todos los microservicios usando configuración estática (sin Eureka).

## Características

### 🚀 Enrutamiento Directo
- Enrutamiento a microservicios usando URLs configuradas estáticamente
- Circuit breakers integrados para resilencia
- Health checks manuales de servicios

### 🔒 Seguridad
- Autenticación JWT con OAuth2
- Filtros de seguridad personalizados
- Configuración CORS
- Rutas públicas y protegidas

### 🛡️ Resiliencia
- Circuit Breakers con Resilience4j
- Fallback controllers para manejo de errores
- Rate limiting
- Retry automático

### 📊 Observabilidad
- Health checks integrados
- Verificación de estado de microservicios
- Logging estructurado
- Métricas con Actuator

## Configuración de URLs

### Variables de Entorno
```yaml
microservices:
  flotas:
    url: http://localhost:8081
  solicitudes:
    url: http://localhost:8082
  rutas:
    url: http://localhost:8083
  precios:
    url: http://localhost:8084
  localizaciones:
    url: http://localhost:8085
```

## Rutas Configuradas

### Microservicios
- `/api/flotas/**` → http://localhost:8081
- `/api/solicitudes/**` → http://localhost:8082
- `/api/rutas/**` → http://localhost:8083
- `/api/precios/**` → http://localhost:8084
- `/api/localizaciones/**` → http://localhost:8085

### Endpoints Públicos
- `/health/**` - Health checks
- `/actuator/**` - Métricas y monitoreo
- `/fallback/**` - Endpoints de fallback
- `/auth/**` - Autenticación (futuro)

## Configuración

### Variables de Entorno
```yaml
SERVER_PORT: 8080
MICROSERVICES_FLOTAS_URL: http://localhost:8081
MICROSERVICES_SOLICITUDES_URL: http://localhost:8082
# ... etc para cada microservicio
```

### Circuit Breaker
Configurado para todos los microservicios con:
- Ventana deslizante: 10 requests
- Umbral de falla: 50%
- Tiempo de espera en estado abierto: 30s

## Uso

### Ejecutar el Gateway
```bash
mvn spring-boot:run
```

### Verificar Estado
```bash
# Estado del Gateway
curl http://localhost:8080/health

# Estado de todos los microservicios
curl http://localhost:8080/health/services
```

### Ejemplo de Request
```bash
curl -H "Authorization: Bearer <token>" \
     http://localhost:8080/api/flotas/camiones
```

## Desarrollo

### Agregar Nueva Ruta
1. Agregar URL en `application.yml`
2. Modificar `GatewayConfig.java`
3. Agregar circuit breaker en `application.yml`
4. Crear endpoint de fallback en `FallbackController.java`

### Cambiar URLs de Microservicios
Modificar en `application.yml`:
```yaml
microservices:
  flotas:
    url: http://nueva-ip:puerto
```

## Health Checks

### Endpoints de Salud
- `/health` - Estado del Gateway
- `/health/services` - Estado de todos los microservicios
- `/health/ready` - Readiness probe
- `/health/live` - Liveness probe

### Respuesta de Health Check
```json
{
  "timestamp": "2024-01-15T14:30:00",
  "gateway": "UP",
  "services": {
    "flotas": "UP",
    "solicitudes": "DOWN",
    "rutas": "UP",
    "precios": "UP",
    "localizaciones": "UP"
  },
  "overall_status": "DEGRADED"
}
```

## Configuración sin Eureka

Este API Gateway está configurado para funcionar **sin Eureka Server**, usando:

### ✅ URLs Estáticas
- Configuración directa de endpoints de microservicios
- Fácil cambio de URLs via configuración

### ✅ Health Checks Manuales
- `MicroserviceHealthService` verifica estado de servicios
- Timeout configurado a 5 segundos por servicio

### ✅ Circuit Breakers Independientes
- Cada microservicio tiene su propio circuit breaker
- Configuración independiente por servicio

## Monitoreo

### Actuator Endpoints
- `/actuator/gateway/routes` - Rutas configuradas
- `/actuator/health` - Estado detallado
- `/actuator/metrics` - Métricas del sistema
