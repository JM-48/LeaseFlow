package com.rentify.userservice.repository;

import com.rentify.userservice.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad Usuario
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /**
     * Busca un usuario por su email
     * @param email Email del usuario
     * @return Optional con el usuario si existe
     */
    Optional<Usuario> findByEmail(String email);

    /**
     * Busca un usuario por su RUT
     * @param rut RUT del usuario
     * @return Optional con el usuario si existe
     */
    Optional<Usuario> findByRut(String rut);

    /**
     * Busca un usuario por su código de referido
     * @param codigoRef Código de referido
     * @return Optional con el usuario si existe
     */
    Optional<Usuario> findByCodigoRef(String codigoRef);

    /**
     * Verifica si existe un usuario con el email dado
     * @param email Email a verificar
     * @return true si existe, false en caso contrario
     */
    boolean existsByEmail(String email);

    /**
     * Verifica si existe un usuario con el RUT dado
     * @param rut RUT a verificar
     * @return true si existe, false en caso contrario
     */
    boolean existsByRut(String rut);

    /**
     * Verifica si existe un usuario con el código de referido dado
     * @param codigoRef Código de referido a verificar
     * @return true si existe, false en caso contrario
     */
    boolean existsByCodigoRef(String codigoRef);

    /**
     * Busca todos los usuarios con un rol específico
     * @param rolId ID del rol
     * @return Lista de usuarios con ese rol
     */
    List<Usuario> findByRolId(Long rolId);

    /**
     * Busca todos los usuarios con un estado específico
     * @param estadoId ID del estado
     * @return Lista de usuarios con ese estado
     */
    List<Usuario> findByEstadoId(Long estadoId);

    /**
     * Busca todos los usuarios VIP de DUOC
     * @param duocVip true para buscar usuarios VIP, false para no VIP
     * @return Lista de usuarios
     */
    List<Usuario> findByDuocVip(Boolean duocVip);

    /**
     * Busca usuarios por rol y estado
     * @param rolId ID del rol
     * @param estadoId ID del estado
     * @return Lista de usuarios que cumplen ambas condiciones
     */
    List<Usuario> findByRolIdAndEstadoId(Long rolId, Long estadoId);
}