package com.rentify.applicationService.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rentify.applicationService.client.DocumentServiceClient;
import com.rentify.applicationService.client.PropertyServiceClient;
import com.rentify.applicationService.client.UserServiceClient;
import com.rentify.applicationService.dto.PropiedadDTO;
import com.rentify.applicationService.dto.RegistroArriendoDTO;
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

import java.util.Date;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Tests de Integración - RegistroController (Robustos)")
class RegistroControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // --- MOCKS DE LOS CLIENTES EXTERNOS ---
    @MockBean
    private UserServiceClient userServiceClient;

    @MockBean
    private PropertyServiceClient propertyServiceClient;

    @MockBean
    private DocumentServiceClient documentServiceClient;

    private static final String BASE_URL = "/api/registros";
    private static final String SOLICITUD_URL = "/api/solicitudes";

    @BeforeEach
    void setUpMocks() {
        UsuarioDTO mockUser = new UsuarioDTO();
        mockUser.setId(1L);
        mockUser.setRolId(3);
        when(userServiceClient.existsUser(anyLong())).thenReturn(true);
        when(userServiceClient.getUserById(anyLong())).thenReturn(mockUser);

        PropiedadDTO mockProperty = new PropiedadDTO();
        mockProperty.setId(5L);
        when(propertyServiceClient.existsProperty(anyLong())).thenReturn(true);
        when(propertyServiceClient.getPropertyById(anyLong())).thenReturn(mockProperty);
        when(propertyServiceClient.isPropertyAvailable(anyLong())).thenReturn(true);

        when(documentServiceClient.hasApprovedDocuments(anyLong())).thenReturn(true);
    }

    // Método auxiliar para crear solicitudes base y no repetir código
    private Long crearSolicitudBase() throws Exception {
        SolicitudArriendoDTO solicitud = new SolicitudArriendoDTO();
        solicitud.setUsuarioId(1L);
        solicitud.setPropiedadId(5L);

        MvcResult resultSolicitud = mockMvc.perform(post(SOLICITUD_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(solicitud)))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readValue(resultSolicitud.getResponse().getContentAsString(), SolicitudArriendoDTO.class).getId();
    }

    // Método auxiliar para cambiar el estado de una solicitud, ya con los headers requeridos
    private void cambiarEstadoSolicitud(Long solicitudId, String estado) throws Exception {
        mockMvc.perform(patch(SOLICITUD_URL + "/{id}/estado", solicitudId)
                        .param("estado", estado)
                        .header("X-Usuario-Id", 1L)
                        .header("X-Rol-Id", 1L))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST / - Debe crear registro y retornar 201 Created cuando Solicitud está ACEPTADA")
    void crearRegistro_Success() throws Exception {
        Long solicitudId = crearSolicitudBase();

        // ACEPTAMOS la solicitud
        cambiarEstadoSolicitud(solicitudId, "ACEPTADA");

        // Intentamos crear el registro
        RegistroArriendoDTO nuevoRegistro = new RegistroArriendoDTO();
        nuevoRegistro.setSolicitudId(solicitudId);
        nuevoRegistro.setFechaInicio(new Date());
        nuevoRegistro.setMontoMensual(500000.0);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nuevoRegistro)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.solicitudId").value(solicitudId))
                .andExpect(jsonPath("$.montoMensual").value(500000.0));
    }

    @Test
    @DisplayName("POST / - Lanza 400 Bad Request por monto negativo")
    void crearRegistro_MontoNegativo_Retorna400() throws Exception {
        RegistroArriendoDTO registroInvalido = new RegistroArriendoDTO();
        registroInvalido.setSolicitudId(1L);
        registroInvalido.setFechaInicio(new Date());
        registroInvalido.setMontoMensual(-150000.0);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registroInvalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Error"));
    }

    // ==========================================
    // 🔥 NUEVOS TESTS DE REGLAS DE NEGOCIO
    // ==========================================

    @Test
    @DisplayName("POST / - Lanza 400 Bad Request si la Solicitud sigue PENDIENTE")
    void crearRegistro_SolicitudPendiente_Retorna400() throws Exception {
        Long solicitudId = crearSolicitudBase();
        // Omitimos el paso de ACEPTAR la solicitud (queda en PENDIENTE por defecto)

        RegistroArriendoDTO nuevoRegistro = new RegistroArriendoDTO();
        nuevoRegistro.setSolicitudId(solicitudId);
        nuevoRegistro.setFechaInicio(new Date());
        nuevoRegistro.setMontoMensual(500000.0);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nuevoRegistro)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Business Validation Error"));
    }

    @Test
    @DisplayName("POST / - Lanza 400 Bad Request si la Solicitud fue RECHAZADA")
    void crearRegistro_SolicitudRechazada_Retorna400() throws Exception {
        Long solicitudId = crearSolicitudBase();

        // RECHAZAMOS la solicitud
        cambiarEstadoSolicitud(solicitudId, "RECHAZADA");

        RegistroArriendoDTO nuevoRegistro = new RegistroArriendoDTO();
        nuevoRegistro.setSolicitudId(solicitudId);
        nuevoRegistro.setFechaInicio(new Date());
        nuevoRegistro.setMontoMensual(500000.0);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nuevoRegistro)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Business Validation Error"));
    }

    @Test
    @DisplayName("POST / - Lanza 404 Not Found si el ID de la Solicitud no existe")
    void crearRegistro_SolicitudInexistente_Retorna404() throws Exception {
        RegistroArriendoDTO nuevoRegistro = new RegistroArriendoDTO();
        nuevoRegistro.setSolicitudId(99999L); // ID irreal
        nuevoRegistro.setFechaInicio(new Date());
        nuevoRegistro.setMontoMensual(500000.0);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nuevoRegistro)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }
}