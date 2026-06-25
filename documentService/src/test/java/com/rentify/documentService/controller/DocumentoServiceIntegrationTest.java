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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Transactional
@DisplayName("Tests de Integración - DocumentoService (Robustos)")
class DocumentoServiceIntegrationTest {

    private static final String APP_CLIENT_HEADER = "X-App-Client";
    private static final String APP_CLIENT_KEY = "test-key-123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserServiceClient userServiceClient;

    private static final Long USUARIO_ID = 1L;
    private static final Long ROL_USUARIO_ID = 3L;
    private static final Long ADMIN_ID = 5L;
    private static final Long ROL_ADMIN_ID = 1L;

    private MockHttpServletRequestBuilder withAppKey(MockHttpServletRequestBuilder builder) {
        return builder.header(APP_CLIENT_HEADER, APP_CLIENT_KEY);
    }

    @BeforeEach
    void setUpMocks() {
        UsuarioDTO normalUser = new UsuarioDTO();
        normalUser.setId(USUARIO_ID);
        normalUser.setEmail("usuario@rentify.com");
        UsuarioDTO.RolDTO rolUsuario = new UsuarioDTO.RolDTO(ROL_USUARIO_ID, "ARRIENDATARIO");
        normalUser.setRol(rolUsuario);

        when(userServiceClient.existsUser(USUARIO_ID)).thenReturn(true);
        when(userServiceClient.getUserById(USUARIO_ID)).thenReturn(normalUser);
        when(userServiceClient.userHasRole(USUARIO_ID, "ADMIN")).thenReturn(false);

        UsuarioDTO adminUser = new UsuarioDTO();
        adminUser.setId(ADMIN_ID);
        adminUser.setEmail("admin@rentify.com");
        UsuarioDTO.RolDTO rolAdmin = new UsuarioDTO.RolDTO(ROL_ADMIN_ID, "ADMIN");
        adminUser.setRol(rolAdmin);

        when(userServiceClient.existsUser(ADMIN_ID)).thenReturn(true);
        when(userServiceClient.getUserById(ADMIN_ID)).thenReturn(adminUser);
        when(userServiceClient.userHasRole(ADMIN_ID, "ADMIN")).thenReturn(true);
    }

    // ==========================================
    // MÉTODOS AUXILIARES
    // ==========================================

    private Long crearTipoDocumento(String nombre) throws Exception {
        TipoDocumentoDTO tipo = new TipoDocumentoDTO(null, nombre);
        MvcResult result = mockMvc.perform(withAppKey(post("/api/tipos-documentos"))
                        .header("X-Usuario-Id", ADMIN_ID)
                        .header("X-Rol-Id", ROL_ADMIN_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tipo)))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readValue(result.getResponse().getContentAsString(), TipoDocumentoDTO.class).getId();
    }

    private Long crearEstado(String nombre) throws Exception {
        EstadoDTO estado = new EstadoDTO(null, nombre);
        MvcResult result = mockMvc.perform(withAppKey(post("/api/estados"))
                        .header("X-Usuario-Id", ADMIN_ID)
                        .header("X-Rol-Id", ROL_ADMIN_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(estado)))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readValue(result.getResponse().getContentAsString(), EstadoDTO.class).getId();
    }

    // ==========================================
    // TESTS DE SEGURIDAD - X-App-Client
    // ==========================================

    @Test
    @DisplayName("POST /api/documentos - Falla con 403 si falta X-App-Client (acceso directo navegador)")
    void crearDocumento_SinApiKey_Retorna403() throws Exception {
        DocumentoDTO doc = new DocumentoDTO();
        doc.setNombre("test.pdf");

        mockMvc.perform(post("/api/documentos")
                        .header("X-Usuario-Id", USUARIO_ID)
                        .header("X-Rol-Id", ROL_USUARIO_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(doc)))
                .andExpect(status().isForbidden());
    }

    // ==========================================
    // TESTS DE CAMINO FELIZ
    // ==========================================

    @Test
    @DisplayName("Flujo Completo: Subir documento y luego rechazarlo con observaciones (ADMIN)")
    void flujoCompleto_SubirYRechazarDocumento() throws Exception {
        Long tipoId = crearTipoDocumento("DNI");
        Long estadoPendienteId = crearEstado("PENDIENTE");
        Long estadoRechazadoId = crearEstado("RECHAZADO");

        DocumentoDTO nuevoDoc = new DocumentoDTO();
        nuevoDoc.setNombre("carnet_identidad_frente.pdf");
        nuevoDoc.setUsuarioId(USUARIO_ID);
        nuevoDoc.setEstadoId(estadoPendienteId);
        nuevoDoc.setTipoDocId(tipoId);

        MvcResult resultDoc = mockMvc.perform(withAppKey(post("/api/documentos"))
                        .header("X-Usuario-Id", USUARIO_ID)
                        .header("X-Rol-Id", ROL_USUARIO_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nuevoDoc)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("carnet_identidad_frente.pdf"))
                .andReturn();

        Long documentoId = objectMapper.readValue(resultDoc.getResponse().getContentAsString(), DocumentoDTO.class).getId();

        ActualizarEstadoRequest requestRechazo = new ActualizarEstadoRequest();
        requestRechazo.setEstadoId(estadoRechazadoId);
        requestRechazo.setObservaciones("La imagen esta borrosa, por favor suba una foto con mejor iluminacion.");
        requestRechazo.setRevisadoPor(ADMIN_ID);

        mockMvc.perform(withAppKey(patch("/api/documentos/{id}/estado", documentoId))
                        .header("X-Usuario-Id", ADMIN_ID)
                        .header("X-Rol-Id", ROL_ADMIN_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestRechazo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estadoId").value(estadoRechazadoId))
                .andExpect(jsonPath("$.observaciones").value("La imagen esta borrosa, por favor suba una foto con mejor iluminacion."));
    }

    // ==========================================
    // TESTS DE REGLAS DE NEGOCIO Y ERRORES
    // ==========================================

    @Test
    @DisplayName("POST /api/documentos - Falla con 400 si faltan datos obligatorios")
    void crearDocumento_DatosInvalidos_Retorna400() throws Exception {
        DocumentoDTO docInvalido = new DocumentoDTO();

        mockMvc.perform(withAppKey(post("/api/documentos"))
                        .header("X-Usuario-Id", USUARIO_ID)
                        .header("X-Rol-Id", ROL_USUARIO_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(docInvalido)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/documentos - Falla con 400 si el usuario no existe en UserService")
    void crearDocumento_UsuarioNoExiste_RetornaError() throws Exception {
        when(userServiceClient.existsUser(99L)).thenReturn(false);
        when(userServiceClient.getUserById(99L)).thenReturn(null);

        Long tipoId = crearTipoDocumento("CERTIFICADO_AFP");
        Long estadoPendienteId = crearEstado("PENDIENTE");

        DocumentoDTO nuevoDoc = new DocumentoDTO();
        nuevoDoc.setNombre("afp_2026.pdf");
        nuevoDoc.setUsuarioId(99L);
        nuevoDoc.setEstadoId(estadoPendienteId);
        nuevoDoc.setTipoDocId(tipoId);

        mockMvc.perform(withAppKey(post("/api/documentos"))
                        .header("X-Usuario-Id", 99L)
                        .header("X-Rol-Id", ROL_USUARIO_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nuevoDoc)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH /api/documentos/{id}/estado - Falla con 403 si un usuario NO ADMIN intenta cambiar el estado")
    void actualizarEstado_UsuarioNoEsAdmin_RetornaError() throws Exception {
        Long tipoId = crearTipoDocumento("DNI");
        Long estadoPendienteId = crearEstado("PENDIENTE");
        Long estadoAprobadoId = crearEstado("APROBADO");

        DocumentoDTO nuevoDoc = new DocumentoDTO();
        nuevoDoc.setNombre("carnet.pdf");
        nuevoDoc.setUsuarioId(USUARIO_ID);
        nuevoDoc.setEstadoId(estadoPendienteId);
        nuevoDoc.setTipoDocId(tipoId);

        MvcResult resultDoc = mockMvc.perform(withAppKey(post("/api/documentos"))
                        .header("X-Usuario-Id", USUARIO_ID)
                        .header("X-Rol-Id", ROL_USUARIO_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nuevoDoc)))
                .andExpect(status().isCreated())
                .andReturn();

        Long documentoId = objectMapper.readValue(resultDoc.getResponse().getContentAsString(), DocumentoDTO.class).getId();

        ActualizarEstadoRequest requestTramposo = new ActualizarEstadoRequest();
        requestTramposo.setEstadoId(estadoAprobadoId);
        requestTramposo.setRevisadoPor(USUARIO_ID);

        mockMvc.perform(withAppKey(patch("/api/documentos/{id}/estado", documentoId))
                        .header("X-Usuario-Id", USUARIO_ID)
                        .header("X-Rol-Id", ROL_USUARIO_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestTramposo)))
                .andExpect(status().isForbidden());
    }
}