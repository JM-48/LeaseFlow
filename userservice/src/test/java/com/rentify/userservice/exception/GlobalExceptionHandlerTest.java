package com.rentify.userservice.exception;

import com.rentify.userservice.controller.UsuarioController;
import com.rentify.userservice.dto.UsuarioDTO;
import com.rentify.userservice.service.UsuarioService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {UsuarioController.class, GlobalExceptionHandler.class})
@DisplayName("Tests de GlobalExceptionHandler")
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UsuarioService usuarioService;

    @Test
    @DisplayName("Debe manejar ResourceNotFoundException y retornar 404")
    void handleResourceNotFoundException_Returns404() throws Exception {
        // Arrange
        when(usuarioService.obtenerPorId(anyLong(), any(Boolean.class)))
                .thenThrow(new ResourceNotFoundException("Usuario con ID 999 no encontrado"));

        // Act & Assert
        mockMvc.perform(get("/api/usuarios/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Usuario con ID 999 no encontrado"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("Debe manejar BusinessValidationException y retornar 400")
    void handleBusinessValidationException_Returns400() throws Exception {
        // Arrange
        when(usuarioService.registrarUsuario(any(UsuarioDTO.class)))
                .thenThrow(new BusinessValidationException("El email ya está registrado"));

        // CORRECCIÓN: La fecha de nacimiento debe ser un String
        String fechaNacimientoValida = "1995-05-15";

        UsuarioDTO usuarioDTO = UsuarioDTO.builder()
                .pnombre("Juan")
                .snombre("Carlos")
                .papellido("Pérez")
                .fnacimiento(fechaNacimientoValida)
                .email("juan.perez@email.com")
                .rut("12345678-9")
                .ntelefono("987654321")
                .clave("password123")
                .estadoId(1L)
                .rolId(3L)
                .fcreacion(LocalDate.now().toString())
                .factualizacion(LocalDate.now().toString())
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pnombre\":\"Juan\",\"snombre\":\"Carlos\",\"papellido\":\"Pérez\",\"fnacimiento\":\"1995-05-15\",\"email\":\"juan.perez@email.com\",\"rut\":\"12345678-9\",\"ntelefono\":\"987654321\",\"clave\":\"password123\",\"estadoId\":1,\"rolId\":3}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Business Validation Error"))
                .andExpect(jsonPath("$.message").value("El email ya está registrado"));
    }

    @Test
    @DisplayName("Debe manejar AuthenticationException y retornar 401")
    void handleAuthenticationException_Returns401() throws Exception {
        // Arrange
        when(usuarioService.login(any()))
                .thenThrow(new AuthenticationException("Email o contraseña incorrectos"));

        // Act & Assert
        mockMvc.perform(post("/api/usuarios/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@email.com\",\"clave\":\"wrongpass\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Authentication Error"))
                .andExpect(jsonPath("$.message").value("Email o contraseña incorrectos"));
    }

    @Test
    @DisplayName("Debe manejar MethodArgumentNotValidException y retornar 400 con detalles de validación")
    void handleMethodArgumentNotValidException_Returns400WithDetails() throws Exception {
        // Arrange - Enviar usuario sin campos requeridos

        // Act & Assert
        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"invalido\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Error"))
                .andExpect(jsonPath("$.message").value("Errores de validación en los datos enviados"))
                .andExpect(jsonPath("$.validationErrors").exists());
    }

    @Test
    @DisplayName("Debe manejar Exception genérica y retornar 500")
    void handleGenericException_Returns500() throws Exception {
        // Arrange
        when(usuarioService.obtenerPorId(anyLong(), any(Boolean.class)))
                .thenThrow(new RuntimeException("Error inesperado"));

        // Act & Assert
        mockMvc.perform(get("/api/usuarios/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("Ha ocurrido un error inesperado. Por favor intente nuevamente."));
    }
}