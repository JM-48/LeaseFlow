package com.rentify.contactService.service;

import com.rentify.contactService.client.UserServiceClient;
import com.rentify.contactService.constants.ContactConstants.*;
import com.rentify.contactService.dto.MensajeContactoDTO;
import com.rentify.contactService.dto.RespuestaMensajeDTO;
import com.rentify.contactService.dto.external.UsuarioDTO;
import com.rentify.contactService.exception.BusinessValidationException;
import com.rentify.contactService.exception.ResourceNotFoundException;
import com.rentify.contactService.model.MensajeContacto;
import com.rentify.contactService.repository.MensajeContactoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MensajeContactoService {

    private final MensajeContactoRepository repository;
    private final UserServiceClient userServiceClient;
    private final ModelMapper modelMapper;

    /**
     * Crea un nuevo mensaje de contacto
     * @param mensajeDTO Datos del mensaje
     * @return MensajeContactoDTO creado
     */
    @Transactional
    public MensajeContactoDTO crearMensaje(MensajeContactoDTO mensajeDTO) {
        log.info("Creando nuevo mensaje de contacto de: {} ({})",
                mensajeDTO.getNombre(), mensajeDTO.getEmail());

        // 1. Validar longitud del mensaje
        validarLongitudMensaje(mensajeDTO.getMensaje());

        // 2. Si hay usuarioId, validar que el usuario existe y verificar límite
        if (mensajeDTO.getUsuarioId() != null) {
            UsuarioDTO usuario = userServiceClient.getUserById(mensajeDTO.getUsuarioId());
            if (usuario == null) {
                throw new BusinessValidationException(
                        String.format(Mensajes.USUARIO_NO_EXISTE, mensajeDTO.getUsuarioId())
                );
            }

            // Validar límite de mensajes pendientes por usuario autenticado
            long mensajesPendientes = repository.countByUsuarioIdAndEstado(
                    mensajeDTO.getUsuarioId(),
                    EstadoMensaje.PENDIENTE
            );

            if (mensajesPendientes >= Limites.MAX_MENSAJES_PENDIENTES_POR_USUARIO) {
                throw new BusinessValidationException(
                        String.format(Mensajes.MAX_MENSAJES_PENDIENTES,
                                Limites.MAX_MENSAJES_PENDIENTES_POR_USUARIO)
                );
            }
        } else {
            // Para usuarios no autenticados, validar por email
            long mensajesPendientes = repository.countByEmailAndEstado(
                    mensajeDTO.getEmail(),
                    EstadoMensaje.PENDIENTE
            );

            if (mensajesPendientes >= Limites.MAX_MENSAJES_PENDIENTES_POR_USUARIO) {
                throw new BusinessValidationException(
                        String.format(Mensajes.MAX_MENSAJES_PENDIENTES,
                                Limites.MAX_MENSAJES_PENDIENTES_POR_USUARIO)
                );
            }
        }

        // 3. Crear y guardar el mensaje
        MensajeContacto mensaje = modelMapper.map(mensajeDTO, MensajeContacto.class);
        mensaje.setEstado(EstadoMensaje.PENDIENTE);
        mensaje.setFechaCreacion(new Date());

        MensajeContacto saved = repository.save(mensaje);
        log.info("Mensaje de contacto creado exitosamente con ID: {}", saved.getId());

        return convertToDTO(saved, true);
    }

    /**
     * Lista todos los mensajes de contacto
     * @param includeDetails Si debe incluir información del usuario
     * @return Lista de MensajeContactoDTO
     */
    @Transactional(readOnly = true)
    public List<MensajeContactoDTO> listarTodos(boolean includeDetails) {
        log.debug("Listando todos los mensajes (includeDetails: {})", Boolean.valueOf(includeDetails));
        return repository.findAll().stream()
                .map(m -> convertToDTO(m, includeDetails))
                .collect(Collectors.toList());
    }

    /**
     * Obtiene un mensaje por su ID
     * @param id ID del mensaje
     * @param includeDetails Si debe incluir información del usuario
     * @return MensajeContactoDTO encontrado
     */
    @Transactional(readOnly = true)
    public MensajeContactoDTO obtenerPorId(Long id, boolean includeDetails) {
        log.debug("Obteniendo mensaje con ID: {}", id);
        MensajeContacto mensaje = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(Mensajes.MENSAJE_NO_ENCONTRADO, id)
                ));
        return convertToDTO(mensaje, includeDetails);
    }

    /**
     * Lista mensajes por email
     * @param email Email del remitente
     * @return Lista de MensajeContactoDTO
     */
    @Transactional(readOnly = true)
    public List<MensajeContactoDTO> listarPorEmail(String email) {
        log.debug("Listando mensajes del email: {}", email);
        return repository.findByEmail(email).stream()
                .map(m -> convertToDTO(m, false))
                .collect(Collectors.toList());
    }

    /**
     * Lista mensajes por usuario autenticado
     * @param usuarioId ID del usuario
     * @return Lista de MensajeContactoDTO
     */
    @Transactional(readOnly = true)
    public List<MensajeContactoDTO> listarPorUsuario(Long usuarioId) {
        log.debug("Listando mensajes del usuario ID: {}", usuarioId);
        return repository.findByUsuarioId(usuarioId).stream()
                .map(m -> convertToDTO(m, false))
                .collect(Collectors.toList());
    }

    /**
     * Lista mensajes por estado
     * @param estado Estado del mensaje (PENDIENTE, EN_PROCESO, RESUELTO)
     * @return Lista de MensajeContactoDTO
     */
    @Transactional(readOnly = true)
    public List<MensajeContactoDTO> listarPorEstado(String estado) {
        log.debug("Listando mensajes con estado: {}", estado);

        String estadoUpper = estado.toUpperCase();
        if (!EstadoMensaje.esValido(estadoUpper)) {
            throw new BusinessValidationException(
                    String.format(Mensajes.ESTADO_INVALIDO, estado)
            );
        }

        return repository.findByEstado(estadoUpper).stream()
                .map(m -> convertToDTO(m, false))
                .collect(Collectors.toList());
    }

    /**
     * Lista mensajes pendientes sin responder
     * @return Lista de MensajeContactoDTO
     */
    @Transactional(readOnly = true)
    public List<MensajeContactoDTO> listarMensajesSinResponder() {
        log.debug("Listando mensajes sin responder");
        return repository.findMensajesSinResponder().stream()
                .map(m -> convertToDTO(m, true))
                .collect(Collectors.toList());
    }

    /**
     * Actualiza el estado de un mensaje
     * @param id ID del mensaje
     * @param nuevoEstado Nuevo estado
     * @return MensajeContactoDTO actualizado
     */
    @Transactional
    public MensajeContactoDTO actualizarEstado(Long id, String nuevoEstado) {
        log.info("Actualizando estado del mensaje {} a {}", id, nuevoEstado);

        MensajeContacto mensaje = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(Mensajes.MENSAJE_NO_ENCONTRADO, id)
                ));

        String estadoUpper = nuevoEstado.toUpperCase();
        if (!EstadoMensaje.esValido(estadoUpper)) {
            throw new BusinessValidationException(
                    String.format(Mensajes.ESTADO_INVALIDO, nuevoEstado)
            );
        }

        mensaje.setEstado(estadoUpper);
        mensaje.setFechaActualizacion(new Date());

        MensajeContacto updated = repository.save(mensaje);
        log.info("Estado del mensaje {} actualizado exitosamente", id);

        return convertToDTO(updated, true);
    }

    /**
     * Responde a un mensaje de contacto
     * @param id ID del mensaje
     * @param respuestaDTO Datos de la respuesta
     * @return MensajeContactoDTO actualizado
     */
    @Transactional
    public MensajeContactoDTO responderMensaje(Long id, RespuestaMensajeDTO respuestaDTO) {
        log.info("Respondiendo mensaje {} por admin {}", id, respuestaDTO.getRespondidoPor());

        // 1. Verificar que el mensaje existe
        MensajeContacto mensaje = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(Mensajes.MENSAJE_NO_ENCONTRADO, id)
                ));

        // 2. Validar que quien responde es admin
        if (!userServiceClient.isAdmin(respuestaDTO.getRespondidoPor())) {
            throw new BusinessValidationException(Mensajes.SOLO_ADMIN_PUEDE_RESPONDER);
        }

        // 3. Actualizar el mensaje con la respuesta
        mensaje.setRespuesta(respuestaDTO.getRespuesta());
        mensaje.setRespondidoPor(respuestaDTO.getRespondidoPor());
        mensaje.setFechaActualizacion(new Date());

        // 4. Actualizar estado si se proporciona, sino marcar como RESUELTO
        if (respuestaDTO.getNuevoEstado() != null) {
            String estadoUpper = respuestaDTO.getNuevoEstado().toUpperCase();
            if (!EstadoMensaje.esValido(estadoUpper)) {
                throw new BusinessValidationException(
                        String.format(Mensajes.ESTADO_INVALIDO, respuestaDTO.getNuevoEstado())
                );
            }
            mensaje.setEstado(estadoUpper);
        } else {
            mensaje.setEstado(EstadoMensaje.RESUELTO);
        }

        MensajeContacto updated = repository.save(mensaje);
        log.info("Mensaje {} respondido exitosamente", id);

        return convertToDTO(updated, true);
    }

    /**
     * Busca mensajes por palabra clave
     * @param keyword Palabra clave a buscar
     * @return Lista de MensajeContactoDTO
     */
    @Transactional(readOnly = true)
    public List<MensajeContactoDTO> buscarPorPalabraClave(String keyword) {
        log.debug("Buscando mensajes con palabra clave: {}", keyword);
        return repository.searchByKeyword(keyword).stream()
                .map(m -> convertToDTO(m, false))
                .collect(Collectors.toList());
    }

    /**
     * Elimina un mensaje (solo admin)
     * @param id ID del mensaje
     * @param adminId ID del admin que elimina
     */
    @Transactional
    public void eliminarMensaje(Long id, Long adminId) {
        log.info("Eliminando mensaje {} por admin {}", id, adminId);

        // Validar que quien elimina es admin
        if (!userServiceClient.isAdmin(adminId)) {
            throw new BusinessValidationException(Mensajes.SOLO_ADMIN_PUEDE_RESPONDER);
        }

        MensajeContacto mensaje = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(Mensajes.MENSAJE_NO_ENCONTRADO, id)
                ));

        repository.delete(mensaje);
        log.info("Mensaje {} eliminado exitosamente", id);
    }

    /**
     * Obtiene estadísticas de mensajes
     * @return Mapa con estadísticas
     */
    @Transactional(readOnly = true)
    public java.util.Map<String, Long> obtenerEstadisticas() {
        log.debug("Obteniendo estadísticas de mensajes");

        long totalMensajes = repository.count();
        long pendientes = repository.countByEstado(EstadoMensaje.PENDIENTE);
        long enProceso = repository.countByEstado(EstadoMensaje.EN_PROCESO);
        long resueltos = repository.countByEstado(EstadoMensaje.RESUELTO);

        return java.util.Map.of(
                "total", totalMensajes,
                "pendientes", pendientes,
                "enProceso", enProceso,
                "resueltos", resueltos
        );
    }

    // MÉTODOS PRIVADOS

    /**
     * Valida la longitud del mensaje
     */
    private void validarLongitudMensaje(String mensaje) {
        if (mensaje.length() < Limites.MIN_LONGITUD_MENSAJE) {
            throw new BusinessValidationException(
                    String.format(Mensajes.MENSAJE_MUY_CORTO, Limites.MIN_LONGITUD_MENSAJE)
            );
        }
        if (mensaje.length() > Limites.MAX_LONGITUD_MENSAJE) {
            throw new BusinessValidationException(
                    String.format(Mensajes.MENSAJE_MUY_LARGO, Limites.MAX_LONGITUD_MENSAJE)
            );
        }
    }

    /**
     * Convierte una entidad a DTO
     */
    private MensajeContactoDTO convertToDTO(MensajeContacto mensaje, boolean includeDetails) {
        MensajeContactoDTO dto = modelMapper.map(mensaje, MensajeContactoDTO.class);

        if (includeDetails && mensaje.getUsuarioId() != null) {
            try {
                UsuarioDTO usuario = userServiceClient.getUserById(mensaje.getUsuarioId());
                dto.setUsuario(usuario);
            } catch (Exception e) {
                log.warn("No se pudo obtener información del usuario {}: {}",
                        mensaje.getUsuarioId(), e.getMessage());
            }
        }

        return dto;
    }
}