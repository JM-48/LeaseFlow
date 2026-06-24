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
        when(service.listarMensajesSeguros(1L, 1L, false)).thenReturn(List.of(mensajeDTO));

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
                        .header("X-Rol-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(service, times(1)).listarMensajesSinResponder();
    }

    @Test
    @DisplayName("GET /api/contacto/sin-responder - Usuario común (Rol 2) recibe 403")
    void listarSinResponder_NoAdmin_Returns403() throws Exception {
        mockMvc.perform(get("/api/contacto/sin-responder")
                        .header("X-Rol-Id", 2L))
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

    @Test
    @DisplayName("POST /api/contacto - Debe retornar 400 cuando faltan campos obligatorios")
    void crearMensaje_CamposFaltantes_Returns400() throws Exception {
        MensajeContactoDTO mensajeInvalido = MensajeContactoDTO.builder().build();

        mockMvc.perform(post("/api/contacto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mensajeInvalido)))
                .andExpect(status().isBadRequest());
    }

    // ==============================================================================
    // TESTS DE SEGURIDAD PARA GET /api/contacto/{id}
    // ==============================================================================

    @Test
    @DisplayName("GET /api/contacto/{id} - Debe retornar 401 si faltan headers")
    void obtenerPorId_SinHeaders_Returns401() throws Exception {
        mockMvc.perform(get("/api/contacto/1")
                        .param("includeDetails", "true"))
                .andExpect(status().isUnauthorized());

        verify(service, never()).obtenerPorId(any(), anyBoolean());
    }

    @Test
    @DisplayName("GET /api/contacto/{id} - El propietario del mensaje puede consultarlo")
    void obtenerPorId_Propietario_Returns200() throws Exception {
        when(service.obtenerPorId(1L, true)).thenReturn(mensajeDTO);

        mockMvc.perform(get("/api/contacto/1")
                        .param("includeDetails", "true")
                        .header("X-Usuario-Id", 1L)
                        .header("X-Rol-Id", 3L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("GET /api/contacto/{id} - Un ADMIN puede consultar cualquier mensaje")
    void obtenerPorId_Admin_Returns200() throws Exception {
        when(service.obtenerPorId(1L, true)).thenReturn(mensajeDTO);

        mockMvc.perform(get("/api/contacto/1")
                        .param("includeDetails", "true")
                        .header("X-Usuario-Id", 99L)
                        .header("X-Rol-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("GET /api/contacto/{id} - Un usuario que no es dueño ni admin recibe 403")
    void obtenerPorId_UsuarioAjeno_Returns403() throws Exception {
        when(service.obtenerPorId(1L, true)).thenReturn(mensajeDTO);

        mockMvc.perform(get("/api/contacto/1")
                        .param("includeDetails", "true")
                        .header("X-Usuario-Id", 99L)
                        .header("X-Rol-Id", 3L))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/contacto/{id} - Debe retornar 404 cuando no existe")
    void obtenerPorId_MensajeNoExiste_Returns404() throws Exception {
        when(service.obtenerPorId(999L, true)).thenThrow(new ResourceNotFoundException("Mensaje no encontrado"));

        mockMvc.perform(get("/api/contacto/999")
                        .param("includeDetails", "true")
                        .header("X-Usuario-Id", 1L)
                        .header("X-Rol-Id", 1L))
                .andExpect(status().isNotFound());
    }

    // ==============================================================================
    // TESTS DE SEGURIDAD PARA GET /api/contacto/email/{email}
    // ==============================================================================

    @Test
    @DisplayName("GET /api/contacto/email/{email} - Sin rol Admin recibe 403")
    void listarPorEmail_NoAdmin_Returns403() throws Exception {
        mockMvc.perform(get("/api/contacto/email/juan@email.com"))
                .andExpect(status().isForbidden());

        verify(service, never()).listarPorEmail(any());
    }

    @Test
    @DisplayName("GET /api/contacto/email/{email} - Admin puede listar por email")
    void listarPorEmail_Admin_Returns200() throws Exception {
        when(service.listarPorEmail("juan@email.com")).thenReturn(List.of(mensajeDTO));

        mockMvc.perform(get("/api/contacto/email/juan@email.com")
                        .header("X-Rol-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(service, times(1)).listarPorEmail("juan@email.com");
    }

    // ==============================================================================
    // TESTS DE SEGURIDAD PARA GET /api/contacto/usuario/{usuarioId}
    // ==============================================================================

    @Test
    @DisplayName("GET /api/contacto/usuario/{usuarioId} - Sin headers recibe 401")
    void listarPorUsuario_SinHeaders_Returns401() throws Exception {
        mockMvc.perform(get("/api/contacto/usuario/1"))
                .andExpect(status().isUnauthorized());

        verify(service, never()).listarPorUsuario(any());
    }

    @Test
    @DisplayName("GET /api/contacto/usuario/{usuarioId} - Un usuario distinto sin ser admin recibe 403")
    void listarPorUsuario_UsuarioDistintoSinSerAdmin_Returns403() throws Exception {
        mockMvc.perform(get("/api/contacto/usuario/1")
                        .header("X-Usuario-Id", 2L)
                        .header("X-Rol-Id", 3L))
                .andExpect(status().isForbidden());

        verify(service, never()).listarPorUsuario(any());
    }

    @Test
    @DisplayName("GET /api/contacto/usuario/{usuarioId} - El propio usuario puede consultar sus mensajes")
    void listarPorUsuario_Propietario_Returns200() throws Exception {
        when(service.listarPorUsuario(1L)).thenReturn(List.of(mensajeDTO));

        mockMvc.perform(get("/api/contacto/usuario/1")
                        .header("X-Usuario-Id", 1L)
                        .header("X-Rol-Id", 3L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(service, times(1)).listarPorUsuario(1L);
    }

    // ==============================================================================
    // TESTS DE SEGURIDAD PARA GET /api/contacto/estado/{estado}
    // ==============================================================================

    @Test
    @DisplayName("GET /api/contacto/estado/{estado} - Sin rol Admin recibe 403")
    void listarPorEstado_NoAdmin_Returns403() throws Exception {
        mockMvc.perform(get("/api/contacto/estado/PENDIENTE"))
                .andExpect(status().isForbidden());

        verify(service, never()).listarPorEstado(any());
    }

    @Test
    @DisplayName("GET /api/contacto/estado/{estado} - Admin puede filtrar por estado")
    void listarPorEstado_Admin_Returns200() throws Exception {
        when(service.listarPorEstado("PENDIENTE")).thenReturn(List.of(mensajeDTO));

        mockMvc.perform(get("/api/contacto/estado/PENDIENTE")
                        .header("X-Rol-Id", 1L))
                .andExpect(status().isOk());

        verify(service, times(1)).listarPorEstado("PENDIENTE");
    }

    // ==============================================================================
    // TESTS DE SEGURIDAD PARA GET /api/contacto/buscar
    // ==============================================================================

    @Test
    @DisplayName("GET /api/contacto/buscar - Sin rol Admin recibe 403")
    void buscarPorPalabraClave_NoAdmin_Returns403() throws Exception {
        mockMvc.perform(get("/api/contacto/buscar")
                        .param("keyword", "departamento"))
                .andExpect(status().isForbidden());

        verify(service, never()).buscarPorPalabraClave(any());
    }

    @Test
    @DisplayName("GET /api/contacto/buscar - Admin puede buscar por palabra clave")
    void buscarPorPalabraClave_Admin_Returns200() throws Exception {
        when(service.buscarPorPalabraClave("departamento")).thenReturn(List.of(mensajeDTO));

        mockMvc.perform(get("/api/contacto/buscar")
                        .param("keyword", "departamento")
                        .header("X-Rol-Id", 1L))
                .andExpect(status().isOk());

        verify(service, times(1)).buscarPorPalabraClave("departamento");
    }

    // ==============================================================================
    // TESTS DE SEGURIDAD PARA PATCH /api/contacto/{id}/estado
    // ==============================================================================

    @Test
    @DisplayName("PATCH /api/contacto/{id}/estado - Sin rol Admin recibe 403")
    void actualizarEstado_NoAdmin_Returns403() throws Exception {
        mockMvc.perform(patch("/api/contacto/1/estado")
                        .param("estado", "RESUELTO"))
                .andExpect(status().isForbidden());

        verify(service, never()).actualizarEstado(any(), any());
    }

    @Test
    @DisplayName("PATCH /api/contacto/{id}/estado - Admin puede actualizar el estado")
    void actualizarEstado_Admin_Returns200() throws Exception {
        mensajeDTO.setEstado("RESUELTO");
        when(service.actualizarEstado(1L, "RESUELTO")).thenReturn(mensajeDTO);

        mockMvc.perform(patch("/api/contacto/1/estado")
                        .param("estado", "RESUELTO")
                        .header("X-Rol-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("RESUELTO"));

        verify(service, times(1)).actualizarEstado(1L, "RESUELTO");
    }

    // ==============================================================================
    // RESPONDER Y ELIMINAR (requieren X-Rol-Id = Admin a nivel de transporte)
    // ==============================================================================

    @Test
    @DisplayName("POST /api/contacto/{id}/responder - Sin rol Admin recibe 403")
    void responderMensaje_SinRolAdmin_Returns403() throws Exception {
        mockMvc.perform(post("/api/contacto/1/responder")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(respuestaDTO)))
                .andExpect(status().isForbidden());

        verify(service, never()).responderMensaje(any(), any());
    }

    @Test
    @DisplayName("POST /api/contacto/{id}/responder - Debe responder mensaje correctamente")
    void responderMensaje_DatosValidos_ReturnsOk() throws Exception {
        mensajeDTO.setRespuesta("Gracias por contactarnos");
        mensajeDTO.setEstado("RESUELTO");
        when(service.responderMensaje(eq(1L), any(RespuestaMensajeDTO.class))).thenReturn(mensajeDTO);

        mockMvc.perform(post("/api/contacto/1/responder")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(respuestaDTO))
                        .header("X-Rol-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("RESUELTO"));
    }

    @Test
    @DisplayName("DELETE /api/contacto/{id} - Sin rol Admin recibe 403")
    void eliminarMensaje_SinRolAdmin_Returns403() throws Exception {
        mockMvc.perform(delete("/api/contacto/1")
                        .param("adminId", "5"))
                .andExpect(status().isForbidden());

        verify(service, never()).eliminarMensaje(any(), any());
    }

    @Test
    @DisplayName("DELETE /api/contacto/{id} - Debe eliminar mensaje correctamente")
    void eliminarMensaje_AdminValido_ReturnsNoContent() throws Exception {
        doNothing().when(service).eliminarMensaje(1L, 5L);

        mockMvc.perform(delete("/api/contacto/1")
                        .param("adminId", "5")
                        .header("X-Rol-Id", 1L))
                .andExpect(status().isNoContent());
    }
}