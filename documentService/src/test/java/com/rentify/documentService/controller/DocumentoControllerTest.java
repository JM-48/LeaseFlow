package com.rentify.documentService.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rentify.documentService.dto.ActualizarEstadoRequest;
import com.rentify.documentService.dto.DocumentoDTO;
import com.rentify.documentService.exception.ResourceNotFoundException;
import com.rentify.documentService.service.DocumentoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DocumentoController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("Tests de Integración de DocumentoController")
class DocumentoControllerTest {

    private final String BASE_PATH = "/api/documentos";

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private DocumentoService documentoService;

    private final Long DOCUMENTO_ID = 1L;
    private final Long USUARIO_ID = 10L;
    private final Long ROL_ADMIN_ID = 1L;
    private final Long ROL_USER_ID = 2L;
    private DocumentoDTO documentoDTO;

    @BeforeEach
    void setUp() {
        documentoDTO = DocumentoDTO.builder()
                .id(DOCUMENTO_ID)
                .nombre("DNI_Test.pdf")
                .usuarioId(USUARIO_ID)
                .estadoId(1L)
                .tipoDocId(1L)
                .fechaSubido(new Date())
                .build();
    }

    // --- 1. POST /api/documentos: Crear Documento ---

    @Test
    @DisplayName("POST /api/documentos - Crea documento exitosamente (201 Created)")
    void createDocumento_Success() throws Exception {
        when(documentoService.crearDocumento(any(DocumentoDTO.class))).thenReturn(documentoDTO);

        mockMvc.perform(post(BASE_PATH)
                        .header("X-Usuario-Id", USUARIO_ID) // <--- Cabecera obligatoria
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(documentoDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("DNI_Test.pdf"));
    }

    @Test
    @DisplayName("POST /api/documentos - Falla si no hay Usuario-Id en cabecera (401 Unauthorized)")
    void createDocumento_Fails_Unauthorized() throws Exception {
        mockMvc.perform(post(BASE_PATH)
                        // Omitimos la cabecera X-Usuario-Id a propósito
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(documentoDTO)))
                .andExpect(status().isUnauthorized());
    }

    // --- 2. GET /api/documentos: Listar Todos (Solo Admin) ---

    @Test
    @DisplayName("GET /api/documentos - Falla sin header de rol (403 Forbidden)")
    void listarTodos_SinRol_Returns403() throws Exception {
        mockMvc.perform(get(BASE_PATH)
                        .param("includeDetails", "false"))
                .andExpect(status().isForbidden());

        verify(documentoService, never()).listarTodos(anyBoolean());
    }

    @Test
    @DisplayName("GET /api/documentos - Falla si el rol no es ADMIN (403 Forbidden)")
    void listarTodos_NoAdmin_Returns403() throws Exception {
        mockMvc.perform(get(BASE_PATH)
                        .header("X-Rol-Id", ROL_USER_ID)
                        .param("includeDetails", "false"))
                .andExpect(status().isForbidden());

        verify(documentoService, never()).listarTodos(anyBoolean());
    }

    @Test
    @DisplayName("GET /api/documentos - Un ADMIN puede listar todos los documentos (200 OK)")
    void listarTodos_Admin_Returns200() throws Exception {
        when(documentoService.listarTodos(false)).thenReturn(List.of(documentoDTO));

        mockMvc.perform(get(BASE_PATH)
                        .header("X-Rol-Id", ROL_ADMIN_ID)
                        .param("includeDetails", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(DOCUMENTO_ID));

        verify(documentoService).listarTodos(false);
    }

    // --- 3. GET /api/documentos/{id}: Obtener Documento por ID ---

    @Test
    @DisplayName("GET /api/documentos/{id} - Retorna documento si es el dueño (200 OK)")
    void getDocumentoById_Success() throws Exception {
        when(documentoService.obtenerPorId(eq(DOCUMENTO_ID), anyBoolean())).thenReturn(documentoDTO);

        mockMvc.perform(get(BASE_PATH + "/{id}", DOCUMENTO_ID)
                        .header("X-Usuario-Id", USUARIO_ID) // Es el dueño (10L)
                        .header("X-Rol-Id", ROL_USER_ID)
                        .param("includeDetails", "false")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(DOCUMENTO_ID));
    }

    @Test
    @DisplayName("GET /api/documentos/{id} - Falla si intenta ver documento ajeno (403 Forbidden)")
    void getDocumentoById_Fails_Forbidden() throws Exception {
        when(documentoService.obtenerPorId(eq(DOCUMENTO_ID), anyBoolean())).thenReturn(documentoDTO);

        mockMvc.perform(get(BASE_PATH + "/{id}", DOCUMENTO_ID)
                        .header("X-Usuario-Id", 99L) // NO es el dueño
                        .header("X-Rol-Id", ROL_USER_ID)
                        .param("includeDetails", "false")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/documentos/{id} - Un ADMIN puede ver cualquier documento (200 OK)")
    void getDocumentoById_Admin_Returns200() throws Exception {
        when(documentoService.obtenerPorId(eq(DOCUMENTO_ID), anyBoolean())).thenReturn(documentoDTO);

        mockMvc.perform(get(BASE_PATH + "/{id}", DOCUMENTO_ID)
                        .header("X-Usuario-Id", 99L) // No es el dueño, pero es admin
                        .header("X-Rol-Id", ROL_ADMIN_ID)
                        .param("includeDetails", "false")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(DOCUMENTO_ID));
    }

    // --- 4. GET /api/documentos/usuario/{usuarioId}: Listar por Usuario ---

    @Test
    @DisplayName("GET /api/documentos/usuario/{usuarioId} - Retorna lista (200 OK)")
    void getDocumentosByUsuarioId_Success() throws Exception {
        when(documentoService.obtenerPorUsuario(eq(USUARIO_ID), anyBoolean())).thenReturn(Collections.emptyList());

        mockMvc.perform(get(BASE_PATH + "/usuario/{usuarioId}", USUARIO_ID)
                        .header("X-Usuario-Id", USUARIO_ID)
                        .header("X-Rol-Id", ROL_USER_ID)
                        .param("includeDetails", "false")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    @DisplayName("GET /api/documentos/usuario/{usuarioId} - Falla si un usuario consulta a otro (403 Forbidden)")
    void getDocumentosByUsuarioId_UsuarioAjeno_Returns403() throws Exception {
        mockMvc.perform(get(BASE_PATH + "/usuario/{usuarioId}", USUARIO_ID)
                        .header("X-Usuario-Id", 99L) // No es el mismo usuario
                        .header("X-Rol-Id", ROL_USER_ID)
                        .param("includeDetails", "false")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        verify(documentoService, never()).obtenerPorUsuario(any(), anyBoolean());
    }

    @Test
    @DisplayName("GET /api/documentos/usuario/{usuarioId} - Un ADMIN puede consultar a cualquier usuario (200 OK)")
    void getDocumentosByUsuarioId_Admin_Returns200() throws Exception {
        when(documentoService.obtenerPorUsuario(eq(USUARIO_ID), anyBoolean())).thenReturn(Collections.emptyList());

        mockMvc.perform(get(BASE_PATH + "/usuario/{usuarioId}", USUARIO_ID)
                        .header("X-Usuario-Id", 99L)
                        .header("X-Rol-Id", ROL_ADMIN_ID)
                        .param("includeDetails", "false")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    // --- 5. PATCH /api/documentos/{id}/estado/{estadoId}: Actualizar Estado ---

    @Test
    @DisplayName("PATCH /api/documentos/{id}/estado/{estadoId} - Actualiza estado si es ADMIN (200 OK)")
    void updateEstado_Success() throws Exception {
        Long NUEVO_ESTADO_ID = 2L;
        DocumentoDTO updatedDTO = documentoDTO.toBuilder().estadoId(NUEVO_ESTADO_ID).build();
        when(documentoService.actualizarEstado(eq(DOCUMENTO_ID), eq(NUEVO_ESTADO_ID))).thenReturn(updatedDTO);

        mockMvc.perform(patch(BASE_PATH + "/{id}/estado/{estadoId}", DOCUMENTO_ID, NUEVO_ESTADO_ID)
                        .header("X-Rol-Id", ROL_ADMIN_ID) // Tiene que ser ADMIN (1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estadoId").value(NUEVO_ESTADO_ID));
    }

    @Test
    @DisplayName("PATCH /api/documentos/{id}/estado/{estadoId} - Falla si no es ADMIN (403 Forbidden)")
    void updateEstado_NoAdmin_Returns403() throws Exception {
        mockMvc.perform(patch(BASE_PATH + "/{id}/estado/{estadoId}", DOCUMENTO_ID, 2L)
                        .header("X-Rol-Id", ROL_USER_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        verify(documentoService, never()).actualizarEstado(any(), any());
    }

    @Test
    @DisplayName("PATCH /api/documentos/{id}/estado/{estadoId} - Falla sin header de rol (403 Forbidden)")
    void updateEstado_SinRol_Returns403() throws Exception {
        mockMvc.perform(patch(BASE_PATH + "/{id}/estado/{estadoId}", DOCUMENTO_ID, 2L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        verify(documentoService, never()).actualizarEstado(any(), any());
    }

    // --- 6. PATCH /api/documentos/{id}/estado: Actualizar Estado con Observaciones ---

    @Test
    @DisplayName("PATCH /api/documentos/{id}/estado - Falla si no es ADMIN (403 Forbidden)")
    void updateEstadoConObservaciones_NoAdmin_Returns403() throws Exception {
        ActualizarEstadoRequest request = new ActualizarEstadoRequest();
        request.setEstadoId(3L);
        request.setObservaciones("Documento ilegible");

        mockMvc.perform(patch(BASE_PATH + "/{id}/estado", DOCUMENTO_ID)
                        .header("X-Rol-Id", ROL_USER_ID)
                        .header("X-Usuario-Id", USUARIO_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verify(documentoService, never()).actualizarEstadoConObservaciones(any(), any());
    }

    @Test
    @DisplayName("PATCH /api/documentos/{id}/estado - Admin actualiza y se fuerza revisadoPor desde el header")
    void updateEstadoConObservaciones_Admin_Returns200() throws Exception {
        Long adminId = 5L;
        ActualizarEstadoRequest request = new ActualizarEstadoRequest();
        request.setEstadoId(3L);
        request.setObservaciones("Documento ilegible, vuelva a subirlo");

        DocumentoDTO updatedDTO = documentoDTO.toBuilder()
                .estadoId(3L)
                .observaciones("Documento ilegible, vuelva a subirlo")
                .revisadoPor(adminId)
                .build();

        when(documentoService.actualizarEstadoConObservaciones(eq(DOCUMENTO_ID), any(ActualizarEstadoRequest.class)))
                .thenReturn(updatedDTO);

        mockMvc.perform(patch(BASE_PATH + "/{id}/estado", DOCUMENTO_ID)
                        .header("X-Rol-Id", ROL_ADMIN_ID)
                        .header("X-Usuario-Id", adminId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estadoId").value(3))
                .andExpect(jsonPath("$.revisadoPor").value(adminId));
    }

    // --- 7. GET /api/documentos/usuario/{usuarioId}/verificar-aprobados ---

    @Test
    @DisplayName("GET /api/documentos/usuario/{usuarioId}/verificar-aprobados - Retorna true")
    void verificarDocumentosAprobados_True() throws Exception {
        when(documentoService.hasApprovedDocuments(eq(USUARIO_ID))).thenReturn(true);

        mockMvc.perform(get(BASE_PATH + "/usuario/{usuarioId}/verificar-aprobados", USUARIO_ID)
                        .header("X-Usuario-Id", USUARIO_ID)
                        .header("X-Rol-Id", ROL_USER_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    @DisplayName("GET /api/documentos/usuario/{usuarioId}/verificar-aprobados - Falla si consulta a otro usuario (403)")
    void verificarDocumentosAprobados_UsuarioAjeno_Returns403() throws Exception {
        mockMvc.perform(get(BASE_PATH + "/usuario/{usuarioId}/verificar-aprobados", USUARIO_ID)
                        .header("X-Usuario-Id", 99L)
                        .header("X-Rol-Id", ROL_USER_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        verify(documentoService, never()).hasApprovedDocuments(any());
    }

    // --- 8. GET /api/documentos/usuario/{usuarioId}/aprobados (DEPRECATED) ---

    @Test
    @DisplayName("GET /api/documentos/usuario/{usuarioId}/aprobados (deprecated) - Retorna true para el dueño")
    void hasApprovedDocumentsDeprecated_Propietario_Returns200() throws Exception {
        when(documentoService.hasApprovedDocuments(eq(USUARIO_ID))).thenReturn(true);

        mockMvc.perform(get(BASE_PATH + "/usuario/{usuarioId}/aprobados", USUARIO_ID)
                        .header("X-Usuario-Id", USUARIO_ID)
                        .header("X-Rol-Id", ROL_USER_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    @DisplayName("GET /api/documentos/usuario/{usuarioId}/aprobados (deprecated) - Falla si consulta a otro usuario (403)")
    void hasApprovedDocumentsDeprecated_UsuarioAjeno_Returns403() throws Exception {
        mockMvc.perform(get(BASE_PATH + "/usuario/{usuarioId}/aprobados", USUARIO_ID)
                        .header("X-Usuario-Id", 99L)
                        .header("X-Rol-Id", ROL_USER_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        verify(documentoService, never()).hasApprovedDocuments(any());
    }

    // --- 9. DELETE /api/documentos/{id}: Eliminar Documento ---

    @Test
    @DisplayName("DELETE /api/documentos/{id} - Elimina exitosamente siendo el dueño (204 No Content)")
    void deleteDocumento_Success() throws Exception {
        // MOCK CLAVE: El controlador primero busca el documento para saber si le pertenece
        when(documentoService.obtenerPorId(eq(DOCUMENTO_ID), eq(false))).thenReturn(documentoDTO);
        doNothing().when(documentoService).eliminarDocumento(DOCUMENTO_ID);

        mockMvc.perform(delete(BASE_PATH + "/{id}", DOCUMENTO_ID)
                        .header("X-Usuario-Id", USUARIO_ID) // Es el dueño
                        .header("X-Rol-Id", ROL_USER_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/documentos/{id} - Falla si no existe (404 Not Found)")
    void deleteDocumento_NotFound() throws Exception {
        // MOCK CLAVE: Hacemos que la primera búsqueda falle simulando que no existe en BD
        doThrow(new ResourceNotFoundException("Documento no encontrado")).when(documentoService)
                .obtenerPorId(eq(DOCUMENTO_ID), eq(false));

        mockMvc.perform(delete(BASE_PATH + "/{id}", DOCUMENTO_ID)
                        .header("X-Usuario-Id", USUARIO_ID)
                        .header("X-Rol-Id", ROL_USER_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Documento no encontrado"));
    }

    @Test
    @DisplayName("DELETE /api/documentos/{id} - Falla si intenta eliminar documento ajeno (403 Forbidden)")
    void deleteDocumento_DocumentoAjeno_Returns403() throws Exception {
        when(documentoService.obtenerPorId(eq(DOCUMENTO_ID), eq(false))).thenReturn(documentoDTO);

        mockMvc.perform(delete(BASE_PATH + "/{id}", DOCUMENTO_ID)
                        .header("X-Usuario-Id", 99L) // NO es el dueño (el dueño real es 10L)
                        .header("X-Rol-Id", ROL_USER_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        verify(documentoService, never()).eliminarDocumento(any());
    }

    @Test
    @DisplayName("DELETE /api/documentos/{id} - Un ADMIN puede eliminar documentos ajenos (204 No Content)")
    void deleteDocumento_Admin_Returns204() throws Exception {
        when(documentoService.obtenerPorId(eq(DOCUMENTO_ID), eq(false))).thenReturn(documentoDTO);
        doNothing().when(documentoService).eliminarDocumento(DOCUMENTO_ID);

        mockMvc.perform(delete(BASE_PATH + "/{id}", DOCUMENTO_ID)
                        .header("X-Usuario-Id", 99L) // No es el dueño, pero es admin
                        .header("X-Rol-Id", ROL_ADMIN_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }
}