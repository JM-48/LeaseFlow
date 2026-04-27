package com.rentify.propertyservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rentify.propertyservice.dto.PropertyDTO;
import com.rentify.propertyservice.exception.ResourceNotFoundException;
import com.rentify.propertyservice.service.PropertyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de integración para PropertyController.
 * Utiliza @WebMvcTest con Spring Boot 3.4+ @MockitoBean.
 */
@WebMvcTest(PropertyController.class)
@DisplayName("Tests de PropertyController")
class PropertyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PropertyService propertyService;

    private PropertyDTO propertyDTO;

    @BeforeEach
    void setUp() {
        propertyDTO = PropertyDTO.builder()
                .id(1L)
                .codigo("DP001")
                .titulo("Dpto 2D/2B Providencia")
                .precioMensual(BigDecimal.valueOf(650000))
                .divisa("CLP")
                .m2(BigDecimal.valueOf(65.5))
                .nHabit(2)
                .nBanos(2)
                .petFriendly(true)
                .direccion("Av. Providencia 1234")
                .fcreacion(LocalDate.now())
                .tipoId(1L)
                .comunaId(1L)
                .build();
    }

    // ==================== Tests POST - Crear ====================

    @Test
    @DisplayName("POST /api/propiedades - Debe crear propiedad y retornar 201 CREATED")
    void crear_DatosValidos_Returns201() throws Exception {
        // Arrange
        when(propertyService.crearProperty(any(PropertyDTO.class)))
                .thenReturn(propertyDTO);

        // Act & Assert
        mockMvc.perform(post("/api/propiedades")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(propertyDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.codigo").value("DP001"))
                .andExpect(jsonPath("$.titulo").value("Dpto 2D/2B Providencia"))
                .andExpect(jsonPath("$.petFriendly").value(true));

        verify(propertyService, times(1)).crearProperty(any(PropertyDTO.class));
    }

    @Test
    @DisplayName("POST /api/propiedades - Debe retornar 400 si datos son inválidos")
    void crear_DatosInvalidos_Returns400() throws Exception {
        // Given invalid DTO (falta tipoId)
        PropertyDTO invalidDTO = PropertyDTO.builder()
                .codigo("DP001")
                .titulo("Dpto")
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/propiedades")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());

        verify(propertyService, never()).crearProperty(any());
    }

    // ==================== Tests GET - Listar ====================

    @Test
    @DisplayName("GET /api/propiedades - Debe retornar lista de propiedades")
    void listar_SinDetalles_Returns200() throws Exception {
        // Arrange
        when(propertyService.listarTodas(false))
                .thenReturn(List.of(propertyDTO));

        // Act & Assert
        mockMvc.perform(get("/api/propiedades")
                        .param("includeDetails", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigo").value("DP001"));

        verify(propertyService, times(1)).listarTodas(false);
    }

    @Test
    @DisplayName("GET /api/propiedades - Debe incluir detalles cuando se solicita")
    void listar_ConDetalles_Returns200() throws Exception {
        // Arrange
        when(propertyService.listarTodas(true))
                .thenReturn(List.of(propertyDTO));

        // Act & Assert
        mockMvc.perform(get("/api/propiedades")
                        .param("includeDetails", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(propertyService, times(1)).listarTodas(true);
    }

    @Test
    @DisplayName("GET /api/propiedades - Por defecto no incluye detalles")
    void listar_ParametroDefecto_Returns200() throws Exception {
        // Arrange
        when(propertyService.listarTodas(false))
                .thenReturn(List.of(propertyDTO));

        // Act & Assert
        mockMvc.perform(get("/api/propiedades"))
                .andExpect(status().isOk());

        verify(propertyService, times(1)).listarTodas(false);
    }

    // ==================== Tests GET/{id} ====================

    @Test
    @DisplayName("GET /api/propiedades/{id} - Debe retornar propiedad cuando existe")
    void obtenerPorId_Existe_Returns200() throws Exception {
        // Arrange
        when(propertyService.obtenerPorId(1L, true))
                .thenReturn(propertyDTO);

        // Act & Assert
        mockMvc.perform(get("/api/propiedades/1")
                        .param("includeDetails", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.codigo").value("DP001"));

        verify(propertyService, times(1)).obtenerPorId(1L, true);
    }

    @Test
    @DisplayName("GET /api/propiedades/{id} - Debe retornar 404 cuando no existe")
    void obtenerPorId_NoExiste_Returns404() throws Exception {
        // Arrange
        when(propertyService.obtenerPorId(999L, true))
                .thenThrow(new ResourceNotFoundException("Propiedad no encontrada"));

        // Act & Assert
        mockMvc.perform(get("/api/propiedades/999")
                        .param("includeDetails", "true"))
                .andExpect(status().isNotFound());
    }

    // ==================== Tests GET/codigo/{codigo} ====================

    @Test
    @DisplayName("GET /api/propiedades/codigo/{codigo} - Debe retornar propiedad por código")
    void obtenerPorCodigo_Existe_Returns200() throws Exception {
        // Arrange
        when(propertyService.obtenerPorCodigo("DP001", true))
                .thenReturn(propertyDTO);

        // Act & Assert
        mockMvc.perform(get("/api/propiedades/codigo/DP001")
                        .param("includeDetails", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value("DP001"));

        verify(propertyService, times(1)).obtenerPorCodigo("DP001", true);
    }

    @Test
    @DisplayName("GET /api/propiedades/codigo/{codigo} - Debe retornar 404 si no existe")
    void obtenerPorCodigo_NoExiste_Returns404() throws Exception {
        // Arrange
        when(propertyService.obtenerPorCodigo("NOEXISTE", true))
                .thenThrow(new ResourceNotFoundException("Propiedad no encontrada"));

        // Act & Assert
        mockMvc.perform(get("/api/propiedades/codigo/NOEXISTE")
                        .param("includeDetails", "true"))
                .andExpect(status().isNotFound());
    }

    // ==================== Tests PUT - Actualizar ====================

    @Test
    @DisplayName("PUT /api/propiedades/{id} - Debe actualizar propiedad y retornar 200")
    void actualizar_DatosValidos_Returns200() throws Exception {
        // Arrange
        // ✅ CORREGIDO: Incluir todos los campos obligatorios en el updateDTO
        PropertyDTO updateDTO = PropertyDTO.builder()
                .codigo("DP001")
                .titulo("Dpto Actualizado")
                .precioMensual(BigDecimal.valueOf(700000))
                .divisa("CLP")
                .m2(BigDecimal.valueOf(65.5))
                .nHabit(2)
                .nBanos(2)
                .petFriendly(true)
                .direccion("Av. Providencia 1234")
                .tipoId(1L)
                .comunaId(1L)
                .build();

        when(propertyService.actualizar(eq(1L), any(PropertyDTO.class)))
                .thenReturn(propertyDTO);

        // Act & Assert
        mockMvc.perform(put("/api/propiedades/1")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(propertyService, times(1)).actualizar(eq(1L), any(PropertyDTO.class));
    }

    @Test
    @DisplayName("PUT /api/propiedades/{id} - Debe retornar 404 si no existe")
    void actualizar_NoExiste_Returns404() throws Exception {
        // Arrange
        when(propertyService.actualizar(eq(999L), any(PropertyDTO.class)))
                .thenThrow(new ResourceNotFoundException("Propiedad no encontrada"));

        // Act & Assert
        mockMvc.perform(put("/api/propiedades/999")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(propertyDTO)))
                .andExpect(status().isNotFound());
    }

    // ==================== Tests DELETE ====================

    @Test
    @DisplayName("DELETE /api/propiedades/{id} - Debe eliminar propiedad y retornar 204")
    void eliminar_Existe_Returns204() throws Exception {
        // Arrange
        doNothing().when(propertyService).eliminar(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/propiedades/1"))
                .andExpect(status().isNoContent());

        verify(propertyService, times(1)).eliminar(1L);
    }

    @Test
    @DisplayName("DELETE /api/propiedades/{id} - Debe retornar 404 si no existe")
    void eliminar_NoExiste_Returns404() throws Exception {
        // Arrange
        doThrow(new ResourceNotFoundException("Propiedad no encontrada"))
                .when(propertyService).eliminar(999L);

        // Act & Assert
        mockMvc.perform(delete("/api/propiedades/999"))
                .andExpect(status().isNotFound());
    }

    // ==================== Tests GET/buscar ====================

    @Test
    @DisplayName("GET /api/propiedades/buscar - Debe retornar propiedades con filtros")
    void buscarConFiltros_ConFiltros_Returns200() throws Exception {

        when(propertyService.buscarConFiltros(
                // Argumentos 0 (tipoId), 1 (comunaId)
                anyLong(),
                nullable(Long.class),

                // Argumentos 2 (minPrecio), 3 (maxPrecio)
                any(BigDecimal.class),
                any(BigDecimal.class),

                // Argumentos 4 (nHabit), 5 (nBanos)
                nullable(Integer.class),
                nullable(Integer.class),

                // Argumentos 6 (petFriendly), 7 (includeDetails)
                nullable(Boolean.class),
                anyBoolean()))
                .thenReturn(List.of(propertyDTO));



        mockMvc.perform(get("/api/propiedades/buscar")
                        .param("comunaId", "1") // Corresponde al 1er Long o 2do Long en la firma? ASUMIMOS el 2do Long (comunaId)
                        .param("minPrecio", "600000") // Mapeado a BigDecimal
                        .param("maxPrecio", "700000") // Mapeado a BigDecimal
                        .param("includeDetails", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        // Verificamos la llamada con los matchers corregidos
        verify(propertyService, times(1)).buscarConFiltros(
                nullable(Long.class), // TipoId (no se envió, debe ser null)
                eq(1L),               // ComunaId (se envió "1")
                any(BigDecimal.class), // minPrecio
                any(BigDecimal.class), // maxPrecio
                nullable(Integer.class), // nHabit (no se envió, debe ser null)
                nullable(Integer.class), // nBanos (no se envió, debe ser null)
                nullable(Boolean.class), // petFriendly (no se envió, debe ser null)
                eq(false)); // includeDetails (se envió "false")
    }
    // ==================== Tests GET/{id}/existe ====================

    @Test
    @DisplayName("GET /api/propiedades/{id}/existe - Debe retornar true si existe")
    void existe_PropiedadExiste_ReturnsTrue() throws Exception {
        // Arrange
        when(propertyService.existsProperty(1L))
                .thenReturn(true);

        // Act & Assert
        mockMvc.perform(get("/api/propiedades/1/existe"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));

        verify(propertyService, times(1)).existsProperty(1L);
    }

    @Test
    @DisplayName("GET /api/propiedades/{id}/existe - Debe retornar false si no existe")
    void existe_PropiedadNoExiste_ReturnsFalse() throws Exception {
        // Arrange
        when(propertyService.existsProperty(999L))
                .thenReturn(false);

        // Act & Assert
        mockMvc.perform(get("/api/propiedades/999/existe"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(false));

        verify(propertyService, times(1)).existsProperty(999L);
    }
}