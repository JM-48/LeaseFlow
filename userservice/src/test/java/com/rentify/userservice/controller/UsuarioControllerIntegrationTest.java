package com.rentify.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rentify.userservice.dto.EstadoDTO;
import com.rentify.userservice.dto.LoginDTO;
import com.rentify.userservice.dto.RolDTO;
import com.rentify.userservice.dto.UsuarioDTO;
import com.rentify.userservice.repository.UsuarioRepository;
import com.rentify.userservice.service.EstadoService;
import com.rentify.userservice.service.RolService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false) // 🔥 SOLUCIÓN 1: Desactiva filtros de Spring Security (Evita error 403 por falta de CSRF)
@ActiveProfiles("test")
@Transactional
@DisplayName("Tests de Integración - UsuarioController")
class UsuarioControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @MockBean
    private RolService rolService;

    @MockBean
    private EstadoService estadoService;

    private static final String BASE_URL = "/api/usuarios";

    @BeforeEach
    void setUp() {
        usuarioRepository.deleteAll();

        // Simulamos las respuestas para cumplir con las restricciones del UsuarioService
        Mockito.when(rolService.obtenerPorId(anyLong()))
                .thenReturn(RolDTO.builder().id(3L).nombre("ARRIENDATARIO").build());

        // Usamos Mockito.any() para asegurar que capture la llamada de forma segura
        Mockito.when(estadoService.obtenerPorId(Mockito.any()))
                .thenReturn(EstadoDTO.builder().id(1L).nombre("ACTIVO").build());
    }

    // ==========================================
    // 🛠️ MÉTODOS AUXILIARES (DRY)
    // ==========================================

    private UsuarioDTO generarUsuarioDTO(String email, String rut) {
        return UsuarioDTO.builder()
                .pnombre("Usuario")
                .snombre("Miau")
                .papellido("Test")
                .email(email)
                .rut(rut)
                .fnacimiento("1990-01-01")
                .clave("secreta123")
                .ntelefono("+56912345678")
                .rolId(3L)
                .estadoId(1L) // 🔥 SOLUCIÓN 2: Enviamos estadoId para evitar que rompa el 'NOT NULL' en la BD
                .build();
    }

    private Long registrarUsuarioHelper(UsuarioDTO dto) throws Exception {
        MvcResult result = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readValue(result.getResponse().getContentAsString(), UsuarioDTO.class).getId();
    }

    // ==========================================
    // 🟢 TESTS DE CAMINO FELIZ
    // ==========================================

    @Test
    @DisplayName("POST / - Debe registrar usuario y retornar 201 Created")
    void registrarUsuario_Success() throws Exception {
        UsuarioDTO nuevoUsuario = generarUsuarioDTO("integracion@test.com", "11222333-4");

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nuevoUsuario)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("integracion@test.com"))
                .andExpect(jsonPath("$.clave").doesNotExist());
    }

    @Test
    @DisplayName("POST /login - Login exitoso retorna 200 OK con mensaje y usuario")
    void login_Success() throws Exception {
        registrarUsuarioHelper(generarUsuarioDTO("login@test.com", "99888777-6"));

        LoginDTO loginDTO = new LoginDTO("login@test.com", "secreta123");

        mockMvc.perform(post(BASE_URL + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Login exitoso"))
                .andExpect(jsonPath("$.usuario.email").value("login@test.com"));
    }

    @Test
    @DisplayName("DELETE /{id} - Lanza 204 No Content si se elimina correctamente")
    void eliminarUsuario_Success() throws Exception {
        Long idGenerado = registrarUsuarioHelper(generarUsuarioDTO("borrar@test.com", "77666555-4"));

        mockMvc.perform(delete(BASE_URL + "/{id}", idGenerado))
                .andExpect(status().isNoContent());

        mockMvc.perform(get(BASE_URL + "/{id}", idGenerado))
                .andExpect(status().isNotFound());
    }

    // ==========================================
    // 🔴 TESTS DE REGLAS DE NEGOCIO Y ERRORES
    // ==========================================

    @Test
    @DisplayName("POST / - Lanza 400 Bad Request si el email ya existe")
    void registrarUsuario_EmailDuplicado_Retorna400() throws Exception {
        registrarUsuarioHelper(generarUsuarioDTO("duplicado@mail.com", "11111111-1"));
        UsuarioDTO usuarioDuplicado = generarUsuarioDTO("duplicado@mail.com", "22222222-2");

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usuarioDuplicado)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Business Validation Error"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("POST / - Lanza 400 Bad Request si el RUT ya existe")
    void registrarUsuario_RutDuplicado_Retorna400() throws Exception {
        registrarUsuarioHelper(generarUsuarioDTO("uno@mail.com", "12345678-9"));
        UsuarioDTO usuarioDuplicado = generarUsuarioDTO("dos@mail.com", "12345678-9");

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usuarioDuplicado)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Business Validation Error"));
    }

    @Test
    @DisplayName("POST /login - Lanza 401 Unauthorized por clave incorrecta")
    void login_ClaveIncorrecta_Retorna401() throws Exception {
        registrarUsuarioHelper(generarUsuarioDTO("user@mail.com", "55555555-5"));

        LoginDTO loginDTO = new LoginDTO("user@mail.com", "malaclave123");

        mockMvc.perform(post(BASE_URL + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Authentication Error"));
    }

    @Test
    @DisplayName("GET /{id} - Lanza 404 Not Found si el ID no existe")
    void obtenerPorId_NoExiste_Retorna404() throws Exception {
        mockMvc.perform(get(BASE_URL + "/{id}", 99999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }
}