package com.rentify.reviewService.repository;

import com.rentify.reviewService.model.TipoResena;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para operaciones de base de datos de TipoResena.
 */
@Repository
public interface TipoResenaRepository extends JpaRepository<TipoResena, Long> {

    /**
     * Busca un tipo de reseña por su nombre.
     * @param nombre nombre del tipo de reseña
     * @return Optional con el tipo de reseña si existe
     */
    Optional<TipoResena> findByNombre(String nombre);

    /**
     * Verifica si existe un tipo de reseña con el nombre dado.
     * @param nombre nombre del tipo de reseña
     * @return true si existe, false en caso contrario
     */
    boolean existsByNombre(String nombre);
}