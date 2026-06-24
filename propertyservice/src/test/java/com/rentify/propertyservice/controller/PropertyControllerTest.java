package com.rentify.propertyservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rentify.propertyservice.dto.PropertyDTO;
import com.rentify.propertyservice.exception.ResourceNotFoundException;
import com.rentify.propertyservice.service.PropertyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de integración para PropertyController.
 * Actualizado con endpoints GET públicos y POST/PUT/DELETE protegidos.
 */
@WebMvcTest(PropertyController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("Tests de PropertyController")
class PropertyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PropertyService propertyService;

    private PropertyDTO propertyDTO;

    // Headers requeridos por los endpoints protegidos
    private static final String HEADER_USER = "X-Usuario-Id";
    private static final String HEADER_ROLE = "X-Rol-Id";

    // Roles y Usuarios simulados
    private static final String ROL_ADMIN = "1";
    private static final String ROL_USER = "2";
    private static final String OWNER_ID = "1";     // Dueño de la propiedad
    private static final String OTHER_USER_ID = "9"; // Otro usuario cualquiera

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
                .propietarioId(Long.valueOf(OWNER_ID)) // ID del dueño = 1
                .build();
    }

    // ==================== Tests POST - Crear (PROTEGIDO) ====================

    @Test
    @DisplayName("POST /api/propiedades - Faltan cabeceras retorna 401 UNAUTHORIZED")
    void crear_FaltanCabeceras_Returns401() throws Exception {
        mockMvc.perform(post("/api/propiedades")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(propertyDTO)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/propiedades - Debe crear propiedad y retornar 201 CREATED")
    void crear_DatosValidos_Returns201() throws Exception {
        when(propertyService.crearProperty(any(PropertyDTO.class))).thenReturn(propertyDTO);

        mockMvc.perform(post("/api/propiedades")
                        .header(HEADER_USER, OWNER_ID)
                        .header(HEADER_ROLE, ROL_USER)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(propertyDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.codigo").value("DP001"));

        verify(propertyService, times(1)).crearProperty(any(PropertyDTO.class));
    }

    // ==================== Tests GET - Listar (PÚBLICO) ====================

    @Test
    @DisplayName("GET /api/propiedades - Debe retornar lista de propiedades sin requerir headers")
    void listar_SinDetalles_Returns200() throws Exception {
        when(propertyService.listarTodas(any(Pageable.class), eq(false)))
                .thenReturn(new PageImpl<>(List.of(propertyDTO)));

        mockMvc.perform(get("/api/propiedades")
                        .param("includeDetails", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].codigo").value("DP001"));
    }

    // ==================== Tests GET/{id} (PÚBLICO) ====================

    @Test
    @DisplayName("GET /api/propiedades/{id} - Debe retornar propiedad sin requerir headers")
    void obtenerPorId_Existe_Returns200() throws Exception {
        when(propertyService.obtenerPorId(1L, true)).thenReturn(propertyDTO);

        mockMvc.perform(get("/api/propiedades/1")
                        .param("includeDetails", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value("DP001"));
    }

    // ==================== Tests GET/codigo/{codigo} (PÚBLICO) ====================

    @Test
    @DisplayName("GET /api/propiedades/codigo/{codigo} - Debe retornar propiedad sin requerir headers")
    void obtenerPorCodigo_Existe_Returns200() throws Exception {
        when(propertyService.obtenerPorCodigo("DP001", true)).thenReturn(propertyDTO);

        mockMvc.perform(get("/api/propiedades/codigo/DP001")
                        .param("includeDetails", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value("DP001"));
    }

    // ==================== Tests PUT - Actualizar (PROTEGIDO) ====================

    @Test
    @DisplayName("PUT /api/propiedades/{id} - Usuario no dueño retorna 403 FORBIDDEN")
    void actualizar_UsuarioNoDueno_Returns403() throws Exception {
        when(propertyService.obtenerPorId(1L, false)).thenReturn(propertyDTO);

        mockMvc.perform(put("/api/propiedades/1")
                        .header(HEADER_USER, OTHER_USER_ID) // NO es el dueño
                        .header(HEADER_ROLE, ROL_USER)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(propertyDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PUT /api/propiedades/{id} - Dueño actualiza propiedad y retorna 200")
    void actualizar_DatosValidos_Returns200() throws Exception {
        when(propertyService.obtenerPorId(1L, false)).thenReturn(propertyDTO); // Simula BD
        when(propertyService.actualizar(eq(1L), any(PropertyDTO.class))).thenReturn(propertyDTO);

        mockMvc.perform(put("/api/propiedades/1")
                        .header(HEADER_USER, OWNER_ID) // ES el dueño
                        .header(HEADER_ROLE, ROL_USER)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(propertyDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    // ==================== Tests DELETE (PROTEGIDO) ====================

    @Test
    @DisplayName("DELETE /api/propiedades/{id} - Usuario no dueño retorna 403 FORBIDDEN")
    void eliminar_UsuarioNoDueno_Returns403() throws Exception {
        when(propertyService.obtenerPorId(1L, false)).thenReturn(propertyDTO);

        mockMvc.perform(delete("/api/propiedades/1")
                        .header(HEADER_USER, OTHER_USER_ID) // NO es el dueño
                        .header(HEADER_ROLE, ROL_USER))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE /api/propiedades/{id} - Administrador elimina propiedad y retorna 204")
    void eliminar_Admin_Returns204() throws Exception {
        when(propertyService.obtenerPorId(1L, false)).thenReturn(propertyDTO);
        doNothing().when(propertyService).eliminar(1L);

        mockMvc.perform(delete("/api/propiedades/1")
                        .header(HEADER_USER, OTHER_USER_ID) // NO es dueño
                        .header(HEADER_ROLE, ROL_ADMIN))    // PERO es Admin
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/propiedades/{id} - Dueño elimina propiedad y retorna 204")
    void eliminar_Existe_Returns204() throws Exception {
        when(propertyService.obtenerPorId(1L, false)).thenReturn(propertyDTO);
        doNothing().when(propertyService).eliminar(1L);

        mockMvc.perform(delete("/api/propiedades/1")
                        .header(HEADER_USER, OWNER_ID) // ES el dueño
                        .header(HEADER_ROLE, ROL_USER))
                .andExpect(status().isNoContent());
    }

    // ==================== Tests GET/buscar (PÚBLICO) ====================

    @Test
    @DisplayName("GET /api/propiedades/buscar - Debe retornar propiedades sin requerir headers")
    void buscarConFiltros_ConFiltros_Returns200() throws Exception {
        when(propertyService.buscarConFiltros(anyLong(), nullable(Long.class), any(), any(), nullable(Integer.class), nullable(Integer.class), nullable(Boolean.class), anyBoolean()))
                .thenReturn(List.of(propertyDTO));

        mockMvc.perform(get("/api/propiedades/buscar")
                        .param("comunaId", "1")
                        .param("minPrecio", "600000")
                        .param("maxPrecio", "700000")
                        .param("includeDetails", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    // ==================== Tests GET/{id}/existe (PÚBLICO) ====================

    @Test
    @DisplayName("GET /api/propiedades/{id}/existe - Debe retornar true sin requerir headers")
    void existe_PropiedadExiste_ReturnsTrue() throws Exception {
        when(propertyService.existsProperty(1L)).thenReturn(true);

        mockMvc.perform(get("/api/propiedades/1/existe"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));
    }
}