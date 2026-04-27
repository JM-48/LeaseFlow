package com.rentify.propertyservice.repository;

import com.rentify.propertyservice.model.Foto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para operaciones con Foto.
 */
@Repository
public interface FotoRepository extends JpaRepository<Foto, Long> {

    /**
     * Busca todas las fotos de una propiedad.
     */
    List<Foto> findByPropertyId(Long propertyId);

    /**
     * Busca todas las fotos de una propiedad ordenadas por sortOrder.
     */
    List<Foto> findByPropertyIdOrderBySortOrderAsc(Long propertyId);

    /**
     * Cuenta las fotos de una propiedad.
     */
    long countByPropertyId(Long propertyId);

    /**
     * Elimina todas las fotos de una propiedad.
     */
    void deleteByPropertyId(Long propertyId);

    /**
     * Obtiene el último sortOrder de las fotos de una propiedad.
     */
    @Query("SELECT MAX(f.sortOrder) FROM Foto f WHERE f.property.id = :propertyId")
    Integer findMaxSortOrderByPropertyId(@Param("propertyId") Long propertyId);
}