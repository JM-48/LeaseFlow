//package com.rentify.applicationService.client;
//
//import com.rentify.applicationService.dto.PropiedadDTO;
//import com.rentify.applicationService.exception.MicroserviceException;
//import okhttp3.mockwebserver.MockResponse;
//import okhttp3.mockwebserver.MockWebServer;
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.web.reactive.function.client.WebClient;
//
//import java.io.IOException;
//import java.util.concurrent.TimeUnit;
//
//import static org.assertj.core.api.Assertions.*;
//
///**
// * Tests unitarios para PropertyServiceClient
// * Utiliza MockWebServer para simular las respuestas del servicio
// */
//@DisplayName("Tests de PropertyServiceClient")
//class PropertyServiceClientTest {
//
//    private MockWebServer mockWebServer;
//    private PropertyServiceClient client;
//
//    @BeforeEach
//    void setUp() throws IOException {
//        mockWebServer = new MockWebServer();
//        mockWebServer.start();
//
//        String baseUrl = mockWebServer.url("/").toString();
//
//        // Crear el cliente con el builder real
//        WebClient.Builder webClientBuilder = WebClient.builder();
//        client = new PropertyServiceClient(webClientBuilder);
//
//        // Usar reflexión para inyectar la URL del mock server
//        try {
//            var field = PropertyServiceClient.class.getDeclaredField("propertyServiceUrl");
//            field.setAccessible(true);
//            field.set(client, baseUrl.substring(0, baseUrl.length() - 1));
//        } catch (Exception e) {
//            throw new RuntimeException("Error configurando test", e);
//        }
//    }
//
//    @AfterEach
//    void tearDown() throws IOException {
//        try {
//            // Dar tiempo para que las requests pendientes terminen
//            mockWebServer.shutdown();
//        } catch (IOException e) {
//            // Si falla el shutdown normal, intentar un shutdown más agresivo
//            System.err.println("Warning: MockWebServer shutdown failed - " + e.getMessage());
//        }
//    }
//
//    @Test
//    @DisplayName("Debe retornar propiedad cuando existe")
//    void getPropertyById_PropiedadExiste_ReturnsPropiedad() {
//        // Arrange
//        String jsonResponse = """
//            {
//                "id": 1,
//                "titulo": "Depto 2D/1B",
//                "direccion": "Av. Providencia 123",
//                "precio": 500000.0,
//                "tipo": "Departamento",
//                "disponible": true
//            }
//            """;
//
//        mockWebServer.enqueue(new MockResponse()
//                .setBody(jsonResponse)
//                .addHeader("Content-Type", "application/json"));
//
//        // Act
//        PropiedadDTO result = client.getPropertyById(1L);
//
//        // Assert
//        assertThat(result).isNotNull();
//        assertThat(result.getId()).isEqualTo(1L);
//        assertThat(result.getTitulo()).isEqualTo("Depto 2D/1B");
//        assertThat(result.getPrecio()).isEqualTo(500000.0);
//        assertThat(result.getDisponible()).isTrue();
//    }
//
//    @Test
//    @DisplayName("Debe retornar null cuando la propiedad no existe")
//    void getPropertyById_PropiedadNoExiste_ReturnsNull() {
//        // Arrange
//        mockWebServer.enqueue(new MockResponse()
//                .setResponseCode(404)
//                .setBody("Not Found"));
//
//        // Act
//        PropiedadDTO result = client.getPropertyById(999L);
//
//        // Assert
//        assertThat(result).isNull();
//    }
//
//    @Test
//    @DisplayName("Debe retornar null cuando hay error de comunicación (el cliente usa onErrorResume)")
//    void getPropertyById_ErrorComunicacion_ReturnsNull() {
//        // Arrange - simular error 500
//        mockWebServer.enqueue(new MockResponse()
//                .setResponseCode(500)
//                .setBody("Internal Server Error"));
//
//        // Act
//        PropiedadDTO result = client.getPropertyById(1L);
//
//        // Assert
//        // El cliente actual maneja errores con onErrorResume y retorna null
//        // En lugar de lanzar excepción
//        assertThat(result).isNull();
//    }
//
//    @Test
//    @DisplayName("Debe retornar null cuando hay error de servidor no disponible")
//    void getPropertyById_ServidorNoDisponible_ReturnsNull() {
//        // Arrange - Crear un cliente con URL inválida
//        try {
//            var field = PropertyServiceClient.class.getDeclaredField("propertyServiceUrl");
//            field.setAccessible(true);
//            field.set(client, "http://localhost:65535"); // Puerto que no escucha
//        } catch (Exception e) {
//            throw new RuntimeException("Error configurando test", e);
//        }
//
//        // Act
//        PropiedadDTO result = client.getPropertyById(1L);
//
//        // Assert
//        // El onErrorResume del cliente captura el error y retorna null
//        assertThat(result).isNull();
//    }
//
//    @Test
//    @DisplayName("Debe retornar true cuando la propiedad existe")
//    void existsProperty_PropiedadExiste_ReturnsTrue() {
//        // Arrange
//        String jsonResponse = """
//            {
//                "id": 1,
//                "titulo": "Depto 2D/1B",
//                "disponible": true
//            }
//            """;
//
//        mockWebServer.enqueue(new MockResponse()
//                .setBody(jsonResponse)
//                .addHeader("Content-Type", "application/json"));
//
//        // Act
//        boolean exists = client.existsProperty(1L);
//
//        // Assert
//        assertThat(exists).isTrue();
//    }
//
//    @Test
//    @DisplayName("Debe retornar false cuando la propiedad no existe")
//    void existsProperty_PropiedadNoExiste_ReturnsFalse() {
//        // Arrange
//        mockWebServer.enqueue(new MockResponse()
//                .setResponseCode(404));
//
//        // Act
//        boolean exists = client.existsProperty(999L);
//
//        // Assert
//        assertThat(exists).isFalse();
//    }
//
//    @Test
//    @DisplayName("Debe retornar true cuando la propiedad está disponible")
//    void isPropertyAvailable_PropiedadDisponible_ReturnsTrue() {
//        // Arrange
//        String jsonResponse = """
//            {
//                "id": 1,
//                "titulo": "Depto 2D/1B",
//                "disponible": true
//            }
//            """;
//
//        mockWebServer.enqueue(new MockResponse()
//                .setBody(jsonResponse)
//                .addHeader("Content-Type", "application/json"));
//
//        // Act
//        boolean available = client.isPropertyAvailable(1L);
//
//        // Assert
//        assertThat(available).isTrue();
//    }
//
//    @Test
//    @DisplayName("Debe retornar false cuando la propiedad no está disponible")
//    void isPropertyAvailable_PropiedadNoDisponible_ReturnsFalse() {
//        // Arrange
//        String jsonResponse = """
//            {
//                "id": 1,
//                "titulo": "Depto 2D/1B",
//                "disponible": false
//            }
//            """;
//
//        mockWebServer.enqueue(new MockResponse()
//                .setBody(jsonResponse)
//                .addHeader("Content-Type", "application/json"));
//
//        // Act
//        boolean available = client.isPropertyAvailable(1L);
//
//        // Assert
//        assertThat(available).isFalse();
//    }
//
//    @Test
//    @DisplayName("Debe retornar false cuando hay error al verificar disponibilidad")
//    void isPropertyAvailable_ErrorComunicacion_ReturnsFalse() {
//        // Arrange
//        mockWebServer.enqueue(new MockResponse()
//                .setResponseCode(500));
//
//        // Act
//        boolean available = client.isPropertyAvailable(1L);
//
//        // Assert
//        assertThat(available).isFalse();
//    }
//
//    @Test
//    @DisplayName("Debe manejar timeout correctamente")
//    void getPropertyById_Timeout_ReturnsNull() {
//        // Arrange - delay de 6 segundos (mayor al timeout de 5)
//        mockWebServer.enqueue(new MockResponse()
//                .setBody("{}")
//                .setBodyDelay(6, TimeUnit.SECONDS));
//
//        // Act
//        PropiedadDTO result = client.getPropertyById(1L);
//
//        // Assert
//        assertThat(result).isNull();
//    }
//
//    @Test
//    @DisplayName("Debe manejar respuesta vacía correctamente")
//    void getPropertyById_RespuestaVacia_ReturnsNull() {
//        // Arrange
//        mockWebServer.enqueue(new MockResponse()
//                .setBody("")
//                .addHeader("Content-Type", "application/json"));
//
//        // Act
//        PropiedadDTO result = client.getPropertyById(1L);
//
//        // Assert
//        assertThat(result).isNull();
//    }
//
//    @Test
//    @DisplayName("Debe manejar JSON malformado")
//    void getPropertyById_JsonMalformado_ReturnsNull() {
//        // Arrange
//        mockWebServer.enqueue(new MockResponse()
//                .setBody("{invalid json}")
//                .addHeader("Content-Type", "application/json"));
//
//        // Act
//        PropiedadDTO result = client.getPropertyById(1L);
//
//        // Assert
//        assertThat(result).isNull();
//    }
//
//    @Test
//    @DisplayName("Debe retornar false cuando existe pero sin ID válido")
//    void existsProperty_PropiedadSinId_ReturnsFalse() {
//        // Arrange - propiedad sin ID
//        String jsonResponse = """
//            {
//                "titulo": "Depto sin ID",
//                "disponible": true
//            }
//            """;
//
//        mockWebServer.enqueue(new MockResponse()
//                .setBody(jsonResponse)
//                .addHeader("Content-Type", "application/json"));
//
//        // Act
//        boolean exists = client.existsProperty(1L);
//
//        // Assert
//        assertThat(exists).isFalse();
//    }
//}