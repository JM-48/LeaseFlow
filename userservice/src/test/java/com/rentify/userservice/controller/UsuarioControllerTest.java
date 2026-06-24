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
                .id(1L)
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
    // 🛡️ TESTS DE SEGURIDAD (ENDPOINTS PROTEGIDOS)
    // =========================================================================

    @Test
    @DisplayName("GET /api/usuarios - Retorna 401 si no hay cabeceras de identidad")
    void obtenerTodos_SinHeaders_Returns401() throws Exception {
        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PUT /api/usuarios/{id} - Retorna 403 si un usuario normal intenta actualizar como Admin")
    void actualizarUsuario_UsuarioNormal_Returns403() throws Exception {
        mockMvc.perform(put("/api/usuarios/1")
                        .header(HEADER_USER, USUARIO_ID)
                        .header(HEADER_ROLE, ROL_USUARIO) // Rol insuficiente
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usuarioDTO)))
                .andExpect(status().isForbidden());
    }

    // =========================================================================
    // 🟢 TESTS DE REGISTRO (PÚBLICOS)
    // =========================================================================

    @Test
    @DisplayName("POST /api/usuarios - Debe registrar usuario y retornar 201")
    void registrarUsuario_DatosValidos_Returns201() throws Exception {
        when(usuarioService.registrarUsuario(any(UsuarioDTO.class))).thenReturn(usuarioDTO);

        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usuarioDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
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
    @DisplayName("POST /api/usuarios - Debe retornar 400 cuando el email es inválido")
    void registrarUsuario_EmailInvalido_Returns400() throws Exception {
        usuarioDTO.setEmail("email-invalido");

        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usuarioDTO)))
                .andExpect(status().isBadRequest());

        verify(usuarioService, never()).registrarUsuario(any());
    }

    @Test
    @DisplayName("POST /api/usuarios - Debe retornar 400 cuando el RUT es inválido")
    void registrarUsuario_RutInvalido_Returns400() throws Exception {
        usuarioDTO.setRut("rut-invalido");

        when(usuarioService.registrarUsuario(any(UsuarioDTO.class)))
                .thenThrow(new BusinessValidationException("El RUT ingresado no es válido"));

        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usuarioDTO)))
                .andExpect(status().isBadRequest());

        verify(usuarioService, times(1)).registrarUsuario(any(UsuarioDTO.class));
    }

    @Test
    @DisplayName("POST /api/usuarios - Debe retornar 400 cuando el email está duplicado")
    void registrarUsuario_EmailDuplicado_Returns400() throws Exception {
        when(usuarioService.registrarUsuario(any(UsuarioDTO.class)))
                .thenThrow(new BusinessValidationException("El email ya está registrado"));

        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usuarioDTO)))
                .andExpect(status().isBadRequest());

        verify(usuarioService, times(1)).registrarUsuario(any(UsuarioDTO.class));
    }

    @Test
    @DisplayName("POST /api/usuarios - Debe retornar 400 cuando el usuario es menor de edad")
    void registrarUsuario_MenorDeEdad_Returns400() throws Exception {
        String fechaMenorEdadStr = LocalDate.now().minusYears(17).toString();
        usuarioDTO.setFnacimiento(fechaMenorEdadStr);

        when(usuarioService.registrarUsuario(any(UsuarioDTO.class)))
                .thenThrow(new BusinessValidationException("Debe ser mayor de 18 años"));

        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usuarioDTO)))
                .andExpect(status().isBadRequest());
    }

    // =========================================================================
    // 🟢 TESTS DE LOGIN (PÚBLICOS)
    // =========================================================================

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

    @Test
    @DisplayName("POST /api/usuarios/login - Debe retornar 400 cuando faltan campos")
    void login_CamposFaltantes_Returns400() throws Exception {
        LoginDTO loginInvalido = LoginDTO.builder().build();

        mockMvc.perform(post("/api/usuarios/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginInvalido)))
                .andExpect(status().isBadRequest());

        verify(usuarioService, never()).login(any());
    }

    // =========================================================================
    // 🟡 TESTS DE CONSULTAS (PROTEGIDOS POR AUTH)
    // =========================================================================

    @Test
    @DisplayName("GET /api/usuarios - Debe retornar lista de usuarios (Requiere Admin)")
    void obtenerTodos_DeberiaRetornarListaDeUsuarios() throws Exception {
        // Arrange
        UsuarioDTO usuario2 = UsuarioDTO.builder()
                .id(2L)
                .email("maria@email.com")
                .fnacimiento("1990-01-01")
                .build();
        List<UsuarioDTO> usuarios = Arrays.asList(usuarioDTO, usuario2);

        when(usuarioService.obtenerTodos(false)).thenReturn(usuarios);

        // ACT: Cambiamos las cabeceras para que simule ser ADMIN, ya que solo ellos listan todo
        mockMvc.perform(get("/api/usuarios")
                        .header(HEADER_USER, ADMIN_ID)   // <-- Cambiado a ADMIN
                        .header(HEADER_ROLE, ROL_ADMIN)   // <-- Cambiado a ADMIN
                        .param("includeDetails", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));

        verify(usuarioService, times(1)).obtenerTodos(false);
    }

    @Test
    @DisplayName("GET /api/usuarios/{id} - Debe retornar usuario por ID")
    void obtenerPorId_UsuarioExiste_Returns200() throws Exception {
        when(usuarioService.obtenerPorId(1L, true)).thenReturn(usuarioDTO);

        mockMvc.perform(get("/api/usuarios/1")
                        .header(HEADER_USER, ADMIN_ID)
                        .header(HEADER_ROLE, ROL_ADMIN)
                        .param("includeDetails", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("juan.perez@email.com"));

        verify(usuarioService, times(1)).obtenerPorId(1L, true);
    }

    @Test
    @DisplayName("GET /api/usuarios/{id} - Debe retornar 404 cuando no existe (Consultado por Admin o Dueño)")
    void obtenerPorId_UsuarioNoExiste_Returns404() throws Exception {
        // Arrange
        when(usuarioService.obtenerPorId(999L, true))
                .thenThrow(new ResourceNotFoundException("Usuario con ID 999 no encontrado"));

        // ACT: Si usamos ADMIN_ID, tiene permiso para ver cualquier ID y el flujo llegará al Service (404)
        mockMvc.perform(get("/api/usuarios/999")
                        .header(HEADER_USER, ADMIN_ID)   // <-- Cambiado a ADMIN para evitar el bloqueo perimetral 403
                        .header(HEADER_ROLE, ROL_ADMIN)   // <-- Cambiado a ADMIN
                        .param("includeDetails", "true"))
                .andExpect(status().isNotFound());

        verify(usuarioService, times(1)).obtenerPorId(999L, true);
    }

    @Test
    @DisplayName("GET /api/usuarios/email/{email} - Debe retornar usuario por email")
    void obtenerPorEmail_UsuarioExiste_Returns200() throws Exception {
        when(usuarioService.obtenerPorEmail("juan.perez@email.com", true)).thenReturn(usuarioDTO);

        mockMvc.perform(get("/api/usuarios/email/juan.perez@email.com")
                        .header(HEADER_USER, ADMIN_ID)
                        .header(HEADER_ROLE, ROL_ADMIN)
                        .param("includeDetails", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("juan.perez@email.com"));

        verify(usuarioService, times(1)).obtenerPorEmail("juan.perez@email.com", true);
    }

    @Test
    @DisplayName("GET /api/usuarios/rol/{rolId} - Debe retornar usuarios por rol")
    void obtenerPorRol_DeberiaRetornarUsuarios() throws Exception {
        List<UsuarioDTO> usuarios = Arrays.asList(usuarioDTO);
        when(usuarioService.obtenerPorRol(3L, false)).thenReturn(usuarios);

        mockMvc.perform(get("/api/usuarios/rol/3")
                        .header(HEADER_USER, ADMIN_ID)
                        .header(HEADER_ROLE, ROL_ADMIN)
                        .param("includeDetails", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));

        verify(usuarioService, times(1)).obtenerPorRol(3L, false);
    }

    @Test
    @DisplayName("GET /api/usuarios/vip - Debe retornar usuarios DUOC VIP")
    void obtenerUsuariosVIP_DeberiaRetornarUsuariosVIP() throws Exception {
        usuarioDTO.setDuocVip(true);
        List<UsuarioDTO> usuariosVip = Arrays.asList(usuarioDTO);
        when(usuarioService.obtenerUsuariosVIP(false)).thenReturn(usuariosVip);

        mockMvc.perform(get("/api/usuarios/vip")
                        .header(HEADER_USER, ADMIN_ID)
                        .header(HEADER_ROLE, ROL_ADMIN)
                        .param("includeDetails", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));

        verify(usuarioService, times(1)).obtenerUsuariosVIP(false);
    }

    // =========================================================================
    // 🔴 TESTS DE ACTUALIZACIÓN (PROTEGIDOS POR ADMIN)
    // =========================================================================

    @Test
    @DisplayName("PUT /api/usuarios/{id} - Debe actualizar usuario correctamente (Requiere Admin)")
    void actualizarUsuario_DatosValidos_Returns200() throws Exception {
        when(usuarioService.actualizarUsuarioAdmin(eq(1L), any(UsuarioUpdateDTO.class))).thenReturn(usuarioDTO);

        mockMvc.perform(put("/api/usuarios/1")
                        .header(HEADER_USER, ADMIN_ID)
                        .header(HEADER_ROLE, ROL_ADMIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usuarioDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(usuarioService, times(1)).actualizarUsuarioAdmin(eq(1L), any(UsuarioUpdateDTO.class));
    }

    @Test
    @DisplayName("PATCH /api/usuarios/{id}/rol - Debe cambiar rol de usuario (Requiere Admin)")
    void cambiarRol_RolValido_Returns200() throws Exception {
        when(usuarioService.cambiarRol(1L, 2L)).thenReturn(usuarioDTO);

        mockMvc.perform(patch("/api/usuarios/1/rol")
                        .header(HEADER_USER, ADMIN_ID)
                        .header(HEADER_ROLE, ROL_ADMIN)
                        .param("rolId", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(usuarioService, times(1)).cambiarRol(1L, 2L);
    }

    @Test
    @DisplayName("PATCH /api/usuarios/{id}/estado - Debe cambiar estado de usuario (Requiere Admin)")
    void cambiarEstado_EstadoValido_Returns200() throws Exception {
        when(usuarioService.cambiarEstado(1L, 2L)).thenReturn(usuarioDTO);

        mockMvc.perform(patch("/api/usuarios/1/estado")
                        .header(HEADER_USER, ADMIN_ID)
                        .header(HEADER_ROLE, ROL_ADMIN)
                        .param("estadoId", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(usuarioService, times(1)).cambiarEstado(1L, 2L);
    }

    @Test
    @DisplayName("PATCH /api/usuarios/{id}/puntos - Debe agregar puntos al usuario (Requiere Admin)")
    void agregarPuntos_PuntosValidos_Returns200() throws Exception {
        when(usuarioService.agregarPuntos(1L, 100)).thenReturn(usuarioDTO);

        mockMvc.perform(patch("/api/usuarios/1/puntos")
                        .header(HEADER_USER, ADMIN_ID)
                        .header(HEADER_ROLE, ROL_ADMIN)
                        .param("puntos", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(usuarioService, times(1)).agregarPuntos(1L, 100);
    }

    @Test
    @DisplayName("GET /api/usuarios/{id}/exists - Debe verificar si usuario existe (Usuario Autenticado)")
    void existeUsuario_UsuarioExiste_RetornaTrue() throws Exception {
        when(usuarioService.existeUsuario(1L)).thenReturn(true);

        mockMvc.perform(get("/api/usuarios/1/exists")
                        .header(HEADER_USER, USUARIO_ID)
                        .header(HEADER_ROLE, ROL_USUARIO))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(usuarioService, times(1)).existeUsuario(1L);
    }

    @Test
    @DisplayName("GET /api/usuarios/{id}/exists - Debe retornar false cuando no existe (Usuario Autenticado)")
    void existeUsuario_UsuarioNoExiste_RetornaFalse() throws Exception {
        when(usuarioService.existeUsuario(999L)).thenReturn(false);

        mockMvc.perform(get("/api/usuarios/999/exists")
                        .header(HEADER_USER, USUARIO_ID)
                        .header(HEADER_ROLE, ROL_USUARIO))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));

        verify(usuarioService, times(1)).existeUsuario(999L);
    }
}