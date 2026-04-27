package com.rentify.documentService.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rentify.documentService.dto.DocumentoDTO;
import com.rentify.documentService.exception.BusinessValidationException;
import com.rentify.documentService.exception.ResourceNotFoundException;
import com.rentify.documentService.service.DocumentoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch; // Importación corregida a patch
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DocumentoController.class)
@DisplayName("Tests de Integración de DocumentoController")
class DocumentoControllerTest {

    private final String BASE_PATH = "/api/documentos"; // RUTA BASE CORREGIDA

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private DocumentoService documentoService;

    private final Long DOCUMENTO_ID = 1L;
    private final Long USUARIO_ID = 10L;
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

        mockMvc.perform(post(BASE_PATH) // Uso de BASE_PATH
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(documentoDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("DNI_Test.pdf"));
    }

    @Test
    @DisplayName("POST /api/documentos - Falla por validación de negocio (400 Bad Request)")
    void createDocumento_Fails_BusinessValidation() throws Exception {
        doThrow(new BusinessValidationException("Límite alcanzado")).when(documentoService)
                .crearDocumento(any(DocumentoDTO.class));

        mockMvc.perform(post(BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(documentoDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Límite alcanzado"));
    }

    // --- 2. GET /api/documentos/{id}: Obtener Documento por ID ---

    @Test
    @DisplayName("GET /api/documentos/{id} - Retorna documento (200 OK)")
    void getDocumentoById_Success() throws Exception {
        when(documentoService.obtenerPorId(eq(DOCUMENTO_ID), anyBoolean())).thenReturn(documentoDTO);

        mockMvc.perform(get(BASE_PATH + "/{id}", DOCUMENTO_ID)
                        .param("details", "false")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(DOCUMENTO_ID));
    }

    @Test
    @DisplayName("GET /api/documentos/{id} - Falla si no existe (404 Not Found)")
    void getDocumentoById_NotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Documento no existe")).when(documentoService)
                .obtenerPorId(eq(DOCUMENTO_ID), anyBoolean());

        mockMvc.perform(get(BASE_PATH + "/{id}", DOCUMENTO_ID)
                        .param("details", "false")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Documento no existe"));
    }

    // --- 3. GET /api/documentos/usuario/{usuarioId}: Listar por Usuario ---

    @Test
    @DisplayName("GET /api/documentos/usuario/{usuarioId} - Retorna lista vacía (200 OK)")
    void getDocumentosByUsuarioId_Empty() throws Exception {
        when(documentoService.obtenerPorUsuario(eq(USUARIO_ID), anyBoolean())).thenReturn(Collections.emptyList());

        mockMvc.perform(get(BASE_PATH + "/usuario/{usuarioId}", USUARIO_ID)
                        .param("details", "false")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    // --- 4. PATCH /api/documentos/{id}/estado/{estadoId}: Actualizar Estado (CORREGIDO) ---

    @Test
    @DisplayName("PATCH /api/documentos/{id}/estado/{estadoId} - Actualiza estado (200 OK)")
    void updateEstado_Success() throws Exception {
        Long NUEVO_ESTADO_ID = 2L;
        DocumentoDTO updatedDTO = documentoDTO.toBuilder().estadoId(NUEVO_ESTADO_ID).build();
        when(documentoService.actualizarEstado(eq(DOCUMENTO_ID), eq(NUEVO_ESTADO_ID))).thenReturn(updatedDTO);

        // USAMOS PATCH EN LUGAR DE PUT
        mockMvc.perform(patch(BASE_PATH + "/{id}/estado/{estadoId}", DOCUMENTO_ID, NUEVO_ESTADO_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estadoId").value(NUEVO_ESTADO_ID));
    }

    // --- 5. GET /api/documentos/usuario/{usuarioId}/verificar-aprobados: Verificar Aprobados (NUEVO TEST) ---

    @Test
    @DisplayName("GET /api/documentos/usuario/{usuarioId}/verificar-aprobados - Retorna true si tiene aprobados")
    void verificarDocumentosAprobados_True() throws Exception {
        when(documentoService.hasApprovedDocuments(eq(USUARIO_ID))).thenReturn(true);

        mockMvc.perform(get(BASE_PATH + "/usuario/{usuarioId}/verificar-aprobados", USUARIO_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    // --- 6. DELETE /api/documentos/{id}: Eliminar Documento ---

    @Test
    @DisplayName("DELETE /api/documentos/{id} - Elimina exitosamente (204 No Content)")
    void deleteDocumento_Success() throws Exception {
        doNothing().when(documentoService).eliminarDocumento(DOCUMENTO_ID);

        mockMvc.perform(delete(BASE_PATH + "/{id}", DOCUMENTO_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/documentos/{id} - Falla si no existe (404 Not Found)")
    void deleteDocumento_NotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Documento a eliminar no existe")).when(documentoService)
                .eliminarDocumento(DOCUMENTO_ID);

        mockMvc.perform(delete(BASE_PATH + "/{id}", DOCUMENTO_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Documento a eliminar no existe"));
    }
}