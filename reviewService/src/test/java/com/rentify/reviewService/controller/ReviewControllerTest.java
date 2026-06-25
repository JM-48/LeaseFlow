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
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReviewController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = "app.security.client-key=test-key-123")
@DisplayName("Tests de ReviewController")
class ReviewControllerTest {

    private static final String APP_CLIENT_HEADER = "X-App-Client";
    private static final String APP_CLIENT_KEY = "test-key-123";
    private static final String HEADER_USER = "X-Usuario-Id";
    private static final String HEADER_ROLE = "X-Rol-Id";
    private static final String ROL_ADMIN = "1";
    private static final String ROL_USER = "2";
    private static final String CREATOR_USER_ID = "1";
    private static final String OTHER_USER_ID = "99";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReviewService service;

    private ReviewDTO reviewDTO;

    private MockHttpServletRequestBuilder withAppKey(MockHttpServletRequestBuilder builder) {
        return builder.header(APP_CLIENT_HEADER, APP_CLIENT_KEY);
    }

    @BeforeEach
    void setUp() {
        reviewDTO = ReviewDTO.builder()
                .id(1L)
                .usuarioId(Long.valueOf(CREATOR_USER_ID))
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

        mockMvc.perform(withAppKey(post("/api/reviews"))
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
    @DisplayName("POST /api/reviews - Debe retornar 403 si falta X-App-Client")
    void crearResena_SinApiKey_Returns403() throws Exception {
        mockMvc.perform(post("/api/reviews")
                        .header(HEADER_USER, CREATOR_USER_ID)
                        .header(HEADER_ROLE, ROL_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reviewDTO)))
                .andExpect(status().isForbidden());

        verify(service, never()).crearResena(any());
    }

    @Test
    @DisplayName("POST /api/reviews - Debe retornar 403 si un usuario intenta suplantar a otro")
    void crearResena_SuplantacionIdentidad_Returns403() throws Exception {
        mockMvc.perform(withAppKey(post("/api/reviews"))
                        .header(HEADER_USER, OTHER_USER_ID)
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

        mockMvc.perform(withAppKey(get("/api/reviews"))
                        .header(HEADER_USER, OTHER_USER_ID)
                        .header(HEADER_ROLE, ROL_ADMIN)
                        .param("includeDetails", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("GET /api/reviews - Usuario común no puede listar todas las reseñas (403)")
    void listarTodas_UsuarioComun_Returns403() throws Exception {
        mockMvc.perform(withAppKey(get("/api/reviews"))
                        .header(HEADER_USER, CREATOR_USER_ID)
                        .header(HEADER_ROLE, ROL_USER)
                        .param("includeDetails", "false"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/reviews - Debe retornar 401 si faltan cabeceras de identidad")
    void listarTodas_SinCabeceras_Returns401() throws Exception {
        mockMvc.perform(withAppKey(get("/api/reviews")))
                .andExpect(status().isUnauthorized());

        verify(service, never()).listarTodas(anyBoolean());
    }

    // ==================== Tests GET/{id} y Filtros Públicos ====================

    @Test
    @DisplayName("GET /api/reviews/{id} - Debe retornar reseña cuando existe")
    void obtenerPorId_ResenaExiste_ReturnsResena() throws Exception {
        when(service.obtenerPorId(1L, true)).thenReturn(reviewDTO);

        mockMvc.perform(withAppKey(get("/api/reviews/1"))
                        .header(HEADER_USER, OTHER_USER_ID)
                        .header(HEADER_ROLE, ROL_USER)
                        .param("includeDetails", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("GET /api/reviews/{id} - Debe retornar 401 si faltan cabeceras")
    void obtenerPorId_SinCabeceras_Returns401() throws Exception {
        mockMvc.perform(withAppKey(get("/api/reviews/1")))
                .andExpect(status().isUnauthorized());

        verify(service, never()).obtenerPorId(any(), anyBoolean());
    }

    @Test
    @DisplayName("GET /api/reviews/usuario/{usuarioId} - Debe retornar reseñas de un usuario")
    void obtenerPorUsuario_ReturnsResenasDelUsuario() throws Exception {
        List<ReviewDTO> reviews = Arrays.asList(reviewDTO);
        when(service.obtenerPorUsuario(1L, false)).thenReturn(reviews);

        mockMvc.perform(withAppKey(get("/api/reviews/usuario/1"))
                        .header(HEADER_USER, OTHER_USER_ID)
                        .header(HEADER_ROLE, ROL_USER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].usuarioId").value(1));
    }

    @Test
    @DisplayName("GET /api/reviews/propiedad/{propiedadId}/promedio - Debe retornar el promedio numérico")
    void calcularPromedioPorPropiedad_ReturnsPromedio() throws Exception {
        when(service.calcularPromedioPorPropiedad(1L)).thenReturn(8.5);

        mockMvc.perform(withAppKey(get("/api/reviews/propiedad/1/promedio"))
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

        mockMvc.perform(withAppKey(patch("/api/reviews/1/estado"))
                        .header(HEADER_USER, OTHER_USER_ID)
                        .header(HEADER_ROLE, ROL_ADMIN)
                        .param("estado", "BANEADA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("BANEADA"));
    }

    @Test
    @DisplayName("PATCH /api/reviews/{id}/estado - Usuario común no puede moderar (403)")
    void actualizarEstado_UsuarioNormal_Returns403() throws Exception {
        mockMvc.perform(withAppKey(patch("/api/reviews/1/estado"))
                        .header(HEADER_USER, CREATOR_USER_ID)
                        .header(HEADER_ROLE, ROL_USER)
                        .param("estado", "BANEADA"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PATCH /api/reviews/{id}/estado - Debe retornar 401 si faltan cabeceras")
    void actualizarEstado_SinCabeceras_Returns401() throws Exception {
        mockMvc.perform(withAppKey(patch("/api/reviews/1/estado"))
                        .param("estado", "BANEADA"))
                .andExpect(status().isUnauthorized());

        verify(service, never()).actualizarEstado(any(), any());
    }

    // ==================== Tests DELETE - Eliminar ====================

    @Test
    @DisplayName("DELETE /api/reviews/{id} - Autor de la reseña puede eliminarla (204)")
    void eliminarResena_Autor_Returns204() throws Exception {
        when(service.obtenerPorId(1L, false)).thenReturn(reviewDTO);
        doNothing().when(service).eliminarResena(1L);

        mockMvc.perform(withAppKey(delete("/api/reviews/1"))
                        .header(HEADER_USER, CREATOR_USER_ID)
                        .header(HEADER_ROLE, ROL_USER))
                .andExpect(status().isNoContent());

        verify(service, times(1)).eliminarResena(1L);
    }

    @Test
    @DisplayName("DELETE /api/reviews/{id} - Administrador puede eliminar reseñas ajenas (204)")
    void eliminarResena_Admin_Returns204() throws Exception {
        when(service.obtenerPorId(1L, false)).thenReturn(reviewDTO);
        doNothing().when(service).eliminarResena(1L);

        mockMvc.perform(withAppKey(delete("/api/reviews/1"))
                        .header(HEADER_USER, OTHER_USER_ID)
                        .header(HEADER_ROLE, ROL_ADMIN))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/reviews/{id} - Tercero no autorizado no puede eliminarla (403)")
    void eliminarResena_NoAutorizado_Returns403() throws Exception {
        when(service.obtenerPorId(1L, false)).thenReturn(reviewDTO);

        mockMvc.perform(withAppKey(delete("/api/reviews/1"))
                        .header(HEADER_USER, OTHER_USER_ID)
                        .header(HEADER_ROLE, ROL_USER))
                .andExpect(status().isForbidden());

        verify(service, never()).eliminarResena(anyLong());
    }

    @Test
    @DisplayName("DELETE /api/reviews/{id} - Debe retornar 401 si faltan cabeceras")
    void eliminarResena_SinCabeceras_Returns401() throws Exception {
        mockMvc.perform(withAppKey(delete("/api/reviews/1")))
                .andExpect(status().isUnauthorized());

        verify(service, never()).eliminarResena(anyLong());
    }

    // ==================== Tests de Control Defensivo ====================

    @Test
    @DisplayName("Cualquier Endpoint - Debe retornar 401 si faltan las cabeceras de identidad")
    void endpoints_SinCabeceras_Returns401() throws Exception {
        mockMvc.perform(withAppKey(get("/api/reviews/1")))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(withAppKey(post("/api/reviews"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reviewDTO)))
                .andExpect(status().isUnauthorized());
    }
}