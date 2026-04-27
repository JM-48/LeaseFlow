package com.rentify.documentService.repository;

import com.rentify.documentService.model.Estado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para la entidad Estado.
 */
@Repository
public interface EstadoRepository extends JpaRepository<Estado, Long> {

    /**
     * Encuentra un estado por su nombre.
     */
    Optional<Estado> findByNombre(String nombre);

    /**
     * Verifica si existe un estado con el nombre dado.
     */
    boolean existsByNombre(String nombre);
}