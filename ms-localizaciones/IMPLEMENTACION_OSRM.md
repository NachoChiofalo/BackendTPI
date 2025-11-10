# 🎯 Resumen de Implementación - API OSRM en ms-localizaciones

## ✅ ¿Qué se implementó?

Se integró completamente la API de OSRM en el microservicio `ms-localizaciones` para calcular distancias reales por carretera entre dos puntos geográficos en Argentina.

## 📁 Archivos Creados

### 1. DTOs (Data Transfer Objects)
- `dto/osrm/OsrmRouteResponse.java` - DTO para respuesta de OSRM
- `dto/osrm/OsrmDistanciaRequest.java` - DTO para solicitud
- `dto/osrm/OsrmDistanciaResponse.java` - DTO simplificado de respuesta

### 2. Cliente HTTP
- `client/OsrmClient.java` - Cliente para comunicarse con la API de OSRM

### 3. Servicio de Negocio
- `service/OsrmDistanciaService.java` - Lógica de negocio con caché inteligente

### 4. Controlador REST
- `controller/OsrmController.java` - Endpoints públicos para consumir

### 5. Configuración
- `config/OsrmConfig.java` - Configuración del cliente OSRM
- Actualización de `application.yml` con parámetros OSRM

### 6. Repositorio
- Actualización de `DistanciaCalculadaRepository.java` con nuevos métodos

### 7. Documentación
- `README_OSRM.md` - Documentación completa
- `osrm-requests.http` - Ejemplos de requests HTTP

## 🚀 Cómo Usar

### Paso 1: Asegurar que OSRM esté corriendo

Según el archivo `osrm.txt`, debes tener OSRM corriendo en Docker:

```bash
docker compose -f docker-compose.osrm.yml up
```

### Paso 2: Iniciar el microservicio

```bash
cd ms-localizaciones
mvn spring-boot:run
```

### Paso 3: Probar los endpoints

**Opción A - Con curl:**
```bash
curl "http://localhost:8082/api/osrm/distancia?latOrigen=-31.4135&lonOrigen=-64.18105&latDestino=-32.9471&lonDestino=-60.6985"
```

**Opción B - Con archivo .http:**
Abre `ms-localizaciones/osrm-requests.http` en IntelliJ y ejecuta los requests directamente.

**Opción C - Con Swagger:**
Visita: `http://localhost:8082/swagger-ui.html`

## 🔑 Características Principales

### 1. Caché Inteligente
- Las distancias calculadas se guardan en la base de datos
- Vigencia de 24 horas por defecto
- Evita consultas innecesarias a OSRM
- Contador de uso para estadísticas

### 2. Endpoints Disponibles

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/osrm/distancia` | Calcular distancia simple |
| GET | `/api/osrm/ruta` | Calcular con detalles de ruta |
| GET | `/api/osrm/health` | Verificar disponibilidad |
| DELETE | `/api/osrm/cache/limpiar` | Limpiar caché expirado |
| GET | `/api/osrm/ejemplo` | Ejemplo predefinido |

### 3. Parámetros de Configuración

En `application.yml`:
```yaml
osrm:
  base-url: http://localhost:5000  # URL del servidor OSRM
  profile: driving                  # Perfil (driving, bike, foot)
  enabled: true                     # Habilitar/deshabilitar
```

## 📊 Ejemplo de Respuesta

```json
{
  "distanciaKm": 351.000,
  "duracionMinutos": 255.0,
  "distanciaMetros": 351000.3,
  "duracionSegundos": 15300.7,
  "codigo": "Ok",
  "exitoso": true
}
```

## 🔄 Integración con otros Microservicios

Desde `ms-rutas` o cualquier otro microservicio, puedes usar el servicio así:

```java
@Autowired
private OsrmDistanciaService osrmDistanciaService;

public void calcularRutaTransporte() {
    OsrmDistanciaResponse distancia = osrmDistanciaService.calcularDistancia(
        new BigDecimal("-31.4135"),  // lat origen
        new BigDecimal("-64.18105"), // lon origen
        new BigDecimal("-32.9471"),  // lat destino
        new BigDecimal("-60.6985")   // lon destino
    );
    
    if (distancia.getExitoso()) {
        // Usar distancia.getDistanciaKm() y distancia.getDuracionMinutos()
    }
}
```

## 🎓 Casos de Uso en el TPI

1. **Calcular costo de traslado**: Usar la distancia en km para calcular costo por kilómetro
2. **Estimar tiempo de entrega**: Usar la duración en minutos
3. **Optimizar rutas**: Comparar distancias entre diferentes depósitos
4. **Asignar camiones**: Considerar distancias para asignar el camión más cercano

## ⚠️ Notas Importantes

1. **Orden de coordenadas**: OSRM espera `longitud,latitud` pero el servicio maneja esto automáticamente
2. **Datos de Argentina**: El servidor OSRM debe tener cargados los datos de argentina-latest.osm.pbf
3. **Puerto**: El microservicio corre en el puerto 8082
4. **Base de datos**: Se guardan las distancias en el esquema `localizaciones`

## 📖 Documentación Adicional

Consulta `README_OSRM.md` para documentación detallada con más ejemplos y troubleshooting.

---

**¡Implementación completa y lista para usar! 🎉**

