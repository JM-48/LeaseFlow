package com.rentify.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rentify.userservice.dto.EstadoDTO;
import com.rentify.userservice.exception.ResourceNotFoundException;
import com.rentify.userservice.service.EstadoService;
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

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EstadoController.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("Tests de EstadoController")
class EstadoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EstadoService estadoService;

    private EstadoDTO estadoDTO;

    // Constantes para simular el API Gateway
    private static final String HEADER_USER = "X-Usuario-Id";
    private static final String HEADER_ROLE = "X-Rol-Id";
    private static final String ADMIN_ID = "1";
    private static final String ROL_ADMIN = "1";
    private static final String ROL_USUARIO = "3"; // Simulamos un Arriendatario normal

    @BeforeEach
    void setUp() {
        estadoDTO = EstadoDTO.builder()
                .id(1L)
                .nombre("ACTIVO")
                .build();
    }

    // =========================================================================
    // 🛡️ TESTS DE SEGURIDAD (RBAC Y HEADERS)
    // =========================================================================

    @Test
    @DisplayName("POST /api/estados - Retorna 401 si no hay cabeceras de identidad")
    void crearEstado_SinHeaders_Returns401() throws Exception {
        mockMvc.perform(post("/api/estados")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(estadoDTO)))
                .andExpect(status().isUnauthorized());

        verify(estadoService, never()).crearEstado(any());
    }

    @Test
    @DisplayName("POST /api/estados - Retorna 403 si el usuario no es ADMIN")
    void crearEstado_UsuarioNormal_Returns403() throws Exception {
        mockMvc.perform(post("/api/estados")
                        .header(HEADER_USER, "5")
                        .header(HEADER_ROLE, ROL_USUARIO) // Rol no autorizado
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(estadoDTO)))
                .andExpect(status().isForbidden());

        verify(estadoService, never()).crearEstado(any());
    }

    @Test
    @DisplayName("GET /api/estados - Retorna 401 si no hay cabeceras de identidad")
    void obtenerTodos_SinHeaders_Returns401() throws Exception {
        mockMvc.perform(get("/api/estados"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/estados/{id} - Retorna 401 si no hay cabeceras de identidad")
    void obtenerPorId_SinHeaders_Returns401() throws Exception {
        mockMvc.perform(get("/api/estados/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/estados/nombre/{nombre} - Retorna 401 si no hay cabeceras de identidad")
    void obtenerPorNombre_SinHeaders_Returns401() throws Exception {
        mockMvc.perform(get("/api/estados/nombre/ACTIVO"))
                .andExpect(status().isUnauthorized());
    }

    // =========================================================================
    // 🔴 TESTS DE ESCRITURA (BLINDADOS)
    // =========================================================================

    @Test
    @DisplayName("POST /api/estados - Debe crear estado y retornar 201 (Siendo Admin)")
    void crearEstado_DatosValidos_Returns201() throws Exception {
        // Arrange
        when(estadoService.crearEstado(any(EstadoDTO.class))).thenReturn(estadoDTO);

        // Act & Assert
        mockMvc.perform(post("/api/estados")
                        .header(HEADER_USER, ADMIN_ID)
                        .header(HEADER_ROLE, ROL_ADMIN) // Simula ser Admin
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(estadoDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("ACTIVO"));

        verify(estadoService, times(1)).crearEstado(any(EstadoDTO.class));
    }

    @Test
    @DisplayName("POST /api/estados - Debe retornar 400 cuando el nombre está vacío (Siendo Admin)")
    void crearEstado_NombreVacio_Returns400() throws Exception {
        // Arrange
        EstadoDTO estadoInvalido = EstadoDTO.builder()
                .nombre("")
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/estados")
                        .header(HEADER_USER, ADMIN_ID)
                        .header(HEADER_ROLE, ROL_ADMIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(estadoInvalido)))
                .andExpect(status().isBadRequest());

        verify(estadoService, never()).crearEstado(any());
    }

    // =========================================================================
    // 🟡 TESTS DE LECTURA (PROTEGIDOS)
    // =========================================================================

    @Test
    @DisplayName("GET /api/estados - Debe retornar lista de estados (Usuario Autenticado)")
    void obtenerTodos_DeberiaRetornarListaDeEstados() throws Exception {
        // Arrange
        EstadoDTO estado2 = EstadoDTO.builder().id(2L).nombre("INACTIVO").build();
        List<EstadoDTO> estados = Arrays.asList(estadoDTO, estado2);

        when(estadoService.obtenerTodos()).thenReturn(estados);

        // Act & Assert
        mockMvc.perform(get("/api/estados")
                        .header(HEADER_USER, "10")
                        .header(HEADER_ROLE, ROL_USUARIO)) // Cualquier rol autenticado puede ver
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].nombre").value("ACTIVO"))
                .andExpect(jsonPath("$[1].nombre").value("INACTIVO"));

        verify(estadoService, times(1)).obtenerTodos();
    }

    @Test
    @DisplayName("GET /api/estados/{id} - Debe retornar estado por ID (Usuario Autenticado)")
    void obtenerPorId_EstadoExiste_Returns200() throws Exception {
        // Arrange
        when(estadoService.obtenerPorId(1L)).thenReturn(estadoDTO);

        // Act & Assert
        mockMvc.perform(get("/api/estados/1")
                        .header(HEADER_USER, "10")
                        .header(HEADER_ROLE, ROL_USUARIO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("ACTIVO"));

        verify(estadoService, times(1)).obtenerPorId(1L);
    }

    @Test
    @DisplayName("GET /api/estados/{id} - Debe retornar 404 cuando no existe (Usuario Autenticado)")
    void obtenerPorId_EstadoNoExiste_Returns404() throws Exception {
        // Arrange
        when(estadoService.obtenerPorId(999L))
                .thenThrow(new ResourceNotFoundException("Estado con ID 999 no encontrado"));

        // Act & Assert
        mockMvc.perform(get("/api/estados/999")
                        .header(HEADER_USER, "10")
                        .header(HEADER_ROLE, ROL_USUARIO))
                .andExpect(status().isNotFound());

        verify(estadoService, times(1)).obtenerPorId(999L);
    }

    @Test
    @DisplayName("GET /api/estados/nombre/{nombre} - Debe retornar estado por nombre (Usuario Autenticado)")
    void obtenerPorNombre_EstadoExiste_Returns200() throws Exception {
        // Arrange
        when(estadoService.obtenerPorNombre("ACTIVO")).thenReturn(estadoDTO);

        // Act & Assert
        mockMvc.perform(get("/api/estados/nombre/ACTIVO")
                        .header(HEADER_USER, "10")
                        .header(HEADER_ROLE, ROL_USUARIO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("ACTIVO"));

        verify(estadoService, times(1)).obtenerPorNombre("ACTIVO");
    }

    @Test
    @DisplayName("GET /api/estados/nombre/{nombre} - Debe retornar 404 cuando no existe (Usuario Autenticado)")
    void obtenerPorNombre_EstadoNoExiste_Returns404() throws Exception {
        // Arrange
        when(estadoService.obtenerPorNombre("NO_EXISTE"))
                .thenThrow(new ResourceNotFoundException("Estado NO_EXISTE no encontrado"));

        // Act & Assert
        mockMvc.perform(get("/api/estados/nombre/NO_EXISTE")
                        .header(HEADER_USER, "10")
                        .header(HEADER_ROLE, ROL_USUARIO))
                .andExpect(status().isNotFound());

        verify(estadoService, times(1)).obtenerPorNombre("NO_EXISTE");
    }
}