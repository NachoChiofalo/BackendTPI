package com.tpi.solicitudes.service;

import com.tpi.solicitudes.dto.SolicitudCrearDto;
import com.tpi.solicitudes.dto.TramoCrearDto;
import com.tpi.solicitudes.entity.Cliente;
import com.tpi.solicitudes.entity.Contenedor;
import com.tpi.solicitudes.entity.Solicitud;
import com.tpi.solicitudes.repository.SolicitudRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class SolicitudService {

    private final SolicitudRepository solicitudRepository;
    private final ClienteService clienteService;
    private final ContenedorService contenedorService;
    private final SecureRandom random = new SecureRandom();

    public List<Solicitud> obtenerTodos() {
        log.info("Obteniendo todas las solicitudes");
        return solicitudRepository.findAll();
    }

    public Optional<Solicitud> obtenerPorId(Integer id) {
        log.info("Obteniendo solicitud por id: {}", id);
        return solicitudRepository.findById(id);
    }

    @Transactional
    public Solicitud crear(Solicitud solicitud) {
        log.info("Creando nueva solicitud");
        return solicitudRepository.save(solicitud);
    }

    /**
     * Crea una solicitud a partir del DTO que contiene datos de cliente y contenedor.
     * - Crea cliente si no existe
     * - Crea contenedor si no existe
     * - Fija estado inicial BORRADOR = 1
     * - Genera automáticamente la ruta y los tramos asociados
     */
    @Transactional
    public Solicitud crearConDetalles(SolicitudCrearDto dto) {
        log.info("crearConDetalles: creando solicitud con cliente y contenedor si es necesario");

        // 1) Validaciones básicas
        if (dto == null) throw new IllegalArgumentException("Solicitud vacía");
        if (dto.getCliente() == null) throw new IllegalArgumentException("Cliente es requerido");
        if (dto.getContenedor() == null) throw new IllegalArgumentException("Contenedor es requerido");

        // 2) Cliente: crear si no existe
        SolicitudCrearDto.ClienteInfo ci = dto.getCliente();
        Cliente clienteEntidad = Cliente.builder()
                .tipoDocClienteId(ci.getTipoDocumento())
                .numDocCliente(ci.getNumDocumento())
                .nombres(ci.getNombres() != null ? ci.getNombres() : "SIN_NOMBRE")
                .apellidos(ci.getApellidos() != null ? ci.getApellidos() : "SIN_APELLIDO")
                .domicilio(ci.getDomicilio() != null ? ci.getDomicilio() : "")
                .telefono(ci.getTelefono() != null ? ci.getTelefono() : "")
                .build();

        Cliente clientePersistido = clienteService.guardarSiNoExiste(
                clienteEntidad.getTipoDocClienteId(), clienteEntidad.getNumDocCliente(), clienteEntidad);

        // 3) Contenedor: crear si no existe
        SolicitudCrearDto.ContenedorInfo contInfo = dto.getContenedor();
        Contenedor contEntidad = new Contenedor();
        if (contInfo.getIdContenedor() != null) contEntidad.setIdContenedor(contInfo.getIdContenedor());
        if (contInfo.getPeso() != null) contEntidad.setPesoKg(contInfo.getPeso());
        if (contInfo.getVolumen() != null) contEntidad.setVolumenM3(contInfo.getVolumen());

        // Establecer estado "Creado" (ID = 1) cuando se crea la solicitud
        contEntidad.setIdEstadoContenedor(1); // 1 = "Creado"

        Contenedor contPersistido = contenedorService.guardarSiNoExiste(contInfo.getIdContenedor(), contEntidad);

        // 4) Obtener o crear ubicaciones basándose en coordenadas
        Integer idUbicacionOrigen = obtenerOCrearUbicacionPorCoordenadas(
                dto.getLatitudOrigen(), dto.getLongitudOrigen());
        Integer idUbicacionDestino = obtenerOCrearUbicacionPorCoordenadas(
                dto.getLatitudDestino(), dto.getLongitudDestino());

        // 5) Generar la ruta y los tramos automáticamente
        Integer rutaId = generarRutaYTramos(idUbicacionOrigen, idUbicacionDestino);

        // 6) Crear la entidad Solicitud y poblarla mínimamente. Necesitamos generar un id para la solicitud (si no hay @GeneratedValue)
        // Aquí asumimos que la tabla no tiene generación automática; usar el max id + 1 como heurística.
        Integer newSolicitudId = generateNewSolicitudId();

        // Calcular valores aleatorios
        BigDecimal costoEstimado = calcularCostoEstimadoAleatorio();
        LocalDateTime fechaHoraEstimadaFin = calcularFechaHoraEstimadaFinAleatoria();

        Solicitud solicitud = Solicitud.builder()
                .solicitudId(newSolicitudId)
                .tipoDocCliente(clientePersistido.getTipoDocClienteId())
                .numDocCliente(clientePersistido.getNumDocCliente())
                .estadoSolicitud(1) // 1 = Creada
                .idContenedor(contPersistido.getIdContenedor())
                .idRuta(rutaId)
                .idUbicacionOrigen(idUbicacionOrigen)
                .idUbicacionDestino(idUbicacionDestino)
                .costoEstimado(costoEstimado)
                .costoReal(null)
                .fechaHoraInicio(null)
                .fechaHoraEstimadaFin(fechaHoraEstimadaFin)
                .fechaHoraFin(null)
                .textoAdicional(dto.getObservaciones())
                .build();

        Solicitud guardada = solicitudRepository.save(solicitud);
        log.info("Solicitud creada con id {} asociada al cliente {}-{} y contenedor {}, ruta {}, costo estimado: {}, fecha estimada fin: {}",
                guardada.getSolicitudId(), guardada.getTipoDocCliente(), guardada.getNumDocCliente(), guardada.getIdContenedor(), rutaId, costoEstimado, fechaHoraEstimadaFin);
        return guardada;
    }

    private Integer generateNewSolicitudId() {
        // Intento simple: buscar max id entre las solicitudes actuales
        Optional<Solicitud> top = solicitudRepository.findAll().stream()
                .max(Comparator.comparing(Solicitud::getSolicitudId));
        return top.map(s -> s.getSolicitudId() + 1).orElse(1);
    }

    /**
     * Obtiene o crea una ubicación basándose en coordenadas (latitud y longitud).
     * Hace una llamada al microservicio de localizaciones.
     */
    private Integer obtenerOCrearUbicacionPorCoordenadas(Double latitud, Double longitud) {
        log.info("Obteniendo o creando ubicación para coordenadas: lat={}, lon={}", latitud, longitud);

        // Validaciones básicas
        if (latitud == null || longitud == null) {
            throw new RuntimeException("Latitud y longitud son requeridas");
        }

        if (latitud < -90 || latitud > 90) {
            throw new RuntimeException("Latitud debe estar entre -90 y 90 grados");
        }

        if (longitud < -180 || longitud > 180) {
            throw new RuntimeException("Longitud debe estar entre -180 y 180 grados");
        }

        SimpleClientHttpRequestFactory rf = new SimpleClientHttpRequestFactory();
        rf.setConnectTimeout(10000); // Aumentar timeout
        rf.setReadTimeout(15000);
        RestTemplate rt = new RestTemplate(rf);

        String msLocalBase = System.getenv().getOrDefault("MS_LOCALIZACIONES_URL", "http://localhost:8087");
        log.info("Conectando con microservicio de localizaciones en: {}", msLocalBase);

        try {
            String url = msLocalBase + "/api/ubicaciones/por-coordenadas";
            log.info("URL del endpoint: {}", url);

            // Crear el DTO con las coordenadas
            Map<String, Double> coordenadas = new HashMap<>();
            coordenadas.put("latitud", latitud);
            coordenadas.put("longitud", longitud);

            // Configurar headers para enviar JSON
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

            HttpEntity<Map<String, Double>> request = new HttpEntity<>(coordenadas, headers);

            log.info("Enviando payload: {}", coordenadas);

            @SuppressWarnings("unchecked")
            ResponseEntity<Map> response = rt.exchange(url, HttpMethod.POST, request, Map.class);

            log.info("Respuesta del microservicio: status={}, body={}", response.getStatusCode(), response.getBody());

            Map<String, Object> ubicacion = response.getBody();

            if (ubicacion == null) {
                throw new RuntimeException("Respuesta vacía del microservicio de localizaciones");
            }

            Object ubicacionIdObj = ubicacion.get("ubicacionId");
            if (ubicacionIdObj == null) {
                log.error("Respuesta del microservicio no contiene ubicacionId. Contenido: {}", ubicacion);
                throw new RuntimeException("La respuesta del microservicio no contiene ubicacionId válido");
            }

            Integer ubicacionId;
            if (ubicacionIdObj instanceof Number) {
                ubicacionId = ((Number) ubicacionIdObj).intValue();
            } else {
                try {
                    ubicacionId = Integer.parseInt(ubicacionIdObj.toString());
                } catch (NumberFormatException e) {
                    throw new RuntimeException("El ubicacionId recibido no es válido: " + ubicacionIdObj);
                }
            }

            log.info("Ubicación obtenida/creada exitosamente con ID: {}", ubicacionId);
            return ubicacionId;

        } catch (HttpClientErrorException e) {
            log.error("Error HTTP del microservicio de localizaciones: status={}, body={}",
                     e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Error del microservicio de localizaciones (" + e.getStatusCode() + "): " +
                                     e.getResponseBodyAsString());
        } catch (ResourceAccessException e) {
            log.error("Error de conectividad con microservicio de localizaciones: {}", e.getMessage());
            throw new RuntimeException("No se pudo conectar con el microservicio de localizaciones. " +
                                     "Verifica que esté ejecutándose en: " + msLocalBase);
        } catch (Exception e) {
            log.error("Error inesperado obteniendo/creando ubicación: {}", e.getMessage(), e);
            throw new RuntimeException("Error procesando ubicación: " + e.getMessage(), e);
        }
    }

    /**
     * Genera automáticamente la ruta y sus tramos para una solicitud.
     * Implementa la lógica del requerimiento:
     * - Si no hay depósitos: crea un tramo directo origen-destino
     * - Si hay depósitos: evalúa todas las combinaciones y selecciona la ruta más corta
     */
    private Integer generarRutaYTramos(Integer origenId, Integer destinoId) {
        log.info("Generando ruta automática desde ubicación {} hasta {}", origenId, destinoId);

        // RestTemplate para comunicación con otros microservicios
        SimpleClientHttpRequestFactory rf = new SimpleClientHttpRequestFactory();
        rf.setConnectTimeout(5000);
        rf.setReadTimeout(10000);
        RestTemplate rt = new RestTemplate(rf);

        // Usar nombres de contenedores de Docker para comunicación entre microservicios
        String msLocalBase = System.getenv().getOrDefault("MS_LOCALIZACIONES_URL", "http://localhost:8087");
        String msRutasBase = System.getenv().getOrDefault("MS_RUTAS_URL", "http://localhost:8085");

        try {
            // 1) Obtener ubicaciones origen y destino
            @SuppressWarnings("unchecked")
            Map<String, Object> ubicacionOrigen = rt.getForObject(msLocalBase + "/api/ubicaciones/{id}", Map.class, origenId);
            @SuppressWarnings("unchecked")
            Map<String, Object> ubicacionDestino = rt.getForObject(msLocalBase + "/api/ubicaciones/{id}", Map.class, destinoId);

            if (ubicacionOrigen == null || ubicacionDestino == null) {
                throw new RuntimeException("No se pudieron obtener las ubicaciones origen o destino");
            }

            String origenLat = String.valueOf(ubicacionOrigen.get("latitud"));
            String origenLng = String.valueOf(ubicacionOrigen.get("longitud"));
            String destinoLat = String.valueOf(ubicacionDestino.get("latitud"));
            String destinoLng = String.valueOf(ubicacionDestino.get("longitud"));

            // 2) Obtener todos los depósitos
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> depositos = rt.getForObject(msLocalBase + "/api/depositos", List.class);

            if (depositos == null || depositos.isEmpty()) {
                // No hay depósitos: crear ruta con un solo tramo directo
                log.info("No hay depósitos registrados, creando ruta directa");
                return crearRutaDirecta(rt, msRutasBase, origenId, destinoId, origenLng, origenLat, destinoLng, destinoLat);
            }

            // 3) Hay depósitos: evaluar combinaciones y seleccionar la mejor ruta
            log.info("Evaluando {} depósitos para encontrar la ruta óptima", depositos.size());
            return crearRutaOptima(rt, msLocalBase, msRutasBase, origenId, destinoId, origenLng, origenLat,
                                  destinoLng, destinoLat, depositos);

        } catch (Exception e) {
            log.error("Error generando ruta automática: {}", e.getMessage(), e);
            throw new RuntimeException("No se pudo generar la ruta automáticamente: " + e.getMessage(), e);
        }
    }

    /**
     * Crea una ruta directa (sin depósitos intermedios)
     */
    private Integer crearRutaDirecta(RestTemplate rt, String msRutasBase, Integer origenId, Integer destinoId,
                                     String origenLng, String origenLat, String destinoLng, String destinoLat) {
        try {
            // Calcular distancia usando OSRM
            String origenCoords = origenLng + "," + origenLat;
            String destinoCoords = destinoLng + "," + destinoLat;

            String msLocalBase = "http://ms-localizaciones:8087";
            @SuppressWarnings("unchecked")
            Map<String, Object> distanciaResp = rt.getForObject(
                msLocalBase + "/api/distancia?origen={o}&destino={d}", Map.class, origenCoords, destinoCoords);

            Double kilometros = 0.0;
            if (distanciaResp != null && distanciaResp.get("kilometros") != null) {
                Object kmObj = distanciaResp.get("kilometros");
                kilometros = kmObj instanceof Number ? ((Number) kmObj).doubleValue() : Double.parseDouble(String.valueOf(kmObj));
            }

            // Crear la ruta
            Integer rutaId = generateNewRutaId(rt, msRutasBase);
            Map<String, Object> rutaPayload = new HashMap<>();
            rutaPayload.put("rutaId", rutaId);
            rutaPayload.put("cantidadTramos", 1);
            rutaPayload.put("cantidadDepositos", 0);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> rutaRequest = new HttpEntity<>(rutaPayload, headers);
            rt.postForObject(msRutasBase + "/api/rutas/interno", rutaRequest, Map.class);

            log.info("Ruta {} creada con 1 tramo directo", rutaId);

            // Crear el tramo origen-destino (tipo 5 = Directo)
            crearTramo(rt, msRutasBase, rutaId, origenId, destinoId, 5, kilometros);

            return rutaId;

        } catch (Exception e) {
            log.error("Error creando ruta directa: {}", e.getMessage(), e);
            throw new RuntimeException("Error creando ruta directa", e);
        }
    }

    /**
     * Evalúa todas las combinaciones posibles con depósitos y selecciona la ruta más corta
     */
    private Integer crearRutaOptima(RestTemplate rt, String msLocalBase, String msRutasBase,
                                    Integer origenId, Integer destinoId, String origenLng, String origenLat,
                                    String destinoLng, String destinoLat, List<Map<String, Object>> depositos) {
        try {
            // Lista para almacenar todas las rutas posibles con sus distancias
            List<RutaCandidato> candidatos = new ArrayList<>();

            // 1) Ruta directa (sin depósitos)
            String origenCoords = origenLng + "," + origenLat;
            String destinoCoords = destinoLng + "," + destinoLat;
            Double distanciaDirecta = calcularDistancia(rt, msLocalBase, origenCoords, destinoCoords);

            RutaCandidato rutaDirecta = new RutaCandidato();
            rutaDirecta.distanciaTotal = distanciaDirecta;
            rutaDirecta.depositos = new ArrayList<>();
            candidatos.add(rutaDirecta);

            // 2) Rutas con un depósito intermedio
            log.info("Evaluando {} depósitos para rutas con 1 depósito intermedio", depositos.size());
            for (Map<String, Object> deposito : depositos) {
                @SuppressWarnings("unchecked")
                Map<String, Object> ubicacion = (Map<String, Object>) deposito.get("ubicacion");
                if (ubicacion == null) {
                    log.warn("Depósito {} no tiene ubicación asociada, se omite", deposito.get("depositoId"));
                    continue;
                }

                String depLat = String.valueOf(ubicacion.get("latitud"));
                String depLng = String.valueOf(ubicacion.get("longitud"));
                String depCoords = depLng + "," + depLat;
                Integer depUbicacionId = (Integer) ubicacion.get("ubicacionId");

                // VALIDACIÓN: Excluir depósitos que estén en la misma ubicación que el origen o destino
                if (depUbicacionId.equals(origenId)) {
                    log.info("Depósito ubicación {} coincide con el origen, se omite", depUbicacionId);
                    continue;
                }
                if (depUbicacionId.equals(destinoId)) {
                    log.info("Depósito ubicación {} coincide con el destino, se omite", depUbicacionId);
                    continue;
                }

                log.debug("Evaluando depósito ubicación ID {}: coords = {}", depUbicacionId, depCoords);

                // Calcular: origen -> deposito -> destino
                Double dist1 = calcularDistancia(rt, msLocalBase, origenCoords, depCoords);
                Double dist2 = calcularDistancia(rt, msLocalBase, depCoords, destinoCoords);
                Double distanciaTotal = dist1 + dist2;

                log.info("Ruta con depósito {}: {}km (origen->dep) + {}km (dep->destino) = {}km total",
                        depUbicacionId, dist1, dist2, distanciaTotal);

                RutaCandidato candidato = new RutaCandidato();
                candidato.distanciaTotal = distanciaTotal;
                candidato.depositos = new ArrayList<>();
                candidato.depositos.add(depUbicacionId);
                candidatos.add(candidato);
            }

            // 3) Rutas con dos depósitos intermedios
            log.info("Evaluando combinaciones de 2 depósitos intermedios");
            int combinacionesEvaluadas = 0;
            for (int i = 0; i < depositos.size(); i++) {
                for (int j = 0; j < depositos.size(); j++) {
                    if (i == j) continue; // No usar el mismo depósito dos veces

                    @SuppressWarnings("unchecked")
                    Map<String, Object> ubicacion1 = (Map<String, Object>) depositos.get(i).get("ubicacion");
                    @SuppressWarnings("unchecked")
                    Map<String, Object> ubicacion2 = (Map<String, Object>) depositos.get(j).get("ubicacion");

                    if (ubicacion1 == null || ubicacion2 == null) {
                        log.warn("Depósito {} o {} sin ubicación, se omite combinación", i, j);
                        continue;
                    }

                    String dep1Coords = ubicacion1.get("longitud") + "," + ubicacion1.get("latitud");
                    String dep2Coords = ubicacion2.get("longitud") + "," + ubicacion2.get("latitud");
                    Integer dep1Id = (Integer) ubicacion1.get("ubicacionId");
                    Integer dep2Id = (Integer) ubicacion2.get("ubicacionId");

                    // VALIDACIÓN: Excluir depósitos que coincidan con origen o destino
                    if (dep1Id.equals(origenId) || dep1Id.equals(destinoId) ||
                        dep2Id.equals(origenId) || dep2Id.equals(destinoId)) {
                        log.debug("Combinación [{}, {}] omitida: coincide con origen {} o destino {}",
                                dep1Id, dep2Id, origenId, destinoId);
                        continue;
                    }

                    // Calcular: origen -> dep1 -> dep2 -> destino
                    Double dist1 = calcularDistancia(rt, msLocalBase, origenCoords, dep1Coords);
                    Double dist2 = calcularDistancia(rt, msLocalBase, dep1Coords, dep2Coords);
                    Double dist3 = calcularDistancia(rt, msLocalBase, dep2Coords, destinoCoords);
                    Double distanciaTotal = dist1 + dist2 + dist3;

                    combinacionesEvaluadas++;
                    log.debug("Ruta con 2 depósitos [{}, {}]: {}km + {}km + {}km = {}km",
                            dep1Id, dep2Id, dist1, dist2, dist3, distanciaTotal);

                    RutaCandidato candidato = new RutaCandidato();
                    candidato.distanciaTotal = distanciaTotal;
                    candidato.depositos = new ArrayList<>();
                    candidato.depositos.add(dep1Id);
                    candidato.depositos.add(dep2Id);
                    candidatos.add(candidato);
                }
            }
            log.info("Se evaluaron {} combinaciones de 2 depósitos", combinacionesEvaluadas);

            // RESTRICCIÓN DE NEGOCIO: Si la distancia directa > 100 km, DEBE usar depósitos
            // NOTA: Valor temporal reducido de 500 a 100 para pruebas
            final double DISTANCIA_MAXIMA_DIRECTA = 100.0;

            log.info("Distancia directa calculada: {} km. Límite: {} km", distanciaDirecta, DISTANCIA_MAXIMA_DIRECTA);
            log.info("Total de candidatos evaluados: {}", candidatos.size());

            RutaCandidato mejorRuta;
            if (distanciaDirecta > DISTANCIA_MAXIMA_DIRECTA) {
                // Filtrar solo rutas con depósitos
                List<RutaCandidato> candidatosConDepositos = candidatos.stream()
                    .filter(c -> !c.depositos.isEmpty())
                    .collect(java.util.stream.Collectors.toList());

                log.info("Distancia directa {} km supera el límite de {} km. Candidatos con depósitos: {}",
                        distanciaDirecta, DISTANCIA_MAXIMA_DIRECTA, candidatosConDepositos.size());

                if (candidatosConDepositos.isEmpty()) {
                    throw new RuntimeException("No se encontró ruta con depósitos para distancia > 500 km. Se requieren depósitos intermedios.");
                }

                mejorRuta = candidatosConDepositos.stream()
                    .min(Comparator.comparing(c -> c.distanciaTotal))
                    .orElseThrow(() -> new RuntimeException("No se pudo seleccionar ruta con depósitos"));

                log.info("Ruta con depósitos obligatoria seleccionada: {} km, {} depósitos",
                        mejorRuta.distanciaTotal, mejorRuta.depositos.size());
            } else {
                // Permitir ruta directa o con depósitos (la más corta)
                mejorRuta = candidatos.stream()
                    .min(Comparator.comparing(c -> c.distanciaTotal))
                    .orElse(rutaDirecta);

                log.info("Ruta óptima seleccionada (permite directa): {} km, {} depósitos",
                        mejorRuta.distanciaTotal, mejorRuta.depositos.size());
            }

            log.info("Mejor ruta seleccionada FINAL: {} km, {} depósitos, IDs: {}",
                    mejorRuta.distanciaTotal, mejorRuta.depositos.size(), mejorRuta.depositos);

            // Crear la ruta y sus tramos
            Integer rutaId = generateNewRutaId(rt, msRutasBase);
            int cantidadTramos = mejorRuta.depositos.isEmpty() ? 1 : mejorRuta.depositos.size() + 1;

            Map<String, Object> rutaPayload = new HashMap<>();
            rutaPayload.put("rutaId", rutaId);
            rutaPayload.put("cantidadTramos", cantidadTramos);
            rutaPayload.put("cantidadDepositos", mejorRuta.depositos.size());

            log.info("Creando ruta con payload: {}", rutaPayload);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> rutaRequest = new HttpEntity<>(rutaPayload, headers);

            try {
                Map<String, Object> rutaResponse = rt.postForObject(msRutasBase + "/api/rutas/interno", rutaRequest, Map.class);
                log.info("Ruta {} creada exitosamente. Respuesta: {}", rutaId, rutaResponse);
            } catch (HttpClientErrorException e) {
                log.error("Error creando ruta HTTP {}: {}, Body: {}", e.getStatusCode(), e.getMessage(), e.getResponseBodyAsString());
                throw new RuntimeException("Error creando ruta: " + e.getResponseBodyAsString(), e);
            }

            log.info("Ruta {} lista para crear {} tramos con {} depósitos", rutaId, cantidadTramos, mejorRuta.depositos.size());

            // Crear los tramos según la secuencia
            if (mejorRuta.depositos.isEmpty()) {
                // Tramo directo
                log.info("Creando tramo directo: origen {} -> destino {}", origenId, destinoId);
                crearTramo(rt, msRutasBase, rutaId, origenId, destinoId, 5, distanciaDirecta);
            } else {
                // Tramos con depósitos
                log.info("Creando ruta con {} depósitos. IDs de depósitos: {}", mejorRuta.depositos.size(), mejorRuta.depositos);

                Integer puntoActual = origenId;
                for (int i = 0; i < mejorRuta.depositos.size(); i++) {
                    Integer depositoId = mejorRuta.depositos.get(i);

                    // Validación: No crear tramo si origen y destino son iguales
                    if (puntoActual.equals(depositoId)) {
                        log.warn("ADVERTENCIA: Intento de crear tramo con origen y destino iguales ({}), se omite", puntoActual);
                        continue;
                    }

                    int tipoTramo = 4; // Tipo 4 = Con Depósito

                    log.info("Tramo {}: {} -> {} (depósito)", i+1, puntoActual, depositoId);

                    // Obtener ubicación del depósito para calcular distancia
                    @SuppressWarnings("unchecked")
                    Map<String, Object> ubicOrigen = rt.getForObject(msLocalBase + "/api/ubicaciones/{id}", Map.class, puntoActual);
                    @SuppressWarnings("unchecked")
                    Map<String, Object> ubicDep = rt.getForObject(msLocalBase + "/api/ubicaciones/{id}", Map.class, depositoId);

                    String coords1 = ubicOrigen.get("longitud") + "," + ubicOrigen.get("latitud");
                    String coords2 = ubicDep.get("longitud") + "," + ubicDep.get("latitud");
                    Double dist = calcularDistancia(rt, msLocalBase, coords1, coords2);

                    log.info("Distancia calculada para tramo {} -> {}: {} km", puntoActual, depositoId, dist);
                    crearTramo(rt, msRutasBase, rutaId, puntoActual, depositoId, tipoTramo, dist);
                    puntoActual = depositoId;
                }

                // Validación: Solo crear último tramo si puntoActual != destinoId
                if (!puntoActual.equals(destinoId)) {
                    log.info("Último tramo: {} -> {} (destino final)", puntoActual, destinoId);

                    // Último tramo: último depósito -> destino
                    @SuppressWarnings("unchecked")
                    Map<String, Object> ubicDep = rt.getForObject(msLocalBase + "/api/ubicaciones/{id}", Map.class, puntoActual);
                    @SuppressWarnings("unchecked")
                    Map<String, Object> ubicDest = rt.getForObject(msLocalBase + "/api/ubicaciones/{id}", Map.class, destinoId);

                    String coords1 = ubicDep.get("longitud") + "," + ubicDep.get("latitud");
                    String coords2 = ubicDest.get("longitud") + "," + ubicDest.get("latitud");
                    Double dist = calcularDistancia(rt, msLocalBase, coords1, coords2);

                    log.info("Distancia calculada para último tramo {} -> {}: {} km", puntoActual, destinoId, dist);
                    crearTramo(rt, msRutasBase, rutaId, puntoActual, destinoId, 4, dist);
                } else {
                    log.warn("ADVERTENCIA: El último depósito ({}) es igual al destino ({}), no se crea tramo duplicado", puntoActual, destinoId);
                }
            }

            return rutaId;

        } catch (Exception e) {
            log.error("Error creando ruta óptima: {}", e.getMessage(), e);
            throw new RuntimeException("Error creando ruta óptima", e);
        }
    }

    /**
     * Calcula la distancia entre dos puntos usando OSRM
     */
    private Double calcularDistancia(RestTemplate rt, String msLocalBase, String origen, String destino) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> resp = rt.getForObject(
                msLocalBase + "/api/distancia?origen={o}&destino={d}", Map.class, origen, destino);

            if (resp != null && resp.get("kilometros") != null) {
                Object kmObj = resp.get("kilometros");
                return kmObj instanceof Number ? ((Number) kmObj).doubleValue() : Double.parseDouble(String.valueOf(kmObj));
            }
            return 0.0;
        } catch (Exception e) {
            log.warn("Error calculando distancia entre {} y {}: {}", origen, destino, e.getMessage());
            return 0.0;
        }
    }

    /**
     * Crea un tramo en el microservicio ms-rutas
     */
    private void crearTramo(RestTemplate rt, String msRutasBase, Integer rutaId, Integer origenId,
                           Integer destinoId, Integer tipoTramo, Double distanciaKm) {
        try {
            Integer tramoId = generateNewTramoId(rt, msRutasBase);

            TramoCrearDto tramoDto = TramoCrearDto.builder()
                    .tramoId(tramoId)
                    .rutaId(rutaId)
                    .tipoTramoId(tipoTramo)
                    .dominio(null) // Valor por defecto para tramos sin camión asignado
                    .ubicacionOrigenId(origenId)
                    .ubicacionDestinoId(destinoId)
                    .transportistaId(null) // Se asignará manualmente por tramo
                    .costoAproximado(distanciaKm != null ? BigDecimal.valueOf(distanciaKm) : BigDecimal.ZERO)
                    .costoReal(null)
                    .fechaHoraInicio(null) // Se establecerá cuando se inicie el tramo explícitamente
                    .fechaHoraFin(null)
                    .fechaHoraEstimadaFin(null)
                    .build();

            log.info("Creando tramo con DTO: {}", tramoDto);
            log.info("Enviando request a: {}/api/tramos/interno", msRutasBase);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<TramoCrearDto> tramoRequest = new HttpEntity<>(tramoDto, headers);

            Map<String, Object> tramoResponse = rt.postForObject(msRutasBase + "/api/tramos/interno", tramoRequest, Map.class);

            log.info("Tramo {} creado exitosamente: ruta={}, tipo={}, origen={}, destino={}, distancia={} km. Respuesta: {}",
                     tramoId, rutaId, tipoTramo, origenId, destinoId, distanciaKm, tramoResponse);

        } catch (HttpClientErrorException e) {
            log.error("Error creando tramo HTTP {}: {}, Body: '{}'", e.getStatusCode(), e.getMessage(), e.getResponseBodyAsString());
            log.error("Headers de respuesta: {}", e.getResponseHeaders());
            throw new RuntimeException("Error creando tramo: " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            log.error("Error inesperado creando tramo: {}", e.getMessage(), e);
            throw new RuntimeException("Error creando tramo", e);
        }
    }

    /**
     * Genera un nuevo ID para una ruta
     */
    private Integer generateNewRutaId(RestTemplate rt, String msRutasBase) {
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> rutas = rt.getForObject(msRutasBase + "/api/rutas", List.class);
            if (rutas == null || rutas.isEmpty()) return 1;

            return rutas.stream()
                .map(r -> (Integer) r.get("rutaId"))
                .max(Comparator.naturalOrder())
                .orElse(0) + 1;
        } catch (Exception e) {
            log.warn("Error obteniendo rutas existentes, usando ID 1: {}", e.getMessage());
            return 1;
        }
    }

    /**
     * Genera un nuevo ID para un tramo
     */
    private Integer generateNewTramoId(RestTemplate rt, String msRutasBase) {
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> tramos = rt.getForObject(msRutasBase + "/api/tramos", List.class);
            if (tramos == null || tramos.isEmpty()) return 1;

            return tramos.stream()
                .map(t -> (Integer) t.get("tramoId"))
                .max(Comparator.naturalOrder())
                .orElse(0) + 1;
        } catch (Exception e) {
            log.warn("Error obteniendo tramos existentes, usando ID 1: {}", e.getMessage());
            return 1;
        }
    }

    /**
     * Clase auxiliar para almacenar candidatos de ruta
     */
    private static class RutaCandidato {
        Double distanciaTotal;
        List<Integer> depositos;
    }

    public List<Solicitud> obtenerPorCliente(Integer tipoDoc, Long numDoc) {
        log.info("Obteniendo solicitudes del cliente: {} - {}", tipoDoc, numDoc);
        return solicitudRepository.findByTipoDocClienteAndNumDocCliente(tipoDoc, numDoc);
    }

    public List<Solicitud> obtenerPendientes() {
        log.info("Obteniendo solicitudes pendientes");
        // Estados: 1=Creada, 2=En Proceso, 3=Finalizada
        // Pendientes son las que no están finalizadas (estado != 3)
        return solicitudRepository.findByEstadoSolicitudNot(3);
    }

    public List<Solicitud> obtenerPorUbicacionDestino(Integer ubicacionId) {
        log.info("Obteniendo solicitudes con destino en ubicación: {}", ubicacionId);
        return solicitudRepository.findByIdUbicacionDestino(ubicacionId);
    }

    @Transactional
    public Solicitud asignarRuta(Integer solicitudId, Integer rutaId) {
        log.info("Asignando ruta {} a solicitud {}", rutaId, solicitudId);
        return solicitudRepository.findById(solicitudId)
                .map(solicitud -> {
                    solicitud.setIdRuta(rutaId);
                    return solicitudRepository.save(solicitud);
                })
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada con id: " + solicitudId));
    }

    /**
     * Finaliza la solicitud y registra el tiempo real y costo real.
     * Implementación mínima y síncrona: llama a ms-localizaciones para obtener coords y distancia
     * y a ms-precios para calcular el precio. No crea archivos nuevos.
     */
    @Transactional
    public Solicitud finalizarYRegistrarCalculos(Integer solicitudId) {
        log.info("Finalizando solicitud {} y registrando cálculos reales (modo simple)", solicitudId);

        Solicitud solicitud = solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada: " + solicitudId));

        // Obtener contenedor para peso/volumen
        Contenedor contenedor = contenedorService.obtenerPorId(solicitud.getIdContenedor())
                .orElseThrow(() -> new RuntimeException("Contenedor no encontrado: " + solicitud.getIdContenedor()));

        // RestTemplate local con timeouts cortos
        SimpleClientHttpRequestFactory rf = new SimpleClientHttpRequestFactory();
        rf.setConnectTimeout(5000);
        rf.setReadTimeout(10000);
        RestTemplate rt = new RestTemplate(rf);

        // URLs ajustadas a la configuración de docker-compose
        String msLocalBase = "http://ms-localizaciones:8087";
        String msPreciosBase = "http://ms-precios:8083";

        try {
            // 1) Intentar obtener coordenadas y distancia desde ms-localizaciones (opcional)
            Double kilometros = null;
            String duracionTexto = null;
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> origen = rt.getForObject(msLocalBase + "/api/ubicaciones/{id}", Map.class, solicitud.getIdUbicacionOrigen());
                @SuppressWarnings("unchecked")
                Map<String, Object> destino = rt.getForObject(msLocalBase + "/api/ubicaciones/{id}", Map.class, solicitud.getIdUbicacionDestino());

                if (origen != null && destino != null) {
                    String origenCoords = Objects.toString(origen.get("longitud"), "") + "," + Objects.toString(origen.get("latitud"), "");
                    String destinoCoords = Objects.toString(destino.get("longitud"), "") + "," + Objects.toString(destino.get("latitud"), "");

                    @SuppressWarnings("unchecked")
                    Map<String, Object> distanciaResp = rt.getForObject(msLocalBase + "/api/distancia?origen={o}&destino={d}", Map.class, origenCoords, destinoCoords);

                    if (distanciaResp != null) {
                        Object kmObj = distanciaResp.get("kilometros");
                        if (kmObj instanceof Number) kilometros = ((Number) kmObj).doubleValue();
                        else if (kmObj != null) kilometros = Double.parseDouble(String.valueOf(kmObj));
                        duracionTexto = distanciaResp.get("duracionTexto") != null ? String.valueOf(distanciaResp.get("duracionTexto")) : null;
                    }
                } else {
                    log.warn("ms-localizaciones devolvió ubicaciones nulas para ids {} -> {}", solicitud.getIdUbicacionOrigen(), solicitud.getIdUbicacionDestino());
                }
            } catch (ResourceAccessException rae) {
                // Fallback: ms-localizaciones no disponible (Connection refused), seguir con ms-precios
                log.warn("ms-localizaciones no disponible (fallback): {}. Se continuará llamando a ms-precios para cálculo estimado", rae.getMessage());
            }

             // 3) Llamar a ms-precios para calcular el costo real usando /api/cotizaciones/calcular
             Map<String, Object> payload = new HashMap<>();
             payload.put("ubicacionOrigenId", solicitud.getIdUbicacionOrigen());
             payload.put("ubicacionDestinoId", solicitud.getIdUbicacionDestino());
             payload.put("pesoKg", contenedor.getPesoKg());
             payload.put("volumenM3", contenedor.getVolumenM3());
             payload.put("tipoServicio", null);
             payload.put("esUrgente", false);
             payload.put("observaciones", solicitud.getTextoAdicional());
             payload.put("tipoDocCliente", solicitud.getTipoDocCliente());
             payload.put("numDocCliente", solicitud.getNumDocCliente());

             HttpHeaders headers = new HttpHeaders();
             headers.setContentType(MediaType.APPLICATION_JSON);
             HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(payload, headers);

             // Manejar errores 4xx de ms-precios (por ejemplo: no hay tarifas vigentes -> RuntimeException en el servicio)
             @SuppressWarnings("unchecked")
             Map<String, Object> precioResp = null;
             try {
                 precioResp = rt.postForObject(msPreciosBase + "/api/cotizaciones/calcular", requestEntity, Map.class);
             } catch (HttpClientErrorException hce) {
                 // ms-precios devolvió 4xx (ej. 400 cuando no hay tarifas vigentes). No abortar la finalización; solo loggear y continuar con precio null.
                 String body = "";
                 try { body = hce.getResponseBodyAsString(); } catch (Exception ex) { /* ignore */ }
                 log.warn("ms-precios respondió con error {}: {} - cuerpo='{}' - se hará fallback sin costoReal", hce.getStatusCode(), hce.getMessage(), body);
                 precioResp = null;
             }

             BigDecimal precioFinal = null;
             if (precioResp != null && precioResp.get("precioFinal") != null) {
                 precioFinal = new BigDecimal(String.valueOf(precioResp.get("precioFinal")));
             }

             // 4) Guardar en la entidad solicitud: costoReal y marcar fechas (uso LocalDateTime ahora)
             if (precioFinal != null) {
                 solicitud.setCostoReal(precioFinal);
             }

             // Fecha inicio si no estaba y fecha fin ahora
             LocalDateTime ahora = LocalDateTime.now();
             if (solicitud.getFechaHoraInicio() == null) solicitud.setFechaHoraInicio(ahora);
             solicitud.setFechaHoraFin(ahora);

             // Actualizar estado a finalizada (3)
             solicitud.setEstadoSolicitud(3);

             Solicitud guardada = solicitudRepository.save(solicitud);

             // Actualizar el contenedor asociado a estado 3 "Entregado"
             log.info("Actualizando contenedor {} a estado 3 (Entregado) para solicitud finalizada {}", solicitud.getIdContenedor(), solicitudId);
             contenedor.setIdEstadoContenedor(3); // 3 = "Entregado"
             contenedorService.guardar(contenedor);
             log.info("Contenedor {} actualizado a estado 'Entregado' exitosamente", solicitud.getIdContenedor());

             log.info("Solicitud {} finalizada: costoReal={} km={} duracion={}, contenedor {} actualizado a 'Entregado'",
                     solicitudId, precioFinal, kilometros, duracionTexto, solicitud.getIdContenedor());
             return guardada;

         } catch (Exception e) {
             log.error("Error finalizando solicitud {}: {}", solicitudId, e.getMessage(), e);
             throw new RuntimeException("No se pudo finalizar y registrar cálculos: " + e.getMessage(), e);
         }
    }

    // Obtener el estado actual de un contenedor para un cliente (una sola solicitud más reciente)
    public Optional<Solicitud> obtenerEstadoContenedorParaCliente(Integer idContenedor, Integer tipoDoc, Long numDoc) {
        log.info("Obteniendo estado del contenedor {} para cliente {} - {}", idContenedor, tipoDoc, numDoc);
        return solicitudRepository.findTopByIdContenedorAndTipoDocClienteAndNumDocClienteOrderByFechaHoraInicioDesc(
                idContenedor, tipoDoc, numDoc);
    }

    // Modificado: obtener todos los contenedores relacionados a un cliente con su solicitud más reciente
    public List<Solicitud> obtenerContenedoresPorClienteConEstado(Integer tipoDoc, Long numDoc) {
        log.info("Obteniendo contenedores y estado más reciente para cliente {} - {}", tipoDoc, numDoc);
        // Usar el método del repositorio que ya devuelve ordenado por fechaHoraInicio desc
        List<Solicitud> solicitudes = solicitudRepository.findByTipoDocClienteAndNumDocClienteOrderByFechaHoraInicioDesc(tipoDoc, numDoc);

        solicitudes.sort((a, b) -> {
            if (a.getFechaHoraInicio() == null && b.getFechaHoraInicio() == null) return 0;
            if (a.getFechaHoraInicio() == null) return 1;
            if (b.getFechaHoraInicio() == null) return -1;
            return b.getFechaHoraInicio().compareTo(a.getFechaHoraInicio()); // desc
        });

        // Mantener un map por idContenedor con la primera (más reciente) aparición, guardando la Solicitud completa
        Map<Integer, Solicitud> latestByContenedor = new LinkedHashMap<>();
        for (Solicitud s : solicitudes) {
            Integer idCont = s.getIdContenedor();
            if (!latestByContenedor.containsKey(idCont)) {
                latestByContenedor.put(idCont, s);
            }
        }

        return new ArrayList<>(latestByContenedor.values());
    }

    /**
     * Calcula un costo estimado aleatorio entre 10,000 y 500,000 pesos
     */
    private BigDecimal calcularCostoEstimadoAleatorio() {
        // Generar un costo entre 10,000 y 500,000 pesos
        int costoMin = 10000;
        int costoMax = 500000;
        int costo = random.nextInt(costoMax - costoMin + 1) + costoMin;

        log.info("Costo estimado calculado aleatoriamente: {}", costo);
        return BigDecimal.valueOf(costo);
    }

    /**
     * Calcula una fecha y hora estimada de fin aleatoria entre 1 y 30 días desde ahora
     */
    private LocalDateTime calcularFechaHoraEstimadaFinAleatoria() {
        // Generar una fecha entre 1 y 30 días en el futuro
        int diasMin = 1;
        int diasMax = 30;
        int dias = random.nextInt(diasMax - diasMin + 1) + diasMin;

        // Agregar horas aleatorias (0-23) y minutos aleatorios (0-59)
        int horas = random.nextInt(24);
        int minutos = random.nextInt(60);

        LocalDateTime fechaEstimada = LocalDateTime.now()
                .plusDays(dias)
                .withHour(horas)
                .withMinute(minutos)
                .withSecond(0)
                .withNano(0);

        log.info("Fecha hora estimada fin calculada aleatoriamente: {}", fechaEstimada);
        return fechaEstimada;
    }

    /**
     * Obtiene una solicitud por su rutaId
     */
    public Optional<Solicitud> obtenerPorRuta(Integer rutaId) {
        log.info("Obteniendo solicitud por rutaId: {}", rutaId);
        return solicitudRepository.findFirstByIdRuta(rutaId);
    }
}
