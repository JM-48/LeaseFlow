package com.rentify.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rentify.userservice.dto.EstadoDTO;
import com.rentify.userservice.exception.ResourceNotFoundException;
import com.rentify.userservice.service.EstadoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EstadoController.class)
@DisplayName("Tests de EstadoController")
class EstadoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EstadoService estadoService;

    private EstadoDTO estadoDTO;

    @BeforeEach
    void setUp() {
        estadoDTO = EstadoDTO.builder()
                .id(1L)
                .nombre("ACTIVO")
                .build();
    }

    @Test
    @DisplayName("POST /api/estados - Debe crear estado y retornar 201")
    void crearEstado_DatosValidos_Returns201() throws Exception {
        // Arrange
        when(estadoService.crearEstado(any(EstadoDTO.class))).thenReturn(estadoDTO);

        // Act & Assert
        mockMvc.perform(post("/api/estados")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(estadoDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("ACTIVO"));

        verify(estadoService, times(1)).crearEstado(any(EstadoDTO.class));
    }

    @Test
    @DisplayName("POST /api/estados - Debe retornar 400 cuando el nombre está vacío")
    void crearEstado_NombreVacio_Returns400() throws Exception {
        // Arrange
        EstadoDTO estadoInvalido = EstadoDTO.builder()
                .nombre("")
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/estados")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(estadoInvalido)))
                .andExpect(status().isBadRequest());

        verify(estadoService, never()).crearEstado(any());
    }

    @Test
    @DisplayName("GET /api/estados - Debe retornar lista de estados")
    void obtenerTodos_DeberiaRetornarListaDeEstados() throws Exception {
        // Arrange
        EstadoDTO estado2 = EstadoDTO.builder().id(2L).nombre("INACTIVO").build();
        List<EstadoDTO> estados = Arrays.asList(estadoDTO, estado2);

        when(estadoService.obtenerTodos()).thenReturn(estados);

        // Act & Assert
        mockMvc.perform(get("/api/estados"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].nombre").value("ACTIVO"))
                .andExpect(jsonPath("$[1].nombre").value("INACTIVO"));

        verify(estadoService, times(1)).obtenerTodos();
    }

    @Test
    @DisplayName("GET /api/estados/{id} - Debe retornar estado por ID")
    void obtenerPorId_EstadoExiste_Returns200() throws Exception {
        // Arrange
        when(estadoService.obtenerPorId(1L)).thenReturn(estadoDTO);

        // Act & Assert
        mockMvc.perform(get("/api/estados/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("ACTIVO"));

        verify(estadoService, times(1)).obtenerPorId(1L);
    }

    @Test
    @DisplayName("GET /api/estados/{id} - Debe retornar 404 cuando no existe")
    void obtenerPorId_EstadoNoExiste_Returns404() throws Exception {
        // Arrange
        when(estadoService.obtenerPorId(999L))
                .thenThrow(new ResourceNotFoundException("Estado con ID 999 no encontrado"));

        // Act & Assert
        mockMvc.perform(get("/api/estados/999"))
                .andExpect(status().isNotFound());

        verify(estadoService, times(1)).obtenerPorId(999L);
    }

    @Test
    @DisplayName("GET /api/estados/nombre/{nombre} - Debe retornar estado por nombre")
    void obtenerPorNombre_EstadoExiste_Returns200() throws Exception {
        // Arrange
        when(estadoService.obtenerPorNombre("ACTIVO")).thenReturn(estadoDTO);

        // Act & Assert
        mockMvc.perform(get("/api/estados/nombre/ACTIVO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("ACTIVO"));

        verify(estadoService, times(1)).obtenerPorNombre("ACTIVO");
    }

    @Test
    @DisplayName("GET /api/estados/nombre/{nombre} - Debe retornar 404 cuando no existe")
    void obtenerPorNombre_EstadoNoExiste_Returns404() throws Exception {
        // Arrange
        when(estadoService.obtenerPorNombre("NO_EXISTE"))
                .thenThrow(new ResourceNotFoundException("Estado NO_EXISTE no encontrado"));

        // Act & Assert
        mockMvc.perform(get("/api/estados/nombre/NO_EXISTE"))
                .andExpect(status().isNotFound());

        verify(estadoService, times(1)).obtenerPorNombre("NO_EXISTE");
    }
}