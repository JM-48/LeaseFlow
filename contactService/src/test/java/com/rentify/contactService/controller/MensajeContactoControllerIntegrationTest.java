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

import static org.mockito.ArgumentMatchers.anyLong;
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
        when(userServiceClient.isAdmin(USUARIO_NORMAL_ID)).thenReturn(false); // NO es admin
        when(userServiceClient.isUserActive(USUARIO_NORMAL_ID)).thenReturn(true);
    }

    // Método auxiliar DRY para crear un mensaje rápidamente en los tests
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
    // 🟢 TESTS DE CAMINO FELIZ (HAPPY PATH)
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
                .andExpect(jsonPath("$.nombre").value("Juan Pérez"))
                .andExpect(jsonPath("$.email").value("juan.perez@email.com"))
                .andExpect(jsonPath("$.estado").exists());
    }

    @Test
    @DisplayName("POST /{id}/responder - Admin debe poder responder un mensaje")
    void responderMensaje_Success() throws Exception {
        Long mensajeId = crearMensajeBase();

        RespuestaMensajeDTO respuesta = new RespuestaMensajeDTO();
        respuesta.setRespondidoPor(ADMIN_ID); // Usa el ID autorizado
        respuesta.setRespuesta("Hola Ana, hemos verificado el sistema. Intenta nuevamente.");
        respuesta.setNuevoEstado("RESUELTO");

        mockMvc.perform(post(BASE_URL + "/{id}/responder", mensajeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(respuesta)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.respuesta").value(respuesta.getRespuesta()))
                .andExpect(jsonPath("$.respondidoPor").value(ADMIN_ID))
                .andExpect(jsonPath("$.estado").value("RESUELTO"));
    }

    @Test
    @DisplayName("DELETE /{id} - Admin debe poder eliminar un mensaje")
    void eliminarMensaje_Success() throws Exception {
        Long mensajeId = crearMensajeBase();

        mockMvc.perform(delete(BASE_URL + "/{id}", mensajeId)
                        .param("adminId", String.valueOf(ADMIN_ID)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get(BASE_URL + "/{id}", mensajeId))
                .andExpect(status().isNotFound());
    }

    // ==========================================
    // 🔥 TESTS DE REGLAS DE NEGOCIO Y ERRORES
    // ==========================================

    @Test
    @DisplayName("POST / - Falla con 400 Bad Request si los datos de creación son inválidos")
    void crearMensaje_DatosInvalidos_Retorna400() throws Exception {
        MensajeContactoDTO mensajeInvalido = new MensajeContactoDTO();
        mensajeInvalido.setNombre("J"); // Falla @Size
        mensajeInvalido.setEmail("correo-no-valido"); // Falla @Email
        mensajeInvalido.setAsunto(""); // Falla @NotBlank
        mensajeInvalido.setMensaje("Corto"); // Falla @Size

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mensajeInvalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Error"));
    }

    @Test
    @DisplayName("POST /{id}/responder - Falla con 400/403 si el usuario NO es admin")
    void responderMensaje_NoEsAdmin_RetornaError() throws Exception {
        Long mensajeId = crearMensajeBase();

        RespuestaMensajeDTO respuesta = new RespuestaMensajeDTO();
        respuesta.setRespondidoPor(USUARIO_NORMAL_ID); // Intentamos con un usuario sin privilegios
        respuesta.setRespuesta("Respuesta ilegítima intentando saltar seguridad.");
        respuesta.setNuevoEstado("RESUELTO");

        mockMvc.perform(post(BASE_URL + "/{id}/responder", mensajeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(respuesta)))
                // Dependiendo de tu GlobalExceptionHandler, puede ser isBadRequest() o isForbidden()
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Business Validation Error"));
    }

    @Test
    @DisplayName("DELETE /{id} - Falla con 400/403 si el usuario NO es admin")
    void eliminarMensaje_NoEsAdmin_RetornaError() throws Exception {
        Long mensajeId = crearMensajeBase();

        mockMvc.perform(delete(BASE_URL + "/{id}", mensajeId)
                        .param("adminId", String.valueOf(USUARIO_NORMAL_ID))) // Pasamos ID sin privilegios
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Business Validation Error"));
    }
}