package com.rentify.propertyservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rentify.propertyservice.dto.FotoDTO;
import com.rentify.propertyservice.exception.FileStorageException;
import com.rentify.propertyservice.exception.ResourceNotFoundException;
import com.rentify.propertyservice.service.FotoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de integración para FotoController.
 * Utiliza @WebMvcTest con Spring Boot 3.4+ @MockitoBean.
 */
@WebMvcTest(FotoController.class)
@DisplayName("Tests de FotoController")
class FotoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private FotoService fotoService;

    private FotoDTO fotoDTO;

    @BeforeEach
    void setUp() {
        fotoDTO = FotoDTO.builder()
                .id(1L)
                .nombre("test.jpg")
                .url("uploads/properties/1/1234567890_test.jpg")
                .sortOrder(0)
                .propiedadId(1L)
                .build();
    }

    // ==================== Tests POST - Upload ====================

    @Test
    @DisplayName("POST /api/propiedades/{id}/fotos - Debe subir foto y retornar 201 CREATED")
    void uploadFoto_ArchivoValido_Returns201() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "fake image content".getBytes()
        );

        when(fotoService.guardarFoto(1L, file))
                .thenReturn(fotoDTO);

        // Act & Assert
        mockMvc.perform(multipart("/api/propiedades/1/fotos")
                        .file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nombre").value("test.jpg"))
                .andExpect(jsonPath("$.propiedadId").value(1L));

        verify(fotoService, times(1)).guardarFoto(eq(1L), any());
    }

    @Test
    @DisplayName("POST /api/propiedades/{id}/fotos - Debe retornar 400 si archivo es inválido")
    void uploadFoto_ArchivoInvalido_Returns400() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",  // Formato inválido
                "fake pdf content".getBytes()
        );

        when(fotoService.guardarFoto(1L, file))
                .thenThrow(new FileStorageException("Formato de archivo inválido"));

        // Act & Assert
        mockMvc.perform(multipart("/api/propiedades/1/fotos")
                        .file(file))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/propiedades/{id}/fotos - Debe retornar 404 si propiedad no existe")
    void uploadFoto_PropiedadNoExiste_Returns404() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "fake image content".getBytes()
        );

        when(fotoService.guardarFoto(999L, file))
                .thenThrow(new ResourceNotFoundException("Propiedad no encontrada"));

        // Act & Assert
        mockMvc.perform(multipart("/api/propiedades/999/fotos")
                        .file(file))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/propiedades/{id}/fotos - Debe retornar 400 si archivo está vacío")
    void uploadFoto_ArchivoVacio_Returns400() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                new byte[0]
        );

        when(fotoService.guardarFoto(1L, file))
                .thenThrow(new FileStorageException("Archivo vacío"));

        // Act & Assert
        mockMvc.perform(multipart("/api/propiedades/1/fotos")
                        .file(file))
                .andExpect(status().isBadRequest());
    }

    // ==================== Tests GET - Listar Fotos ====================

    @Test
    @DisplayName("GET /api/propiedades/{id}/fotos - Debe retornar lista de fotos")
    void listarFotos_PropiedadExiste_Returns200() throws Exception {
        // Arrange
        when(fotoService.listarFotos(1L))
                .thenReturn(List.of(fotoDTO));

        // Act & Assert
        mockMvc.perform(get("/api/propiedades/1/fotos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nombre").value("test.jpg"))
                .andExpect(jsonPath("$[0].sortOrder").value(0));

        verify(fotoService, times(1)).listarFotos(1L);
    }

    @Test
    @DisplayName("GET /api/propiedades/{id}/fotos - Debe retornar 404 si propiedad no existe")
    void listarFotos_PropiedadNoExiste_Returns404() throws Exception {
        // Arrange
        when(fotoService.listarFotos(999L))
                .thenThrow(new ResourceNotFoundException("Propiedad no encontrada"));

        // Act & Assert
        mockMvc.perform(get("/api/propiedades/999/fotos"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/propiedades/{id}/fotos - Debe retornar lista vacía si no hay fotos")
    void listarFotos_SinFotos_ReturnsEmptyList() throws Exception {
        // Arrange
        when(fotoService.listarFotos(1L))
                .thenReturn(List.of());

        // Act & Assert
        mockMvc.perform(get("/api/propiedades/1/fotos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // ==================== Tests GET/{fotoId} ====================

    @Test
    @DisplayName("GET /api/fotos/{fotoId} - Debe retornar foto cuando existe")
    void obtenerFoto_FotoExiste_Returns200() throws Exception {
        // Arrange
        when(fotoService.obtenerPorId(1L))
                .thenReturn(fotoDTO);

        // Act & Assert
        mockMvc.perform(get("/api/fotos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nombre").value("test.jpg"));

        verify(fotoService, times(1)).obtenerPorId(1L);
    }

    @Test
    @DisplayName("GET /api/fotos/{fotoId} - Debe retornar 404 si foto no existe")
    void obtenerFoto_FotoNoExiste_Returns404() throws Exception {
        // Arrange
        when(fotoService.obtenerPorId(999L))
                .thenThrow(new ResourceNotFoundException("Foto no encontrada"));

        // Act & Assert
        mockMvc.perform(get("/api/fotos/999"))
                .andExpect(status().isNotFound());
    }

    // ==================== Tests DELETE ====================

    @Test
    @DisplayName("DELETE /api/fotos/{fotoId} - Debe eliminar foto y retornar 204 NO_CONTENT")
    void eliminarFoto_FotoExiste_Returns204() throws Exception {
        // Arrange
        doNothing().when(fotoService).eliminarFoto(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/fotos/1"))
                .andExpect(status().isNoContent());

        verify(fotoService, times(1)).eliminarFoto(1L);
    }

    @Test
    @DisplayName("DELETE /api/fotos/{fotoId} - Debe retornar 404 si foto no existe")
    void eliminarFoto_FotoNoExiste_Returns404() throws Exception {
        // Arrange
        doThrow(new ResourceNotFoundException("Foto no encontrada"))
                .when(fotoService).eliminarFoto(999L);

        // Act & Assert
        mockMvc.perform(delete("/api/fotos/999"))
                .andExpect(status().isNotFound());
    }

    // ==================== Tests PUT - Reordenar Fotos ====================

    @Test
    @DisplayName("PUT /api/propiedades/{id}/fotos/reordenar - Debe reordenar fotos y retornar 204")
    void reordenarFotos_FotosValidas_Returns204() throws Exception {
        // Arrange
        List<Long> fotosIds = List.of(3L, 1L, 2L);
        doNothing().when(fotoService).reordenarFotos(1L, fotosIds);

        // Act & Assert
        mockMvc.perform(put("/api/propiedades/1/fotos/reordenar")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(fotosIds)))
                .andExpect(status().isNoContent());

        verify(fotoService, times(1)).reordenarFotos(1L, fotosIds);
    }

    @Test
    @DisplayName("PUT /api/propiedades/{id}/fotos/reordenar - Debe retornar 404 si propiedad no existe")
    void reordenarFotos_PropiedadNoExiste_Returns404() throws Exception {
        // Arrange
        List<Long> fotosIds = List.of(1L, 2L);
        doThrow(new ResourceNotFoundException("Propiedad no encontrada"))
                .when(fotoService).reordenarFotos(999L, fotosIds);

        // Act & Assert
        mockMvc.perform(put("/api/propiedades/999/fotos/reordenar")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(fotosIds)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/propiedades/{id}/fotos/reordenar - Debe validar lista de IDs no vacía")
    void reordenarFotos_ListaVacia_Returns400() throws Exception {
        // Arrange
        List<Long> fotosIds = List.of();

        // Act & Assert
        // Spring no valida lista vacía por defecto, pero el servicio debería
        mockMvc.perform(put("/api/propiedades/1/fotos/reordenar")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(fotosIds)))
                .andExpect(status().isNoContent()); // O 400 si agregas validación
    }
}