package com.tpi.localizaciones.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ubicacion")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Ubicacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ubicacion_id")
    private Integer ubicacionId;

    @Column(name = "ciudad", nullable = false, length = 100)
    private String ciudad;

    @Column(name = "direccion", length = 50)
    private String direccion;

    @Column(name = "latitud", nullable = false, length = 50)
    private String latitud;

    @Column(name = "longitud", nullable = false, length = 50)
    private String longitud;

    @Column(name = "nombre", length = 30)
    private String nombre;
}
