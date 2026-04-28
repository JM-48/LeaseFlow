package com.rentify.applicationService.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para GlobalExceptionHandler
 * Valida el manejo correcto de todas las excepciones
 */
@DisplayName("Tests de GlobalExceptionHandler")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("Debe manejar ResourceNotFoundException correctamente")
    void handleResourceNotFound_ReturnsNotFound() {
        // Arrange
        ResourceNotFoundException ex = new ResourceNotFoundException("Recurso no encontrado");

        // Act
        ResponseEntity<ErrorResponse> response = handler.handleResourceNotFound(ex);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(404);
        assertThat(response.getBody().getError()).isEqualTo("Not Found");
        assertThat(response.getBody().getMessage()).isEqualTo("Recurso no encontrado");
        assertThat(response.getBody().getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("Debe manejar MicroserviceException correctamente")
    void handleMicroserviceException_ReturnsServiceUnavailable() {
        // Arrange
        MicroserviceException ex = new MicroserviceException("Servicio no disponible");

        // Act
        ResponseEntity<ErrorResponse> response = handler.handleMicroserviceException(ex);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(503);
        assertThat(response.getBody().getError()).isEqualTo("Service Unavailable");
        assertThat(response.getBody().getMessage()).isEqualTo("Servicio no disponible");
    }

    @Test
    @DisplayName("Debe manejar BusinessValidationException correctamente")
    void handleBusinessValidation_ReturnsBadRequest() {
        // Arrange
        BusinessValidationException ex = new BusinessValidationException("Validación de negocio falló");

        // Act
        ResponseEntity<ErrorResponse> response = handler.handleBusinessValidation(ex);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getError()).isEqualTo("Business Validation Error");
        assertThat(response.getBody().getMessage()).isEqualTo("Validación de negocio falló");
    }

    @Test
    @DisplayName("Debe manejar MethodArgumentNotValidException correctamente")
    void handleValidationErrors_ReturnsBadRequestWithDetails() {
        // Arrange
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        org.springframework.validation.BindingResult bindingResult =
                mock(org.springframework.validation.BindingResult.class);

        FieldError fieldError1 = new FieldError("solicitudDTO", "usuarioId", "El ID del usuario es obligatorio");
        FieldError fieldError2 = new FieldError("solicitudDTO", "propiedadId", "El ID de la propiedad es obligatorio");

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError1, fieldError2));

        // Act
        ResponseEntity<ErrorResponse> response = handler.handleValidationErrors(ex);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getError()).isEqualTo("Validation Error");
        assertThat(response.getBody().getMessage()).contains("Errores de validación");
        assertThat(response.getBody().getValidationErrors()).isNotNull();
        assertThat(response.getBody().getValidationErrors()).hasSize(2);
        assertThat(response.getBody().getValidationErrors())
                .containsKey("usuarioId")
                .containsKey("propiedadId");
    }

    @Test
    @DisplayName("Debe manejar Exception genérica correctamente")
    void handleGenericException_ReturnsInternalServerError() {
        // Arrange
        Exception ex = new Exception("Error inesperado");

        // Act
        ResponseEntity<ErrorResponse> response = handler.handleGenericException(ex);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(500);
        assertThat(response.getBody().getError()).isEqualTo("Internal Server Error");
        assertThat(response.getBody().getMessage()).contains("error inesperado");
    }

    @Test
    @DisplayName("Debe incluir timestamp en todas las respuestas de error")
    void allErrorResponses_ShouldIncludeTimestamp() {
        // Test con diferentes tipos de excepciones
        ResourceNotFoundException notFoundEx = new ResourceNotFoundException("Not found");
        ResponseEntity<ErrorResponse> notFoundResponse = handler.handleResourceNotFound(notFoundEx);
        assertThat(notFoundResponse.getBody().getTimestamp()).isNotNull();

        MicroserviceException microserviceEx = new MicroserviceException("Service error");
        ResponseEntity<ErrorResponse> microserviceResponse = handler.handleMicroserviceException(microserviceEx);
        assertThat(microserviceResponse.getBody().getTimestamp()).isNotNull();

        BusinessValidationException businessEx = new BusinessValidationException("Business error");
        ResponseEntity<ErrorResponse> businessResponse = handler.handleBusinessValidation(businessEx);
        assertThat(businessResponse.getBody().getTimestamp()).isNotNull();

        Exception genericEx = new Exception("Generic error");
        ResponseEntity<ErrorResponse> genericResponse = handler.handleGenericException(genericEx);
        assertThat(genericResponse.getBody().getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("Debe manejar mensajes de error largos correctamente")
    void handleException_LongMessage_HandlesCorrectly() {
        // Arrange
        String longMessage = "A".repeat(500);
        BusinessValidationException ex = new BusinessValidationException(longMessage);

        // Act
        ResponseEntity<ErrorResponse> response = handler.handleBusinessValidation(ex);

        // Assert
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).hasSize(500);
    }

    @Test
    @DisplayName("Debe manejar excepciones con mensajes null")
    void handleException_NullMessage_HandlesGracefully() {
        // Arrange
        Exception ex = new Exception((String) null);

        // Act
        ResponseEntity<ErrorResponse> response = handler.handleGenericException(ex);

        // Assert
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}