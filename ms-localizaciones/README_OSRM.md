# Integración OSRM en Microservicio de Localizaciones

## 📋 Descripción

Este documento describe la integración de OSRM (Open Source Routing Machine) en el microservicio de localizaciones para calcular distancias reales por carretera entre dos puntos geográficos.

## 🏗️ Arquitectura

### Componentes Implementados

1. **OsrmClient** - Cliente HTTP para comunicarse con la API de OSRM
2. **OsrmDistanciaService** - Servicio de negocio con caché de distancias
3. **OsrmController** - Controlador REST con endpoints públicos
4. **DTOs** - Objetos de transferencia de datos para request/response

### Flujo de Funcionamiento

```
Cliente → OsrmController → OsrmDistanciaService → OsrmClient → OSRM API
                                 ↓
                        DistanciaCalculada (Caché)
```

## 🚀 Endpoints Disponibles

### 1. Calcular Distancia Simple

**GET** `/api/osrm/distancia`

Calcula la distancia entre dos puntos. Usa caché si está disponible.

**Parámetros:**
- `latOrigen` (BigDecimal): Latitud del punto de origen
- `lonOrigen` (BigDecimal): Longitud del punto de origen
- `latDestino` (BigDecimal): Latitud del punto de destino
- `lonDestino` (BigDecimal): Longitud del punto de destino
- `forzarRecalculo` (Boolean, opcional): Forzar recálculo ignorando caché (default: false)

**Ejemplo:**
```bash
curl "http://localhost:8082/api/osrm/distancia?latOrigen=-31.4135&lonOrigen=-64.18105&latDestino=-32.9471&lonDestino=-60.6985"
```

**Respuesta:**
```json
{
  "distanciaKm": 351.000,
  "duracionMinutos": 255.0,
  "distanciaMetros": 351000.3,
  "duracionSegundos": 15300.7,
  "codigo": "Ok",
  "exitoso": true,
  "mensajeError": null,
  "rutaJson": null
}
```

### 2. Calcular Distancia con Ruta Completa

**GET** `/api/osrm/ruta`

Calcula la distancia e incluye detalles completos de la ruta (geometría, pasos de navegación).

**Parámetros:** Los mismos que el endpoint anterior

**Ejemplo:**
```bash
curl "http://localhost:8082/api/osrm/ruta?latOrigen=-31.4135&lonOrigen=-64.18105&latDestino=-32.9471&lonDestino=-60.6985"
```

**Respuesta:** Igual que el anterior pero incluye `rutaJson` con detalles completos.

### 3. Verificar Disponibilidad de OSRM

**GET** `/api/osrm/health`

Verifica si el servicio OSRM está disponible y respondiendo.

**Ejemplo:**
```bash
curl "http://localhost:8082/api/osrm/health"
```

**Respuesta:**
```json
{
  "disponible": true,
  "servicio": "OSRM",
  "timestamp": 1699651234567,
  "estado": "OK",
  "mensaje": "Servicio OSRM disponible y respondiendo correctamente"
}
```

### 4. Limpiar Caché Expirado

**DELETE** `/api/osrm/cache/limpiar`

Elimina del caché las distancias calculadas que ya expiraron.

**Ejemplo:**
```bash
curl -X DELETE "http://localhost:8082/api/osrm/cache/limpiar"
```

**Respuesta:**
```json
{
  "exitoso": true,
  "distanciasEliminadas": 15,
  "mensaje": "Caché limpiado exitosamente"
}
```

### 5. Ejemplo Predefinido

**GET** `/api/osrm/ejemplo`

Calcula la distancia entre Córdoba Capital y Rosario (ejemplo predefinido para testing).

**Ejemplo:**
```bash
curl "http://localhost:8082/api/osrm/ejemplo"
```

## ⚙️ Configuración

En `application.yml`:

```yaml
osrm:
  base-url: http://localhost:5000      # URL del servidor OSRM
  profile: driving                      # Perfil de enrutamiento (driving, bike, foot)
  connection-timeout: 5000              # Timeout de conexión en ms
  read-timeout: 10000                   # Timeout de lectura en ms
  enabled: true                         # Si OSRM está habilitado
  max-retries: 3                        # Número máximo de reintentos
```

## 📦 Caché de Distancias

El servicio implementa un sistema de caché inteligente:

- **Vigencia**: Las distancias calculadas tienen una vigencia de 24 horas por defecto
- **Reutilización**: Si existe una distancia válida en caché, se retorna sin consultar OSRM
- **Contador de Uso**: Cada vez que se usa una distancia del caché, se incrementa su contador
- **Limpieza**: Se pueden eliminar distancias expiradas usando el endpoint DELETE

### Entidad DistanciaCalculada

Las distancias se guardan con:
- Coordenadas de origen y destino
- Distancia en km y duración en minutos
- Proveedor API (OSRM)
- Fecha de cálculo y expiración
- JSON completo de la ruta (opcional)
- Número de veces que se ha usado

## 🔧 Uso Programático

### Desde otro microservicio

```java
@Autowired
private OsrmDistanciaService osrmDistanciaService;

public void calcularRuta() {
    BigDecimal latOrigen = new BigDecimal("-31.4135");
    BigDecimal lonOrigen = new BigDecimal("-64.18105");
    BigDecimal latDestino = new BigDecimal("-32.9471");
    BigDecimal lonDestino = new BigDecimal("-60.6985");
    
    OsrmDistanciaResponse response = osrmDistanciaService.calcularDistancia(
        latOrigen, lonOrigen, latDestino, lonDestino
    );
    
    if (response.getExitoso()) {
        System.out.println("Distancia: " + response.getDistanciaKm() + " km");
        System.out.println("Duración: " + response.getDuracionMinutos() + " minutos");
    }
}
```

### Forzar Recálculo

```java
// Ignora el caché y calcula nuevamente
OsrmDistanciaResponse response = osrmDistanciaService.calcularDistancia(
    latOrigen, lonOrigen, latDestino, lonDestino, true // forzar recálculo
);
```

### Incluir Detalles de Ruta

```java
OsrmDistanciaResponse response = osrmDistanciaService.calcularDistanciaConRuta(
    latOrigen, lonOrigen, latDestino, lonDestino
);

// La respuesta incluye rutaJson con geometría y pasos de navegación
String rutaCompleta = response.getRutaJson();
```

## 🧪 Testing

### Probar con curl

```bash
# Ejemplo: Córdoba a Rosario
curl "http://localhost:8082/api/osrm/distancia?latOrigen=-31.4135&lonOrigen=-64.18105&latDestino=-32.9471&lonDestino=-60.6985"

# Forzar recálculo
curl "http://localhost:8082/api/osrm/distancia?latOrigen=-31.4135&lonOrigen=-64.18105&latDestino=-32.9471&lonDestino=-60.6985&forzarRecalculo=true"

# Verificar health
curl "http://localhost:8082/api/osrm/health"
```

### Probar con Postman

1. Importar colección desde `/tests/postman/`
2. Ejecutar el request "OSRM - Calcular Distancia"
3. Verificar respuesta exitosa

## 📍 Coordenadas de Ejemplo (Argentina)

| Ciudad | Latitud | Longitud |
|--------|---------|----------|
| Córdoba Capital | -31.4135 | -64.18105 |
| Rosario | -32.9471 | -60.6985 |
| Buenos Aires | -34.6037 | -58.3816 |
| Mendoza | -32.8895 | -68.8458 |
| Tucumán | -26.8083 | -65.2176 |

## ⚠️ Consideraciones

1. **Servidor OSRM**: Debe estar corriendo en `http://localhost:5000` (o la URL configurada)
2. **Formato de Coordenadas**: OSRM espera longitud,latitud (NO latitud,longitud)
3. **Límites**: El servicio funciona solo con datos de Argentina (según el mapa descargado)
4. **Rendimiento**: La primera consulta puede ser más lenta, las siguientes usan caché
5. **Precisión**: La distancia es aproximada basada en rutas de OpenStreetMap

## 🐛 Troubleshooting

### OSRM no responde

```bash
# Verificar que OSRM esté corriendo
curl "http://localhost:5000/route/v1/driving/-64.18105,-31.4135;-60.6985,-32.9471?overview=false"
```

Si no responde, iniciar OSRM con Docker:
```bash
docker compose -f docker-compose.osrm.yml up
```

### Error de timeout

Aumentar los timeouts en `application.yml`:
```yaml
osrm:
  connection-timeout: 10000
  read-timeout: 20000
```

### Caché no funciona

Verificar que la base de datos esté corriendo y el esquema `localizaciones` exista.

## 📚 Referencias

- [OSRM Documentation](http://project-osrm.org/)
- [OSRM HTTP API](https://github.com/Project-OSRM/osrm-backend/blob/master/docs/http.md)
- [OpenStreetMap](https://www.openstreetmap.org/)

