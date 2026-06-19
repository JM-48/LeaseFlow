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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Tests de Integración - DocumentoService (Robustos)")
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
        // --- SETUP USUARIO REGULAR ---
        UsuarioDTO normalUser = new UsuarioDTO();
        normalUser.setId(USUARIO_ID);
        normalUser.setEmail("usuario@rentify.com");
        UsuarioDTO.RolDTO rolUsuario = new UsuarioDTO.RolDTO(3L, "ARRIENDATARIO");
        normalUser.setRol(rolUsuario);

        when(userServiceClient.existsUser(USUARIO_ID)).thenReturn(true);
        when(userServiceClient.getUserById(USUARIO_ID)).thenReturn(normalUser);
        when(userServiceClient.userHasRole(USUARIO_ID, "ADMIN")).thenReturn(false);

        // --- SETUP USUARIO ADMINISTRADOR ---
        UsuarioDTO adminUser = new UsuarioDTO();
        adminUser.setId(ADMIN_ID);
        adminUser.setEmail("admin@rentify.com");
        UsuarioDTO.RolDTO rolAdmin = new UsuarioDTO.RolDTO(1L, "ADMIN");
        adminUser.setRol(rolAdmin);

        when(userServiceClient.existsUser(ADMIN_ID)).thenReturn(true);
        when(userServiceClient.getUserById(ADMIN_ID)).thenReturn(adminUser);
        when(userServiceClient.userHasRole(ADMIN_ID, "ADMIN")).thenReturn(true);
    }

    // ==========================================
    // 🛠️ MÉTODOS AUXILIARES (DRY)
    // ==========================================

    private Long crearTipoDocumento(String nombre) throws Exception {
        TipoDocumentoDTO tipo = new TipoDocumentoDTO(null, nombre);
        MvcResult result = mockMvc.perform(post("/api/tipos-documentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tipo)))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readValue(result.getResponse().getContentAsString(), TipoDocumentoDTO.class).getId();
    }

    private Long crearEstado(String nombre) throws Exception {
        EstadoDTO estado = new EstadoDTO(null, nombre);
        MvcResult result = mockMvc.perform(post("/api/estados")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(estado)))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readValue(result.getResponse().getContentAsString(), EstadoDTO.class).getId();
    }

    // ==========================================
    // 🟢 TESTS DE CAMINO FELIZ
    // ==========================================

    @Test
    @DisplayName("Flujo Completo: Subir documento y luego rechazarlo con observaciones (ADMIN)")
    void flujoCompleto_SubirYRechazarDocumento() throws Exception {
        // 1. Preparar dependencias usando helpers
        Long tipoId = crearTipoDocumento("DNI");
        Long estadoPendienteId = crearEstado("PENDIENTE");
        Long estadoRechazadoId = crearEstado("RECHAZADO");

        // 2. Subir el documento
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

        // 3. Administrador rechaza el documento
        ActualizarEstadoRequest requestRechazo = new ActualizarEstadoRequest();
        requestRechazo.setEstadoId(estadoRechazadoId);
        requestRechazo.setObservaciones("La imagen esta borrosa, por favor suba una foto con mejor iluminacion.");
        requestRechazo.setRevisadoPor(ADMIN_ID); // Usa el ID del Admin

        mockMvc.perform(patch("/api/documentos/{id}/estado", documentoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestRechazo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estadoId").value(estadoRechazadoId))
                .andExpect(jsonPath("$.observaciones").value("La imagen esta borrosa, por favor suba una foto con mejor iluminacion."));
    }

    // ==========================================
    // 🔥 TESTS DE REGLAS DE NEGOCIO Y ERRORES
    // ==========================================

    @Test
    @DisplayName("POST /api/documentos - Falla con 400 si faltan datos obligatorios")
    void crearDocumento_DatosInvalidos_Retorna400() throws Exception {
        DocumentoDTO docInvalido = new DocumentoDTO();
        // Faltan todos los campos obligatorios

        mockMvc.perform(post("/api/documentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(docInvalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Error"));
    }

    @Test
    @DisplayName("POST /api/documentos - Falla con 400/404 si el usuario no existe en UserService")
    void crearDocumento_UsuarioNoExiste_RetornaError() throws Exception {
        // Forzamos a que el usuario 99L no exista
        when(userServiceClient.existsUser(99L)).thenReturn(false);
        when(userServiceClient.getUserById(99L)).thenReturn(null);

        Long tipoId = crearTipoDocumento("CERTIFICADO_AFP");
        Long estadoPendienteId = crearEstado("PENDIENTE");

        DocumentoDTO nuevoDoc = new DocumentoDTO();
        nuevoDoc.setNombre("afp_2026.pdf");
        nuevoDoc.setUsuarioId(99L); // Usuario inexistente
        nuevoDoc.setEstadoId(estadoPendienteId);
        nuevoDoc.setTipoDocId(tipoId);

        mockMvc.perform(post("/api/documentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nuevoDoc)))
                .andExpect(status().isBadRequest()); // O isNotFound() dependiendo de tu lógica
    }

    @Test
    @DisplayName("PATCH /api/documentos/{id}/estado - Falla con 400/403 si un usuario NO ADMIN intenta cambiar el estado")
    void actualizarEstado_UsuarioNoEsAdmin_RetornaError() throws Exception {
        Long tipoId = crearTipoDocumento("DNI");
        Long estadoPendienteId = crearEstado("PENDIENTE");
        Long estadoAprobadoId = crearEstado("APROBADO");

        // Creamos el documento primero
        DocumentoDTO nuevoDoc = new DocumentoDTO();
        nuevoDoc.setNombre("carnet.pdf");
        nuevoDoc.setUsuarioId(USUARIO_ID);
        nuevoDoc.setEstadoId(estadoPendienteId);
        nuevoDoc.setTipoDocId(tipoId);

        MvcResult resultDoc = mockMvc.perform(post("/api/documentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nuevoDoc)))
                .andExpect(status().isCreated())
                .andReturn();

        Long documentoId = objectMapper.readValue(resultDoc.getResponse().getContentAsString(), DocumentoDTO.class).getId();

        // El arrendatario intenta aprobar su propio documento
        ActualizarEstadoRequest requestTramposo = new ActualizarEstadoRequest();
        requestTramposo.setEstadoId(estadoAprobadoId);
        requestTramposo.setRevisadoPor(USUARIO_ID); // Usuario sin privilegios

        mockMvc.perform(patch("/api/documentos/{id}/estado", documentoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestTramposo)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Business Validation Error"));
    }
}