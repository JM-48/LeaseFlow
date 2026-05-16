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

@WebMvcTest(RolController.class)
@DisplayName("Tests de RolController")
class RolControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RolService rolService;

    private RolDTO rolDTO;

    @BeforeEach
    void setUp() {
        rolDTO = RolDTO.builder()
                .id(1L)
                .nombre("ADMIN")
                .build();
    }

    @Test
    @DisplayName("POST /api/roles - Debe crear rol y retornar 201")
    void crearRol_DatosValidos_Returns201() throws Exception {
        // Arrange
        when(rolService.crearRol(any(RolDTO.class))).thenReturn(rolDTO);

        // Act & Assert
        mockMvc.perform(post("/api/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rolDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("ADMIN"));

        verify(rolService, times(1)).crearRol(any(RolDTO.class));
    }

    @Test
    @DisplayName("POST /api/roles - Debe retornar 400 cuando el nombre está vacío")
    void crearRol_NombreVacio_Returns400() throws Exception {
        // Arrange
        RolDTO rolInvalido = RolDTO.builder()
                .nombre("")
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rolInvalido)))
                .andExpect(status().isBadRequest());

        verify(rolService, never()).crearRol(any());
    }

    @Test
    @DisplayName("POST /api/roles - Debe retornar 400 cuando el rol es inválido")
    void crearRol_RolInvalido_Returns400() throws Exception {
        // Arrange
        RolDTO rolInvalido = RolDTO.builder()
                .nombre("ROL_INVALIDO")
                .build();

        when(rolService.crearRol(any(RolDTO.class)))
                .thenThrow(new BusinessValidationException("El rol ROL_INVALIDO no es válido"));

        // Act & Assert
        mockMvc.perform(post("/api/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rolInvalido)))
                .andExpect(status().isBadRequest());

        verify(rolService, times(1)).crearRol(any(RolDTO.class));
    }

    @Test
    @DisplayName("GET /api/roles - Debe retornar lista de roles")
    void obtenerTodos_DeberiaRetornarListaDeRoles() throws Exception {
        // Arrange
        RolDTO rol2 = RolDTO.builder().id(2L).nombre("PROPIETARIO").build();
        RolDTO rol3 = RolDTO.builder().id(3L).nombre("ARRIENDATARIO").build();
        List<RolDTO> roles = Arrays.asList(rolDTO, rol2, rol3);

        when(rolService.obtenerTodos()).thenReturn(roles);

        // Act & Assert
        mockMvc.perform(get("/api/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].nombre").value("ADMIN"))
                .andExpect(jsonPath("$[1].nombre").value("PROPIETARIO"))
                .andExpect(jsonPath("$[2].nombre").value("ARRIENDATARIO"));

        verify(rolService, times(1)).obtenerTodos();
    }

    @Test
    @DisplayName("GET /api/roles/{id} - Debe retornar rol por ID")
    void obtenerPorId_RolExiste_Returns200() throws Exception {
        // Arrange
        when(rolService.obtenerPorId(1L)).thenReturn(rolDTO);

        // Act & Assert
        mockMvc.perform(get("/api/roles/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("ADMIN"));

        verify(rolService, times(1)).obtenerPorId(1L);
    }

    @Test
    @DisplayName("GET /api/roles/{id} - Debe retornar 404 cuando no existe")
    void obtenerPorId_RolNoExiste_Returns404() throws Exception {
        // Arrange
        when(rolService.obtenerPorId(999L))
                .thenThrow(new ResourceNotFoundException("Rol con ID 999 no encontrado"));

        // Act & Assert
        mockMvc.perform(get("/api/roles/999"))
                .andExpect(status().isNotFound());

        verify(rolService, times(1)).obtenerPorId(999L);
    }

    @Test
    @DisplayName("GET /api/roles/nombre/{nombre} - Debe retornar rol por nombre")
    void obtenerPorNombre_RolExiste_Returns200() throws Exception {
        // Arrange
        when(rolService.obtenerPorNombre("ADMIN")).thenReturn(rolDTO);

        // Act & Assert
        mockMvc.perform(get("/api/roles/nombre/ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("ADMIN"));

        verify(rolService, times(1)).obtenerPorNombre("ADMIN");
    }

    @Test
    @DisplayName("GET /api/roles/nombre/{nombre} - Debe retornar 404 cuando no existe")
    void obtenerPorNombre_RolNoExiste_Returns404() throws Exception {
        // Arrange
        when(rolService.obtenerPorNombre("NO_EXISTE"))
                .thenThrow(new ResourceNotFoundException("Rol NO_EXISTE no encontrado"));

        // Act & Assert
        mockMvc.perform(get("/api/roles/nombre/NO_EXISTE"))
                .andExpect(status().isNotFound());

        verify(rolService, times(1)).obtenerPorNombre("NO_EXISTE");
    }
}