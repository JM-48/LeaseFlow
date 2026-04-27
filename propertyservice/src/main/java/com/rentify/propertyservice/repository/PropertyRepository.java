package com.rentify.propertyservice.repository;

import com.rentify.propertyservice.model.Property;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para operaciones con Property.
 */
@Repository
public interface PropertyRepository extends JpaRepository<Property, Long> {

    /**
     * Busca una propiedad por su código único.
     */
    Optional<Property> findByCodigo(String codigo);

    /**
     * Verifica si existe una propiedad con el código dado.
     */
    boolean existsByCodigo(String codigo);

    /**
     * Busca propiedades por comuna.
     */
    List<Property> findByComunaId(Long comunaId);

    /**
     * Busca propiedades por tipo.
     */
    List<Property> findByTipoId(Long tipoId);

    /**
     * Busca propiedades que aceptan mascotas.
     */
    List<Property> findByPetFriendly(Boolean petFriendly);

    /**
     * Busca propiedades por rango de precio.
     */
    @Query("SELECT p FROM Property p WHERE p.precioMensual BETWEEN :minPrecio AND :maxPrecio")
    List<Property> findByPrecioRange(
            @Param("minPrecio") BigDecimal minPrecio,
            @Param("maxPrecio") BigDecimal maxPrecio
    );

    /**
     * Busca propiedades por número de habitaciones.
     * ✅ USAR @Query EXPLÍCITA para evitar problemas con Spring Data JPA
     */
    @Query("SELECT p FROM Property p WHERE p.nHabit = :nHabit")
    List<Property> findByNHabit(@Param("nHabit") Integer nHabit);

    /**
     * Busca propiedades por número de baños.
     * ✅ USAR @Query EXPLÍCITA para evitar problemas con Spring Data JPA
     */
    @Query("SELECT p FROM Property p WHERE p.nBanos = :nBanos")
    List<Property> findByNBanos(@Param("nBanos") Integer nBanos);

    /**
     * Busca propiedades con filtros combinados.
     */
    @Query("SELECT p FROM Property p WHERE " +
            "(:comunaId IS NULL OR p.comuna.id = :comunaId) AND " +
            "(:tipoId IS NULL OR p.tipo.id = :tipoId) AND " +
            "(:minPrecio IS NULL OR p.precioMensual >= :minPrecio) AND " +
            "(:maxPrecio IS NULL OR p.precioMensual <= :maxPrecio) AND " +
            "(:nHabit IS NULL OR p.nHabit = :nHabit) AND " +
            "(:nBanos IS NULL OR p.nBanos = :nBanos) AND " +
            "(:petFriendly IS NULL OR p.petFriendly = :petFriendly)")
    List<Property> findByFilters(
            @Param("comunaId") Long comunaId,
            @Param("tipoId") Long tipoId,
            @Param("minPrecio") BigDecimal minPrecio,
            @Param("maxPrecio") BigDecimal maxPrecio,
            @Param("nHabit") Integer nHabit,
            @Param("nBanos") Integer nBanos,
            @Param("petFriendly") Boolean petFriendly
    );

    /**
     * Cuenta propiedades por comuna.
     */
    long countByComunaId(Long comunaId);

    /**
     * Cuenta propiedades por tipo.
     */
    long countByTipoId(Long tipoId);
}