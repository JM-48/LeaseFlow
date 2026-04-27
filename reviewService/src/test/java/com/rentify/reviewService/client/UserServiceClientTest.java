package com.rentify.reviewService.client;

import com.rentify.reviewService.dto.external.UsuarioDTO;
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
    void tearDown() throws IOException {
        try {
            mockWebServer.shutdown();
        } catch (IOException e) {
            System.err.println("Warning: MockWebServer shutdown failed - " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Debe retornar usuario cuando existe")
    void getUserById_UsuarioExiste_ReturnsUsuario() {
        // Arrange
        String jsonResponse = """
            {
                "id": 1,
                "pnombre": "Juan",
                "papellido": "Pérez",
                "email": "juan@email.com",
                "rol": "ARRIENDATARIO",
                "estado": "ACTIVO"
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
        assertThat(result.getRol()).isEqualTo("ARRIENDATARIO");
    }

    @Test
    @DisplayName("Debe retornar null cuando hay error 404")
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
    @DisplayName("Debe retornar null cuando hay error 500")
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
    @DisplayName("Debe manejar timeout correctamente")
    void getUserById_Timeout_ReturnsNull() {
        // Arrange
        mockWebServer.enqueue(new MockResponse()
                .setBody("{}")
                .setBodyDelay(6, TimeUnit.SECONDS)); // Mayor al timeout de 5s

        // Act
        UsuarioDTO result = client.getUserById(1L);

        // Assert
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("existsUser - Debe retornar true cuando usuario existe")
    void existsUser_UsuarioExiste_ReturnsTrue() {
        // Arrange
        String jsonResponse = """
            {
                "id": 1,
                "pnombre": "Juan",
                "papellido": "Pérez",
                "email": "juan@email.com",
                "rol": "ARRIENDATARIO"
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
    @DisplayName("existsUser - Debe retornar false cuando usuario no existe")
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
    @DisplayName("hasRole - Debe retornar true cuando usuario tiene el rol")
    void hasRole_UsuarioTieneRol_ReturnsTrue() {
        // Arrange
        String jsonResponse = """
            {
                "id": 1,
                "pnombre": "Juan",
                "rol": "ARRIENDATARIO"
            }
            """;

        mockWebServer.enqueue(new MockResponse()
                .setBody(jsonResponse)
                .addHeader("Content-Type", "application/json"));

        // Act
        boolean hasRole = client.hasRole(1L, "ARRIENDATARIO");

        // Assert
        assertThat(hasRole).isTrue();
    }

    @Test
    @DisplayName("hasRole - Debe retornar false cuando usuario no tiene el rol")
    void hasRole_UsuarioNoTieneRol_ReturnsFalse() {
        // Arrange
        String jsonResponse = """
            {
                "id": 1,
                "pnombre": "Juan",
                "rol": "PROPIETARIO"
            }
            """;

        mockWebServer.enqueue(new MockResponse()
                .setBody(jsonResponse)
                .addHeader("Content-Type", "application/json"));

        // Act
        boolean hasRole = client.hasRole(1L, "ARRIENDATARIO");

        // Assert
        assertThat(hasRole).isFalse();
    }

    @Test
    @DisplayName("getUserRole - Debe retornar el rol del usuario")
    void getUserRole_UsuarioExiste_ReturnsRol() {
        // Arrange
        String jsonResponse = """
            {
                "id": 1,
                "pnombre": "Juan",
                "rol": "ADMIN"
            }
            """;

        mockWebServer.enqueue(new MockResponse()
                .setBody(jsonResponse)
                .addHeader("Content-Type", "application/json"));

        // Act
        String rol = client.getUserRole(1L);

        // Assert
        assertThat(rol).isEqualTo("ADMIN");
    }
}