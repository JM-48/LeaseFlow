package com.rentify.contactService.repository;

import com.rentify.contactService.model.MensajeContacto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MensajeContactoRepository extends JpaRepository<MensajeContacto, Long> {

    // Consultas por email
    List<MensajeContacto> findByEmail(String email);

    // Consultas por usuario autenticado
    List<MensajeContacto> findByUsuarioId(Long usuarioId);

    // Consultas por estado
    List<MensajeContacto> findByEstado(String estado);

    // Consultas combinadas
    List<MensajeContacto> findByEmailAndEstado(String email, String estado);
    List<MensajeContacto> findByUsuarioIdAndEstado(Long usuarioId, String estado);

    // Contadores
    long countByEmailAndEstado(String email, String estado);
    long countByUsuarioIdAndEstado(Long usuarioId, String estado);
    long countByEstado(String estado);

    // Verificaciones de existencia
    boolean existsByEmailAndEstado(String email, String estado);

    // Consultas personalizadas
    @Query("SELECT m FROM MensajeContacto m WHERE m.estado = :estado ORDER BY m.fechaCreacion DESC")
    List<MensajeContacto> findMensajesPendientesOrdenados(@Param("estado") String estado);

    @Query("SELECT m FROM MensajeContacto m WHERE m.respondidoPor = :adminId ORDER BY m.fechaActualizacion DESC")
    List<MensajeContacto> findMensajesRespondidosPorAdmin(@Param("adminId") Long adminId);

    // Buscar mensajes sin responder (sin respondidoPor)
    @Query("SELECT m FROM MensajeContacto m WHERE m.respondidoPor IS NULL AND m.estado = 'PENDIENTE' ORDER BY m.fechaCreacion ASC")
    List<MensajeContacto> findMensajesSinResponder();

    // Buscar por palabra clave en asunto o mensaje
    @Query("SELECT m FROM MensajeContacto m WHERE LOWER(m.asunto) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(m.mensaje) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<MensajeContacto> searchByKeyword(@Param("keyword") String keyword);
}