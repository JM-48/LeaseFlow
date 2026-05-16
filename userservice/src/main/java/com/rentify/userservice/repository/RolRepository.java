package com.rentify.userservice.repository;

import com.rentify.userservice.model.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para la entidad Rol
 */
@Repository
public interface RolRepository extends JpaRepository<Rol, Long> {

    /**
     * Busca un rol por su nombre
     * @param nombre Nombre del rol (ADMIN, PROPIETARIO, ARRIENDATARIO)
     * @return Optional con el rol si existe
     */
    Optional<Rol> findByNombre(String nombre);

    /**
     * Verifica si existe un rol con el nombre dado
     * @param nombre Nombre del rol
     * @return true si existe, false en caso contrario
     */
    boolean existsByNombre(String nombre);
}