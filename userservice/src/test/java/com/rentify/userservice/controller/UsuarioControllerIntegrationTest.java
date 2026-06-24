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
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean; // 🔄 Actualizado para Spring Boot 3.4+
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false) // Desactiva filtros de Spring Security (Evita error 403 por falta de CSRF)
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

    // 🔄 Cambiado @MockBean por @MockitoBean para unificar la versión del contexto de Spring
    @MockitoBean
    private RolService rolService;

    @MockitoBean
    private EstadoService estadoService;

    private static final String BASE_URL = "/api/usuarios";

    // Constantes para simular el API Gateway en endpoints protegidos
    private static final String HEADER_USER = "X-Usuario-Id";
    private static final String HEADER_ROLE = "X-Rol-Id";
    private static final String ADMIN_ID = "1";
    private static final String ROL_ADMIN = "1";
    private static final String USUARIO_ID = "10";
    private static final String ROL_USUARIO = "3";

    @BeforeEach
    void setUp() {
        usuarioRepository.deleteAll();

        // Simulamos las respuestas para cumplir con las restricciones del UsuarioService
        Mockito.when(rolService.obtenerPorId(anyLong()))
                .thenReturn(RolDTO.builder().id(3L).nombre("ARRIENDATARIO").build());

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
                .estadoId(1L)
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
    // 🛡️ TESTS DE SEGURIDAD (ENDPOINTS PROTEGIDOS)
    // ==========================================

    @Test
    @DisplayName("GET /{id} - Retorna 401 si no hay cabeceras de identidad")
    void obtenerPorId_SinHeaders_Retorna401() throws Exception {
        mockMvc.perform(get(BASE_URL + "/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("DELETE /{id} - Retorna 401 si no hay cabeceras de identidad")
    void eliminarUsuario_SinHeaders_Retorna401() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("DELETE /{id} - Retorna 403 si el usuario no es ADMIN")
    void eliminarUsuario_UsuarioNormal_Retorna403() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/1")
                        .header(HEADER_USER, USUARIO_ID)
                        .header(HEADER_ROLE, ROL_USUARIO))
                .andExpect(status().isForbidden());
    }

    // ==========================================
    // 🟢 TESTS DE CAMINO FELIZ (PÚBLICOS Y PRIVADOS)
    // ==========================================

    @Test
    @DisplayName("POST / - Debe registrar usuario y retornar 201 Created (Endpoint Público)")
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
    @DisplayName("POST /login - Login exitoso retorna 200 OK con mensaje y usuario (Endpoint Público)")
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
    @DisplayName("DELETE /{id} - Lanza 204 No Content si se elimina correctamente (Requiere ADMIN)")
    void eliminarUsuario_Success() throws Exception {
        Long idGenerado = registrarUsuarioHelper(generarUsuarioDTO("borrar@test.com", "77666555-4"));

        // Se elimina usando cabeceras de ADMIN
        mockMvc.perform(delete(BASE_URL + "/{id}", idGenerado)
                        .header(HEADER_USER, ADMIN_ID)
                        .header(HEADER_ROLE, ROL_ADMIN))
                .andExpect(status().isNoContent());

        // Se verifica que no exista usando cabeceras de ADMIN para saltar el 403 perimetral
        mockMvc.perform(get(BASE_URL + "/{id}", idGenerado)
                        .header(HEADER_USER, ADMIN_ID)
                        .header(HEADER_ROLE, ROL_ADMIN))
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
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST / - Lanza 400 Bad Request si el RUT ya existe")
    void registrarUsuario_RutDuplicado_Retorna400() throws Exception {
        registrarUsuarioHelper(generarUsuarioDTO("uno@mail.com", "12345678-9"));
        UsuarioDTO usuarioDuplicado = generarUsuarioDTO("dos@mail.com", "12345678-9");

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usuarioDuplicado)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /login - Lanza 401 Unauthorized por clave incorrecta")
    void login_ClaveIncorrecta_Retorna401() throws Exception {
        registrarUsuarioHelper(generarUsuarioDTO("user@mail.com", "55555555-5"));

        LoginDTO loginDTO = new LoginDTO("user@mail.com", "malaclave123");

        mockMvc.perform(post(BASE_URL + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /{id} - Lanza 404 Not Found si el ID no existe (Consultado como Admin)")
    void obtenerPorId_NoExiste_Retorna404() throws Exception {
        // 🛡️ Cambiado a ADMIN_ID y ROL_ADMIN para que tu controlador le permita el paso
        // y la base de datos pueda retornar el 404 real en vez de un 403 de bloqueo.
        mockMvc.perform(get(BASE_URL + "/{id}", 99999L)
                        .header(HEADER_USER, ADMIN_ID)
                        .header(HEADER_ROLE, ROL_ADMIN))
                .andExpect(status().isNotFound());
    }
}