package com.rentify.applicationService.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rentify.applicationService.dto.RegistroArriendoDTO;
import com.rentify.applicationService.exception.BusinessValidationException;
import com.rentify.applicationService.exception.ResourceNotFoundException;
import com.rentify.applicationService.service.RegistroArriendoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;  // CAMBIADO
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;  // SOLO MOCKITO
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RegistroController.class)
@DisplayName("Tests de integración para RegistroController")
class RegistroControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean  // CAMBIADO de @MockBean
    private RegistroArriendoService service;

    private RegistroArriendoDTO registroDTO;

    @BeforeEach
    void setUp() {
        registroDTO = RegistroArriendoDTO.builder()
                .id(1L)
                .solicitudId(1L)
                .fechaInicio(new Date())
                .fechaFin(new Date())
                .montoMensual(500000.0)
                .activo(true)
                .build();
    }

    @Test
    @DisplayName("POST /api/registros - Crear registro exitosamente")
    void crearRegistro_DeberiaRetornar201() throws Exception {
        // Given
        when(service.crearRegistro(any(RegistroArriendoDTO.class))).thenReturn(registroDTO);

        // When & Then
        mockMvc.perform(post("/api/registros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registroDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.solicitudId").value(1))
                .andExpect(jsonPath("$.montoMensual").value(500000.0))
                .andExpect(jsonPath("$.activo").value(true));

        verify(service, times(1)).crearRegistro(any(RegistroArriendoDTO.class));
    }

    @Test
    @DisplayName("POST /api/registros - Validación fallida")
    void crearRegistro_DatosInvalidos_DeberiaRetornar400() throws Exception {
        // Given - DTO sin solicitudId (campo requerido)
        RegistroArriendoDTO dtoInvalido = RegistroArriendoDTO.builder()
                .fechaInicio(new Date())
                .montoMensual(500000.0)
                .build();

        // When & Then
        mockMvc.perform(post("/api/registros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoInvalido)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/registros - Solicitud no aceptada")
    void crearRegistro_SolicitudNoAceptada_DeberiaRetornar400() throws Exception {
        // Given
        when(service.crearRegistro(any(RegistroArriendoDTO.class)))
                .thenThrow(new BusinessValidationException("Solo se pueden crear registros para solicitudes aceptadas"));

        // When & Then
        mockMvc.perform(post("/api/registros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registroDTO)))
                .andExpect(status().isBadRequest());

        verify(service, times(1)).crearRegistro(any(RegistroArriendoDTO.class));
    }

    @Test
    @DisplayName("GET /api/registros - Listar todos")
    void listarTodos_DeberiaRetornar200ConLista() throws Exception {
        // Given
        List<RegistroArriendoDTO> registros = Arrays.asList(registroDTO);
        when(service.listarTodos(false)).thenReturn(registros);

        // When & Then
        mockMvc.perform(get("/api/registros")
                        .param("includeDetails", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1));

        verify(service, times(1)).listarTodos(false);
    }

    @Test
    @DisplayName("GET /api/registros/{id} - Obtener por ID exitoso")
    void obtenerPorId_RegistroExiste_DeberiaRetornar200() throws Exception {
        // Given
        when(service.obtenerPorId(1L, true)).thenReturn(registroDTO);

        // When & Then
        mockMvc.perform(get("/api/registros/1")
                        .param("includeDetails", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.solicitudId").value(1));

        verify(service, times(1)).obtenerPorId(1L, true);
    }

    @Test
    @DisplayName("GET /api/registros/{id} - No encontrado")
    void obtenerPorId_RegistroNoExiste_DeberiaRetornar404() throws Exception {
        // Given
        when(service.obtenerPorId(999L, true))
                .thenThrow(new ResourceNotFoundException("Registro no encontrado con ID: 999"));

        // When & Then
        mockMvc.perform(get("/api/registros/999")
                        .param("includeDetails", "true"))
                .andExpect(status().isNotFound());

        verify(service, times(1)).obtenerPorId(999L, true);
    }

    @Test
    @DisplayName("GET /api/registros/solicitud/{solicitudId}")
    void obtenerPorSolicitud_DeberiaRetornar200() throws Exception {
        // Given
        List<RegistroArriendoDTO> registros = Arrays.asList(registroDTO);
        when(service.obtenerPorSolicitud(1L)).thenReturn(registros);

        // When & Then
        mockMvc.perform(get("/api/registros/solicitud/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(service, times(1)).obtenerPorSolicitud(1L);
    }

    @Test
    @DisplayName("PATCH /api/registros/{id}/finalizar - Finalizar exitoso")
    void finalizarRegistro_DeberiaRetornar200() throws Exception {
        // Given
        registroDTO.setActivo(false);
        when(service.finalizarRegistro(1L)).thenReturn(registroDTO);

        // When & Then
        mockMvc.perform(patch("/api/registros/1/finalizar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activo").value(false));

        verify(service, times(1)).finalizarRegistro(1L);
    }

    @Test
    @DisplayName("PATCH /api/registros/{id}/finalizar - Ya está inactivo")
    void finalizarRegistro_YaInactivo_DeberiaRetornar400() throws Exception {
        // Given
        when(service.finalizarRegistro(1L))
                .thenThrow(new BusinessValidationException("El registro ya está inactivo"));

        // When & Then
        mockMvc.perform(patch("/api/registros/1/finalizar"))
                .andExpect(status().isBadRequest());

        verify(service, times(1)).finalizarRegistro(1L);
    }

    @Test
    @DisplayName("PATCH /api/registros/{id}/finalizar - No encontrado")
    void finalizarRegistro_RegistroNoExiste_DeberiaRetornar404() throws Exception {
        // Given
        when(service.finalizarRegistro(999L))
                .thenThrow(new ResourceNotFoundException("Registro no encontrado con ID: 999"));

        // When & Then
        mockMvc.perform(patch("/api/registros/999/finalizar"))
                .andExpect(status().isNotFound());

        verify(service, times(1)).finalizarRegistro(999L);
    }
}