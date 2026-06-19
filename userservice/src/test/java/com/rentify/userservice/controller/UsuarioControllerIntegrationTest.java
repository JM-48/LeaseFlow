package com.rentify.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rentify.userservice.dto.LoginDTO;
import com.rentify.userservice.dto.UsuarioDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Tests de Integración - UsuarioController")
class UsuarioControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE_URL = "/api/usuarios";

    @Test
    @DisplayName("POST / - Debe registrar usuario y retornar 201 Created")
    void registrarUsuario_Success() throws Exception {
        // Arrange
        UsuarioDTO nuevoUsuario = new UsuarioDTO();
        nuevoUsuario.setPnombre("Integracion");
        nuevoUsuario.setPapellido("Test");
        nuevoUsuario.setEmail("integracion@test.com");
        nuevoUsuario.setRut("11222333-4");
        nuevoUsuario.setFnacimiento("1995-10-15");
        nuevoUsuario.setClave("secreta123");
        nuevoUsuario.setRolId(3L);

        // Act & Assert
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nuevoUsuario)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("integracion@test.com"))
                .andExpect(jsonPath("$.pnombre").value("Integracion"))
                .andExpect(jsonPath("$.clave").doesNotExist()); // Verifica que no viaje el hash
    }

    @Test
    @DisplayName("POST / - Lanza 400 Bad Request si el email ya existe")
    void registrarUsuario_EmailDuplicado_Retorna400() throws Exception {
        // Arrange: Creamos el primer usuario
        UsuarioDTO usuario1 = new UsuarioDTO();
        usuario1.setPnombre("Primero");
        usuario1.setPapellido("Test");
        usuario1.setEmail("mismo@mail.com");
        usuario1.setRut("11111111-1");
        usuario1.setFnacimiento("1990-01-01");
        usuario1.setClave("1234");
        usuario1.setRolId(3L);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usuario1)))
                .andExpect(status().isCreated());

        // Arrange: Creamos el segundo con el mismo email pero distinto RUT
        UsuarioDTO usuario2 = new UsuarioDTO();
        usuario2.setPnombre("Segundo");
        usuario2.setPapellido("Test");
        usuario2.setEmail("mismo@mail.com"); // Email duplicado
        usuario2.setRut("22222222-2");
        usuario2.setFnacimiento("1992-02-02");
        usuario2.setClave("1234");
        usuario2.setRolId(3L);

        // Act & Assert
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usuario2)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Business Validation Error"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("POST /login - Login exitoso retorna 200 OK con mensaje y usuario")
    void login_Success() throws Exception {
        // Arrange: Primero registramos un usuario en la BD de prueba
        UsuarioDTO nuevoUsuario = new UsuarioDTO();
        nuevoUsuario.setPnombre("Login");
        nuevoUsuario.setPapellido("Test");
        nuevoUsuario.setEmail("login@test.com");
        nuevoUsuario.setRut("99888777-6");
        nuevoUsuario.setFnacimiento("1990-01-01");
        nuevoUsuario.setClave("miclave");
        nuevoUsuario.setRolId(3L);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nuevoUsuario)))
                .andExpect(status().isCreated());

        // Armamos el payload del login
        LoginDTO loginDTO = new LoginDTO("login@test.com", "miclave");

        // Act & Assert
        mockMvc.perform(post(BASE_URL + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Login exitoso"))
                .andExpect(jsonPath("$.usuario.email").value("login@test.com"));
    }

    @Test
    @DisplayName("POST /login - Lanza 401 Unauthorized por clave incorrecta")
    void login_ClaveIncorrecta_Retorna401() throws Exception {
        // Arrange
        LoginDTO loginDTO = new LoginDTO("no.existe@mail.com", "malaclave");

        // Act & Assert
        mockMvc.perform(post(BASE_URL + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Authentication Error"));
    }

    @Test
    @DisplayName("GET /{id} - Lanza 404 Not Found si el ID no existe")
    void obtenerPorId_NoExiste_Retorna404() throws Exception {
        // Act & Assert
        mockMvc.perform(get(BASE_URL + "/{id}", 99999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    @DisplayName("DELETE /{id} - Lanza 204 No Content si se elimina correctamente")
    void eliminarUsuario_Success() throws Exception {
        // Arrange: Registrar un usuario primero para obtener un ID real
        UsuarioDTO nuevoUsuario = new UsuarioDTO();
        nuevoUsuario.setPnombre("Borrar");
        nuevoUsuario.setPapellido("Test");
        nuevoUsuario.setEmail("borrar@test.com");
        nuevoUsuario.setRut("77666555-4");
        nuevoUsuario.setFnacimiento("1990-01-01");
        nuevoUsuario.setClave("1234");
        nuevoUsuario.setRolId(3L);

        MvcResult result = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nuevoUsuario)))
                .andExpect(status().isCreated())
                .andReturn();

        // Extraer el ID del usuario recién creado
        String responseBody = result.getResponse().getContentAsString();
        UsuarioDTO usuarioCreado = objectMapper.readValue(responseBody, UsuarioDTO.class);
        Long idGenerado = usuarioCreado.getId();

        // Act: Eliminar el usuario
        mockMvc.perform(delete(BASE_URL + "/{id}", idGenerado))
                .andExpect(status().isNoContent());

        // Assert: Verificar que ya no existe (debe dar 404)
        mockMvc.perform(get(BASE_URL + "/{id}", idGenerado))
                .andExpect(status().isNotFound());
    }
}