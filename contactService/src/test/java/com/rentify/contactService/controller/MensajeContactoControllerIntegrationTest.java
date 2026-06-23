package com.rentify.contactService.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rentify.contactService.client.UserServiceClient;
import com.rentify.contactService.dto.MensajeContactoDTO;
import com.rentify.contactService.dto.RespuestaMensajeDTO;
import com.rentify.contactService.dto.external.UsuarioDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Tests de Integración - MensajeContactoController (Robustos)")
class MensajeContactoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserServiceClient userServiceClient;

    private static final String BASE_URL = "/api/contacto";
    private static final Long ADMIN_ID = 5L;
    private static final Long USUARIO_NORMAL_ID = 2L;

    @BeforeEach
    void setUpMocks() {
        // --- SETUP ADMIN ---
        UsuarioDTO adminUser = new UsuarioDTO();
        adminUser.setId(ADMIN_ID);
        adminUser.setEmail("admin@rentify.com");

        UsuarioDTO.RolDTO rolAdmin = new UsuarioDTO.RolDTO();
        rolAdmin.setId(1L);
        rolAdmin.setNombre("ADMIN");
        adminUser.setRol(rolAdmin);

        UsuarioDTO.EstadoDTO estadoActivo = new UsuarioDTO.EstadoDTO();
        estadoActivo.setId(1L);
        estadoActivo.setNombre("ACTIVO");
        adminUser.setEstado(estadoActivo);

        when(userServiceClient.existsUser(ADMIN_ID)).thenReturn(true);
        when(userServiceClient.isAdmin(ADMIN_ID)).thenReturn(true);
        when(userServiceClient.getUserById(ADMIN_ID)).thenReturn(adminUser);
        when(userServiceClient.isUserActive(ADMIN_ID)).thenReturn(true);

        // --- SETUP USUARIO NORMAL ---
        when(userServiceClient.existsUser(USUARIO_NORMAL_ID)).thenReturn(true);
        when(userServiceClient.isAdmin(USUARIO_NORMAL_ID)).thenReturn(false);
        when(userServiceClient.isUserActive(USUARIO_NORMAL_ID)).thenReturn(true);
    }

    private Long crearMensajeBase() throws Exception {
        MensajeContactoDTO nuevoMensaje = new MensajeContactoDTO();
        nuevoMensaje.setNombre("Ana López");
        nuevoMensaje.setEmail("ana@email.com");
        nuevoMensaje.setAsunto("Problema genérico");
        nuevoMensaje.setMensaje("Tengo un problema que requiere asistencia inmediata.");

        MvcResult result = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nuevoMensaje)))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readValue(result.getResponse().getContentAsString(), MensajeContactoDTO.class).getId();
    }

    // ==========================================
    // 🟢 TESTS DE SEGURIDAD NUEVOS (INTEGRACIÓN)
    // ==========================================

    @Test
    @DisplayName("GET / - Con Headers de Admin permite listar exitosamente")
    void listarMensajes_ComoAdmin_Success() throws Exception {
        crearMensajeBase();

        mockMvc.perform(get(BASE_URL)
                        .header("X-Usuario-Id", ADMIN_ID)
                        .header("X-Rol-Id", 1L)) // Rol Admin
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("GET / - Sin Headers de seguridad rebota con 401 Unauthorized")
    void listarMensajes_SinHeaders_Retorna401() throws Exception {
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /sin-responder - Admin accede (200), Usuario común rebota (403)")
    void verSinResponder_FiltroRoles_Correcto() throws Exception {
        // Admin Ok
        mockMvc.perform(get(BASE_URL + "/sin-responder")
                        .header("X-Rol-Id", 1L))
                .andExpect(status().isOk());

        // Usuario común Bloqueado
        mockMvc.perform(get(BASE_URL + "/sin-responder")
                        .header("X-Rol-Id", 2L))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /estadisticas - Admin accede (200), Usuario común rebota (403)")
    void verEstadisticas_FiltroRoles_Correcto() throws Exception {
        // Admin Ok
        mockMvc.perform(get(BASE_URL + "/estadisticas")
                        .header("X-Rol-Id", 1L))
                .andExpect(status().isOk());

        // Usuario común Bloqueado
        mockMvc.perform(get(BASE_URL + "/estadisticas")
                        .header("X-Rol-Id", 2L))
                .andExpect(status().isForbidden());
    }

    // ==========================================
    // 🟦 TESTS ANTERIORES PRESERVADOS
    // ==========================================

    @Test
    @DisplayName("POST / - Debe crear mensaje de contacto y retornar 201 Created")
    void crearMensaje_Success() throws Exception {
        MensajeContactoDTO nuevoMensaje = new MensajeContactoDTO();
        nuevoMensaje.setNombre("Juan Pérez");
        nuevoMensaje.setEmail("juan.perez@email.com");
        nuevoMensaje.setAsunto("Consulta arriendo Providencia");
        nuevoMensaje.setMensaje("Hola, quisiera agendar una visita para el departamento.");

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nuevoMensaje)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Juan Pérez"));
    }

    @Test
    @DisplayName("POST /{id}/responder - Admin debe poder responder un mensaje")
    void responderMensaje_Success() throws Exception {
        Long mensajeId = crearMensajeBase();

        RespuestaMensajeDTO respuesta = new RespuestaMensajeDTO();
        respuesta.setRespondidoPor(ADMIN_ID);
        respuesta.setRespuesta("Hola Ana, hemos verificado el sistema. Intenta nuevamente.");
        respuesta.setNuevoEstado("RESUELTO");

        mockMvc.perform(post(BASE_URL + "/{id}/responder", mensajeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(respuesta)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("RESUELTO"));
    }

    @Test
    @DisplayName("DELETE /{id} - Admin debe poder eliminar un mensaje")
    void eliminarMensaje_Success() throws Exception {
        Long mensajeId = crearMensajeBase();

        mockMvc.perform(delete(BASE_URL + "/{id}", mensajeId)
                        .param("adminId", String.valueOf(ADMIN_ID)))
                .andExpect(status().isNoContent());
    }
}