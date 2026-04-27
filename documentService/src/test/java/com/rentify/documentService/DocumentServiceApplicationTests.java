package com.rentify.documentService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Smoke test para verificar que el contexto de Spring se carga correctamente.
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Tests de DocumentServiceApplication")
class DocumentServiceApplicationTests {

	@Test
	@DisplayName("El contexto de Spring debe cargar correctamente")
	void contextLoads() {
		// Este test verifica que todas las configuraciones y beans se carguen sin errores
	}
}