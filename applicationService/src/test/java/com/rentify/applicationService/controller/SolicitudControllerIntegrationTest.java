package com.rentify.applicationService.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rentify.applicationService.client.DocumentServiceClient;
import com.rentify.applicationService.client.PropertyServiceClient;
import com.rentify.applicationService.client.UserServiceClient;
import com.rentify.applicationService.dto.PropiedadDTO;
import com.rentify.applicationService.dto.SolicitudArriendoDTO;
import com.rentify.applicationService.dto.UsuarioDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Tests de Integración - SolicitudController (Robustos)")
class SolicitudControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserServiceClient userServiceClient;

    @MockBean
    private PropertyServiceClient propertyServiceClient;

    @MockBean
    private DocumentServiceClient documentServiceClient;

    private static final String BASE_URL = "/api/solicitudes";

    @BeforeEach
    void setUpMocks() {
        // Comportamiento por defecto (Mundo Ideal)
        UsuarioDTO mockUser = new UsuarioDTO();
        mockUser.setId(1L);
        mockUser.setRolId(3); // Arrendatario
        when(userServiceClient.existsUser(anyLong())).thenReturn(true);
        when(userServiceClient.getUserById(anyLong())).thenReturn(mockUser);

        PropiedadDTO mockProperty = new PropiedadDTO();
        mockProperty.setId(5L);
        when(propertyServiceClient.existsProperty(anyLong())).thenReturn(true);
        when(propertyServiceClient.getPropertyById(anyLong())).thenReturn(mockProperty);
        when(propertyServiceClient.isPropertyAvailable(anyLong())).thenReturn(true);

        when(documentServiceClient.hasApprovedDocuments(anyLong())).thenReturn(true);
    }

    @Test
    @DisplayName("POST / - Debe crear solicitud y retornar 201 Created")
    void crearSolicitud_Success() throws Exception {
        SolicitudArriendoDTO nuevaSolicitud = new SolicitudArriendoDTO();
        nuevaSolicitud.setUsuarioId(1L);
        nuevaSolicitud.setPropiedadId(5L);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nuevaSolicitud)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.usuarioId").value(1L))
                .andExpect(jsonPath("$.propiedadId").value(5L))
                .andExpect(jsonPath("$.estado").value("PENDIENTE"));
    }

    // ==========================================
    // 🔥 NUEVOS TESTS DE REGLAS DE NEGOCIO REALES
    // ==========================================

    @Test
    @DisplayName("POST / - Lanza 400/404 si el usuario no existe en el microservicio de usuarios")
    void crearSolicitud_UsuarioNoExiste_RetornaError() throws Exception {
        // 🔥 Forzamos a ambos métodos a decir que el usuario NO existe
        when(userServiceClient.existsUser(1L)).thenReturn(false);
        when(userServiceClient.getUserById(1L)).thenReturn(null); // <--- Esta es la clave

        SolicitudArriendoDTO nuevaSolicitud = new SolicitudArriendoDTO();
        nuevaSolicitud.setUsuarioId(1L);
        nuevaSolicitud.setPropiedadId(5L);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nuevaSolicitud)))
                .andExpect(status().isBadRequest()); // O isNotFound() según corresponda
    }

    @Test
    @DisplayName("POST / - Lanza 400 si la propiedad no está disponible")
    void crearSolicitud_PropiedadNoDisponible_Retorna400() throws Exception {
        // Simulamos que la propiedad está ocupada o pausada
        when(propertyServiceClient.isPropertyAvailable(5L)).thenReturn(false);

        SolicitudArriendoDTO nuevaSolicitud = new SolicitudArriendoDTO();
        nuevaSolicitud.setUsuarioId(1L);
        nuevaSolicitud.setPropiedadId(5L);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nuevaSolicitud)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST / - Lanza 400 si el usuario no tiene sus documentos aprobados")
    void crearSolicitud_DocumentosNoAprobados_Retorna400() throws Exception {
        // Simulamos que el cliente falló la validación de antecedentes/renta
        when(documentServiceClient.hasApprovedDocuments(1L)).thenReturn(false);

        SolicitudArriendoDTO nuevaSolicitud = new SolicitudArriendoDTO();
        nuevaSolicitud.setUsuarioId(1L);
        nuevaSolicitud.setPropiedadId(5L);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nuevaSolicitud)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH /{id}/estado - Debe actualizar el estado correctamente")
    void actualizarEstado_Success() throws Exception {
        SolicitudArriendoDTO nueva = new SolicitudArriendoDTO();
        nueva.setUsuarioId(1L);
        nueva.setPropiedadId(5L);

        MvcResult result = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nueva)))
                .andExpect(status().isCreated())
                .andReturn();

        Long idGenerado = objectMapper.readValue(result.getResponse().getContentAsString(), SolicitudArriendoDTO.class).getId();

        mockMvc.perform(patch(BASE_URL + "/{id}/estado", idGenerado)
                        .param("estado", "ACEPTADA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ACEPTADA"));
    }
}