package com.rentify.reviewService.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rentify.reviewService.dto.ReviewDTO;
import com.rentify.reviewService.exception.ResourceNotFoundException;
import com.rentify.reviewService.service.ReviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de integración unitaria para ReviewController.
 * Configurado para validar el control de acceso por cabeceras (Gateway RBAC).
 */
@WebMvcTest(ReviewController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("Tests de ReviewController")
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReviewService service;

    private ReviewDTO reviewDTO;

    // Cabeceras de control de identidad
    private static final String HEADER_USER = "X-Usuario-Id";
    private static final String HEADER_ROLE = "X-Rol-Id";

    // Constantes de roles y usuarios ficticios
    private static final String ROL_ADMIN = "1";
    private static final String ROL_USER = "2";
    private static final String CREATOR_USER_ID = "1";
    private static final String OTHER_USER_ID = "99";

    @BeforeEach
    void setUp() {
        reviewDTO = ReviewDTO.builder()
                .id(1L)
                .usuarioId(Long.valueOf(CREATOR_USER_ID)) // ID del autor = 1
                .propiedadId(1L)
                .puntaje(8)
                .comentario("Excelente propiedad, muy bien ubicada")
                .tipoResenaId(1L)
                .fechaResena(new Date())
                .estado("ACTIVA")
                .build();
    }

    // ==================== Tests POST - Crear ====================

    @Test
    @DisplayName("POST /api/reviews - Debe crear reseña y retornar 201 cuando el usuario coincide")
    void crearResena_DatosValidos_Returns201() throws Exception {
        when(service.crearResena(any(ReviewDTO.class))).thenReturn(reviewDTO);

        mockMvc.perform(post("/api/reviews")
                        .header(HEADER_USER, CREATOR_USER_ID)
                        .header(HEADER_ROLE, ROL_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reviewDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.usuarioId").value(1));

        verify(service, times(1)).crearResena(any(ReviewDTO.class));
    }

    @Test
    @DisplayName("POST /api/reviews - Debe retornar 403 si un usuario intenta suplantar a otro")
    void crearResena_SuplantacionIdentidad_Returns403() throws Exception {
        mockMvc.perform(post("/api/reviews")
                        .header(HEADER_USER, OTHER_USER_ID) // Usuario 99 intentando firmar como Usuario 1
                        .header(HEADER_ROLE, ROL_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reviewDTO)))
                .andExpect(status().isForbidden());

        verify(service, never()).crearResena(any());
    }

    // ==================== Tests GET - Listar Todo ====================

    @Test
    @DisplayName("GET /api/reviews - Administrador puede listar todas las reseñas globales")
    void listarTodas_Admin_ReturnsListaResenas() throws Exception {
        List<ReviewDTO> reviews = Arrays.asList(reviewDTO);
        when(service.listarTodas(false)).thenReturn(reviews);

        mockMvc.perform(get("/api/reviews")
                        .header(HEADER_USER, OTHER_USER_ID)
                        .header(HEADER_ROLE, ROL_ADMIN) // Cuenta como Admin
                        .param("includeDetails", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("GET /api/reviews - Usuario común no puede listar todas las reseñas (403)")
    void listarTodas_UsuarioComun_Returns403() throws Exception {
        mockMvc.perform(get("/api/reviews")
                        .header(HEADER_USER, CREATOR_USER_ID)
                        .header(HEADER_ROLE, ROL_USER)
                        .param("includeDetails", "false"))
                .andExpect(status().isForbidden());
    }

    // ==================== Tests GET/{id} y Filtros Públicos ====================

    @Test
    @DisplayName("GET /api/reviews/{id} - Debe retornar reseña cuando existe")
    void obtenerPorId_ResenaExiste_ReturnsResena() throws Exception {
        when(service.obtenerPorId(1L, true)).thenReturn(reviewDTO);

        mockMvc.perform(get("/api/reviews/1")
                        .header(HEADER_USER, OTHER_USER_ID)
                        .header(HEADER_ROLE, ROL_USER)
                        .param("includeDetails", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("GET /api/reviews/usuario/{usuarioId} - Debe retornar reseñas de un usuario")
    void obtenerPorUsuario_ReturnsResenasDelUsuario() throws Exception {
        List<ReviewDTO> reviews = Arrays.asList(reviewDTO);
        when(service.obtenerPorUsuario(1L, false)).thenReturn(reviews);

        mockMvc.perform(get("/api/reviews/usuario/1")
                        .header(HEADER_USER, OTHER_USER_ID)
                        .header(HEADER_ROLE, ROL_USER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].usuarioId").value(1));
    }

    @Test
    @DisplayName("GET /api/reviews/propiedad/{propiedadId}/promedio - Debe retornar el promedio numérico")
    void calcularPromedioPorPropiedad_ReturnsPromedio() throws Exception {
        when(service.calcularPromedioPorPropiedad(1L)).thenReturn(8.5);

        mockMvc.perform(get("/api/reviews/propiedad/1/promedio")
                        .header(HEADER_USER, OTHER_USER_ID)
                        .header(HEADER_ROLE, ROL_USER))
                .andExpect(status().isOk())
                .andExpect(content().string("8.5"));
    }

    // ==================== Tests PATCH - Moderación ====================

    @Test
    @DisplayName("PATCH /api/reviews/{id}/estado - Administrador puede cambiar el estado")
    void actualizarEstado_Admin_ReturnsOk() throws Exception {
        reviewDTO.setEstado("BANEADA");
        when(service.actualizarEstado(1L, "BANEADA")).thenReturn(reviewDTO);

        mockMvc.perform(patch("/api/reviews/1/estado")
                        .header(HEADER_USER, OTHER_USER_ID)
                        .header(HEADER_ROLE, ROL_ADMIN) // Es Admin
                        .param("estado", "BANEADA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("BANEADA"));
    }

    @Test
    @DisplayName("PATCH /api/reviews/{id}/estado - Usuario común no puede moderar (403)")
    void actualizarEstado_UsuarioNormal_Returns403() throws Exception {
        mockMvc.perform(patch("/api/reviews/1/estado")
                        .header(HEADER_USER, CREATOR_USER_ID)
                        .header(HEADER_ROLE, ROL_USER) // Es usuario común, incluso siendo el dueño
                        .param("estado", "BANEADA"))
                .andExpect(status().isForbidden());
    }

    // ==================== Tests DELETE - Eliminar ====================

    @Test
    @DisplayName("DELETE /api/reviews/{id} - Autor de la reseña puede eliminarla (204)")
    void eliminarResena_Autor_Returns204() throws Exception {
        when(service.obtenerPorId(1L, false)).thenReturn(reviewDTO);
        doNothing().when(service).eliminarResena(1L);

        mockMvc.perform(delete("/api/reviews/1")
                        .header(HEADER_USER, CREATOR_USER_ID) // Coincide con reviewDTO.usuarioId
                        .header(HEADER_ROLE, ROL_USER))
                .andExpect(status().isNoContent());

        verify(service, times(1)).eliminarResena(1L);
    }

    @Test
    @DisplayName("DELETE /api/reviews/{id} - Administrador puede eliminar reseñas ajenas (204)")
    void eliminarResena_Admin_Returns204() throws Exception {
        when(service.obtenerPorId(1L, false)).thenReturn(reviewDTO);
        doNothing().when(service).eliminarResena(1L);

        mockMvc.perform(delete("/api/reviews/1")
                        .header(HEADER_USER, OTHER_USER_ID) // Usuario ajeno
                        .header(HEADER_ROLE, ROL_ADMIN))    // Pero es administrador
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/reviews/{id} - Tercero no autorizado no puede eliminarla (403)")
    void eliminarResena_NoAutorizado_Returns403() throws Exception {
        when(service.obtenerPorId(1L, false)).thenReturn(reviewDTO);

        mockMvc.perform(delete("/api/reviews/1")
                        .header(HEADER_USER, OTHER_USER_ID) // No es dueño
                        .header(HEADER_ROLE, ROL_USER))    // No es admin
                .andExpect(status().isForbidden());

        verify(service, never()).eliminarResena(anyLong());
    }

    // ==================== Tests de Control Defensivo ====================

    @Test
    @DisplayName("Cualquier Endpoint - Debe retornar 401 si faltan las cabeceras de identidad")
    void endpoints_SinCabeceras_Returns401() throws Exception {
        mockMvc.perform(get("/api/reviews/1")) // Sin headers
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reviewDTO)))
                .andExpect(status().isUnauthorized());
    }
}