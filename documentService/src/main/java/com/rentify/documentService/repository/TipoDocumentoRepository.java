package com.rentify.documentService.repository;

import com.rentify.documentService.model.TipoDocumento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para la entidad TipoDocumento.
 */
@Repository
public interface TipoDocumentoRepository extends JpaRepository<TipoDocumento, Long> {

    /**
     * Encuentra un tipo de documento por su nombre.
     */
    Optional<TipoDocumento> findByNombre(String nombre);

    /**
     * Verifica si existe un tipo de documento con el nombre dado.
     */
    boolean existsByNombre(String nombre);
}