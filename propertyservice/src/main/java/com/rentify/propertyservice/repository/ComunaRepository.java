package com.rentify.propertyservice.repository;

import com.rentify.propertyservice.model.Comuna;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para operaciones con Comuna.
 */
@Repository
public interface ComunaRepository extends JpaRepository<Comuna, Long> {

    /**
     * Busca una comuna por nombre.
     */
    Optional<Comuna> findByNombre(String nombre);

    /**
     * Busca todas las comunas de una región.
     */
    List<Comuna> findByRegionId(Long regionId);

    /**
     * Verifica si existe una comuna con el nombre dado.
     */
    boolean existsByNombre(String nombre);

    /**
     * Cuenta comunas por región.
     */
    long countByRegionId(Long regionId);
}