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
@DisplayName("Tests de Integración - MensajeContactoController")
class MensajeContactoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // Mock del cliente externo para que la lógica de negocio no falle al validar admins
    @MockBean
    private UserServiceClient userServiceClient;

    private static final String BASE_URL = "/api/contacto";
    private static final Long ADMIN_ID = 5L;

    @BeforeEach
    void setUpMocks() {
        // Configuramos un usuario ADMIN simulado con sus objetos anidados reales
        UsuarioDTO adminUser = new UsuarioDTO();
        adminUser.setId(ADMIN_ID);
        adminUser.setEmail("admin@rentify.com");

        // Creamos y asignamos el objeto Rol
        UsuarioDTO.RolDTO rolAdmin = new UsuarioDTO.RolDTO();
        rolAdmin.setId(1L);
        rolAdmin.setNombre("ADMIN");
        adminUser.setRol(rolAdmin);

        // Creamos y asignamos el objeto Estado
        UsuarioDTO.EstadoDTO estadoActivo = new UsuarioDTO.EstadoDTO();
        estadoActivo.setId(1L);
        estadoActivo.setNombre("ACTIVO");
        adminUser.setEstado(estadoActivo);

        // Al consultar por el ID 5, el sistema jurará que es un administrador activo
        when(userServiceClient.existsUser(ADMIN_ID)).thenReturn(true);
        when(userServiceClient.isAdmin(ADMIN_ID)).thenReturn(true);
        when(userServiceClient.getUserById(ADMIN_ID)).thenReturn(adminUser);
        when(userServiceClient.isUserActive(ADMIN_ID)).thenReturn(true);

        // Configuramos el mock para cualquier otro ID (como usuarios normales)
        when(userServiceClient.existsUser(1L)).thenReturn(true);
        when(userServiceClient.isAdmin(1L)).thenReturn(false);
    }

    @Test
    @DisplayName("POST / - Debe crear mensaje de contacto y retornar 201 Created")
    void crearMensaje_Success() throws Exception {
        MensajeContactoDTO nuevoMensaje = new MensajeContactoDTO();
        nuevoMensaje.setNombre("Juan Pérez");
        nuevoMensaje.setEmail("juan.perez@email.com");
        nuevoMensaje.setAsunto("Consulta arriendo Providencia");
        nuevoMensaje.setMensaje("Hola, quisiera agendar una visita para el departamento."); // > 10 caracteres

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nuevoMensaje)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Juan Pérez"))
                .andExpect(jsonPath("$.email").value("juan.perez@email.com"))
                // El servicio probablemente asigna el estado inicial a PENDIENTE
                .andExpect(jsonPath("$.estado").exists());
    }

    @Test
    @DisplayName("POST / - Falla con 400 Bad Request si los datos son inválidos")
    void crearMensaje_DatosInvalidos_Retorna400() throws Exception {
        MensajeContactoDTO mensajeInvalido = new MensajeContactoDTO();
        mensajeInvalido.setNombre("J"); // Falla @Size (min=2)
        mensajeInvalido.setEmail("correo-no-valido"); // Falla @Email
        mensajeInvalido.setAsunto(""); // Falla @NotBlank
        mensajeInvalido.setMensaje("Corto"); // Falla @Size (min=10)

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mensajeInvalido)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /{id}/responder - Admin debe poder responder un mensaje")
    void responderMensaje_Success() throws Exception {
        // 1. Creamos un mensaje previo en la base de datos de prueba
        MensajeContactoDTO nuevoMensaje = new MensajeContactoDTO();
        nuevoMensaje.setNombre("Ana López");
        nuevoMensaje.setEmail("ana@email.com");
        nuevoMensaje.setAsunto("Problema con pago");
        nuevoMensaje.setMensaje("Tengo problemas para procesar mi pago de la reserva.");

        MvcResult result = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nuevoMensaje)))
                .andExpect(status().isCreated())
                .andReturn();

        Long mensajeId = objectMapper.readValue(result.getResponse().getContentAsString(), MensajeContactoDTO.class).getId();

        // 2. Intentamos responderlo actuando como ADMIN (ID = 5)
        RespuestaMensajeDTO respuesta = new RespuestaMensajeDTO();
        respuesta.setRespondidoPor(ADMIN_ID);
        respuesta.setRespuesta("Hola Ana, hemos verificado el sistema. Intenta nuevamente."); // > 10 caracteres
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
        // 1. Creamos el mensaje
        MensajeContactoDTO nuevoMensaje = new MensajeContactoDTO();
        nuevoMensaje.setNombre("Spammer");
        nuevoMensaje.setEmail("spam@spam.com");
        nuevoMensaje.setAsunto("Gana dinero rapido");
        nuevoMensaje.setMensaje("Haz clic aqui para ganar millones.");

        MvcResult result = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nuevoMensaje)))
                .andExpect(status().isCreated())
                .andReturn();

        Long mensajeId = objectMapper.readValue(result.getResponse().getContentAsString(), MensajeContactoDTO.class).getId();

        // 2. Lo eliminamos usando el adminId
        mockMvc.perform(delete(BASE_URL + "/{id}", mensajeId)
                        .param("adminId", String.valueOf(ADMIN_ID)))
                .andExpect(status().isNoContent());

        // 3. Verificamos que ya no existe (Lanzará 404 Not Found)
        mockMvc.perform(get(BASE_URL + "/{id}", mensajeId))
                .andExpect(status().isNotFound());
    }
}