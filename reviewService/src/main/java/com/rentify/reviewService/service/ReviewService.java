package com.rentify.reviewService.service;

import com.rentify.reviewService.dto.ReviewDTO;

import java.util.List;

/**
 * Interfaz del servicio de reseñas.
 * Define las operaciones disponibles para la gestión de reseñas.
 */
public interface ReviewService {

    /**
     * Crea una nueva reseña con validaciones de negocio.
     * @param reviewDTO datos de la reseña a crear
     * @return DTO de la reseña creada
     */
    ReviewDTO crearResena(ReviewDTO reviewDTO);

    /**
     * Obtiene todas las reseñas del sistema.
     * @param includeDetails si se deben incluir detalles de usuarios y propiedades
     * @return lista de reseñas
     */
    List<ReviewDTO> listarTodas(boolean includeDetails);

    /**
     * Obtiene una reseña por su ID.
     * @param id ID de la reseña
     * @param includeDetails si se deben incluir detalles de usuarios y propiedades
     * @return DTO de la reseña
     */
    ReviewDTO obtenerPorId(Long id, boolean includeDetails);

    /**
     * Obtiene todas las reseñas creadas por un usuario.
     * @param usuarioId ID del usuario
     * @param includeDetails si se deben incluir detalles de usuarios y propiedades
     * @return lista de reseñas del usuario
     */
    List<ReviewDTO> obtenerPorUsuario(Long usuarioId, boolean includeDetails);

    /**
     * Obtiene todas las reseñas de una propiedad.
     * @param propiedadId ID de la propiedad
     * @param includeDetails si se deben incluir detalles de usuarios y propiedades
     * @return lista de reseñas de la propiedad
     */
    List<ReviewDTO> obtenerPorPropiedad(Long propiedadId, boolean includeDetails);

    /**
     * Obtiene todas las reseñas sobre un usuario específico.
     * @param usuarioResenadoId ID del usuario reseñado
     * @param includeDetails si se deben incluir detalles de usuarios y propiedades
     * @return lista de reseñas sobre el usuario
     */
    List<ReviewDTO> obtenerPorUsuarioResenado(Long usuarioResenadoId, boolean includeDetails);

    /**
     * Calcula el promedio de puntaje de una propiedad.
     * @param propiedadId ID de la propiedad
     * @return promedio de puntaje (0.0 si no hay reseñas)
     */
    Double calcularPromedioPorPropiedad(Long propiedadId);

    /**
     * Calcula el promedio de puntaje de un usuario reseñado.
     * @param usuarioResenadoId ID del usuario reseñado
     * @return promedio de puntaje (0.0 si no hay reseñas)
     */
    Double calcularPromedioPorUsuario(Long usuarioResenadoId);

    /**
     * Actualiza el estado de una reseña (ACTIVA, BANEADA, OCULTA).
     * @param id ID de la reseña
     * @param nuevoEstado nuevo estado
     * @return DTO de la reseña actualizada
     */
    ReviewDTO actualizarEstado(Long id, String nuevoEstado);

    /**
     * Elimina una reseña del sistema.
     * @param id ID de la reseña a eliminar
     */
    void eliminarResena(Long id);
}