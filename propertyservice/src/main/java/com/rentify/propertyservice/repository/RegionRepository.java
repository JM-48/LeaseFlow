package com.rentify.propertyservice.repository;

import com.rentify.propertyservice.model.Region;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para operaciones con Region.
 */
@Repository
public interface RegionRepository extends JpaRepository<Region, Long> {

    /**
     * Busca una región por nombre.
     */
    Optional<Region> findByNombre(String nombre);

    /**
     * Verifica si existe una región con el nombre dado.
     */
    boolean existsByNombre(String nombre);
}