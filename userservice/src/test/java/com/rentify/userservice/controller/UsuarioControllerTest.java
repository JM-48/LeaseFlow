package com.rentify.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rentify.userservice.dto.*;
import com.rentify.userservice.exception.AuthenticationException;
import com.rentify.userservice.exception.BusinessValidationException;
import com.rentify.userservice.exception.ResourceNotFoundException;
import com.rentify.userservice.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UsuarioController.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("Tests de UsuarioController")
class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UsuarioService usuarioService;

    private UsuarioDTO usuarioDTO;
    private LoginDTO loginDTO;

    // Constantes para simular el API Gateway
    private static final String HEADER_USER = "X-Usuario-Id";
    private static final String HEADER_ROLE = "X-Rol-Id";
    private static final String ADMIN_ID = "1";
    private static final String ROL_ADMIN = "1";
    private static final String USUARIO_ID = "10";
    private static final String ROL_USUARIO = "3"; // Ej: Arriendatario

    @BeforeEach
    void setUp() {
        String fechaNacimientoValida = "1995-05-15";

        usuarioDTO = UsuarioDTO.builder()
                .id(10L) // Coincide con USUARIO_ID para test de dueño
                .pnombre("Juan")
                .snombre("Carlos")
                .papellido("Pérez")
                .fnacimiento(fechaNacimientoValida)
                .email("juan.perez@email.com")
                .rut("12345678-9")
                .ntelefono("987654321")
                .duocVip(false)
                .clave("password123")
                .puntos(0)
                .codigoRef("ABC123XYZ")
                .fcreacion(LocalDate.now().toString())
                .factualizacion(LocalDate.now().toString())
                .estadoId(1L)
                .rolId(3L)
                .build();

        loginDTO = LoginDTO.builder()
                .email("juan.perez@email.com")
                .clave("password123")
                .build();
    }

    // =========================================================================
    // 🛡️ TESTS DE SEGURIDAD GENERAL (ENDPOINTS PROTEGIDOS)
    // =========================================================================

    @Test
    @DisplayName("GET /api/usuarios - Retorna 401 si no hay cabeceras de identidad")
    void obtenerTodos_SinHeaders_Returns401() throws Exception {
        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PUT /api/usuarios/{id} - Retorna 403 si un usuario normal intenta actualizar")
    void actualizarUsuario_UsuarioNormal_Returns403() throws Exception {
        mockMvc.perform(put("/api/usuarios/10")
                        .header(HEADER_USER, USUARIO_ID)
                        .header(HEADER_ROLE, ROL_USUARIO) // Rol insuficiente
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usuarioDTO)))
                .andExpect(status().isForbidden());
    }

    // =========================================================================
    // 🟢 TESTS DE REGISTRO Y LOGIN (PÚBLICOS)
    // =========================================================================

    @Test
    @DisplayName("POST /api/usuarios - Debe registrar usuario y retornar 201")
    void registrarUsuario_DatosValidos_Returns201() throws Exception {
        when(usuarioService.registrarUsuario(any(UsuarioDTO.class))).thenReturn(usuarioDTO);

        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usuarioDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.email").value("juan.perez@email.com"))
                .andExpect(jsonPath("$.pnombre").value("Juan"));

        verify(usuarioService, times(1)).registrarUsuario(any(UsuarioDTO.class));
    }

    @Test
    @DisplayName("POST /api/usuarios - Debe retornar 400 cuando faltan campos obligatorios")
    void registrarUsuario_CamposFaltantes_Returns400() throws Exception {
        UsuarioDTO usuarioInvalido = UsuarioDTO.builder()
                .email("invalido@email.com")
                .clave("password123")
                .rut("12345678-9")
                .estadoId(1L)
                .rolId(3L)
                .build();

        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usuarioInvalido)))
                .andExpect(status().isBadRequest());

        verify(usuarioService, never()).registrarUsuario(any());
    }

    @Test
    @DisplayName("POST /api/usuarios/login - Debe autenticar usuario exitosamente")
    void login_CredencialesValidas_Returns200() throws Exception {
        when(usuarioService.login(any(LoginDTO.class))).thenReturn(usuarioDTO);

        mockMvc.perform(post("/api/usuarios/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Login exitoso"))
                .andExpect(jsonPath("$.usuario.email").value("juan.perez@email.com"));

        verify(usuarioService, times(1)).login(any(LoginDTO.class));
    }

    @Test
    @DisplayName("POST /api/usuarios/login - Debe retornar 401 cuando las credenciales son inválidas")
    void login_CredencialesInvalidas_Returns401() throws Exception {
        when(usuarioService.login(any(LoginDTO.class)))
                .thenThrow(new AuthenticationException("Email o contraseña incorrectos"));

        mockMvc.perform(post("/api/usuarios/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isUnauthorized());

        verify(usuarioService, times(1)).login(any(LoginDTO.class));
    }

    // =========================================================================
    // 🟡 TESTS DE CONSULTAS (PROTEGIDOS POR AUTH / PERMISOS ESPECÍFICOS)
    // =========================================================================

    @Test
    @DisplayName("GET /api/usuarios - Debe retornar lista de usuarios (Requiere Admin)")
    void obtenerTodos_DeberiaRetornarListaDeUsuarios() throws Exception {
        UsuarioDTO usuario2 = UsuarioDTO.builder().id(2L).email("maria@email.com").build();
        List<UsuarioDTO> usuarios = Arrays.asList(usuarioDTO, usuario2);

        when(usuarioService.obtenerTodos(false)).thenReturn(usuarios);

        mockMvc.perform(get("/api/usuarios")
                        .header(HEADER_USER, ADMIN_ID)
                        .header(HEADER_ROLE, ROL_ADMIN)
                        .param("includeDetails", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));

        verify(usuarioService, times(1)).obtenerTodos(false);
    }

    @Test
    @DisplayName("GET /api/usuarios/{id} - Dueño consulta su propio perfil, retorna 200")
    void obtenerPorId_DuenoConsulta_Returns200() throws Exception {
        when(usuarioService.obtenerPorId(10L, true)).thenReturn(usuarioDTO);

        mockMvc.perform(get("/api/usuarios/10")
                        .header(HEADER_USER, USUARIO_ID) // 10
                        .header(HEADER_ROLE, ROL_USUARIO)
                        .param("includeDetails", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));

        verify(usuarioService, times(1)).obtenerPorId(10L, true);
    }

    @Test
    @DisplayName("GET /api/usuarios/{id} - Usuario normal consulta perfil de otro, retorna 403")
    void obtenerPorId_UsuarioNormalConsultaOtro_Returns403() throws Exception {
        mockMvc.perform(get("/api/usuarios/99") // Intenta ver el ID 99
                        .header(HEADER_USER, USUARIO_ID) // El usuario es el ID 10
                        .header(HEADER_ROLE, ROL_USUARIO)
                        .param("includeDetails", "true"))
                .andExpect(status().isForbidden());

        // El servicio no debe llegar a ser llamado por el bloqueo de seguridad
        verify(usuarioService, never()).obtenerPorId(anyLong(), anyBoolean());
    }

    @Test
    @DisplayName("GET /api/usuarios/email/{email} - Dueño consulta su propio email, retorna 200")
    void obtenerPorEmail_DuenoConsulta_Returns200() throws Exception {
        when(usuarioService.obtenerPorEmail("juan.perez@email.com", true)).thenReturn(usuarioDTO);

        mockMvc.perform(get("/api/usuarios/email/juan.perez@email.com")
                        .header(HEADER_USER, USUARIO_ID) // 10
                        .header(HEADER_ROLE, ROL_USUARIO)
                        .param("includeDetails", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("juan.perez@email.com"));

        verify(usuarioService, times(1)).obtenerPorEmail("juan.perez@email.com", true);
    }

    @Test
    @DisplayName("GET /api/usuarios/email/{email} - Usuario normal busca email de otro, retorna 403")
    void obtenerPorEmail_UsuarioNormalBuscaOtro_Returns403() throws Exception {
        // Simulamos que el email pertenece al ID 99
        UsuarioDTO otroUsuario = UsuarioDTO.builder().id(99L).email("otro@email.com").build();
        when(usuarioService.obtenerPorEmail("otro@email.com", true)).thenReturn(otroUsuario);

        mockMvc.perform(get("/api/usuarios/email/otro@email.com")
                        .header(HEADER_USER, USUARIO_ID) // El usuario logueado es el ID 10
                        .header(HEADER_ROLE, ROL_USUARIO)
                        .param("includeDetails", "true"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/usuarios/{id} - Admin consulta perfil de otro, retorna 200")
    void obtenerPorId_AdminConsulta_Returns200() throws Exception {
        when(usuarioService.obtenerPorId(10L, true)).thenReturn(usuarioDTO);

        mockMvc.perform(get("/api/usuarios/10")
                        .header(HEADER_USER, ADMIN_ID)
                        .header(HEADER_ROLE, ROL_ADMIN)
                        .param("includeDetails", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));

        verify(usuarioService, times(1)).obtenerPorId(10L, true);
    }

    // =========================================================================
    // 🔴 TESTS DE ACTUALIZACIÓN Y BORRADO (PROTEGIDOS POR ADMIN)
    // =========================================================================

    @Test
    @DisplayName("PUT /api/usuarios/{id} - Debe actualizar usuario correctamente (Requiere Admin)")
    void actualizarUsuario_DatosValidos_Returns200() throws Exception {
        when(usuarioService.actualizarUsuarioAdmin(eq(10L), any(UsuarioUpdateDTO.class))).thenReturn(usuarioDTO);

        mockMvc.perform(put("/api/usuarios/10")
                        .header(HEADER_USER, ADMIN_ID)
                        .header(HEADER_ROLE, ROL_ADMIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usuarioDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));

        verify(usuarioService, times(1)).actualizarUsuarioAdmin(eq(10L), any(UsuarioUpdateDTO.class));
    }

    @Test
    @DisplayName("PATCH /api/usuarios/{id}/estado - Debe cambiar estado de usuario (Requiere Admin)")
    void cambiarEstado_EstadoValido_Returns200() throws Exception {
        when(usuarioService.cambiarEstado(10L, 2L)).thenReturn(usuarioDTO);

        mockMvc.perform(patch("/api/usuarios/10/estado")
                        .header(HEADER_USER, ADMIN_ID)
                        .header(HEADER_ROLE, ROL_ADMIN)
                        .param("estadoId", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));

        verify(usuarioService, times(1)).cambiarEstado(10L, 2L);
    }

    @Test
    @DisplayName("DELETE /api/usuarios/{id} - Usuario normal intenta eliminar, retorna 403")
    void eliminarUsuario_UsuarioNormal_Returns403() throws Exception {
        mockMvc.perform(delete("/api/usuarios/10")
                        .header(HEADER_USER, USUARIO_ID)
                        .header(HEADER_ROLE, ROL_USUARIO))
                .andExpect(status().isForbidden());

        verify(usuarioService, never()).eliminarUsuario(anyLong());
    }

    @Test
    @DisplayName("DELETE /api/usuarios/{id} - Admin elimina usuario, retorna 204")
    void eliminarUsuario_Admin_Returns204() throws Exception {
        doNothing().when(usuarioService).eliminarUsuario(10L);

        mockMvc.perform(delete("/api/usuarios/10")
                        .header(HEADER_USER, ADMIN_ID)
                        .header(HEADER_ROLE, ROL_ADMIN))
                .andExpect(status().isNoContent());

        verify(usuarioService, times(1)).eliminarUsuario(10L);
    }

    @Test
    @DisplayName("GET /api/usuarios/{id}/exists - Debe verificar si usuario existe (Usuario Autenticado)")
    void existeUsuario_UsuarioExiste_RetornaTrue() throws Exception {
        when(usuarioService.existeUsuario(10L)).thenReturn(true);

        mockMvc.perform(get("/api/usuarios/10/exists")
                        .header(HEADER_USER, USUARIO_ID)
                        .header(HEADER_ROLE, ROL_USUARIO))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(usuarioService, times(1)).existeUsuario(10L);
    }
}