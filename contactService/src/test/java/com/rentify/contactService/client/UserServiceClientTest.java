package com.rentify.contactService.client;

import com.rentify.contactService.dto.external.UsuarioDTO;
import com.rentify.contactService.exception.MicroserviceException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;

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
    @DisplayName("getUserById - Debe retornar usuario cuando existe")
    void getUserById_UsuarioExiste_ReturnsUsuario() {
        // Arrange
        String jsonResponse = """
                {
                    "id": 1,
                    "pnombre": "Juan",
                    "snombre": "Carlos",
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
        assertThat(result.getEmail()).isEqualTo("juan@email.com");
        assertThat(result.getRol()).isEqualTo("ARRIENDATARIO");
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
    @DisplayName("getUserById - Debe manejar timeout correctamente")
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
    @DisplayName("isAdmin - Debe retornar true cuando usuario es ADMIN")
    void isAdmin_UsuarioEsAdmin_ReturnsTrue() {
        // Arrange
        String jsonResponse = """
                {
                    "id": 5,
                    "pnombre": "Admin",
                    "email": "admin@rentify.com",
                    "rol": "ADMIN"
                }
                """;

        mockWebServer.enqueue(new MockResponse()
                .setBody(jsonResponse)
                .addHeader("Content-Type", "application/json"));

        // Act
        boolean isAdmin = client.isAdmin(5L);

        // Assert
        assertThat(isAdmin).isTrue();
    }

    @Test
    @DisplayName("isAdmin - Debe retornar false cuando usuario no es ADMIN")
    void isAdmin_UsuarioNoEsAdmin_ReturnsFalse() {
        // Arrange
        String jsonResponse = """
                {
                    "id": 1,
                    "pnombre": "Juan",
                    "email": "juan@email.com",
                    "rol": "ARRIENDATARIO"
                }
                """;

        mockWebServer.enqueue(new MockResponse()
                .setBody(jsonResponse)
                .addHeader("Content-Type", "application/json"));

        // Act
        boolean isAdmin = client.isAdmin(1L);

        // Assert
        assertThat(isAdmin).isFalse();
    }

    @Test
    @DisplayName("isAdmin - Debe retornar false cuando usuario no existe")
    void isAdmin_UsuarioNoExiste_ReturnsFalse() {
        // Arrange
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(404));

        // Act
        boolean isAdmin = client.isAdmin(999L);

        // Assert
        assertThat(isAdmin).isFalse();
    }

    @Test
    @DisplayName("getUserRole - Debe retornar rol del usuario")
    void getUserRole_UsuarioExiste_ReturnsRole() {
        // Arrange
        String jsonResponse = """
                {
                    "id": 1,
                    "pnombre": "Juan",
                    "email": "juan@email.com",
                    "rol": "PROPIETARIO"
                }
                """;

        mockWebServer.enqueue(new MockResponse()
                .setBody(jsonResponse)
                .addHeader("Content-Type", "application/json"));

        // Act
        String rol = client.getUserRole(1L);

        // Assert
        assertThat(rol).isEqualTo("PROPIETARIO");
    }

    @Test
    @DisplayName("getUserRole - Debe retornar null cuando usuario no existe")
    void getUserRole_UsuarioNoExiste_ReturnsNull() {
        // Arrange
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(404));

        // Act
        String rol = client.getUserRole(999L);

        // Assert
        assertThat(rol).isNull();
    }

    @Test
    @DisplayName("isUserActive - Debe retornar true cuando usuario está activo")
    void isUserActive_UsuarioActivo_ReturnsTrue() {
        // Arrange
        String jsonResponse = """
                {
                    "id": 1,
                    "pnombre": "Juan",
                    "email": "juan@email.com",
                    "rol": "ARRIENDATARIO",
                    "estado": "ACTIVO"
                }
                """;

        mockWebServer.enqueue(new MockResponse()
                .setBody(jsonResponse)
                .addHeader("Content-Type", "application/json"));

        // Act
        boolean isActive = client.isUserActive(1L);

        // Assert
        assertThat(isActive).isTrue();
    }

    @Test
    @DisplayName("isUserActive - Debe retornar false cuando usuario está inactivo")
    void isUserActive_UsuarioInactivo_ReturnsFalse() {
        // Arrange
        String jsonResponse = """
                {
                    "id": 1,
                    "pnombre": "Juan",
                    "email": "juan@email.com",
                    "rol": "ARRIENDATARIO",
                    "estado": "INACTIVO"
                }
                """;

        mockWebServer.enqueue(new MockResponse()
                .setBody(jsonResponse)
                .addHeader("Content-Type", "application/json"));

        // Act
        boolean isActive = client.isUserActive(1L);

        // Assert
        assertThat(isActive).isFalse();
    }

    @Test
    @DisplayName("isUserActive - Debe retornar false cuando usuario no existe")
    void isUserActive_UsuarioNoExiste_ReturnsFalse() {
        // Arrange
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(404));

        // Act
        boolean isActive = client.isUserActive(999L);

        // Assert
        assertThat(isActive).isFalse();
    }
}