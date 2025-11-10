package com.tpi.localizaciones.controller;

import com.tpi.localizaciones.dto.UbicacionDto;
import com.tpi.localizaciones.entity.TipoUbicacion;
import com.tpi.localizaciones.entity.Ubicacion;
import com.tpi.localizaciones.service.UbicacionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/ubicaciones")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class UbicacionController {

    private final UbicacionService ubicacionService;

    @GetMapping
    public ResponseEntity<List<Ubicacion>> obtenerTodas() {
        log.info("GET /api/ubicaciones - Obteniendo todas las ubicaciones");
        return ResponseEntity.ok(ubicacionService.obtenerTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Ubicacion> obtenerPorId(@PathVariable Long id) {
        log.info("GET /api/ubicaciones/{} - Obteniendo por id", id);
        return ubicacionService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<Ubicacion>> buscarPorNombre(@RequestParam String nombre) {
        log.info("GET /api/ubicaciones/buscar - Buscando por nombre: {}", nombre);
        return ResponseEntity.ok(ubicacionService.buscarPorNombre(nombre));
    }

    @GetMapping("/tipo/{tipo}")
    public ResponseEntity<List<Ubicacion>> buscarPorTipo(@PathVariable TipoUbicacion tipo) {
        log.info("GET /api/ubicaciones/tipo/{} - Buscando por tipo", tipo);
        return ResponseEntity.ok(ubicacionService.buscarPorTipo(tipo));
    }

    @GetMapping("/ciudad/{ciudadId}")
    public ResponseEntity<List<Ubicacion>> buscarPorCiudad(@PathVariable Long ciudadId) {
        log.info("GET /api/ubicaciones/ciudad/{} - Buscando por ciudad", ciudadId);
        return ResponseEntity.ok(ubicacionService.buscarPorCiudad(ciudadId));
    }

    @GetMapping("/radio")
    public ResponseEntity<List<Ubicacion>> buscarPorRadioYTipo(
            @RequestParam BigDecimal lat,
            @RequestParam BigDecimal lon,
            @RequestParam Double radioKm,
            @RequestParam(required = false) TipoUbicacion tipo) {
        log.info("GET /api/ubicaciones/radio - Buscando en radio {}km de ({}, {})", radioKm, lat, lon);
        return ResponseEntity.ok(ubicacionService.buscarPorRadioYTipo(lat, lon, radioKm, tipo));
    }

    @GetMapping("/depositos")
    public ResponseEntity<List<Ubicacion>> buscarDepositosConCapacidad(
            @RequestParam(required = false) BigDecimal pesoMaximo,
            @RequestParam(required = false) BigDecimal alturaMaxima) {
        log.info("GET /api/ubicaciones/depositos - Buscando depósitos con capacidad");
        return ResponseEntity.ok(ubicacionService.buscarDepositosConCapacidad(pesoMaximo, alturaMaxima));
    }

    @PostMapping
    public ResponseEntity<Ubicacion> crear(@Valid @RequestBody UbicacionDto ubicacionDto) {
        log.info("POST /api/ubicaciones - Creando nueva ubicación: {}", ubicacionDto.getNombre());
        try {
            Ubicacion ubicacion = mapearDtoAEntidad(ubicacionDto);
            Ubicacion guardada = ubicacionService.guardar(ubicacion);
            return ResponseEntity.status(HttpStatus.CREATED).body(guardada);
        } catch (Exception e) {
            log.error("Error al crear ubicación: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Ubicacion> actualizar(@PathVariable Long id,
                                              @Valid @RequestBody UbicacionDto ubicacionDto) {
        log.info("PUT /api/ubicaciones/{} - Actualizando", id);
        try {
            Ubicacion ubicacion = mapearDtoAEntidad(ubicacionDto);
            Ubicacion actualizada = ubicacionService.actualizar(id, ubicacion);
            return ResponseEntity.ok(actualizada);
        } catch (RuntimeException e) {
            log.error("Error al actualizar ubicación {}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        log.info("DELETE /api/ubicaciones/{} - Eliminando", id);
        try {
            ubicacionService.eliminar(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            log.error("Error al eliminar ubicación {}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    private Ubicacion mapearDtoAEntidad(UbicacionDto dto) {
        return Ubicacion.builder()
                .id(dto.getId())
                .nombre(dto.getNombre())
                .direccion(dto.getDireccion())
                .latitud(dto.getLatitud())
                .longitud(dto.getLongitud())
                .tipoUbicacion(dto.getTipoUbicacion())
                .codigoPostal(dto.getCodigoPostal())
                .numeroPuerta(dto.getNumeroPuerta())
                .piso(dto.getPiso())
                .departamento(dto.getDepartamento())
                .entreCalles(dto.getEntreCalles())
                .referencias(dto.getReferencias())
                .contactoNombre(dto.getContactoNombre())
                .contactoTelefono(dto.getContactoTelefono())
                .contactoEmail(dto.getContactoEmail())
                .horarioAtencion(dto.getHorarioAtencion())
                .instruccionesAcceso(dto.getInstruccionesAcceso())
                .tieneMuelleCarga(dto.getTieneMuelleCarga())
                .tieneGrua(dto.getTieneGrua())
                .alturaMaximaMetros(dto.getAlturaMaximaMetros())
                .pesoMaximoToneladas(dto.getPesoMaximoToneladas())
                .espacioManiobraMetros(dto.getEspacioManiobraMetros())
                .build();
    }
}