package com.tpi.solicitudes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import jakarta.annotation.PostConstruct; // Importación necesaria
import java.util.TimeZone;

@SpringBootApplication
@EnableFeignClients // <-- ¡Clave para activar el OsrmFeignClient!
public class SolicitudesApplication {

    public static void main(String[] args) {
        // La clase a ejecutar debe ser SolicitudesApplication.class
        SpringApplication.run(SolicitudesApplication.class, args); 
    }
    
    /**
     * Garantiza que la zona horaria predeterminada sea UTC antes de 
     * que se inicialicen otros componentes de la aplicación (como JPA o Feign).
     */
    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }
}