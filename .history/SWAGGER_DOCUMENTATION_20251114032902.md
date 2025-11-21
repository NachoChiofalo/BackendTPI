# Swagger/OpenAPI - Documentación de APIs

## ¿Qué es Swagger?

Swagger es una herramienta que permite documentar, visualizar y probar APIs REST de forma automática. Genera una interfaz web interactiva donde los desarrolladores pueden:

- Ver todos los endpoints disponibles
- Conocer los parámetros requeridos
- Probar las APIs directamente desde el navegador
- Ver ejemplos de respuestas

## Implementación en el Proyecto

### Dependencias Configuradas

Todos los microservicios tienen configurada la dependencia `springdoc-openapi-starter-webmvc-ui` versión 2.2.0.

### URLs de Acceso

Una vez que los microservicios estén ejecutándose, puedes acceder a Swagger UI en:

#### Microservicios Individuales:
- **Flotas**: http://localhost:8081/swagger-ui.html
- **Precios**: http://localhost:8083/swagger-ui.html
- **Solicitudes**: http://localhost:8084/swagger-ui.html
- **Rutas**: http://localhost:8085/swagger-ui.html
- **Localizaciones**: http://localhost:8087/swagger-ui.html

#### API Gateway (Centralizado):
- **Gateway**: http://localhost:8080/swagger-ui.html

#### Documentación JSON:
- Cada microservicio expone su documentación en: `http://localhost:<puerto>/api-docs`

### Configuraciones Principales

#### 1. Configuración Base (`SwaggerConfig.java`)
Cada microservicio tiene su configuración personalizada que incluye:
- Información del API (título, descripción, versión)
- Contacto del equipo de desarrollo
- Licencia
- Servidores disponibles

#### 2. Configuración en `application.yml`
```yaml
springdoc:
  api-docs:
    path: /api-docs
    enabled: true
  swagger-ui:
    path: /swagger-ui.html
    enabled: true
    tryItOutEnabled: true
    operationsSorter: method
    tagsSorter: alpha
  show-actuator: true
```

### Anotaciones de Swagger Implementadas

#### En Controladores:
- `@Tag`: Agrupa endpoints relacionados
- `@Operation`: Describe la operación del endpoint
- `@ApiResponses`: Define las posibles respuestas HTTP
- `@Parameter`: Describe parámetros de entrada

#### Ejemplo de Uso:
```java
@RestController
@RequestMapping("/api/solicitudes")
@Tag(name = "Solicitudes", description = "API para gestión de solicitudes")
public class SolicitudController {

    @GetMapping("/{id}")
    @Operation(summary = "Obtener solicitud por ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Solicitud encontrada"),
        @ApiResponse(responseCode = "404", description = "Solicitud no encontrada")
    })
    public ResponseEntity<Solicitud> getSolicitud(
            @Parameter(description = "ID de la solicitud") 
            @PathVariable Long id) {
        // implementación
    }
}
```

## Beneficios de la Implementación

1. **Documentación Automática**: Se genera automáticamente desde el código
2. **Testing Integrado**: Permite probar endpoints directamente
3. **Estándar OpenAPI**: Compatible con herramientas de la industria
4. **Mantenimiento**: Se actualiza automáticamente cuando cambia el código
5. **Colaboración**: Facilita el trabajo entre equipos frontend y backend

## Próximos Pasos

1. Ejecutar los microservicios
2. Acceder a las URLs de Swagger UI
3. Explorar la documentación generada
4. Probar los endpoints disponibles
5. Agregar más anotaciones a medida que se desarrollen los DTOs y controladores
