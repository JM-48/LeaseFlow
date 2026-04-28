package com.rentify.applicationService.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rentify.applicationService.dto.SolicitudArriendoDTO;
import com.rentify.applicationService.exception.BusinessValidationException;
import com.rentify.applicationService.exception.ResourceNotFoundException;
import com.rentify.applicationService.service.SolicitudArriendoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;  // CAMBIADO
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Date;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;  // SOLO MOCKITO
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de integración para SolicitudController
 * Valida las respuestas HTTP y el comportamiento del endpoint
 */
@WebMvcTest(SolicitudController.class)
@DisplayName("Tests de SolicitudController")
class SolicitudControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean  // CAMBIADO de @MockBean
    private SolicitudArriendoService service;

    private SolicitudArriendoDTO solicitudDTO;

    @BeforeEach
    void setUp() {
        solicitudDTO = SolicitudArriendoDTO.builder()
                .id(1L)
                .usuarioId(1L)
                .propiedadId(1L)
                .estado("PENDIENTE")
                .fechaSolicitud(new Date())
                .build();
    }

    @Test
    @DisplayName("POST /api/solicitudes - Debe crear solicitud y retornar 201")
    void crearSolicitud_DatosValidos_Returns201() throws Exception {
        // Arrange
        when(service.crearSolicitud(any(SolicitudArriendoDTO.class))).thenReturn(solicitudDTO);

        // Act & Assert
        mockMvc.perform(post("/api/solicitudes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(solicitudDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.usuarioId").value(1))
                .andExpect(jsonPath("$.propiedadId").value(1))
                .andExpect(jsonPath("$.estado").value("PENDIENTE"));

        verify(service, times(1)).crearSolicitud(any(SolicitudArriendoDTO.class));
    }

    @Test
    @DisplayName("POST /api/solicitudes - Debe retornar 400 cuando faltan datos requeridos")
    void crearSolicitud_SinUsuarioId_Returns400() throws Exception {
        // Arrange
        solicitudDTO.setUsuarioId(null);

        // Act & Assert
        mockMvc.perform(post("/api/solicitudes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(solicitudDTO)))
                .andExpect(status().isBadRequest());

        verify(service, never()).crearSolicitud(any(SolicitudArriendoDTO.class));
    }

    @Test
    @DisplayName("POST /api/solicitudes - Debe retornar 400 cuando hay error de negocio")
    void crearSolicitud_ErrorNegocio_Returns400() throws Exception {
        // Arrange
        when(service.crearSolicitud(any(SolicitudArriendoDTO.class)))
                .thenThrow(new BusinessValidationException("El usuario ya tiene 3 solicitudes activas"));

        // Act & Assert
        mockMvc.perform(post("/api/solicitudes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(solicitudDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("3 solicitudes activas")));
    }

    @Test
    @DisplayName("GET /api/solicitudes - Debe listar todas las solicitudes")
    void listarTodas_Returns200() throws Exception {
        // Arrange
        when(service.listarTodas(false)).thenReturn(Arrays.asList(solicitudDTO));

        // Act & Assert
        mockMvc.perform(get("/api/solicitudes")
                        .param("includeDetails", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1));

        verify(service, times(1)).listarTodas(false);
    }

    @Test
    @DisplayName("GET /api/solicitudes/{id} - Debe retornar solicitud cuando existe")
    void obtenerPorId_SolicitudExiste_Returns200() throws Exception {
        // Arrange
        when(service.obtenerPorId(1L, true)).thenReturn(solicitudDTO);

        // Act & Assert
        mockMvc.perform(get("/api/solicitudes/1")
                        .param("includeDetails", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.estado").value("PENDIENTE"));

        verify(service, times(1)).obtenerPorId(1L, true);
    }

    @Test
    @DisplayName("GET /api/solicitudes/{id} - Debe retornar 404 cuando no existe")
    void obtenerPorId_SolicitudNoExiste_Returns404() throws Exception {
        // Arrange
        when(service.obtenerPorId(999L, true))
                .thenThrow(new ResourceNotFoundException("Solicitud no encontrada con ID: 999"));

        // Act & Assert
        mockMvc.perform(get("/api/solicitudes/999")
                        .param("includeDetails", "true"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("no encontrada")));
    }

    @Test
    @DisplayName("GET /api/solicitudes/usuario/{usuarioId} - Debe retornar solicitudes del usuario")
    void obtenerPorUsuario_Returns200() throws Exception {
        // Arrange
        when(service.obtenerPorUsuario(1L)).thenReturn(Arrays.asList(solicitudDTO));

        // Act & Assert
        mockMvc.perform(get("/api/solicitudes/usuario/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].usuarioId").value(1));

        verify(service, times(1)).obtenerPorUsuario(1L);
    }

    @Test
    @DisplayName("GET /api/solicitudes/propiedad/{propiedadId} - Debe retornar solicitudes de la propiedad")
    void obtenerPorPropiedad_Returns200() throws Exception {
        // Arrange
        when(service.obtenerPorPropiedad(1L)).thenReturn(Arrays.asList(solicitudDTO));

        // Act & Assert
        mockMvc.perform(get("/api/solicitudes/propiedad/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].propiedadId").value(1));

        verify(service, times(1)).obtenerPorPropiedad(1L);
    }

    @Test
    @DisplayName("PATCH /api/solicitudes/{id}/estado - Debe actualizar estado")
    void actualizarEstado_EstadoValido_Returns200() throws Exception {
        // Arrange
        solicitudDTO.setEstado("ACEPTADA");
        when(service.actualizarEstado(1L, "ACEPTADA")).thenReturn(solicitudDTO);

        // Act & Assert
        mockMvc.perform(patch("/api/solicitudes/1/estado")
                        .param("estado", "ACEPTADA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.estado").value("ACEPTADA"));

        verify(service, times(1)).actualizarEstado(1L, "ACEPTADA");
    }

    @Test
    @DisplayName("PATCH /api/solicitudes/{id}/estado - Debe retornar 400 con estado inválido")
    void actualizarEstado_EstadoInvalido_Returns400() throws Exception {
        // Arrange
        when(service.actualizarEstado(1L, "INVALIDO"))
                .thenThrow(new BusinessValidationException("Estado inválido: INVALIDO"));

        // Act & Assert
        mockMvc.perform(patch("/api/solicitudes/1/estado")
                        .param("estado", "INVALIDO"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("Estado inválido")));
    }

    @Test
    @DisplayName("GET /api/solicitudes - Debe usar includeDetails=false por defecto")
    void listarTodas_SinParametro_UsaDefaultFalse() throws Exception {
        // Arrange
        when(service.listarTodas(false)).thenReturn(Arrays.asList(solicitudDTO));

        // Act & Assert
        mockMvc.perform(get("/api/solicitudes"))
                .andExpect(status().isOk());

        verify(service, times(1)).listarTodas(false);
    }
}