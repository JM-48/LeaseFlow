package com.rentify.contactService.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rentify.contactService.dto.MensajeContactoDTO;
import com.rentify.contactService.dto.RespuestaMensajeDTO;
import com.rentify.contactService.exception.BusinessValidationException;
import com.rentify.contactService.exception.ResourceNotFoundException;
import com.rentify.contactService.service.MensajeContactoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MensajeContactoController.class)
@DisplayName("Tests de MensajeContactoController")
class MensajeContactoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MensajeContactoService service;

    private MensajeContactoDTO mensajeDTO;
    private RespuestaMensajeDTO respuestaDTO;

    @BeforeEach
    void setUp() {
        mensajeDTO = MensajeContactoDTO.builder()
                .id(1L)
                .nombre("Juan Pérez")
                .email("juan@email.com")
                .asunto("Consulta sobre arriendo")
                .mensaje("Quisiera más información sobre el departamento en Providencia")
                .numeroTelefono("+56912345678")
                .usuarioId(1L)
                .estado("PENDIENTE")
                .fechaCreacion(new Date())
                .build();

        respuestaDTO = RespuestaMensajeDTO.builder()
                .respuesta("Gracias por contactarnos. Le responderemos pronto.")
                .respondidoPor(5L)
                .nuevoEstado("RESUELTO")
                .build();
    }

    @Test
    @DisplayName("POST /api/contacto - Debe crear mensaje y retornar 201")
    void crearMensaje_DatosValidos_Returns201() throws Exception {
        when(service.crearMensaje(any(MensajeContactoDTO.class))).thenReturn(mensajeDTO);

        mockMvc.perform(post("/api/contacto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mensajeDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Juan Pérez"));

        verify(service, times(1)).crearMensaje(any(MensajeContactoDTO.class));
    }

    @Test
    @DisplayName("GET /api/contacto - Debe listar mensajes autorizados con Headers")
    void listarMensajesSeguros_ConHeadersValidos_Returns200() throws Exception {
        // Arrange - Ahora mockeamos el nuevo método seguro
        when(service.listarMensajesSeguros(1L, 1L, false)).thenReturn(List.of(mensajeDTO));

        // Act & Assert - Enviamos los headers de seguridad simulados por la Gateway
        mockMvc.perform(get("/api/contacto")
                        .header("X-Usuario-Id", 1L)
                        .header("X-Rol-Id", 1L)
                        .param("includeDetails", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1));

        verify(service, times(1)).listarMensajesSeguros(1L, 1L, false);
    }

    @Test
    @DisplayName("GET /api/contacto - Debe retornar 401 si faltan Headers de seguridad")
    void listarMensajesSeguros_SinHeaders_Returns401() throws Exception {
        mockMvc.perform(get("/api/contacto")
                        .param("includeDetails", "false"))
                .andExpect(status().isUnauthorized());

        verify(service, never()).listarMensajesSeguros(anyLong(), anyLong(), anyBoolean());
    }

    @Test
    @DisplayName("GET /api/contacto/sin-responder - Admin (Rol 1) puede ver pendientes")
    void listarSinResponder_Admin_Returns200() throws Exception {
        when(service.listarMensajesSinResponder()).thenReturn(List.of(mensajeDTO));

        mockMvc.perform(get("/api/contacto/sin-responder")
                        .header("X-Rol-Id", 1L)) // Rol Admin
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(service, times(1)).listarMensajesSinResponder();
    }

    @Test
    @DisplayName("GET /api/contacto/sin-responder - Usuario común (Rol 2) recibe 403")
    void listarSinResponder_NoAdmin_Returns403() throws Exception {
        mockMvc.perform(get("/api/contacto/sin-responder")
                        .header("X-Rol-Id", 2L)) // Rol común
                .andExpect(status().isForbidden());

        verify(service, never()).listarMensajesSinResponder();
    }

    @Test
    @DisplayName("GET /api/contacto/estadisticas - Admin (Rol 1) puede ver estadísticas")
    void obtenerEstadisticas_Admin_Returns200() throws Exception {
        Map<String, Long> estadisticas = Map.of("total", 10L, "pendientes", 3L);
        when(service.obtenerEstadisticas()).thenReturn(estadisticas);

        mockMvc.perform(get("/api/contacto/estadisticas")
                        .header("X-Rol-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(10));

        verify(service, times(1)).obtenerEstadisticas();
    }

    @Test
    @DisplayName("GET /api/contacto/estadisticas - Intruso recibe 403 Forbidden")
    void obtenerEstadisticas_NoAdmin_Returns403() throws Exception {
        mockMvc.perform(get("/api/contacto/estadisticas")
                        .header("X-Rol-Id", 2L))
                .andExpect(status().isForbidden());

        verify(service, never()).obtenerEstadisticas();
    }

    // --- Los demás métodos que no sufrieron cambios de endpoint se mantienen limpios ---

    @Test
    @DisplayName("POST /api/contacto - Debe retornar 400 cuando faltan campos obligatorios")
    void crearMensaje_CamposFaltantes_Returns400() throws Exception {
        MensajeContactoDTO mensajeInvalido = MensajeContactoDTO.builder().build();

        mockMvc.perform(post("/api/contacto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mensajeInvalido)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/contacto/{id} - Debe retornar mensaje cuando existe")
    void obtenerPorId_MensajeExiste_ReturnsMensaje() throws Exception {
        when(service.obtenerPorId(1L, true)).thenReturn(mensajeDTO);

        mockMvc.perform(get("/api/contacto/1")
                        .param("includeDetails", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("GET /api/contacto/{id} - Debe retornar 404 cuando no existe")
    void obtenerPorId_MensajeNoExiste_Returns404() throws Exception {
        when(service.obtenerPorId(999L, true)).thenThrow(new ResourceNotFoundException("Mensaje no encontrado"));

        mockMvc.perform(get("/api/contacto/999")
                        .param("includeDetails", "true"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/contacto/{id}/responder - Debe responder mensaje correctamente")
    void responderMensaje_DatosValidos_ReturnsOk() throws Exception {
        mensajeDTO.setRespuesta("Gracias por contactarnos");
        mensajeDTO.setEstado("RESUELTO");
        when(service.responderMensaje(eq(1L), any(RespuestaMensajeDTO.class))).thenReturn(mensajeDTO);

        mockMvc.perform(post("/api/contacto/1/responder")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(respuestaDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("RESUELTO"));
    }

    @Test
    @DisplayName("DELETE /api/contacto/{id} - Debe eliminar mensaje correctamente")
    void eliminarMensaje_AdminValido_ReturnsNoContent() throws Exception {
        doNothing().when(service).eliminarMensaje(1L, 5L);

        mockMvc.perform(delete("/api/contacto/1")
                        .param("adminId", "5"))
                .andExpect(status().isNoContent());
    }
}