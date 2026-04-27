package com.rentify.propertyservice.repository;

import com.rentify.propertyservice.model.Tipo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para operaciones con Tipo.
 */
@Repository
public interface TipoRepository extends JpaRepository<Tipo, Long> {

    /**
     * Busca un tipo por nombre.
     */
    Optional<Tipo> findByNombre(String nombre);

    /**
     * Verifica si existe un tipo con el nombre dado.
     */
    boolean existsByNombre(String nombre);
}