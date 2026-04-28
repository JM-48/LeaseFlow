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
        // Arrange
        when(service.crearMensaje(any(MensajeContactoDTO.class))).thenReturn(mensajeDTO);

        // Act & Assert
        mockMvc.perform(post("/api/contacto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mensajeDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Juan Pérez"))
                .andExpect(jsonPath("$.email").value("juan@email.com"))
                .andExpect(jsonPath("$.estado").value("PENDIENTE"));

        verify(service, times(1)).crearMensaje(any(MensajeContactoDTO.class));
    }

    @Test
    @DisplayName("POST /api/contacto - Debe retornar 400 cuando faltan campos obligatorios")
    void crearMensaje_CamposFaltantes_Returns400() throws Exception {
        // Arrange
        MensajeContactoDTO mensajeInvalido = MensajeContactoDTO.builder().build();

        // Act & Assert
        mockMvc.perform(post("/api/contacto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mensajeInvalido)))
                .andExpect(status().isBadRequest());

        verify(service, never()).crearMensaje(any());
    }

    @Test
    @DisplayName("POST /api/contacto - Debe retornar 400 cuando email es inválido")
    void crearMensaje_EmailInvalido_Returns400() throws Exception {
        // Arrange
        mensajeDTO.setEmail("email-invalido");

        // Act & Assert
        mockMvc.perform(post("/api/contacto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mensajeDTO)))
                .andExpect(status().isBadRequest());

        verify(service, never()).crearMensaje(any());
    }

    @Test
    @DisplayName("GET /api/contacto - Debe listar todos los mensajes")
    void listarTodos_DeberiaRetornarListaDeMensajes() throws Exception {
        // Arrange
        when(service.listarTodos(false)).thenReturn(List.of(mensajeDTO));

        // Act & Assert
        mockMvc.perform(get("/api/contacto")
                        .param("includeDetails", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nombre").value("Juan Pérez"));

        verify(service, times(1)).listarTodos(false);
    }

    @Test
    @DisplayName("GET /api/contacto/{id} - Debe retornar mensaje cuando existe")
    void obtenerPorId_MensajeExiste_ReturnsMensaje() throws Exception {
        // Arrange
        when(service.obtenerPorId(1L, true)).thenReturn(mensajeDTO);

        // Act & Assert
        mockMvc.perform(get("/api/contacto/1")
                        .param("includeDetails", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Juan Pérez"));

        verify(service, times(1)).obtenerPorId(1L, true);
    }

    @Test
    @DisplayName("GET /api/contacto/{id} - Debe retornar 404 cuando no existe")
    void obtenerPorId_MensajeNoExiste_Returns404() throws Exception {
        // Arrange
        when(service.obtenerPorId(999L, true))
                .thenThrow(new ResourceNotFoundException("Mensaje no encontrado"));

        // Act & Assert
        mockMvc.perform(get("/api/contacto/999")
                        .param("includeDetails", "true"))
                .andExpect(status().isNotFound());

        verify(service, times(1)).obtenerPorId(999L, true);
    }

    @Test
    @DisplayName("GET /api/contacto/email/{email} - Debe listar mensajes por email")
    void listarPorEmail_DeberiaRetornarMensajesDelEmail() throws Exception {
        // Arrange
        when(service.listarPorEmail("juan@email.com")).thenReturn(List.of(mensajeDTO));

        // Act & Assert
        mockMvc.perform(get("/api/contacto/email/juan@email.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].email").value("juan@email.com"));

        verify(service, times(1)).listarPorEmail("juan@email.com");
    }

    @Test
    @DisplayName("GET /api/contacto/usuario/{usuarioId} - Debe listar mensajes por usuario")
    void listarPorUsuario_DeberiaRetornarMensajesDelUsuario() throws Exception {
        // Arrange
        when(service.listarPorUsuario(1L)).thenReturn(List.of(mensajeDTO));

        // Act & Assert
        mockMvc.perform(get("/api/contacto/usuario/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].usuarioId").value(1));

        verify(service, times(1)).listarPorUsuario(1L);
    }

    @Test
    @DisplayName("GET /api/contacto/estado/{estado} - Debe listar mensajes por estado")
    void listarPorEstado_DeberiaRetornarMensajesPorEstado() throws Exception {
        // Arrange
        when(service.listarPorEstado("PENDIENTE")).thenReturn(List.of(mensajeDTO));

        // Act & Assert
        mockMvc.perform(get("/api/contacto/estado/PENDIENTE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].estado").value("PENDIENTE"));

        verify(service, times(1)).listarPorEstado("PENDIENTE");
    }

    @Test
    @DisplayName("GET /api/contacto/estado/{estado} - Debe retornar 400 cuando estado es inválido")
    void listarPorEstado_EstadoInvalido_Returns400() throws Exception {
        // Arrange
        when(service.listarPorEstado("INVALIDO"))
                .thenThrow(new BusinessValidationException("Estado inválido"));

        // Act & Assert
        mockMvc.perform(get("/api/contacto/estado/INVALIDO"))
                .andExpect(status().isBadRequest());

        verify(service, times(1)).listarPorEstado("INVALIDO");
    }

    @Test
    @DisplayName("GET /api/contacto/sin-responder - Debe listar mensajes sin responder")
    void listarSinResponder_DeberiaRetornarMensajesSinRespuesta() throws Exception {
        // Arrange
        when(service.listarMensajesSinResponder()).thenReturn(List.of(mensajeDTO));

        // Act & Assert
        mockMvc.perform(get("/api/contacto/sin-responder"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(service, times(1)).listarMensajesSinResponder();
    }

    @Test
    @DisplayName("GET /api/contacto/buscar - Debe buscar mensajes por palabra clave")
    void buscarPorPalabraClave_DeberiaRetornarResultados() throws Exception {
        // Arrange
        when(service.buscarPorPalabraClave("arriendo")).thenReturn(List.of(mensajeDTO));

        // Act & Assert
        mockMvc.perform(get("/api/contacto/buscar")
                        .param("keyword", "arriendo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(service, times(1)).buscarPorPalabraClave("arriendo");
    }

    @Test
    @DisplayName("PATCH /api/contacto/{id}/estado - Debe actualizar estado correctamente")
    void actualizarEstado_EstadoValido_ReturnsOk() throws Exception {
        // Arrange
        mensajeDTO.setEstado("EN_PROCESO");
        when(service.actualizarEstado(1L, "EN_PROCESO")).thenReturn(mensajeDTO);

        // Act & Assert
        mockMvc.perform(patch("/api/contacto/1/estado")
                        .param("estado", "EN_PROCESO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("EN_PROCESO"));

        verify(service, times(1)).actualizarEstado(1L, "EN_PROCESO");
    }

    @Test
    @DisplayName("POST /api/contacto/{id}/responder - Debe responder mensaje correctamente")
    void responderMensaje_DatosValidos_ReturnsOk() throws Exception {
        // Arrange
        mensajeDTO.setRespuesta("Gracias por contactarnos");
        mensajeDTO.setEstado("RESUELTO");
        when(service.responderMensaje(eq(1L), any(RespuestaMensajeDTO.class)))
                .thenReturn(mensajeDTO);

        // Act & Assert
        mockMvc.perform(post("/api/contacto/1/responder")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(respuestaDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("RESUELTO"));

        verify(service, times(1)).responderMensaje(eq(1L), any(RespuestaMensajeDTO.class));
    }

    @Test
    @DisplayName("POST /api/contacto/{id}/responder - Debe retornar 400 cuando respuesta es inválida")
    void responderMensaje_RespuestaInvalida_Returns400() throws Exception {
        // Arrange
        RespuestaMensajeDTO respuestaInvalida = RespuestaMensajeDTO.builder()
                .respuesta("") // Respuesta vacía
                .respondidoPor(5L)
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/contacto/1/responder")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(respuestaInvalida)))
                .andExpect(status().isBadRequest());

        verify(service, never()).responderMensaje(anyLong(), any());
    }

    @Test
    @DisplayName("DELETE /api/contacto/{id} - Debe eliminar mensaje correctamente")
    void eliminarMensaje_AdminValido_ReturnsNoContent() throws Exception {
        // Arrange
        doNothing().when(service).eliminarMensaje(1L, 5L);

        // Act & Assert
        mockMvc.perform(delete("/api/contacto/1")
                        .param("adminId", "5"))
                .andExpect(status().isNoContent());

        verify(service, times(1)).eliminarMensaje(1L, 5L);
    }

    @Test
    @DisplayName("DELETE /api/contacto/{id} - Debe retornar 404 cuando mensaje no existe")
    void eliminarMensaje_MensajeNoExiste_Returns404() throws Exception {
        // Arrange
        doThrow(new ResourceNotFoundException("Mensaje no encontrado"))
                .when(service).eliminarMensaje(999L, 5L);

        // Act & Assert
        mockMvc.perform(delete("/api/contacto/999")
                        .param("adminId", "5"))
                .andExpect(status().isNotFound());

        verify(service, times(1)).eliminarMensaje(999L, 5L);
    }

    @Test
    @DisplayName("GET /api/contacto/estadisticas - Debe retornar estadísticas")
    void obtenerEstadisticas_DeberiaRetornarEstadisticas() throws Exception {
        // Arrange
        Map<String, Long> estadisticas = Map.of(
                "total", 10L,
                "pendientes", 3L,
                "enProceso", 2L,
                "resueltos", 5L
        );
        when(service.obtenerEstadisticas()).thenReturn(estadisticas);

        // Act & Assert
        mockMvc.perform(get("/api/contacto/estadisticas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(10))
                .andExpect(jsonPath("$.pendientes").value(3))
                .andExpect(jsonPath("$.enProceso").value(2))
                .andExpect(jsonPath("$.resueltos").value(5));

        verify(service, times(1)).obtenerEstadisticas();
    }
}