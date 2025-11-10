package com.tpi.localizaciones.dto.osrm;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * DTO para la respuesta de la API de OSRM
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OsrmRouteResponse {

    private String code;

    private List<Route> routes;

    private List<Waypoint> waypoints;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Route {
        /**
         * Distancia en metros
         */
        private Double distance;

        /**
         * Duración en segundos
         */
        private Double duration;

        /**
         * Geometría de la ruta (si se solicita)
         */
        private String geometry;

        /**
         * Pasos de navegación (si se solicitan)
         */
        private List<Leg> legs;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Leg {
        private Double distance;
        private Double duration;
        private String summary;
        private List<Step> steps;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Step {
        private Double distance;
        private Double duration;
        private String name;
        private String mode;
        private Maneuver maneuver;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Maneuver {
        private String type;
        private String modifier;
        @JsonProperty("bearing_after")
        private Integer bearingAfter;
        @JsonProperty("bearing_before")
        private Integer bearingBefore;
        private List<Double> location;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Waypoint {
        private String name;
        private List<Double> location;
        private String hint;
        private Double distance;
    }
}

