package com.rentify.documentService.service;

import com.rentify.documentService.client.UserServiceClient;
import com.rentify.documentService.dto.DocumentoDTO;
import com.rentify.documentService.dto.external.UsuarioDTO;
import com.rentify.documentService.exception.BusinessValidationException;
import com.rentify.documentService.exception.ResourceNotFoundException;
import com.rentify.documentService.model.Documento;
import com.rentify.documentService.model.Estado;
import com.rentify.documentService.model.TipoDocumento;
import com.rentify.documentService.repository.DocumentoRepository;
import com.rentify.documentService.repository.EstadoRepository;
import com.rentify.documentService.repository.TipoDocumentoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.rentify.documentService.constants.DocumentConstants.Limites;
import static com.rentify.documentService.constants.DocumentConstants.Roles;
import static com.rentify.documentService.constants.DocumentConstants.EstadoDocumento;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests Unitarios de DocumentoService")
class DocumentoServiceTest {

    // Dependencias a simular (Mocks)
    @Mock private DocumentoRepository documentoRepository;
    @Mock private EstadoRepository estadoRepository;
    @Mock private TipoDocumentoRepository tipoDocumentoRepository;
    @Mock private UserServiceClient userServiceClient;
    @Mock private ModelMapper modelMapper;

    // Instancia de la clase a probar, inyectando los mocks
    @InjectMocks private DocumentoService documentoService;

    // Constantes y datos de prueba
    private final Long USUARIO_ID = 1L;
    private final Long DOCUMENTO_ID = 5L;
    private final Long ESTADO_ID = 10L;
    private final Long TIPO_DOC_ID = 20L;

    private DocumentoDTO documentoDTO;
    private Documento documentoEntidad;
    private UsuarioDTO usuarioArrendatario;
    private Estado estadoPendiente;
    private TipoDocumento tipoDNI;

    @BeforeEach
    void setUp() {
        // 1. DTO de entrada
        documentoDTO = DocumentoDTO.builder()
                .usuarioId(USUARIO_ID).estadoId(ESTADO_ID).tipoDocId(TIPO_DOC_ID)
                .nombre("Test_Doc.pdf").build();

        // 2. Entidad de usuario (con rol autorizado: ARRIENDATARIO)
        // **Ajuste:** Usamos ARRIENDATARIO (con 'i') para coincidir con DocumentConstants.
        usuarioArrendatario = UsuarioDTO.builder()
                .id(USUARIO_ID)
                .rol(new UsuarioDTO.RolDTO(1L, Roles.ARRIENDATARIO))
                .build();

        // 3. Entidades de soporte
        estadoPendiente = Estado.builder().id(ESTADO_ID).nombre(EstadoDocumento.PENDIENTE).build();
        tipoDNI = TipoDocumento.builder().id(TIPO_DOC_ID).nombre("DNI").build();

        // 4. Entidad que se espera guardar
        documentoEntidad = Documento.builder()
                .id(DOCUMENTO_ID)
                .usuarioId(USUARIO_ID)
                .estadoId(ESTADO_ID)
                .tipoDocId(TIPO_DOC_ID)
                .nombre("Test_Doc.pdf")
                .build();

        // Configuración general para ModelMapper (mapea entidad -> DTO)
        lenient().when(modelMapper.map(any(Documento.class), eq(DocumentoDTO.class))).thenReturn(documentoDTO);
    }

    // --- 1. Tests para crearDocumento (Lógica de validación) ---

    @Test
    @DisplayName("crearDocumento: Crea exitosamente un documento válido")
    void crearDocumento_Exitoso() {
        // GIVEN
        when(userServiceClient.getUserById(USUARIO_ID)).thenReturn(usuarioArrendatario);
        when(documentoRepository.countByUsuarioId(USUARIO_ID)).thenReturn(0L);
        when(estadoRepository.findById(ESTADO_ID)).thenReturn(Optional.of(estadoPendiente));
        when(tipoDocumentoRepository.findById(TIPO_DOC_ID)).thenReturn(Optional.of(tipoDNI));
        when(documentoRepository.save(any(Documento.class))).thenReturn(documentoEntidad);

        // WHEN
        DocumentoDTO resultado = documentoService.crearDocumento(documentoDTO);

        // THEN
        assertNotNull(resultado);
        assertEquals(USUARIO_ID, resultado.getUsuarioId());
        verify(documentoRepository, times(1)).save(any(Documento.class));
    }

    @Test
    @DisplayName("crearDocumento: Falla si el rol del usuario no puede subir documentos")
    void crearDocumento_Falla_RolNoAutorizado() {
        // GIVEN
        // **Ajuste:** Usamos un rol no autorizado como SUPERVISOR
        usuarioArrendatario.getRol().setNombre("SUPERVISOR");
        when(userServiceClient.getUserById(USUARIO_ID)).thenReturn(usuarioArrendatario);

        // WHEN / THEN
        assertThrows(BusinessValidationException.class, () ->
                documentoService.crearDocumento(documentoDTO));

        verify(documentoRepository, never()).save(any());
    }

    @Test
    @DisplayName("crearDocumento: Falla si alcanza el límite de documentos")
    void crearDocumento_Falla_LimiteAlcanzado() {
        // GIVEN
        when(userServiceClient.getUserById(USUARIO_ID)).thenReturn(usuarioArrendatario);
        // **Ajuste:** Simular que ya tiene el máximo permitido (10)
        when(documentoRepository.countByUsuarioId(USUARIO_ID)).thenReturn((long) Limites.MAX_DOCUMENTOS_POR_USUARIO);

        // WHEN / THEN
        assertThrows(BusinessValidationException.class, () ->
                documentoService.crearDocumento(documentoDTO));

        verify(estadoRepository, never()).findById(anyLong());
    }

    // --- 2. Tests para obtenerPorId ---

    @Test
    @DisplayName("obtenerPorId: Retorna documento si existe")
    void obtenerPorId_Existe() {
        // GIVEN
        when(documentoRepository.findById(DOCUMENTO_ID)).thenReturn(Optional.of(documentoEntidad));

        // WHEN
        DocumentoDTO resultado = documentoService.obtenerPorId(DOCUMENTO_ID, false);

        // THEN
        assertNotNull(resultado);
        verify(documentoRepository, times(1)).findById(DOCUMENTO_ID);
    }

    @Test
    @DisplayName("obtenerPorId: Lanza ResourceNotFoundException si no existe")
    void obtenerPorId_NoExiste() {
        // GIVEN
        when(documentoRepository.findById(DOCUMENTO_ID)).thenReturn(Optional.empty());

        // WHEN / THEN
        assertThrows(ResourceNotFoundException.class, () -> {
            documentoService.obtenerPorId(DOCUMENTO_ID, false);
        });
    }

    // --- 3. Tests para actualizarEstado ---

    @Test
    @DisplayName("actualizarEstado: Actualiza el estado exitosamente")
    void actualizarEstado_Exitoso() {
        // GIVEN
        Long NUEVO_ESTADO_ID = 11L;
        Estado nuevoEstado = Estado.builder().id(NUEVO_ESTADO_ID).nombre(EstadoDocumento.RECHAZADO).build();

        when(documentoRepository.findById(DOCUMENTO_ID)).thenReturn(Optional.of(documentoEntidad));
        when(estadoRepository.findById(NUEVO_ESTADO_ID)).thenReturn(Optional.of(nuevoEstado));
        when(documentoRepository.save(any(Documento.class))).thenReturn(documentoEntidad);

        // WHEN
        documentoService.actualizarEstado(DOCUMENTO_ID, NUEVO_ESTADO_ID);

        // THEN
        // Verificar que el estadoId de la entidad fue modificado a NUEVO_ESTADO_ID antes de guardarse
        verify(documentoRepository).save(argThat(d -> d.getEstadoId().equals(NUEVO_ESTADO_ID)));
    }

    @Test
    @DisplayName("actualizarEstado: Falla si documento no existe")
    void actualizarEstado_Falla_DocumentoNoExiste() {
        // GIVEN
        when(documentoRepository.findById(DOCUMENTO_ID)).thenReturn(Optional.empty());

        // WHEN / THEN
        assertThrows(ResourceNotFoundException.class, () ->
                documentoService.actualizarEstado(DOCUMENTO_ID, 11L));
    }

    // --- 4. Tests para hasApprovedDocuments ---

    @Test
    @DisplayName("hasApprovedDocuments: Retorna true si hay documentos aprobados")
    void hasApprovedDocuments_True() {
        // GIVEN
        Long ESTADO_ACEPTADO_ID = 100L;
        Estado estadoAceptado = Estado.builder().id(ESTADO_ACEPTADO_ID).nombre(EstadoDocumento.ACEPTADO).build();

        when(estadoRepository.findByNombre(EstadoDocumento.ACEPTADO)).thenReturn(Optional.of(estadoAceptado));
        when(documentoRepository.countByUsuarioIdAndEstadoId(USUARIO_ID, ESTADO_ACEPTADO_ID)).thenReturn(1L);

        // WHEN / THEN
        assertTrue(documentoService.hasApprovedDocuments(USUARIO_ID));
    }

    @Test
    @DisplayName("hasApprovedDocuments: Retorna false si no hay documentos aprobados")
    void hasApprovedDocuments_False() {
        // GIVEN
        Long ESTADO_ACEPTADO_ID = 100L;
        Estado estadoAceptado = Estado.builder().id(ESTADO_ACEPTADO_ID).nombre(EstadoDocumento.ACEPTADO).build();

        when(estadoRepository.findByNombre(EstadoDocumento.ACEPTADO)).thenReturn(Optional.of(estadoAceptado));
        when(documentoRepository.countByUsuarioIdAndEstadoId(USUARIO_ID, ESTADO_ACEPTADO_ID)).thenReturn(0L);

        // WHEN / THEN
        assertFalse(documentoService.hasApprovedDocuments(USUARIO_ID));
    }

    // --- 5. Tests para eliminarDocumento ---

    @Test
    @DisplayName("eliminarDocumento: Elimina exitosamente el documento")
    void eliminarDocumento_Exitoso() {
        // GIVEN
        when(documentoRepository.existsById(DOCUMENTO_ID)).thenReturn(true);

        // WHEN
        documentoService.eliminarDocumento(DOCUMENTO_ID);

        // THEN
        verify(documentoRepository, times(1)).deleteById(DOCUMENTO_ID);
    }
}