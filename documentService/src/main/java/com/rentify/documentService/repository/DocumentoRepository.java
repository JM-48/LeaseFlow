package com.rentify.documentService.repository;

import com.rentify.documentService.model.Documento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para la entidad Documento.
 * Proporciona métodos de acceso a datos de documentos.
 */
@Repository
public interface DocumentoRepository extends JpaRepository<Documento, Long> {

    /**
     * Encuentra todos los documentos de un usuario.
     */
    List<Documento> findByUsuarioId(Long usuarioId);

    /**
     * Encuentra documentos por usuario y estado.
     */
    List<Documento> findByUsuarioIdAndEstadoId(Long usuarioId, Long estadoId);

    /**
     * Encuentra documentos por usuario y tipo de documento.
     */
    List<Documento> findByUsuarioIdAndTipoDocId(Long usuarioId, Long tipoDocId);

    /**
     * Cuenta documentos de un usuario.
     */
    long countByUsuarioId(Long usuarioId);

    /**
     * Cuenta documentos de un usuario con un estado específico.
     */
    long countByUsuarioIdAndEstadoId(Long usuarioId, Long estadoId);

    /**
     * Verifica si existe un documento con usuario, tipo y estado específicos.
     */
    boolean existsByUsuarioIdAndTipoDocIdAndEstadoId(Long usuarioId, Long tipoDocId, Long estadoId);

    /**
     * Consulta personalizada: documentos pendientes de un usuario.
     */
    @Query("SELECT d FROM Documento d WHERE d.usuarioId = :usuarioId AND d.estadoId = " +
            "(SELECT e.id FROM Estado e WHERE e.nombre = 'PENDIENTE')")
    List<Documento> findDocumentosPendientesByUsuario(@Param("usuarioId") Long usuarioId);

    /**
     * Consulta personalizada: documentos aceptados de un usuario.
     */
    @Query("SELECT d FROM Documento d WHERE d.usuarioId = :usuarioId AND d.estadoId = " +
            "(SELECT e.id FROM Estado e WHERE e.nombre = 'ACEPTADO')")
    List<Documento> findDocumentosAceptadosByUsuario(@Param("usuarioId") Long usuarioId);
}