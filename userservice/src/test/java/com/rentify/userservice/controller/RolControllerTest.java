package com.rentify.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rentify.userservice.dto.RolDTO;
import com.rentify.userservice.exception.BusinessValidationException;
import com.rentify.userservice.exception.ResourceNotFoundException;
import com.rentify.userservice.service.RolService;
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

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RolController.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = "app.security.client-key=test-key-123")
@DisplayName("Tests de RolController")
class RolControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RolService rolService;

    private RolDTO rolDTO;

    private static final String APP_CLIENT_HEADER = "X-App-Client";
    private static final String APP_CLIENT_KEY = "test-key-123";
    private static final String HEADER_USER = "X-Usuario-Id";
    private static final String HEADER_ROLE = "X-Rol-Id";
    private static final String ADMIN_ID = "1";
    private static final String ROL_ADMIN = "1";
    private static final String ROL_USUARIO = "3";

    private MockHttpServletRequestBuilder withAppKey(MockHttpServletRequestBuilder builder) {
        return builder.header(APP_CLIENT_HEADER, APP_CLIENT_KEY);
    }

    @BeforeEach
    void setUp() {
        rolDTO = RolDTO.builder()
                .id(1L)
                .nombre("ADMIN")
                .build();
    }

    // =========================================================================
    // TESTS DE SEGURIDAD DEL INTERCEPTOR (X-App-Client)
    // =========================================================================

    @Test
    @DisplayName("POST /api/roles - Retorna 403 si falta X-App-Client")
    void crearRol_SinApiKey_Returns403() throws Exception {
        mockMvc.perform(post("/api/roles")
                        .header(HEADER_USER, ADMIN_ID)
                        .header(HEADER_ROLE, ROL_ADMIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rolDTO)))
                .andExpect(status().isForbidden());

        verify(rolService, never()).crearRol(any());
    }

    // =========================================================================
    // TESTS DE SEGURIDAD (RBAC Y HEADERS)
    // =========================================================================

    @Test
    @DisplayName("POST /api/roles - Retorna 401 si no hay cabeceras de identidad")
    void crearRol_SinHeaders_Returns401() throws Exception {
        mockMvc.perform(withAppKey(post("/api/roles"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rolDTO)))
                .andExpect(status().isUnauthorized());

        verify(rolService, never()).crearRol(any());
    }

    @Test
    @DisplayName("POST /api/roles - Retorna 403 si el usuario no es ADMIN")
    void crearRol_UsuarioNormal_Returns403() throws Exception {
        mockMvc.perform(withAppKey(post("/api/roles"))
                        .header(HEADER_USER, "5")
                        .header(HEADER_ROLE, ROL_USUARIO)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rolDTO)))
                .andExpect(status().isForbidden());

        verify(rolService, never()).crearRol(any());
    }

    @Test
    @DisplayName("GET /api/roles - Retorna 401 si no hay cabeceras de identidad")
    void obtenerTodos_SinHeaders_Returns401() throws Exception {
        mockMvc.perform(withAppKey(get("/api/roles")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/roles/{id} - Retorna 401 si no hay cabeceras de identidad")
    void obtenerPorId_SinHeaders_Returns401() throws Exception {
        mockMvc.perform(withAppKey(get("/api/roles/1")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/roles/nombre/{nombre} - Retorna 401 si no hay cabeceras de identidad")
    void obtenerPorNombre_SinHeaders_Returns401() throws Exception {
        mockMvc.perform(withAppKey(get("/api/roles/nombre/ADMIN")))
                .andExpect(status().isUnauthorized());
    }

    // =========================================================================
    // TESTS DE ESCRITURA (BLINDADOS)
    // =========================================================================

    @Test
    @DisplayName("POST /api/roles - Debe crear rol y retornar 201 (Siendo Admin)")
    void crearRol_DatosValidos_Returns201() throws Exception {
        when(rolService.crearRol(any(RolDTO.class))).thenReturn(rolDTO);

        mockMvc.perform(withAppKey(post("/api/roles"))
                        .header(HEADER_USER, ADMIN_ID)
                        .header(HEADER_ROLE, ROL_ADMIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rolDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("ADMIN"));

        verify(rolService, times(1)).crearRol(any(RolDTO.class));
    }

    @Test
    @DisplayName("POST /api/roles - Debe retornar 400 cuando el nombre está vacío (Siendo Admin)")
    void crearRol_NombreVacio_Returns400() throws Exception {
        RolDTO rolInvalido = RolDTO.builder()
                .nombre("")
                .build();

        mockMvc.perform(withAppKey(post("/api/roles"))
                        .header(HEADER_USER, ADMIN_ID)
                        .header(HEADER_ROLE, ROL_ADMIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rolInvalido)))
                .andExpect(status().isBadRequest());

        verify(rolService, never()).crearRol(any());
    }

    @Test
    @DisplayName("POST /api/roles - Debe retornar 400 cuando el rol es inválido (Siendo Admin)")
    void crearRol_RolInvalido_Returns400() throws Exception {
        RolDTO rolInvalido = RolDTO.builder()
                .nombre("ROL_INVALIDO")
                .build();

        when(rolService.crearRol(any(RolDTO.class)))
                .thenThrow(new BusinessValidationException("El rol ROL_INVALIDO no es válido"));

        mockMvc.perform(withAppKey(post("/api/roles"))
                        .header(HEADER_USER, ADMIN_ID)
                        .header(HEADER_ROLE, ROL_ADMIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rolInvalido)))
                .andExpect(status().isBadRequest());

        verify(rolService, times(1)).crearRol(any(RolDTO.class));
    }

    // =========================================================================
    // TESTS DE LECTURA (PROTEGIDOS)
    // =========================================================================

    @Test
    @DisplayName("GET /api/roles - Debe retornar lista de roles (Usuario Autenticado)")
    void obtenerTodos_DeberiaRetornarListaDeRoles() throws Exception {
        RolDTO rol2 = RolDTO.builder().id(2L).nombre("PROPIETARIO").build();
        RolDTO rol3 = RolDTO.builder().id(3L).nombre("ARRIENDATARIO").build();
        List<RolDTO> roles = Arrays.asList(rolDTO, rol2, rol3);

        when(rolService.obtenerTodos()).thenReturn(roles);

        mockMvc.perform(withAppKey(get("/api/roles"))
                        .header(HEADER_USER, "10")
                        .header(HEADER_ROLE, ROL_USUARIO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].nombre").value("ADMIN"))
                .andExpect(jsonPath("$[1].nombre").value("PROPIETARIO"))
                .andExpect(jsonPath("$[2].nombre").value("ARRIENDATARIO"));

        verify(rolService, times(1)).obtenerTodos();
    }

    @Test
    @DisplayName("GET /api/roles/{id} - Debe retornar rol por ID (Usuario Autenticado)")
    void obtenerPorId_RolExiste_Returns200() throws Exception {
        when(rolService.obtenerPorId(1L)).thenReturn(rolDTO);

        mockMvc.perform(withAppKey(get("/api/roles/1"))
                        .header(HEADER_USER, "10")
                        .header(HEADER_ROLE, ROL_USUARIO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("ADMIN"));

        verify(rolService, times(1)).obtenerPorId(1L);
    }

    @Test
    @DisplayName("GET /api/roles/{id} - Debe retornar 404 cuando no existe (Usuario Autenticado)")
    void obtenerPorId_RolNoExiste_Returns404() throws Exception {
        when(rolService.obtenerPorId(999L))
                .thenThrow(new ResourceNotFoundException("Rol con ID 999 no encontrado"));

        mockMvc.perform(withAppKey(get("/api/roles/999"))
                        .header(HEADER_USER, "10")
                        .header(HEADER_ROLE, ROL_USUARIO))
                .andExpect(status().isNotFound());

        verify(rolService, times(1)).obtenerPorId(999L);
    }

    @Test
    @DisplayName("GET /api/roles/nombre/{nombre} - Debe retornar rol por nombre (Usuario Autenticado)")
    void obtenerPorNombre_RolExiste_Returns200() throws Exception {
        when(rolService.obtenerPorNombre("ADMIN")).thenReturn(rolDTO);

        mockMvc.perform(withAppKey(get("/api/roles/nombre/ADMIN"))
                        .header(HEADER_USER, "10")
                        .header(HEADER_ROLE, ROL_USUARIO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("ADMIN"));

        verify(rolService, times(1)).obtenerPorNombre("ADMIN");
    }

    @Test
    @DisplayName("GET /api/roles/nombre/{nombre} - Debe retornar 404 cuando no existe (Usuario Autenticado)")
    void obtenerPorNombre_RolNoExiste_Returns404() throws Exception {
        when(rolService.obtenerPorNombre("NO_EXISTE"))
                .thenThrow(new ResourceNotFoundException("Rol NO_EXISTE no encontrado"));

        mockMvc.perform(withAppKey(get("/api/roles/nombre/NO_EXISTE"))
                        .header(HEADER_USER, "10")
                        .header(HEADER_ROLE, ROL_USUARIO))
                .andExpect(status().isNotFound());

        verify(rolService, times(1)).obtenerPorNombre("NO_EXISTE");
    }
}