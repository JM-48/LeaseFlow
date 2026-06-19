package com.rentify.documentService.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rentify.documentService.client.UserServiceClient;
import com.rentify.documentService.dto.ActualizarEstadoRequest;
import com.rentify.documentService.dto.DocumentoDTO;
import com.rentify.documentService.dto.EstadoDTO;
import com.rentify.documentService.dto.TipoDocumentoDTO;
import com.rentify.documentService.dto.external.UsuarioDTO;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Tests de Integración - DocumentoService (Flujo Completo)")
class DocumentoServiceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserServiceClient userServiceClient;

    private static final Long USUARIO_ID = 1L;
    private static final Long ADMIN_ID = 5L;

    @BeforeEach
    void setUpMocks() {
        // 1. Configuramos un usuario regular
        UsuarioDTO normalUser = new UsuarioDTO();
        normalUser.setId(USUARIO_ID);
        normalUser.setEmail("usuario@rentify.com");
        UsuarioDTO.RolDTO rolUsuario = new UsuarioDTO.RolDTO(3L, "ARRIENDATARIO");
        normalUser.setRol(rolUsuario);

        when(userServiceClient.existsUser(USUARIO_ID)).thenReturn(true);
        when(userServiceClient.getUserById(USUARIO_ID)).thenReturn(normalUser);
        when(userServiceClient.userHasRole(USUARIO_ID, "ADMIN")).thenReturn(false);

        // 2. Configuramos un usuario administrador
        UsuarioDTO adminUser = new UsuarioDTO();
        adminUser.setId(ADMIN_ID);
        adminUser.setEmail("admin@rentify.com");
        UsuarioDTO.RolDTO rolAdmin = new UsuarioDTO.RolDTO(1L, "ADMIN");
        adminUser.setRol(rolAdmin);

        when(userServiceClient.existsUser(ADMIN_ID)).thenReturn(true);
        when(userServiceClient.getUserById(ADMIN_ID)).thenReturn(adminUser);
        when(userServiceClient.userHasRole(ADMIN_ID, "ADMIN")).thenReturn(true);
    }

    @Test
    @DisplayName("POST /api/tipos-documentos - Debe crear un tipo de documento")
    void crearTipoDocumento_Success() throws Exception {
        TipoDocumentoDTO nuevoTipo = new TipoDocumentoDTO();
        nuevoTipo.setNombre("LIQUIDACION_SUELDO");

        mockMvc.perform(post("/api/tipos-documentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nuevoTipo)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("LIQUIDACION_SUELDO"))
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    @DisplayName("POST /api/estados - Debe crear un estado")
    void crearEstado_Success() throws Exception {
        EstadoDTO nuevoEstado = new EstadoDTO();
        nuevoEstado.setNombre("RECHAZADO");

        mockMvc.perform(post("/api/estados")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nuevoEstado)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("RECHAZADO"))
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    @DisplayName("Flujo Completo: Subir documento y luego rechazarlo con observaciones")
    void flujoCompleto_SubirYRechazarDocumento() throws Exception {
        // PASO 1: Preparar dependencias (Crear un Tipo y un Estado inicial)
        TipoDocumentoDTO tipo = new TipoDocumentoDTO(null, "DNI");
        MvcResult resultTipo = mockMvc.perform(post("/api/tipos-documentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tipo)))
                .andReturn();
        Long tipoId = objectMapper.readValue(resultTipo.getResponse().getContentAsString(), TipoDocumentoDTO.class).getId();

        EstadoDTO estadoPendiente = new EstadoDTO(null, "PENDIENTE");
        MvcResult resultEstadoPendiente = mockMvc.perform(post("/api/estados")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(estadoPendiente)))
                .andReturn();
        Long estadoPendienteId = objectMapper.readValue(resultEstadoPendiente.getResponse().getContentAsString(), EstadoDTO.class).getId();

        EstadoDTO estadoRechazado = new EstadoDTO(null, "RECHAZADO");
        MvcResult resultEstadoRechazado = mockMvc.perform(post("/api/estados")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(estadoRechazado)))
                .andReturn();
        Long estadoRechazadoId = objectMapper.readValue(resultEstadoRechazado.getResponse().getContentAsString(), EstadoDTO.class).getId();

        // PASO 2: Subir el documento (El usuario sube su DNI)
        DocumentoDTO nuevoDoc = new DocumentoDTO();
        nuevoDoc.setNombre("carnet_identidad_frente.pdf");
        nuevoDoc.setUsuarioId(USUARIO_ID);
        nuevoDoc.setEstadoId(estadoPendienteId);
        nuevoDoc.setTipoDocId(tipoId);

        MvcResult resultDoc = mockMvc.perform(post("/api/documentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nuevoDoc)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("carnet_identidad_frente.pdf"))
                .andReturn();

        Long documentoId = objectMapper.readValue(resultDoc.getResponse().getContentAsString(), DocumentoDTO.class).getId();

        // PASO 3: El Administrador rechaza el documento con observaciones
        ActualizarEstadoRequest requestRechazo = new ActualizarEstadoRequest();
        requestRechazo.setEstadoId(estadoRechazadoId);
        requestRechazo.setObservaciones("La imagen esta borrosa, por favor suba una foto con mejor iluminacion.");
        requestRechazo.setRevisadoPor(ADMIN_ID);

        mockMvc.perform(patch("/api/documentos/{id}/estado", documentoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestRechazo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estadoId").value(estadoRechazadoId))
                .andExpect(jsonPath("$.observaciones").value("La imagen esta borrosa, por favor suba una foto con mejor iluminacion."));
    }

    @Test
    @DisplayName("POST /api/documentos - Falla con 400 si faltan datos obligatorios")
    void crearDocumento_DatosInvalidos_Retorna400() throws Exception {
        DocumentoDTO docInvalido = new DocumentoDTO();
        // Faltan todos los campos obligatorios: nombre, usuarioId, estadoId, tipoDocId

        mockMvc.perform(post("/api/documentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(docInvalido)))
                .andExpect(status().isBadRequest());
    }
}