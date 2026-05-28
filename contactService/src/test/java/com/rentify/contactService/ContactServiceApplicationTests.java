package com.rentify.contactService;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
        // Forzamos el uso de la base de datos en memoria H2
        "spring.datasource.url=jdbc:h2:mem:contacttestdb;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        // Apagamos Eureka o llamadas a otros microservicios si las hubiera al inicio
        "eureka.client.enabled=false"
})
class ContactServiceApplicationTests {

    @Test
    void contextLoads() {
        // Este test verifica que la aplicación puede arrancar correctamente.
        // Al estar vacío y en verde, significa que no hay errores de sintaxis
        // graves ni problemas de inyección de dependencias en tus clases.
    }

}