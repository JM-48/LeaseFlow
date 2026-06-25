package com.rentify.propertyservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rentify.propertyservice.dto.FotoDTO;
import com.rentify.propertyservice.dto.PropertyDTO;
import com.rentify.propertyservice.service.FotoService;
import com.rentify.propertyservice.service.PropertyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FotoController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = "app.security.client-key=test-key-123")
@DisplayName("Tests de FotoController")
class FotoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private FotoService fotoService;

    @MockitoBean
    private PropertyService propertyService;

    private FotoDTO fotoDTO;
    private PropertyDTO mockPropertyDTO;

    private static final String APP_CLIENT_HEADER = "X-App-Client";
    private static final String APP_CLIENT_KEY = "test-key-123";
    private static final String HEADER_USER = "X-Usuario-Id";
    private static final String HEADER_ROLE = "X-Rol-Id";
    private static final String ROL_ADMIN = "1";
    private static final String ROL_USER = "2";
    private static final String OWNER_ID = "5";
    private static final String OTHER_USER_ID = "9";
    private static final Long PROPERTY_ID = 1L;

    private MockMultipartHttpServletRequestBuilder withAppKey(MockMultipartHttpServletRequestBuilder builder) {
        return (MockMultipartHttpServletRequestBuilder) builder.header(APP_CLIENT_HEADER, APP_CLIENT_KEY);
    }

    @BeforeEach
    void setUp() {
        fotoDTO = FotoDTO.builder()
                .id(1L)
                .nombre("test.jpg")
                .url("uploads/properties/1/1234567890_test.jpg")
                .sortOrder(0)
                .propiedadId(PROPERTY_ID)
                .build();

        mockPropertyDTO = mock(PropertyDTO.class);
        when(mockPropertyDTO.getPropietarioId()).thenReturn(Long.valueOf(OWNER_ID));
    }

    // ==================== Tests de seguridad del interceptor (X-App-Client) ====================

    @Test
    @DisplayName("POST /api/propiedades/{id}/fotos - Retorna 403 si falta X-App-Client")
    void uploadFoto_SinApiKey_Returns403() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "fake".getBytes());

        mockMvc.perform(multipart("/api/propiedades/1/fotos")
                        .file(file)
                        .header(HEADER_USER, OWNER_ID)
                        .header(HEADER_ROLE, ROL_USER))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/propiedades/{id}/fotos - Retorna 403 si falta X-App-Client")
    void listarFotos_SinApiKey_Returns403() throws Exception {
        mockMvc.perform(get("/api/propiedades/1/fotos"))
                .andExpect(status().isForbidden());
    }

    // ==================== Tests POST - Upload (PROTEGIDO) ====================

    @Test
    @DisplayName("POST /api/propiedades/{id}/fotos - Faltan cabeceras retorna 401 UNAUTHORIZED")
    void uploadFoto_FaltanCabeceras_Returns401() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "fake".getBytes());

        mockMvc.perform(withAppKey(multipart("/api/propiedades/1/fotos")).file(file))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/propiedades/{id}/fotos - Usuario no dueño retorna 403 FORBIDDEN")
    void uploadFoto_UsuarioNoDueno_Returns403() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "fake".getBytes());
        when(propertyService.obtenerPorId(PROPERTY_ID, false)).thenReturn(mockPropertyDTO);

        mockMvc.perform(withAppKey(multipart("/api/propiedades/1/fotos"))
                        .file(file)
                        .header(HEADER_USER, OTHER_USER_ID)
                        .header(HEADER_ROLE, ROL_USER))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/propiedades/{id}/fotos - Dueño sube foto y retorna 201 CREATED")
    void uploadFoto_DuenoValido_Returns201() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "fake".getBytes());

        when(propertyService.obtenerPorId(PROPERTY_ID, false)).thenReturn(mockPropertyDTO);
        when(fotoService.guardarFoto(eq(PROPERTY_ID), any())).thenReturn(fotoDTO);

        mockMvc.perform(withAppKey(multipart("/api/propiedades/1/fotos"))
                        .file(file)
                        .header(HEADER_USER, OWNER_ID)
                        .header(HEADER_ROLE, ROL_USER))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nombre").value("test.jpg"));

        verify(fotoService, times(1)).guardarFoto(eq(PROPERTY_ID), any());
    }

    // ==================== Tests GET - Listar Fotos (PÚBLICO de negocio) ====================

    @Test
    @DisplayName("GET /api/propiedades/{id}/fotos - Debe retornar lista de fotos")
    void listarFotos_PropiedadExiste_Returns200() throws Exception {
        when(fotoService.listarFotos(1L)).thenReturn(List.of(fotoDTO));

        mockMvc.perform(get("/api/propiedades/1/fotos")
                        .header(APP_CLIENT_HEADER, APP_CLIENT_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nombre").value("test.jpg"));

        verify(fotoService, times(1)).listarFotos(1L);
    }

    // ==================== Tests GET/{fotoId} (PÚBLICO) ====================

    @Test
    @DisplayName("GET /api/fotos/{fotoId} - Debe retornar foto")
    void obtenerFoto_FotoExiste_Returns200() throws Exception {
        when(fotoService.obtenerPorId(1L)).thenReturn(fotoDTO);

        mockMvc.perform(get("/api/fotos/1")
                        .header(APP_CLIENT_HEADER, APP_CLIENT_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));

        verify(fotoService, times(1)).obtenerPorId(1L);
    }

    // ==================== Tests DELETE (PROTEGIDO) ====================

    @Test
    @DisplayName("DELETE /api/fotos/{fotoId} - Usuario no dueño retorna 403 FORBIDDEN")
    void eliminarFoto_UsuarioNoDueno_Returns403() throws Exception {
        when(fotoService.obtenerPorId(1L)).thenReturn(fotoDTO);
        when(propertyService.obtenerPorId(PROPERTY_ID, false)).thenReturn(mockPropertyDTO);

        mockMvc.perform(delete("/api/fotos/1")
                        .header(APP_CLIENT_HEADER, APP_CLIENT_KEY)
                        .header(HEADER_USER, OTHER_USER_ID)
                        .header(HEADER_ROLE, ROL_USER))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE /api/fotos/{fotoId} - Dueño elimina foto y retorna 204 NO_CONTENT")
    void eliminarFoto_DuenoValido_Returns204() throws Exception {
        when(fotoService.obtenerPorId(1L)).thenReturn(fotoDTO);
        when(propertyService.obtenerPorId(PROPERTY_ID, false)).thenReturn(mockPropertyDTO);
        doNothing().when(fotoService).eliminarFoto(1L);

        mockMvc.perform(delete("/api/fotos/1")
                        .header(APP_CLIENT_HEADER, APP_CLIENT_KEY)
                        .header(HEADER_USER, OWNER_ID)
                        .header(HEADER_ROLE, ROL_USER))
                .andExpect(status().isNoContent());

        verify(fotoService, times(1)).eliminarFoto(1L);
    }

    @Test
    @DisplayName("DELETE /api/fotos/{fotoId} - Administrador elimina foto retorna 204 NO_CONTENT")
    void eliminarFoto_Admin_Returns204() throws Exception {
        when(fotoService.obtenerPorId(1L)).thenReturn(fotoDTO);
        when(propertyService.obtenerPorId(PROPERTY_ID, false)).thenReturn(mockPropertyDTO);
        doNothing().when(fotoService).eliminarFoto(1L);

        mockMvc.perform(delete("/api/fotos/1")
                        .header(APP_CLIENT_HEADER, APP_CLIENT_KEY)
                        .header(HEADER_USER, "999")
                        .header(HEADER_ROLE, ROL_ADMIN))
                .andExpect(status().isNoContent());

        verify(fotoService, times(1)).eliminarFoto(1L);
    }

    // ==================== Tests PUT - Reordenar Fotos (PROTEGIDO) ====================

    @Test
    @DisplayName("PUT /api/propiedades/{id}/fotos/reordenar - Faltan cabeceras retorna 401")
    void reordenarFotos_FaltanCabeceras_Returns401() throws Exception {
        List<Long> fotosIds = List.of(3L, 1L, 2L);

        mockMvc.perform(put("/api/propiedades/1/fotos/reordenar")
                        .header(APP_CLIENT_HEADER, APP_CLIENT_KEY)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(fotosIds)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PUT /api/propiedades/{id}/fotos/reordenar - Dueño reordena fotos y retorna 204")
    void reordenarFotos_DuenoValido_Returns204() throws Exception {
        List<Long> fotosIds = List.of(3L, 1L, 2L);

        when(propertyService.obtenerPorId(PROPERTY_ID, false)).thenReturn(mockPropertyDTO);
        doNothing().when(fotoService).reordenarFotos(PROPERTY_ID, fotosIds);

        mockMvc.perform(put("/api/propiedades/1/fotos/reordenar")
                        .header(APP_CLIENT_HEADER, APP_CLIENT_KEY)
                        .header(HEADER_USER, OWNER_ID)
                        .header(HEADER_ROLE, ROL_USER)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(fotosIds)))
                .andExpect(status().isNoContent());

        verify(fotoService, times(1)).reordenarFotos(PROPERTY_ID, fotosIds);
    }
}