package com.rentify.contactService.client;

import com.rentify.contactService.dto.external.UsuarioDTO;
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

        try {
            var urlField = UserServiceClient.class.getDeclaredField("userServiceUrl");
            urlField.setAccessible(true);
            urlField.set(client, baseUrl.substring(0, baseUrl.length() - 1));

            var keyField = UserServiceClient.class.getDeclaredField("appClientKey");
            keyField.setAccessible(true);
            keyField.set(client, "test-key-123");
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
        String jsonResponse = """
                {
                    "id": 1,
                    "pnombre": "Juan",
                    "snombre": "Carlos",
                    "papellido": "Pérez",
                    "email": "juan@email.com",
                    "rol": {
                        "id": 3,
                        "nombre": "ARRIENDATARIO"
                    },
                    "estado": {
                        "id": 1,
                        "nombre": "ACTIVO"
                    }
                }
                """;

        mockWebServer.enqueue(new MockResponse()
                .setBody(jsonResponse)
                .addHeader("Content-Type", "application/json"));

        UsuarioDTO result = client.getUserById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getPnombre()).isEqualTo("Juan");
        assertThat(result.getEmail()).isEqualTo("juan@email.com");
        assertThat(result.getRolNombre()).isEqualTo("ARRIENDATARIO");
        assertThat(result.getEstadoNombre()).isEqualTo("ACTIVO");
    }

    @Test
    @DisplayName("getUserById - Debe retornar null cuando usuario no existe (404)")
    void getUserById_UsuarioNoExiste_ReturnsNull() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(404));

        UsuarioDTO result = client.getUserById(999L);

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("getUserById - Debe retornar null cuando hay error de servidor (500)")
    void getUserById_ErrorServidor_ReturnsNull() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));

        UsuarioDTO result = client.getUserById(1L);

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("getUserById - Debe manejar timeout correctamente")
    void getUserById_Timeout_ReturnsNull() {
        mockWebServer.enqueue(new MockResponse()
                .setBody("{}")
                .setBodyDelay(6, TimeUnit.SECONDS));

        UsuarioDTO result = client.getUserById(1L);

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("existsUser - Debe retornar true cuando usuario existe")
    void existsUser_UsuarioExiste_ReturnsTrue() {
        String jsonResponse = """
                {
                    "id": 1,
                    "pnombre": "Juan",
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

        boolean exists = client.existsUser(1L);

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("existsUser - Debe retornar false cuando usuario no existe")
    void existsUser_UsuarioNoExiste_ReturnsFalse() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(404));

        boolean exists = client.existsUser(999L);

        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("isAdmin - Debe retornar true cuando usuario es ADMIN")
    void isAdmin_UsuarioEsAdmin_ReturnsTrue() {
        String jsonResponse = """
                {
                    "id": 5,
                    "pnombre": "Admin",
                    "email": "admin@rentify.com",
                    "rol": {
                        "id": 1,
                        "nombre": "ADMIN"
                    }
                }
                """;

        mockWebServer.enqueue(new MockResponse()
                .setBody(jsonResponse)
                .addHeader("Content-Type", "application/json"));

        boolean isAdmin = client.isAdmin(5L);

        assertThat(isAdmin).isTrue();
    }

    @Test
    @DisplayName("isAdmin - Debe retornar false cuando usuario no es ADMIN")
    void isAdmin_UsuarioNoEsAdmin_ReturnsFalse() {
        String jsonResponse = """
                {
                    "id": 1,
                    "pnombre": "Juan",
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

        boolean isAdmin = client.isAdmin(1L);

        assertThat(isAdmin).isFalse();
    }

    @Test
    @DisplayName("isAdmin - Debe retornar false cuando usuario no existe")
    void isAdmin_UsuarioNoExiste_ReturnsFalse() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(404));

        boolean isAdmin = client.isAdmin(999L);

        assertThat(isAdmin).isFalse();
    }

    @Test
    @DisplayName("getUserRole - Debe retornar rol del usuario")
    void getUserRole_UsuarioExiste_ReturnsRole() {
        String jsonResponse = """
                {
                    "id": 1,
                    "pnombre": "Juan",
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

        String rol = client.getUserRole(1L);

        assertThat(rol).isEqualTo("PROPIETARIO");
    }

    @Test
    @DisplayName("getUserRole - Debe retornar null cuando usuario no existe")
    void getUserRole_UsuarioNoExiste_ReturnsNull() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(404));

        String rol = client.getUserRole(999L);

        assertThat(rol).isNull();
    }

    @Test
    @DisplayName("isUserActive - Debe retornar true cuando usuario está activo")
    void isUserActive_UsuarioActivo_ReturnsTrue() {
        String jsonResponse = """
                {
                    "id": 1,
                    "pnombre": "Juan",
                    "email": "juan@email.com",
                    "rol": {
                        "id": 3,
                        "nombre": "ARRIENDATARIO"
                    },
                    "estado": {
                        "id": 1,
                        "nombre": "ACTIVO"
                    }
                }
                """;

        mockWebServer.enqueue(new MockResponse()
                .setBody(jsonResponse)
                .addHeader("Content-Type", "application/json"));

        boolean isActive = client.isUserActive(1L);

        assertThat(isActive).isTrue();
    }

    @Test
    @DisplayName("isUserActive - Debe retornar false cuando usuario está inactivo")
    void isUserActive_UsuarioInactivo_ReturnsFalse() {
        String jsonResponse = """
                {
                    "id": 1,
                    "pnombre": "Juan",
                    "email": "juan@email.com",
                    "rol": {
                        "id": 3,
                        "nombre": "ARRIENDATARIO"
                    },
                    "estado": {
                        "id": 2,
                        "nombre": "INACTIVO"
                    }
                }
                """;

        mockWebServer.enqueue(new MockResponse()
                .setBody(jsonResponse)
                .addHeader("Content-Type", "application/json"));

        boolean isActive = client.isUserActive(1L);

        assertThat(isActive).isFalse();
    }

    @Test
    @DisplayName("isUserActive - Debe retornar false cuando usuario no existe")
    void isUserActive_UsuarioNoExiste_ReturnsFalse() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(404));

        boolean isActive = client.isUserActive(999L);

        assertThat(isActive).isFalse();
    }
}