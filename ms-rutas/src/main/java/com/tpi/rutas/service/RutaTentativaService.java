package com.tpi.rutas.service;

import com.tpi.rutas.dto.RutaTentativaDTO;
import com.tpi.rutas.dto.TramoTentativoDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RutaTentativaService {

    @Value("${localizaciones.service.url}")
    private String msLocalizacionesUrl;

    private final RestTemplate restTemplate;
    private final CalculoRutaService calculoRutaService;

    /**
     * Genera exactamente 3 rutas tentativas basadas en coordenadas de origen y destino
     */
    public List<RutaTentativaDTO> generarRutasTentativas(Double latOrigen, Double lngOrigen,
                                                          Double latDestino, Double lngDestino) {
        try {
            log.info("Generando rutas tentativas para origen ({},{}) -> destino ({},{})",
                     latOrigen, lngOrigen, latDestino, lngDestino);

            log.info("Conectando a ms-localizaciones en: {}", msLocalizacionesUrl);

            // 1. Obtener o crear ubicaciones
            Integer origenId = obtenerOCrearUbicacion(latOrigen, lngOrigen);
            Integer destinoId = obtenerOCrearUbicacion(latDestino, lngDestino);

            // 2. Obtener depósitos disponibles
            List<Map<String, Object>> depositos = obtenerDepositos();

            // 3. Evaluar todas las combinaciones posibles
            List<RutaCandidato> candidatos = evaluarCombinaciones(origenId, destinoId,
                                                                   lngOrigen.toString(), latOrigen.toString(),
                                                                   lngDestino.toString(), latDestino.toString(),
                                                                   depositos);

            // 4. Seleccionar las 3 mejores rutas diferentes
            List<RutaCandidato> mejoresTres = seleccionarMejoresTres(candidatos);

            // 5. Convertir a DTOs
            return convertirADTOs(mejoresTres, origenId, destinoId);

        } catch (Exception e) {
            log.error("Error generando rutas tentativas: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    private Integer obtenerOCrearUbicacion(Double latitud, Double longitud) {
        try {
            String url = msLocalizacionesUrl + "/api/ubicaciones/por-coordenadas";
            log.info("Llamando a ms-localizaciones: {}", url);

            Map<String, Double> coordenadas = new HashMap<>();
            coordenadas.put("latitud", latitud);
            coordenadas.put("longitud", longitud);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Double>> request = new HttpEntity<>(coordenadas, headers);

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(url, request, Map.class);

            if (response != null && response.get("ubicacionId") != null) {
                return (Integer) response.get("ubicacionId");
            }

            throw new RuntimeException("No se pudo obtener ubicación para coordenadas: " + latitud + "," + longitud);

        } catch (Exception e) {
            log.error("Error obteniendo ubicación para coordenadas {},{}: {}", latitud, longitud, e.getMessage());
            throw new RuntimeException("Error obteniendo ubicación", e);
        }
    }

    private List<Map<String, Object>> obtenerDepositos() {
        try {
            String url = msLocalizacionesUrl + "/api/depositos";
            log.info("Obteniendo depósitos desde: {}", url);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> depositos = restTemplate.getForObject(url, List.class);

            if (depositos == null || depositos.isEmpty()) {
                log.warn("No se encontraron depósitos disponibles");
                return Collections.emptyList();
            }

            log.info("Se obtuvieron {} depósitos", depositos.size());
            return depositos;

        } catch (Exception e) {
            log.error("Error obteniendo depósitos: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private List<RutaCandidato> evaluarCombinaciones(Integer origenId, Integer destinoId,
                                                     String lngOrigen, String latOrigen,
                                                     String lngDestino, String latDestino,
                                                     List<Map<String, Object>> depositos) {

        List<RutaCandidato> candidatos = new ArrayList<>();
        String origenCoords = lngOrigen + "," + latOrigen;
        String destinoCoords = lngDestino + "," + latDestino;

        // Nota: Ya no incluimos ruta directa - todas las rutas deben tener al menos 2 tramos (mínimo 1 depósito)

        // 2. Rutas con un depósito intermedio
        for (Map<String, Object> deposito : depositos) {
            @SuppressWarnings("unchecked")
            Map<String, Object> ubicacion = (Map<String, Object>) deposito.get("ubicacion");
            if (ubicacion == null) continue;

            Integer depUbicacionId = (Integer) ubicacion.get("ubicacionId");

            // Validar que no coincida con origen o destino
            if (depUbicacionId.equals(origenId) || depUbicacionId.equals(destinoId)) {
                continue;
            }

            String depLat = String.valueOf(ubicacion.get("latitud"));
            String depLng = String.valueOf(ubicacion.get("longitud"));
            String depCoords = depLng + "," + depLat;

            Double dist1 = calcularDistancia(origenCoords, depCoords);
            Double dist2 = calcularDistancia(depCoords, destinoCoords);
            Double distanciaTotal = dist1 + dist2;

            RutaCandidato candidato = new RutaCandidato();
            candidato.distanciaTotal = distanciaTotal;
            candidato.depositos = Collections.singletonList(depUbicacionId);
            candidato.tipo = "Ruta con 1 Depósito";
            candidatos.add(candidato);
        }

        // 3. Rutas con dos depósitos intermedios (limitado para no generar demasiados candidatos)
        int maxCombinaciones = Math.min(depositos.size(), 5); // Limitar a 5 depósitos para evitar explosión combinatorial

        for (int i = 0; i < maxCombinaciones; i++) {
            for (int j = 0; j < maxCombinaciones; j++) {
                if (i == j) continue;

                @SuppressWarnings("unchecked")
                Map<String, Object> ubicacion1 = (Map<String, Object>) depositos.get(i).get("ubicacion");
                @SuppressWarnings("unchecked")
                Map<String, Object> ubicacion2 = (Map<String, Object>) depositos.get(j).get("ubicacion");

                if (ubicacion1 == null || ubicacion2 == null) continue;

                Integer dep1Id = (Integer) ubicacion1.get("ubicacionId");
                Integer dep2Id = (Integer) ubicacion2.get("ubicacionId");

                if (dep1Id.equals(origenId) || dep1Id.equals(destinoId) ||
                    dep2Id.equals(origenId) || dep2Id.equals(destinoId)) {
                    continue;
                }

                String dep1Coords = ubicacion1.get("longitud") + "," + ubicacion1.get("latitud");
                String dep2Coords = ubicacion2.get("longitud") + "," + ubicacion2.get("latitud");

                Double dist1 = calcularDistancia(origenCoords, dep1Coords);
                Double dist2 = calcularDistancia(dep1Coords, dep2Coords);
                Double dist3 = calcularDistancia(dep2Coords, destinoCoords);
                Double distanciaTotal = dist1 + dist2 + dist3;

                RutaCandidato candidato = new RutaCandidato();
                candidato.distanciaTotal = distanciaTotal;
                candidato.depositos = Arrays.asList(dep1Id, dep2Id);
                candidato.tipo = "Ruta con 2 Depósitos";
                candidatos.add(candidato);
            }
        }

        return candidatos;
    }

    private List<RutaCandidato> seleccionarMejoresTres(List<RutaCandidato> candidatos) {
        if (candidatos.isEmpty()) {
            return Collections.emptyList();
        }

        // Agrupar por tipo y seleccionar el mejor de cada tipo
        Map<String, RutaCandidato> mejorPorTipo = candidatos.stream()
            .collect(Collectors.groupingBy(
                c -> c.tipo,
                Collectors.minBy(Comparator.comparing(c -> c.distanciaTotal))
            ))
            .entrySet().stream()
            .filter(entry -> entry.getValue().isPresent())
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> entry.getValue().get()
            ));

        List<RutaCandidato> resultado = new ArrayList<>();

        // Agregar en orden de preferencia: 1 depósito, 2 depósitos
        if (mejorPorTipo.containsKey("Ruta con 1 Depósito")) {
            resultado.add(mejorPorTipo.get("Ruta con 1 Depósito"));
        }
        if (mejorPorTipo.containsKey("Ruta con 2 Depósitos")) {
            resultado.add(mejorPorTipo.get("Ruta con 2 Depósitos"));
        }

        // Si no tenemos 3, agregar más candidatos ordenados por distancia
        if (resultado.size() < 3) {
            candidatos.stream()
                .filter(c -> !resultado.contains(c))
                .sorted(Comparator.comparing(c -> c.distanciaTotal))
                .limit(3 - resultado.size())
                .forEach(resultado::add);
        }

        return resultado.stream().limit(3).collect(Collectors.toList());
    }

    private List<RutaTentativaDTO> convertirADTOs(List<RutaCandidato> candidatos, Integer origenId, Integer destinoId) {
        List<RutaTentativaDTO> resultado = new ArrayList<>();
        int rutaCounter = 1;

        for (RutaCandidato candidato : candidatos) {
            List<TramoTentativoDTO> tramos = crearTramosTentativos(candidato, origenId, destinoId);

            // Calcular costo estimado
            BigDecimal costoTramos = tramos.stream()
                .map(TramoTentativoDTO::getCostoAproximado)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal costoEstadia = BigDecimal.valueOf(candidato.depositos.size() * 100); // $100 por día por depósito
            BigDecimal costoTotal = costoTramos.add(costoEstadia);

            // Calcular tiempo estimado
            BigDecimal distanciaTotal = BigDecimal.valueOf(candidato.distanciaTotal);
            BigDecimal tiempoEstimado = calculoRutaService.calcularTiempoEstimado(distanciaTotal, candidato.depositos.size());

            RutaTentativaDTO rutaDto = RutaTentativaDTO.builder()
                .rutaId(rutaCounter++) // ID temporal para mostrar
                .cantidadTramos(tramos.size())
                .cantidadDepositos(candidato.depositos.size())
                .tramos(tramos)
                .tiempoEstimadoHoras(tiempoEstimado)
                .costoEstimadoTotal(costoTotal)
                .build();

            resultado.add(rutaDto);
        }

        return resultado;
    }

    private List<TramoTentativoDTO> crearTramosTentativos(RutaCandidato candidato, Integer origenId, Integer destinoId) {
        List<TramoTentativoDTO> tramos = new ArrayList<>();
        int tramoCounter = 1;

        // Todas las rutas deben tener al menos un depósito (mínimo 2 tramos)
        if (!candidato.depositos.isEmpty()) {
            // Tramos con depósitos
            Integer actualOrigen = origenId;

            for (int i = 0; i < candidato.depositos.size(); i++) {
                Integer depositoId = candidato.depositos.get(i);
                BigDecimal distanciaTramo = calculoRutaService.calcularDistanciaAleatoria(); // Simplificado

                TramoTentativoDTO tramo = TramoTentativoDTO.builder()
                    .tramoId(tramoCounter++)
                    .rutaId(candidato.hashCode())
                    .orden(i + 1)
                    .ubicacionOrigenId(actualOrigen)
                    .ubicacionDestinoId(depositoId)
                    .distanciaKm(distanciaTramo)
                    .costoAproximado(calculoRutaService.calcularCostoAproximado(distanciaTramo))
                    .build();

                tramos.add(tramo);
                actualOrigen = depositoId;
            }

            // Último tramo: último depósito -> destino
            BigDecimal distanciaFinal = calculoRutaService.calcularDistanciaAleatoria();
            TramoTentativoDTO tramoFinal = TramoTentativoDTO.builder()
                .tramoId(tramoCounter)
                .rutaId(candidato.hashCode())
                .orden(candidato.depositos.size() + 1)
                .ubicacionOrigenId(actualOrigen)
                .ubicacionDestinoId(destinoId)
                .distanciaKm(distanciaFinal)
                .costoAproximado(calculoRutaService.calcularCostoAproximado(distanciaFinal))
                .build();

            tramos.add(tramoFinal);
        } else {
            // Esto no debería suceder ya que no generamos rutas sin depósitos
            log.error("Se intentó crear tramos para una ruta sin depósitos - esto no debería ocurrir");
            throw new IllegalStateException("Todas las rutas deben tener al menos un depósito");
        }

        return tramos;
    }

    private Double calcularDistancia(String origen, String destino) {
        try {
            String url = msLocalizacionesUrl + "/api/distancia?origen={o}&destino={d}";
            log.debug("Calculando distancia: {} -> {} usando {}", origen, destino, url);

            @SuppressWarnings("unchecked")
            Map<String, Object> resp = restTemplate.getForObject(url, Map.class, origen, destino);

            if (resp != null && resp.get("kilometros") != null) {
                Object kmObj = resp.get("kilometros");
                return kmObj instanceof Number ? ((Number) kmObj).doubleValue() : Double.parseDouble(String.valueOf(kmObj));
            }
            return 50.0 + Math.random() * 200; // Distancia simulada si no hay respuesta
        } catch (Exception e) {
            log.warn("Error calculando distancia entre {} y {}: {}", origen, destino, e.getMessage());
            return 50.0 + Math.random() * 200; // Distancia simulada en caso de error
        }
    }

    /**
     * Clase auxiliar para almacenar candidatos de ruta
     */
    private static class RutaCandidato {
        Double distanciaTotal;
        List<Integer> depositos;
        String tipo;
    }
}
