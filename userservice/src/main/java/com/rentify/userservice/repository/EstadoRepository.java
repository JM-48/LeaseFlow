package com.rentify.userservice.repository;

import com.rentify.userservice.model.Estado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para la entidad Estado
 */
@Repository
public interface EstadoRepository extends JpaRepository<Estado, Long> {

    /**
     * Busca un estado por su nombre
     * @param nombre Nombre del estado (ACTIVO, INACTIVO, SUSPENDIDO)
     * @return Optional con el estado si existe
     */
    Optional<Estado> findByNombre(String nombre);

    /**
     * Verifica si existe un estado con el nombre dado
     * @param nombre Nombre del estado
     * @return true si existe, false en caso contrario
     */
    boolean existsByNombre(String nombre);
}