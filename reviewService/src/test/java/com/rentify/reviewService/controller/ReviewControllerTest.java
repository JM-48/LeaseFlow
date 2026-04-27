package com.rentify.reviewService.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rentify.reviewService.dto.ReviewDTO;
import com.rentify.reviewService.exception.ResourceNotFoundException;
import com.rentify.reviewService.service.ReviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de integración para ReviewController.
 */
@WebMvcTest(ReviewController.class)
@DisplayName("Tests de ReviewController")
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReviewService service;

    private ReviewDTO reviewDTO;

    @BeforeEach
    void setUp() {
        reviewDTO = ReviewDTO.builder()
                .id(1L)
                .usuarioId(1L)
                .propiedadId(1L)
                .puntaje(8)
                .comentario("Excelente propiedad, muy bien ubicada")
                .tipoResenaId(1L)
                .fechaResena(new Date())
                .estado("ACTIVA")
                .build();
    }

    @Test
    @DisplayName("POST /api/reviews - Debe crear reseña y retornar 201")
    void crearResena_DatosValidos_Returns201() throws Exception {
        // Arrange
        when(service.crearResena(any(ReviewDTO.class))).thenReturn(reviewDTO);

        // Act & Assert
        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reviewDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.usuarioId").value(1))
                .andExpect(jsonPath("$.puntaje").value(8))
                .andExpect(jsonPath("$.estado").value("ACTIVA"));

        verify(service, times(1)).crearResena(any(ReviewDTO.class));
    }

    @Test
    @DisplayName("GET /api/reviews - Debe retornar lista de reseñas")
    void listarTodas_ReturnsListaResenas() throws Exception {
        // Arrange
        List<ReviewDTO> reviews = Arrays.asList(reviewDTO);
        when(service.listarTodas(false)).thenReturn(reviews);

        // Act & Assert
        mockMvc.perform(get("/api/reviews")
                        .param("includeDetails", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1));

        verify(service, times(1)).listarTodas(false);
    }

    @Test
    @DisplayName("GET /api/reviews/{id} - Debe retornar reseña cuando existe")
    void obtenerPorId_ResenaExiste_ReturnsResena() throws Exception {
        // Arrange
        when(service.obtenerPorId(1L, true)).thenReturn(reviewDTO);

        // Act & Assert
        mockMvc.perform(get("/api/reviews/1")
                        .param("includeDetails", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.puntaje").value(8));

        verify(service, times(1)).obtenerPorId(1L, true);
    }

    @Test
    @DisplayName("GET /api/reviews/{id} - Debe retornar 404 cuando no existe")
    void obtenerPorId_ResenaNoExiste_Returns404() throws Exception {
        // Arrange
        when(service.obtenerPorId(999L, true))
                .thenThrow(new ResourceNotFoundException("La reseña con ID 999 no existe"));

        // Act & Assert
        mockMvc.perform(get("/api/reviews/999")
                        .param("includeDetails", "true"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/reviews/usuario/{usuarioId} - Debe retornar reseñas del usuario")
    void obtenerPorUsuario_ReturnsResenasDelUsuario() throws Exception {
        // Arrange
        List<ReviewDTO> reviews = Arrays.asList(reviewDTO);
        when(service.obtenerPorUsuario(1L, false)).thenReturn(reviews);

        // Act & Assert
        mockMvc.perform(get("/api/reviews/usuario/1")
                        .param("includeDetails", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].usuarioId").value(1));

        verify(service, times(1)).obtenerPorUsuario(1L, false);
    }

    @Test
    @DisplayName("GET /api/reviews/propiedad/{propiedadId} - Debe retornar reseñas de la propiedad")
    void obtenerPorPropiedad_ReturnsResenasDeLaPropiedad() throws Exception {
        // Arrange
        List<ReviewDTO> reviews = Arrays.asList(reviewDTO);
        when(service.obtenerPorPropiedad(1L, false)).thenReturn(reviews);

        // Act & Assert
        mockMvc.perform(get("/api/reviews/propiedad/1")
                        .param("includeDetails", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].propiedadId").value(1));

        verify(service, times(1)).obtenerPorPropiedad(1L, false);
    }

    @Test
    @DisplayName("GET /api/reviews/propiedad/{propiedadId}/promedio - Debe retornar promedio")
    void calcularPromedioPorPropiedad_ReturnsPromedio() throws Exception {
        // Arrange
        when(service.calcularPromedioPorPropiedad(1L)).thenReturn(8.5);

        // Act & Assert
        mockMvc.perform(get("/api/reviews/propiedad/1/promedio"))
                .andExpect(status().isOk())
                .andExpect(content().string("8.5"));

        verify(service, times(1)).calcularPromedioPorPropiedad(1L);
    }

    @Test
    @DisplayName("PATCH /api/reviews/{id}/estado - Debe actualizar estado")
    void actualizarEstado_ReturnsResenaActualizada() throws Exception {
        // Arrange
        reviewDTO.setEstado("BANEADA");
        when(service.actualizarEstado(1L, "BANEADA")).thenReturn(reviewDTO);

        // Act & Assert
        mockMvc.perform(patch("/api/reviews/1/estado")
                        .param("estado", "BANEADA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("BANEADA"));

        verify(service, times(1)).actualizarEstado(1L, "BANEADA");
    }

    @Test
    @DisplayName("DELETE /api/reviews/{id} - Debe eliminar reseña y retornar 204")
    void eliminarResena_Returns204() throws Exception {
        // Arrange
        doNothing().when(service).eliminarResena(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/reviews/1"))
                .andExpect(status().isNoContent());

        verify(service, times(1)).eliminarResena(1L);
    }

    @Test
    @DisplayName("POST /api/reviews - Debe retornar 400 cuando faltan campos obligatorios")
    void crearResena_CamposFaltantes_Returns400() throws Exception {
        // Arrange
        ReviewDTO invalidDTO = ReviewDTO.builder().build(); // Sin campos obligatorios

        // Act & Assert
        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());

        verify(service, never()).crearResena(any());
    }
}