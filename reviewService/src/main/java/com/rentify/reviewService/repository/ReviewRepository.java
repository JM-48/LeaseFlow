package com.rentify.reviewService.repository;

import com.rentify.reviewService.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para operaciones de base de datos de Review.
 */
@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    /**
     * Encuentra todas las reseñas creadas por un usuario.
     * @param usuarioId ID del usuario
     * @return lista de reseñas
     */
    List<Review> findByUsuarioId(Long usuarioId);

    /**
     * Encuentra todas las reseñas de una propiedad.
     * @param propiedadId ID de la propiedad
     * @return lista de reseñas
     */
    List<Review> findByPropiedadId(Long propiedadId);

    /**
     * Encuentra todas las reseñas sobre un usuario específico.
     * @param usuarioResenadoId ID del usuario reseñado
     * @return lista de reseñas
     */
    List<Review> findByUsuarioResenadoId(Long usuarioResenadoId);

    /**
     * Encuentra reseñas por tipo.
     * @param tipoResenaId ID del tipo de reseña
     * @return lista de reseñas
     */
    List<Review> findByTipoResenaId(Long tipoResenaId);

    /**
     * Encuentra reseñas por estado.
     * @param estado estado de la reseña (ACTIVA, BANEADA, OCULTA)
     * @return lista de reseñas
     */
    List<Review> findByEstado(String estado);

    /**
     * Verifica si existe una reseña de un usuario para una propiedad específica.
     * @param usuarioId ID del usuario
     * @param propiedadId ID de la propiedad
     * @return true si existe, false en caso contrario
     */
    boolean existsByUsuarioIdAndPropiedadId(Long usuarioId, Long propiedadId);

    /**
     * Verifica si existe una reseña de un usuario sobre otro usuario.
     * @param usuarioId ID del usuario que crea la reseña
     * @param usuarioResenadoId ID del usuario reseñado
     * @return true si existe, false en caso contrario
     */
    boolean existsByUsuarioIdAndUsuarioResenadoId(Long usuarioId, Long usuarioResenadoId);

    /**
     * Cuenta reseñas de una propiedad con un puntaje mínimo.
     * @param propiedadId ID de la propiedad
     * @param puntajeMinimo puntaje mínimo
     * @return cantidad de reseñas
     */
    long countByPropiedadIdAndPuntajeGreaterThanEqual(Long propiedadId, Integer puntajeMinimo);

    /**
     * Calcula el promedio de puntaje de una propiedad.
     * @param propiedadId ID de la propiedad
     * @return promedio de puntaje
     */
    @Query("SELECT AVG(r.puntaje) FROM Review r WHERE r.propiedadId = :propiedadId AND r.estado = 'ACTIVA'")
    Double calcularPromedioPuntajePorPropiedad(@Param("propiedadId") Long propiedadId);

    /**
     * Calcula el promedio de puntaje de un usuario reseñado.
     * @param usuarioResenadoId ID del usuario reseñado
     * @return promedio de puntaje
     */
    @Query("SELECT AVG(r.puntaje) FROM Review r WHERE r.usuarioResenadoId = :usuarioResenadoId AND r.estado = 'ACTIVA'")
    Double calcularPromedioPuntajePorUsuario(@Param("usuarioResenadoId") Long usuarioResenadoId);

    /**
     * Encuentra las últimas N reseñas de una propiedad.
     * @param propiedadId ID de la propiedad
     * @return lista de reseñas ordenadas por fecha descendente
     */
    @Query("SELECT r FROM Review r WHERE r.propiedadId = :propiedadId AND r.estado = 'ACTIVA' ORDER BY r.fechaResena DESC")
    List<Review> findUltimasResenasPorPropiedad(@Param("propiedadId") Long propiedadId);
}