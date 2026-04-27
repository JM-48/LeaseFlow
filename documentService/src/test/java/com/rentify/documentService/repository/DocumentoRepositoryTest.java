package com.rentify.documentService.repository;

import com.rentify.documentService.model.Documento;
import com.rentify.documentService.model.Estado;
import com.rentify.documentService.model.TipoDocumento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests de integración para DocumentoRepository.
 * Usa H2 en memoria y TestEntityManager para preparar datos de prueba.
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Tests de DocumentoRepository")
class DocumentoRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private DocumentoRepository documentoRepository;

    @Autowired
    private EstadoRepository estadoRepository;

    @Autowired
    private TipoDocumentoRepository tipoDocumentoRepository;

    private Estado estadoPendiente;
    private Estado estadoAceptado;
    private TipoDocumento tipoDNI;
    private TipoDocumento tipoLiquidacion;
    private Documento documento1;
    private Documento documento2;

    @BeforeEach
    void setUp() {
        // Crear estados
        estadoPendiente = Estado.builder()
                .nombre("PENDIENTE")
                .build();
        estadoPendiente = entityManager.persist(estadoPendiente);

        estadoAceptado = Estado.builder()
                .nombre("ACEPTADO")
                .build();
        estadoAceptado = entityManager.persist(estadoAceptado);

        // Crear tipos de documento
        tipoDNI = TipoDocumento.builder()
                .nombre("DNI")
                .build();
        tipoDNI = entityManager.persist(tipoDNI);

        tipoLiquidacion = TipoDocumento.builder()
                .nombre("LIQUIDACION_SUELDO")
                .build();
        tipoLiquidacion = entityManager.persist(tipoLiquidacion);

        // Crear documentos de prueba
        documento1 = Documento.builder()
                .nombre("DNI_Juan_Perez.pdf")
                .fechaSubido(new Date())
                .usuarioId(1L)
                .estadoId(estadoPendiente.getId())
                .tipoDocId(tipoDNI.getId())
                .build();
        documento1 = entityManager.persist(documento1);

        documento2 = Documento.builder()
                .nombre("Liquidacion_Enero_2025.pdf")
                .fechaSubido(new Date())
                .usuarioId(1L)
                .estadoId(estadoAceptado.getId())
                .tipoDocId(tipoLiquidacion.getId())
                .build();
        documento2 = entityManager.persist(documento2);

        entityManager.flush();
    }

    @Test
    @DisplayName("findByUsuarioId - Debería encontrar documentos del usuario")
    void findByUsuarioId_DeberiaRetornarDocumentosDelUsuario() {
        // When
        List<Documento> documentos = documentoRepository.findByUsuarioId(1L);

        // Then
        assertThat(documentos).hasSize(2);
        assertThat(documentos).extracting(Documento::getUsuarioId)
                .containsOnly(1L);
    }

    @Test
    @DisplayName("findByUsuarioId - Debería retornar lista vacía si usuario no tiene documentos")
    void findByUsuarioId_UsuarioSinDocumentos_RetornaListaVacia() {
        // When
        List<Documento> documentos = documentoRepository.findByUsuarioId(999L);

        // Then
        assertThat(documentos).isEmpty();
    }

    @Test
    @DisplayName("findByUsuarioIdAndEstadoId - Debería encontrar documentos por usuario y estado")
    void findByUsuarioIdAndEstadoId_DeberiaRetornarDocumentosFiltrados() {
        // When
        List<Documento> documentos = documentoRepository.findByUsuarioIdAndEstadoId(
                1L, estadoPendiente.getId());

        // Then
        assertThat(documentos).hasSize(1);
        assertThat(documentos.get(0).getNombre()).isEqualTo("DNI_Juan_Perez.pdf");
    }

    @Test
    @DisplayName("findByUsuarioIdAndTipoDocId - Debería encontrar documentos por usuario y tipo")
    void findByUsuarioIdAndTipoDocId_DeberiaRetornarDocumentosFiltrados() {
        // When
        List<Documento> documentos = documentoRepository.findByUsuarioIdAndTipoDocId(
                1L, tipoLiquidacion.getId());

        // Then
        assertThat(documentos).hasSize(1);
        assertThat(documentos.get(0).getNombre()).isEqualTo("Liquidacion_Enero_2025.pdf");
    }

    @Test
    @DisplayName("countByUsuarioId - Debería contar documentos del usuario")
    void countByUsuarioId_DeberiaRetornarCantidadCorrecta() {
        // When
        long count = documentoRepository.countByUsuarioId(1L);

        // Then
        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("countByUsuarioIdAndEstadoId - Debería contar documentos por usuario y estado")
    void countByUsuarioIdAndEstadoId_DeberiaRetornarCantidadCorrecta() {
        // When
        long count = documentoRepository.countByUsuarioIdAndEstadoId(
                1L, estadoPendiente.getId());

        // Then
        assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("existsByUsuarioIdAndTipoDocIdAndEstadoId - Debería verificar existencia")
    void existsByUsuarioIdAndTipoDocIdAndEstadoId_DocumentoExiste_RetornaTrue() {
        // When
        boolean exists = documentoRepository.existsByUsuarioIdAndTipoDocIdAndEstadoId(
                1L, tipoDNI.getId(), estadoPendiente.getId());

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("existsByUsuarioIdAndTipoDocIdAndEstadoId - Debería retornar false si no existe")
    void existsByUsuarioIdAndTipoDocIdAndEstadoId_DocumentoNoExiste_RetornaFalse() {
        // When
        boolean exists = documentoRepository.existsByUsuarioIdAndTipoDocIdAndEstadoId(
                999L, tipoDNI.getId(), estadoPendiente.getId());

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("save - Debería persistir documento correctamente")
    void save_DeberiaPersistirDocumento() {
        // Given
        Documento nuevo = Documento.builder()
                .nombre("Certificado_Antecedentes.pdf")
                .fechaSubido(new Date())
                .usuarioId(2L)
                .estadoId(estadoPendiente.getId())
                .tipoDocId(tipoDNI.getId())
                .build();

        // When
        Documento saved = documentoRepository.save(nuevo);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(documentoRepository.findById(saved.getId())).isPresent();
    }

    @Test
    @DisplayName("save - @PrePersist debería establecer fecha si es null")
    void save_PrePersist_DeberiaEstablecerFecha() {
        // Given
        Documento nuevo = Documento.builder()
                .nombre("Test.pdf")
                .usuarioId(2L)
                .estadoId(estadoPendiente.getId())
                .tipoDocId(tipoDNI.getId())
                .build();
        // No establecemos fechaSubido

        // When
        Documento saved = documentoRepository.save(nuevo);
        entityManager.flush();
        entityManager.clear();

        // Then
        Documento found = documentoRepository.findById(saved.getId()).orElseThrow();
        assertThat(found.getFechaSubido()).isNotNull();
    }

    @Test
    @DisplayName("findDocumentosPendientesByUsuario - Debería encontrar solo documentos pendientes")
    void findDocumentosPendientesByUsuario_DeberiaRetornarSoloPendientes() {
        // When
        List<Documento> documentos = documentoRepository.findDocumentosPendientesByUsuario(1L);

        // Then
        assertThat(documentos).hasSize(1);
        assertThat(documentos.get(0).getNombre()).isEqualTo("DNI_Juan_Perez.pdf");
    }

    @Test
    @DisplayName("findDocumentosAceptadosByUsuario - Debería encontrar solo documentos aceptados")
    void findDocumentosAceptadosByUsuario_DeberiaRetornarSoloAceptados() {
        // When
        List<Documento> documentos = documentoRepository.findDocumentosAceptadosByUsuario(1L);

        // Then
        assertThat(documentos).hasSize(1);
        assertThat(documentos.get(0).getNombre()).isEqualTo("Liquidacion_Enero_2025.pdf");
    }
}