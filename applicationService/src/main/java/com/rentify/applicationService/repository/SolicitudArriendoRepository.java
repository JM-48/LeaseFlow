package com.rentify.applicationService.repository;

import com.rentify.applicationService.model.SolicitudArriendo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository para la entidad SolicitudArriendo
 * Proporciona métodos de consulta personalizados
 */
@Repository
public interface SolicitudArriendoRepository extends JpaRepository<SolicitudArriendo, Long> {

    /**
     * Busca todas las solicitudes de un usuario específico
     */
    List<SolicitudArriendo> findByUsuarioId(Long usuarioId);

    /**
     * Busca todas las solicitudes para una propiedad específica
     */
    List<SolicitudArriendo> findByPropiedadId(Long propiedadId);

    /**
     * Busca solicitudes por usuario y estado
     */
    List<SolicitudArriendo> findByUsuarioIdAndEstado(Long usuarioId, String estado);

    /**
     * Cuenta cuántas solicitudes tiene un usuario en un estado específico
     * Útil para validar el límite de 3 solicitudes activas
     */
    long countByUsuarioIdAndEstado(Long usuarioId, String estado);

    /**
     * Verifica si existe una solicitud con los parámetros dados
     * Útil para prevenir solicitudes duplicadas
     */
    boolean existsByUsuarioIdAndPropiedadIdAndEstado(
            Long usuarioId,
            Long propiedadId,
            String estado
    );

    /**
     * Busca solicitudes por estado
     */
    List<SolicitudArriendo> findByEstado(String estado);

    /**
     * Consulta personalizada: Obtiene todas las solicitudes pendientes de un usuario
     */
    @Query("SELECT s FROM SolicitudArriendo s WHERE s.usuarioId = :usuarioId AND s.estado = 'PENDIENTE'")
    List<SolicitudArriendo> findSolicitudesPendientesByUsuario(@Param("usuarioId") Long usuarioId);
}