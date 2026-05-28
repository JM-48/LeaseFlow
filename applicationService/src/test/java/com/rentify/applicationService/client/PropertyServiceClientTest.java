package com.rentify.applicationService.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rentify.applicationService.dto.PropiedadDTO;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class PropertyServiceClientTest {

    private MockWebServer mockWebServer;
    private PropertyServiceClient client;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        String baseUrl = "http://localhost:" + mockWebServer.getPort();
        client = new PropertyServiceClient(WebClient.builder());
        ReflectionTestUtils.setField(client, "propertyServiceUrl", baseUrl);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    // ────────────────── getPropertyById ──────────────────

    @Test
    void getPropertyById_respuesta200_retornaPropiedad() throws Exception {
        PropiedadDTO propiedad = new PropiedadDTO();
        propiedad.setId(7L);
        propiedad.setTitulo("Departamento Providencia");
        propiedad.setPrecioMensual(650000.0);

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(objectMapper.writeValueAsString(propiedad))
                .addHeader("Content-Type", "application/json"));

        PropiedadDTO result = client.getPropertyById(7L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(7L);
        assertThat(result.getTitulo()).isEqualTo("Departamento Providencia");
        assertThat(result.getPrecioMensual()).isEqualTo(650000.0);

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/api/propiedades/7");
        assertThat(request.getMethod()).isEqualTo("GET");
    }

    @Test
    void getPropertyById_respuesta404_retornaNull() throws Exception {
        // onErrorResume captura el 404 y retorna Mono.empty() -> block() = null
        mockWebServer.enqueue(new MockResponse().setResponseCode(404));

        PropiedadDTO result = client.getPropertyById(99L);

        assertThat(result).isNull();
    }

    @Test
    void getPropertyById_respuesta500_retornaNull() {
        // onErrorResume captura el 500 y retorna Mono.empty() -> block() = null
        // El catch externo NO se alcanza porque onErrorResume absorbe el error
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));

        PropiedadDTO result = client.getPropertyById(1L);

        assertThat(result).isNull();
    }

    @Test
    void getPropertyById_servidorInaccesible_retornaNull() throws IOException {
        // onErrorResume captura WebClientRequestException (ConnectionRefused) -> null
        mockWebServer.shutdown();

        PropiedadDTO result = client.getPropertyById(1L);

        assertThat(result).isNull();

        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    // ────────────────── existsProperty ──────────────────

    @Test
    void existsProperty_propiedadExiste_retornaTrue() throws Exception {
        PropiedadDTO propiedad = new PropiedadDTO();
        propiedad.setId(1L);

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(objectMapper.writeValueAsString(propiedad))
                .addHeader("Content-Type", "application/json"));

        boolean result = client.existsProperty(1L);

        assertThat(result).isTrue();
    }

    @Test
    void existsProperty_propiedadNoExiste_retornaFalse() throws Exception {
        mockWebServer.enqueue(new MockResponse().setResponseCode(404));

        boolean result = client.existsProperty(99L);

        assertThat(result).isFalse();
    }

    @Test
    void existsProperty_respuestaConIdNull_retornaFalse() throws Exception {
        // La propiedad existe pero sin id -> existsProperty devuelve false
        PropiedadDTO sinId = new PropiedadDTO();

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(objectMapper.writeValueAsString(sinId))
                .addHeader("Content-Type", "application/json"));

        boolean result = client.existsProperty(1L);

        assertThat(result).isFalse();
    }

    @Test
    void existsProperty_error500_retornaFalse() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));

        boolean result = client.existsProperty(1L);

        assertThat(result).isFalse();
    }

    // ────────────────── isPropertyAvailable ──────────────────

    @Test
    void isPropertyAvailable_propiedadExiste_retornaTrue() throws Exception {
        PropiedadDTO propiedad = new PropiedadDTO();
        propiedad.setId(1L);
        propiedad.setTitulo("Test");

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(objectMapper.writeValueAsString(propiedad))
                .addHeader("Content-Type", "application/json"));

        boolean result = client.isPropertyAvailable(1L);

        assertThat(result).isTrue();
    }

    @Test
    void isPropertyAvailable_propiedadNoExiste_retornaFalse() throws Exception {
        mockWebServer.enqueue(new MockResponse().setResponseCode(404));

        boolean result = client.isPropertyAvailable(99L);

        assertThat(result).isFalse();
    }

    @Test
    void isPropertyAvailable_error500_retornaFalse() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));

        boolean result = client.isPropertyAvailable(1L);

        assertThat(result).isFalse();
    }
}