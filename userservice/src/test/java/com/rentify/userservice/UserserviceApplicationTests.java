package com.rentify.userservice;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test") // <- Esto es clave para usar application-test.properties
@DisplayName("Tests de Inicialización de la Aplicación")
class UserserviceApplicationTests {

    @Test
    @DisplayName("El contexto de Spring debe cargar correctamente")
    void contextLoads() {
        // Este test pasa automáticamente si la aplicación logra arrancar
        // sin lanzar excepciones de configuración o dependencias rotas.
    }

}