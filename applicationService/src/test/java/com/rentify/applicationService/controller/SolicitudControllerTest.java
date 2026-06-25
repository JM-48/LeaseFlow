package com.rentify.applicationService.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rentify.applicationService.dto.SolicitudArriendoDTO;
import com.rentify.applicationService.exception.BusinessValidationException;
import com.rentify.applicationService.exception.ResourceNotFoundException;
import com.rentify.applicationService.service.SolicitudArriendoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.Arrays;
import java.util.Date;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de integración para SolicitudController
 * Valida las respuestas HTTP y el comportamiento del endpoint
 */
// SOLUCIÓN: Excluir la seguridad por defecto de Spring Security
@WebMvcTest(
        controllers = SolicitudController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                SecurityFilterAutoConfiguration.class
        }
)
@TestPropertySource(properties = "app.security.client-key=test-key-123")
@DisplayName("Tests de SolicitudController")
class SolicitudControllerTest {

    private static final String APP_CLIENT_HEADER = "X-App-Client";
    private static final String APP_CLIENT_KEY = "test-key-123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SolicitudArriendoService service;

    private SolicitudArriendoDTO solicitudDTO;

    /**
     * Helper para no repetir el header X-App-Client en cada test.
     * Todas las peticiones reales (legitimas) deben pasar por este interceptor primero.
     */
    private MockHttpServletRequestBuilder withAppKey(MockHttpServletRequestBuilder builder) {
        return builder.header(APP_CLIENT_HEADER, APP_CLIENT_KEY);
    }

    @BeforeEach
    void setUp() {
        solicitudDTO = SolicitudArriendoDTO.builder()
                .id(1L)
                .usuarioId(1L)
                .propiedadId(1L)
                .estado("PENDIENTE")
                .fechaSolicitud(new Date())
                .build();
    }

    @Test
    @DisplayName("POST /api/solicitudes - Debe crear solicitud y retornar 201")
    void crearSolicitud_DatosValidos_Returns201() throws Exception {
        when(service.crearSolicitud(any(SolicitudArriendoDTO.class))).thenReturn(solicitudDTO);

        mockMvc.perform(withAppKey(post("/api/solicitudes"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(solicitudDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.estado").value("PENDIENTE"));

        verify(service, times(1)).crearSolicitud(any(SolicitudArriendoDTO.class));
    }

    @Test
    @DisplayName("POST /api/solicitudes - Debe retornar 400 cuando faltan datos requeridos")
    void crearSolicitud_SinUsuarioId_Returns400() throws Exception {
        solicitudDTO.setUsuarioId(null);

        mockMvc.perform(withAppKey(post("/api/solicitudes"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(solicitudDTO)))
                .andExpect(status().isBadRequest());

        verify(service, never()).crearSolicitud(any(SolicitudArriendoDTO.class));
    }

    @Test
    @DisplayName("POST /api/solicitudes - Debe retornar 400 cuando hay error de negocio")
    void crearSolicitud_ErrorNegocio_Returns400() throws Exception {
        when(service.crearSolicitud(any(SolicitudArriendoDTO.class)))
                .thenThrow(new BusinessValidationException("El usuario ya tiene 3 solicitudes activas"));

        mockMvc.perform(withAppKey(post("/api/solicitudes"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(solicitudDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("3 solicitudes activas")));
    }

    // ==============================================================================
    // TESTS DE SEGURIDAD DEL INTERCEPTOR (X-App-Client) - NUEVOS
    // ==============================================================================

    @Test
    @DisplayName("POST /api/solicitudes - Debe retornar 403 si falta X-App-Client (acceso directo tipo navegador)")
    void crearSolicitud_SinApiKey_Returns403() throws Exception {
        mockMvc.perform(post("/api/solicitudes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(solicitudDTO)))
                .andExpect(status().isForbidden());

        verify(service, never()).crearSolicitud(any(SolicitudArriendoDTO.class));
    }

    @Test
    @DisplayName("GET /api/solicitudes/1 - Debe retornar 403 si X-App-Client es incorrecto")
    void obtenerPorId_ApiKeyIncorrecta_Returns403() throws Exception {
        mockMvc.perform(get("/api/solicitudes/1")
                        .header(APP_CLIENT_HEADER, "valor-incorrecto")
                        .header("X-Usuario-Id", 1L)
                        .header("X-Rol-Id", 1L))
                .andExpect(status().isForbidden());

        verify(service, never()).obtenerPorId(any(), anyBoolean());
    }

    // ==============================================================================
    // TESTS DE SEGURIDAD PARA LISTAR SOLICITUDES (GET /api/solicitudes)
    // ==============================================================================

    @Test
    @DisplayName("GET /api/solicitudes - Debe retornar 401 UNAUTHORIZED si faltan headers de usuario")
    void listarSolicitudes_SinHeaders_Returns401() throws Exception {
        mockMvc.perform(withAppKey(get("/api/solicitudes")))
                .andExpect(status().isUnauthorized());

        // Verificamos que el servicio NUNCA se llame si se rechaza por seguridad
        verify(service, never()).listarSolicitudesSeguras(any(), any(), anyBoolean());
    }

    @Test
    @DisplayName("GET /api/solicitudes - Debe listar solicitudes cuando se envían headers correctos")
    void listarSolicitudesSeguras_ConHeaders_Returns200() throws Exception {
        when(service.listarSolicitudesSeguras(1L, 1L, false)).thenReturn(Arrays.asList(solicitudDTO));

        mockMvc.perform(withAppKey(get("/api/solicitudes"))
                        .header("X-Usuario-Id", 1L)
                        .header("X-Rol-Id", 1L)
                        .param("includeDetails", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1));

        verify(service, times(1)).listarSolicitudesSeguras(1L, 1L, false);
    }

    @Test
    @DisplayName("GET /api/solicitudes - Debe usar includeDetails=false por defecto con headers")
    void listarSolicitudesSeguras_SinParametroIncludeDetails_UsaDefaultFalse() throws Exception {
        when(service.listarSolicitudesSeguras(2L, 3L, false)).thenReturn(Arrays.asList(solicitudDTO));

        mockMvc.perform(withAppKey(get("/api/solicitudes"))
                        .header("X-Usuario-Id", 2L)
                        .header("X-Rol-Id", 3L))
                .andExpect(status().isOk());

        verify(service, times(1)).listarSolicitudesSeguras(2L, 3L, false);
    }

    // ==============================================================================
    // TESTS DE SEGURIDAD PARA GET /api/solicitudes/{id}
    // ==============================================================================

    @Test
    @DisplayName("GET /api/solicitudes/{id} - Debe retornar 401 si faltan headers")
    void obtenerPorId_SinHeaders_Returns401() throws Exception {
        mockMvc.perform(withAppKey(get("/api/solicitudes/1"))
                        .param("includeDetails", "true"))
                .andExpect(status().isUnauthorized());

        verify(service, never()).obtenerPorId(any(), anyBoolean());
    }

    @Test
    @DisplayName("GET /api/solicitudes/{id} - Debe retornar solicitud cuando existe")
    void obtenerPorId_SolicitudExiste_Returns200() throws Exception {
        when(service.obtenerPorId(1L, true)).thenReturn(solicitudDTO);

        mockMvc.perform(withAppKey(get("/api/solicitudes/1"))
                        .param("includeDetails", "true")
                        .header("X-Usuario-Id", 1L)
                        .header("X-Rol-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(service, times(1)).obtenerPorId(1L, true);
    }

    @Test
    @DisplayName("GET /api/solicitudes/{id} - Debe retornar 404 cuando no existe")
    void obtenerPorId_SolicitudNoExiste_Returns404() throws Exception {
        when(service.obtenerPorId(999L, true))
                .thenThrow(new ResourceNotFoundException("Solicitud no encontrada con ID: 999"));

        mockMvc.perform(withAppKey(get("/api/solicitudes/999"))
                        .param("includeDetails", "true")
                        .header("X-Usuario-Id", 1L)
                        .header("X-Rol-Id", 1L))
                .andExpect(status().isNotFound());
    }

    // ==============================================================================
    // TESTS DE SEGURIDAD PARA GET /api/solicitudes/usuario/{usuarioId}
    // ==============================================================================

    @Test
    @DisplayName("GET /api/solicitudes/usuario/{usuarioId} - Debe retornar 401 si faltan headers")
    void obtenerPorUsuario_SinHeaders_Returns401() throws Exception {
        mockMvc.perform(withAppKey(get("/api/solicitudes/usuario/1")))
                .andExpect(status().isUnauthorized());

        verify(service, never()).obtenerPorUsuario(any());
    }

    @Test
    @DisplayName("GET /api/solicitudes/usuario/{usuarioId} - Debe retornar 403 si un usuario normal consulta a otro")
    void obtenerPorUsuario_UsuarioDistintoSinSerAdmin_Returns403() throws Exception {
        mockMvc.perform(withAppKey(get("/api/solicitudes/usuario/1"))
                        .header("X-Usuario-Id", 2L)
                        .header("X-Rol-Id", 3L))
                .andExpect(status().isForbidden());

        verify(service, never()).obtenerPorUsuario(any());
    }

    @Test
    @DisplayName("GET /api/solicitudes/usuario/{usuarioId} - Debe retornar solicitudes del usuario")
    void obtenerPorUsuario_Returns200() throws Exception {
        when(service.obtenerPorUsuario(1L)).thenReturn(Arrays.asList(solicitudDTO));

        mockMvc.perform(withAppKey(get("/api/solicitudes/usuario/1"))
                        .header("X-Usuario-Id", 1L)
                        .header("X-Rol-Id", 3L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(service, times(1)).obtenerPorUsuario(1L);
    }

    @Test
    @DisplayName("GET /api/solicitudes/usuario/{usuarioId} - Un ADMIN puede consultar a cualquier usuario")
    void obtenerPorUsuario_Admin_Returns200() throws Exception {
        when(service.obtenerPorUsuario(1L)).thenReturn(Arrays.asList(solicitudDTO));

        mockMvc.perform(withAppKey(get("/api/solicitudes/usuario/1"))
                        .header("X-Usuario-Id", 99L)
                        .header("X-Rol-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(service, times(1)).obtenerPorUsuario(1L);
    }

    // ==============================================================================
    // TESTS DE SEGURIDAD PARA GET /api/solicitudes/propiedad/{propiedadId}
    // ==============================================================================

    @Test
    @DisplayName("GET /api/solicitudes/propiedad/{propiedadId} - Debe retornar 401 si faltan headers")
    void obtenerPorPropiedad_SinHeaders_Returns401() throws Exception {
        mockMvc.perform(withAppKey(get("/api/solicitudes/propiedad/1")))
                .andExpect(status().isUnauthorized());

        verify(service, never()).obtenerPorPropiedad(any());
    }

    @Test
    @DisplayName("GET /api/solicitudes/propiedad/{propiedadId} - Debe retornar solicitudes de la propiedad")
    void obtenerPorPropiedad_Returns200() throws Exception {
        when(service.obtenerPorPropiedad(1L)).thenReturn(Arrays.asList(solicitudDTO));

        mockMvc.perform(withAppKey(get("/api/solicitudes/propiedad/1"))
                        .header("X-Usuario-Id", 1L)
                        .header("X-Rol-Id", 1L))
                .andExpect(status().isOk());

        verify(service, times(1)).obtenerPorPropiedad(1L);
    }

    // ==============================================================================
    // TESTS DE SEGURIDAD PARA PATCH /api/solicitudes/{id}/estado
    // ==============================================================================

    @Test
    @DisplayName("PATCH /api/solicitudes/{id}/estado - Debe retornar 401 si faltan headers")
    void actualizarEstado_SinHeaders_Returns401() throws Exception {
        mockMvc.perform(withAppKey(patch("/api/solicitudes/1/estado"))
                        .param("estado", "ACEPTADA"))
                .andExpect(status().isUnauthorized());

        verify(service, never()).actualizarEstado(any(), any());
    }

    @Test
    @DisplayName("PATCH /api/solicitudes/{id}/estado - Debe actualizar estado")
    void actualizarEstado_EstadoValido_Returns200() throws Exception {
        solicitudDTO.setEstado("ACEPTADA");
        when(service.actualizarEstado(1L, "ACEPTADA")).thenReturn(solicitudDTO);

        mockMvc.perform(withAppKey(patch("/api/solicitudes/1/estado"))
                        .param("estado", "ACEPTADA")
                        .header("X-Usuario-Id", 1L)
                        .header("X-Rol-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ACEPTADA"));

        verify(service, times(1)).actualizarEstado(1L, "ACEPTADA");
    }

    @Test
    @DisplayName("PATCH /api/solicitudes/{id}/estado - Debe retornar 400 con estado inválido")
    void actualizarEstado_EstadoInvalido_Returns400() throws Exception {
        when(service.actualizarEstado(1L, "INVALIDO"))
                .thenThrow(new BusinessValidationException("Estado inválido: INVALIDO"));

        mockMvc.perform(withAppKey(patch("/api/solicitudes/1/estado"))
                        .param("estado", "INVALIDO")
                        .header("X-Usuario-Id", 1L)
                        .header("X-Rol-Id", 1L))
                .andExpect(status().isBadRequest());
    }
}