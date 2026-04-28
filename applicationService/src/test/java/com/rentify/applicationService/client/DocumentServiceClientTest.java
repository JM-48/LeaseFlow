package com.rentify.applicationService.client;

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

/**
 * Tests unitarios para DocumentServiceClient
 * Utiliza MockWebServer para simular las respuestas del servicio
 */
@DisplayName("Tests de DocumentServiceClient")
class DocumentServiceClientTest {

    private MockWebServer mockWebServer;
    private DocumentServiceClient client;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        String baseUrl = mockWebServer.url("/").toString();

        // Crear el cliente con el builder real
        WebClient.Builder webClientBuilder = WebClient.builder();
        client = new DocumentServiceClient(webClientBuilder);

        // Usar reflexión para inyectar la URL del mock server
        try {
            var field = DocumentServiceClient.class.getDeclaredField("documentServiceUrl");
            field.setAccessible(true);
            field.set(client, baseUrl.substring(0, baseUrl.length() - 1)); // Remover última /
        } catch (Exception e) {
            throw new RuntimeException("Error configurando test", e);
        }
    }

    @AfterEach
    void tearDown() throws IOException {
        try {
            // Dar tiempo para que las requests pendientes terminen
            mockWebServer.shutdown();
        } catch (IOException e) {
            // Si falla el shutdown normal, intentar un shutdown más agresivo
            System.err.println("Warning: MockWebServer shutdown failed - " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Debe retornar true cuando el usuario tiene documentos aprobados")
    void hasApprovedDocuments_UsuarioConDocumentos_ReturnsTrue() {
        // Arrange
        mockWebServer.enqueue(new MockResponse()
                .setBody("true")
                .addHeader("Content-Type", "application/json"));

        // Act
        boolean result = client.hasApprovedDocuments(1L);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Debe retornar false cuando el usuario no tiene documentos aprobados")
    void hasApprovedDocuments_UsuarioSinDocumentos_ReturnsFalse() {
        // Arrange
        mockWebServer.enqueue(new MockResponse()
                .setBody("false")
                .addHeader("Content-Type", "application/json"));

        // Act
        boolean result = client.hasApprovedDocuments(1L);

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Debe retornar false cuando hay error en la comunicación")
    void hasApprovedDocuments_ErrorComunicacion_ReturnsFalse() {
        // Arrange
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody("Internal Server Error"));

        // Act
        boolean result = client.hasApprovedDocuments(1L);

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Debe retornar la cantidad correcta de documentos aprobados")
    void countApprovedDocuments_ReturnsCorrectCount() {
        // Arrange
        mockWebServer.enqueue(new MockResponse()
                .setBody("3")
                .addHeader("Content-Type", "application/json"));

        // Act
        int count = client.countApprovedDocuments(1L);

        // Assert
        assertThat(count).isEqualTo(3);
    }

    @Test
    @DisplayName("Debe retornar 0 cuando hay error al contar documentos")
    void countApprovedDocuments_ErrorComunicacion_Returns0() {
        // Arrange
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500));

        // Act
        int count = client.countApprovedDocuments(1L);

        // Assert
        assertThat(count).isZero();
    }

    @Test
    @DisplayName("Debe retornar true cuando el servicio está disponible")
    void isServiceAvailable_ServicioDisponible_ReturnsTrue() {
        // Arrange
        mockWebServer.enqueue(new MockResponse()
                .setBody("{\"status\":\"UP\"}")
                .addHeader("Content-Type", "application/json"));

        // Act
        boolean available = client.isServiceAvailable();

        // Assert
        assertThat(available).isTrue();
    }

    @Test
    @DisplayName("Debe retornar false cuando el servicio no está disponible")
    void isServiceAvailable_ServicioNoDisponible_ReturnsFalse() {
        // Arrange
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(503));

        // Act
        boolean available = client.isServiceAvailable();

        // Assert
        assertThat(available).isFalse();
    }

    @Test
    @DisplayName("Debe manejar timeout correctamente")
    void hasApprovedDocuments_Timeout_ReturnsFalse() {
        // Arrange - usar delay de 6 segundos (mayor al timeout de 5 segundos)
        mockWebServer.enqueue(new MockResponse()
                .setBody("true")
                .setBodyDelay(6, TimeUnit.SECONDS));

        // Act
        boolean result = client.hasApprovedDocuments(1L);

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Debe manejar respuesta null correctamente")
    void hasApprovedDocuments_RespuestaNull_ReturnsFalse() {
        // Arrange
        mockWebServer.enqueue(new MockResponse()
                .setBody("null")
                .addHeader("Content-Type", "application/json"));

        // Act
        boolean result = client.hasApprovedDocuments(1L);

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Debe manejar respuesta vacía correctamente")
    void countApprovedDocuments_RespuestaVacia_Returns0() {
        // Arrange
        mockWebServer.enqueue(new MockResponse()
                .setBody("")
                .addHeader("Content-Type", "application/json"));

        // Act
        int count = client.countApprovedDocuments(1L);

        // Assert
        assertThat(count).isZero();
    }
}