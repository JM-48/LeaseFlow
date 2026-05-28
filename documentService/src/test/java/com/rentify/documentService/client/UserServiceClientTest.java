package com.rentify.documentService.client;

import com.rentify.documentService.dto.external.UsuarioDTO;
import com.rentify.documentService.exception.MicroserviceException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests para UserServiceClient usando MockWebServer.
 * Simula respuestas del User Service sin necesidad de levantar el servicio real.
 */
@DisplayName("Tests de UserServiceClient")
class UserServiceClientTest {

    private MockWebServer mockWebServer;
    private UserServiceClient client;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        String baseUrl = mockWebServer.url("/").toString();
        WebClient.Builder webClientBuilder = WebClient.builder();
        client = new UserServiceClient(webClientBuilder);

        // Inyectar URL del mock usando reflexión
        try {
            var field = UserServiceClient.class.getDeclaredField("userServiceUrl");
            field.setAccessible(true);
            field.set(client, baseUrl.substring(0, baseUrl.length() - 1));
        } catch (Exception e) {
            throw new RuntimeException("Error configurando test", e);
        }
    }

    @AfterEach
    void tearDown() {
        try {
            mockWebServer.shutdown();
        } catch (IOException e) {
            System.err.println("Warning: MockWebServer shutdown failed - " + e.getMessage());
        }
    }

    @Test
    @DisplayName("getUserById - Debe retornar usuario cuando existe")
    void getUserById_UsuarioExiste_ReturnsUsuario() {
        // Arrange - JSON actualizado a la nueva estructura
        String jsonResponse = """
                {
                    "id": 1,
                    "pnombre": "Juan",
                    "papellido": "Pérez",
                    "email": "juan@email.com",
                    "rol": {
                        "id": 3,
                        "nombre": "ARRIENDATARIO"
                    },
                    "estado": {
                        "id": 1,
                        "nombre": "Activo"
                    }
                }
                """;

        mockWebServer.enqueue(new MockResponse()
                .setBody(jsonResponse)
                .addHeader("Content-Type", "application/json"));

        // Act
        UsuarioDTO result = client.getUserById(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getPnombre()).isEqualTo("Juan");
        assertThat(result.getPapellido()).isEqualTo("Pérez");
        assertThat(result.getEmail()).isEqualTo("juan@email.com");
        // Extraemos el nombre del objeto Rol
        assertThat(result.getRol().getNombre()).isEqualTo("ARRIENDATARIO");
    }

    @Test
    @DisplayName("getUserById - Debe retornar null cuando usuario no existe (404)")
    void getUserById_UsuarioNoExiste_ReturnsNull() {
        // Arrange
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(404));

        // Act
        UsuarioDTO result = client.getUserById(999L);

        // Assert
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("getUserById - Debe retornar null cuando hay error de servidor (500)")
    void getUserById_ErrorServidor_ReturnsNull() {
        // Arrange
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500));

        // Act
        UsuarioDTO result = client.getUserById(1L);

        // Assert
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("getUserById - Debe retornar null tras timeout (manejado internamente)")
    void getUserById_Timeout_ReturnsNull() {
        // Arrange - El código tiene un timeout de 10s, ponemos 12s para asegurar que salte el timeout real
        mockWebServer.enqueue(new MockResponse()
                .setBody("{}")
                .setBodyDelay(12, TimeUnit.SECONDS));

        // Act
        UsuarioDTO result = client.getUserById(1L);

        // Assert - Según tu código reactivo, la excepción se captura en onErrorResume y devuelve null, no explota.
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("existsUser - Debe retornar true cuando el usuario existe")
    void existsUser_UsuarioExiste_ReturnsTrue() {
        // Arrange - JSON actualizado
        String jsonResponse = """
                {
                    "id": 1,
                    "pnombre": "Juan",
                    "papellido": "Pérez",
                    "email": "juan@email.com",
                    "rol": {
                        "id": 3,
                        "nombre": "ARRIENDATARIO"
                    }
                }
                """;

        mockWebServer.enqueue(new MockResponse()
                .setBody(jsonResponse)
                .addHeader("Content-Type", "application/json"));

        // Act
        boolean exists = client.existsUser(1L);

        // Assert
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("existsUser - Debe retornar false cuando el usuario no existe")
    void existsUser_UsuarioNoExiste_ReturnsFalse() {
        // Arrange
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(404));

        // Act
        boolean exists = client.existsUser(999L);

        // Assert
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("existsUser - Debe retornar false cuando hay error de comunicación")
    void existsUser_ErrorComunicacion_ReturnsFalse() {
        // Arrange
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500));

        // Act
        boolean exists = client.existsUser(1L);

        // Assert
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("userHasRole - Debe retornar true cuando el usuario tiene el rol especificado")
    void userHasRole_UsuarioConRol_ReturnsTrue() {
        // Arrange - JSON actualizado
        String jsonResponse = """
                {
                    "id": 1,
                    "pnombre": "Juan",
                    "papellido": "Pérez",
                    "email": "juan@email.com",
                    "rol": {
                        "id": 3,
                        "nombre": "ARRIENDATARIO"
                    }
                }
                """;

        mockWebServer.enqueue(new MockResponse()
                .setBody(jsonResponse)
                .addHeader("Content-Type", "application/json"));

        // Act
        boolean hasRole = client.userHasRole(1L, "ARRIENDATARIO");

        // Assert
        assertThat(hasRole).isTrue();
    }

    @Test
    @DisplayName("userHasRole - Debe retornar false cuando el usuario no tiene el rol")
    void userHasRole_UsuarioSinRol_ReturnsFalse() {
        // Arrange - JSON actualizado
        String jsonResponse = """
                {
                    "id": 1,
                    "pnombre": "Juan",
                    "papellido": "Pérez",
                    "email": "juan@email.com",
                    "rol": {
                        "id": 2,
                        "nombre": "PROPIETARIO"
                    }
                }
                """;

        mockWebServer.enqueue(new MockResponse()
                .setBody(jsonResponse)
                .addHeader("Content-Type", "application/json"));

        // Act
        boolean hasRole = client.userHasRole(1L, "ARRIENDATARIO");

        // Assert
        assertThat(hasRole).isFalse();
    }

    @Test
    @DisplayName("userHasRole - Debe retornar false cuando hay error")
    void userHasRole_ErrorComunicacion_ReturnsFalse() {
        // Arrange
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500));

        // Act
        boolean hasRole = client.userHasRole(1L, "ARRIENDATARIO");

        // Assert
        assertThat(hasRole).isFalse();
    }

    @Test
    @DisplayName("getUserById - Debe manejar respuesta JSON malformada")
    void getUserById_JsonMalformado_ReturnsNull() {
        // Arrange
        mockWebServer.enqueue(new MockResponse()
                .setBody("{ invalid json }")
                .addHeader("Content-Type", "application/json"));

        // Act
        UsuarioDTO result = client.getUserById(1L);

        // Assert
        assertThat(result).isNull();
    }
}