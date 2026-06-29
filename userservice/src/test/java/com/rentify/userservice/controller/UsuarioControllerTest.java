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
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.Map;
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
@TestPropertySource(properties = "app.security.client-key=test-key-123")
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

    private static final String APP_CLIENT_HEADER = "X-App-Client";
    private static final String APP_CLIENT_KEY = "test-key-123";
    private static final String HEADER_USER = "X-Usuario-Id";
    private static final String HEADER_ROLE = "X-Rol-Id";
    private static final String ADMIN_ID = "1";
    private static final String ROL_ADMIN = "1";
    private static final String USUARIO_ID = "10";
    private static final String ROL_USUARIO = "3"; // Ej: Arriendatario

    private MockHttpServletRequestBuilder withAppKey(MockHttpServletRequestBuilder builder) {
        return builder.header(APP_CLIENT_HEADER, APP_CLIENT_KEY);
    }

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
    // TESTS DE SEGURIDAD DEL INTERCEPTOR (X-App-Client)
    // =========================================================================

    @Test
    @DisplayName("GET /api/usuarios - Retorna 403 si falta X-App-Client")
    void obtenerTodos_SinApiKey_Returns403() throws Exception {
        mockMvc.perform(get("/api/usuarios")
                        .header(HEADER_USER, ADMIN_ID)
                        .header(HEADER_ROLE, ROL_ADMIN))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/usuarios - Retorna 403 si falta X-App-Client (registro publico tambien protegido)")
    void registrarUsuario_SinApiKey_Returns403() throws Exception {
        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usuarioDTO)))
                .andExpect(status().isForbidden());

        verify(usuarioService, never()).registrarUsuario(any());
    }

    @Test
    @DisplayName("POST /api/usuarios/login - Retorna 403 si falta X-App-Client (login publico tambien protegido)")
    void login_SinApiKey_Returns403() throws Exception {
        mockMvc.perform(post("/api/usuarios/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isForbidden());

        verify(usuarioService, never()).login(any());
    }

    // =========================================================================
    // TESTS DE SEGURIDAD GENERAL (ENDPOINTS PROTEGIDOS)
    // =========================================================================

    @Test
    @DisplayName("GET /api/usuarios - Retorna 401 si no hay cabeceras de identidad")
    void obtenerTodos_SinHeaders_Returns401() throws Exception {
        mockMvc.perform(withAppKey(get("/api/usuarios")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PUT /api/usuarios/{id} - Retorna 403 si un usuario normal intenta actualizar el perfil de OTRO usuario")
    void actualizarUsuario_UsuarioNormal_Returns403() throws Exception {
        // Usuario 10 intenta editar el perfil del usuario 99 (ajeno) -> 403
        mockMvc.perform(withAppKey(put("/api/usuarios/99"))
                        .header(HEADER_USER, USUARIO_ID) // Usuario 10
                        .header(HEADER_ROLE, ROL_USUARIO)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usuarioDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PUT /api/usuarios/{id} - El dueño puede actualizar su propio perfil (200 OK)")
    void actualizarUsuario_Dueno_Returns200() throws Exception {
        when(usuarioService.actualizarUsuarioAdmin(eq(10L), any(UsuarioUpdateDTO.class))).thenReturn(usuarioDTO);

        // Usuario 10 edita su propio perfil (id 10 == X-Usuario-Id 10) -> 200
        mockMvc.perform(withAppKey(put("/api/usuarios/10"))
                        .header(HEADER_USER, USUARIO_ID) // 10 == id del path -> dueño
                        .header(HEADER_ROLE, ROL_USUARIO)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usuarioDTO)))
                .andExpect(status().isOk());

        verify(usuarioService, times(1)).actualizarUsuarioAdmin(eq(10L), any(UsuarioUpdateDTO.class));
    }

    // =========================================================================
    // TESTS DE REGISTRO Y LOGIN (PÚBLICOS DE NEGOCIO, PROTEGIDOS POR API KEY)
    // =========================================================================

    @Test
    @DisplayName("POST /api/usuarios - Debe registrar usuario y retornar 201")
    void registrarUsuario_DatosValidos_Returns201() throws Exception {
        when(usuarioService.registrarUsuario(any(UsuarioDTO.class))).thenReturn(usuarioDTO);

        mockMvc.perform(withAppKey(post("/api/usuarios"))
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

        mockMvc.perform(withAppKey(post("/api/usuarios"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usuarioInvalido)))
                .andExpect(status().isBadRequest());

        verify(usuarioService, never()).registrarUsuario(any());
    }

    @Test
    @DisplayName("POST /api/usuarios/login - Debe autenticar usuario exitosamente")
    void login_CredencialesValidas_Returns200() throws Exception {
        when(usuarioService.login(any(LoginDTO.class))).thenReturn(usuarioDTO);

        mockMvc.perform(withAppKey(post("/api/usuarios/login"))
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

        mockMvc.perform(withAppKey(post("/api/usuarios/login"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isUnauthorized());

        verify(usuarioService, times(1)).login(any(LoginDTO.class));
    }

    // =========================================================================
    // TESTS DE CONSULTAS (PROTEGIDOS POR AUTH / PERMISOS ESPECÍFICOS)
    // =========================================================================

    @Test
    @DisplayName("GET /api/usuarios - Debe retornar lista de usuarios (Requiere Admin)")
    void obtenerTodos_DeberiaRetornarListaDeUsuarios() throws Exception {
        UsuarioDTO usuario2 = UsuarioDTO.builder().id(2L).email("maria@email.com").build();
        List<UsuarioDTO> usuarios = Arrays.asList(usuarioDTO, usuario2);

        when(usuarioService.obtenerTodos(false)).thenReturn(usuarios);

        mockMvc.perform(withAppKey(get("/api/usuarios"))
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

        mockMvc.perform(withAppKey(get("/api/usuarios/10"))
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
        mockMvc.perform(withAppKey(get("/api/usuarios/99")) // Intenta ver el ID 99
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

        mockMvc.perform(withAppKey(get("/api/usuarios/email/juan.perez@email.com"))
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

        mockMvc.perform(withAppKey(get("/api/usuarios/email/otro@email.com"))
                        .header(HEADER_USER, USUARIO_ID) // El usuario logueado es el ID 10
                        .header(HEADER_ROLE, ROL_USUARIO)
                        .param("includeDetails", "true"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/usuarios/{id} - Admin consulta perfil de otro, retorna 200")
    void obtenerPorId_AdminConsulta_Returns200() throws Exception {
        when(usuarioService.obtenerPorId(10L, true)).thenReturn(usuarioDTO);

        mockMvc.perform(withAppKey(get("/api/usuarios/10"))
                        .header(HEADER_USER, ADMIN_ID)
                        .header(HEADER_ROLE, ROL_ADMIN)
                        .param("includeDetails", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));

        verify(usuarioService, times(1)).obtenerPorId(10L, true);
    }

    // =========================================================================
    // TESTS DE ACTUALIZACIÓN Y BORRADO (PROTEGIDOS POR ADMIN)
    // =========================================================================

    @Test
    @DisplayName("PUT /api/usuarios/{id} - Debe actualizar usuario correctamente (Requiere Admin)")
    void actualizarUsuario_DatosValidos_Returns200() throws Exception {
        when(usuarioService.actualizarUsuarioAdmin(eq(10L), any(UsuarioUpdateDTO.class))).thenReturn(usuarioDTO);

        mockMvc.perform(withAppKey(put("/api/usuarios/10"))
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

        mockMvc.perform(withAppKey(patch("/api/usuarios/10/estado"))
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
        mockMvc.perform(withAppKey(delete("/api/usuarios/10"))
                        .header(HEADER_USER, USUARIO_ID)
                        .header(HEADER_ROLE, ROL_USUARIO))
                .andExpect(status().isForbidden());

        verify(usuarioService, never()).eliminarUsuario(anyLong());
    }

    @Test
    @DisplayName("DELETE /api/usuarios/{id} - Admin elimina usuario, retorna 204")
    void eliminarUsuario_Admin_Returns204() throws Exception {
        doNothing().when(usuarioService).eliminarUsuario(10L);

        mockMvc.perform(withAppKey(delete("/api/usuarios/10"))
                        .header(HEADER_USER, ADMIN_ID)
                        .header(HEADER_ROLE, ROL_ADMIN))
                .andExpect(status().isNoContent());

        verify(usuarioService, times(1)).eliminarUsuario(10L);
    }

    @Test
    @DisplayName("GET /api/usuarios/{id}/exists - Debe verificar si usuario existe (Usuario Autenticado)")
    void existeUsuario_UsuarioExiste_RetornaTrue() throws Exception {
        when(usuarioService.existeUsuario(10L)).thenReturn(true);

        mockMvc.perform(withAppKey(get("/api/usuarios/10/exists"))
                        .header(HEADER_USER, USUARIO_ID)
                        .header(HEADER_ROLE, ROL_USUARIO))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(usuarioService, times(1)).existeUsuario(10L);
    }

    @Test
    @DisplayName("PATCH /api/usuarios/{id}/clave - Dueño cambia su propia clave, retorna 204")
    void cambiarClave_Dueno_Returns204() throws Exception {
        doNothing().when(usuarioService).cambiarClave(eq(10L), anyString(), anyString());

        Map<String, String> body = Map.of("claveActual", "actual123", "claveNueva", "nueva12345");

        mockMvc.perform(withAppKey(patch("/api/usuarios/10/clave"))
                        .header(HEADER_USER, USUARIO_ID)   // 10 == id del path -> dueño
                        .header(HEADER_ROLE, ROL_USUARIO)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isNoContent());

        verify(usuarioService, times(1)).cambiarClave(eq(10L), eq("actual123"), eq("nueva12345"));
    }

    @Test
    @DisplayName("PATCH /api/usuarios/{id}/clave - Usuario normal intenta cambiar clave de otro, retorna 403")
    void cambiarClave_UsuarioNormalCambiaOtro_Returns403() throws Exception {
        Map<String, String> body = Map.of("claveActual", "actual123", "claveNueva", "nueva12345");

        mockMvc.perform(withAppKey(patch("/api/usuarios/99/clave"))  // intenta cambiar ID 99
                        .header(HEADER_USER, USUARIO_ID)              // logueado como ID 10
                        .header(HEADER_ROLE, ROL_USUARIO)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isForbidden());

        verify(usuarioService, never()).cambiarClave(anyLong(), anyString(), anyString());
    }

    @Test
    @DisplayName("PATCH /api/usuarios/{id}/clave - Admin cambia clave de otro usuario, retorna 204")
    void cambiarClave_Admin_Returns204() throws Exception {
        doNothing().when(usuarioService).cambiarClave(eq(10L), anyString(), anyString());

        Map<String, String> body = Map.of("claveActual", "actual123", "claveNueva", "nueva12345");

        mockMvc.perform(withAppKey(patch("/api/usuarios/10/clave"))
                        .header(HEADER_USER, ADMIN_ID)
                        .header(HEADER_ROLE, ROL_ADMIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("PATCH /api/usuarios/{id}/clave - Clave nueva menor a 6 caracteres, retorna 400")
    void cambiarClave_ClaveMuyCorta_Returns400() throws Exception {
        Map<String, String> body = Map.of("claveActual", "actual123", "claveNueva", "abc");

        mockMvc.perform(withAppKey(patch("/api/usuarios/10/clave"))
                        .header(HEADER_USER, USUARIO_ID)
                        .header(HEADER_ROLE, ROL_USUARIO)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());

        verify(usuarioService, never()).cambiarClave(anyLong(), anyString(), anyString());
    }
}