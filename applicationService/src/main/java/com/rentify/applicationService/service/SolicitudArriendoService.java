package com.rentify.applicationService.service;

import com.rentify.applicationService.client.DocumentServiceClient;
import com.rentify.applicationService.client.PropertyServiceClient;
import com.rentify.applicationService.client.UserServiceClient;
import com.rentify.applicationService.dto.PropiedadDTO;
import com.rentify.applicationService.dto.SolicitudArriendoDTO;
import com.rentify.applicationService.dto.UsuarioDTO;
import com.rentify.applicationService.exception.BusinessValidationException;
import com.rentify.applicationService.exception.ResourceNotFoundException;
import com.rentify.applicationService.model.SolicitudArriendo;
import com.rentify.applicationService.repository.SolicitudArriendoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.rentify.applicationService.constants.ApplicationConstants.*;

/**
 * Servicio para gestión de solicitudes de arriendo
 * Implementa toda la lógica de negocio relacionada con solicitudes
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SolicitudArriendoService {

    private final SolicitudArriendoRepository repository;
    private final UserServiceClient userServiceClient;
    private final PropertyServiceClient propertyServiceClient;
    private final DocumentServiceClient documentServiceClient;
    private final ModelMapper modelMapper;

    /**
     * Crea una nueva solicitud de arriendo con todas las validaciones de negocio
     */
    @Transactional
    public SolicitudArriendoDTO crearSolicitud(SolicitudArriendoDTO solicitudDTO) {
        log.info("Creando nueva solicitud para usuario {} y propiedad {}",
                solicitudDTO.getUsuarioId(), solicitudDTO.getPropiedadId());

        // 1. Validar que el usuario existe y obtener su información completa
        UsuarioDTO usuario = userServiceClient.getUserById(solicitudDTO.getUsuarioId());
        if (usuario == null) {
            throw new BusinessValidationException(
                    String.format(Mensajes.USUARIO_NO_EXISTE, solicitudDTO.getUsuarioId())
            );
        }

        // 2. Validar que el usuario tenga rol ARRIENDATARIO o ADMIN
        if (!Roles.puedeCrearSolicitud(usuario.getRolId())) {
            log.warn("Usuario {} con rolId {} intentó crear solicitud",
                    usuario.getId(), usuario.getRolId());
            throw new BusinessValidationException(Mensajes.ROL_INVALIDO_SOLICITUD);
        }

        // 3. Validar que el usuario no tenga más de 3 solicitudes activas
        long solicitudesActivas = repository.countByUsuarioIdAndEstado(
                solicitudDTO.getUsuarioId(),
                EstadoSolicitud.PENDIENTE
        );

        if (solicitudesActivas >= Limites.MAX_SOLICITUDES_ACTIVAS) {
            log.warn("Usuario {} alcanzó el límite de solicitudes activas: {}",
                    usuario.getId(), Long.valueOf(solicitudesActivas));
            throw new BusinessValidationException(
                    String.format(Mensajes.MAX_SOLICITUDES_ALCANZADO, Limites.MAX_SOLICITUDES_ACTIVAS)
            );
        }

        // 4. Verificar que no existe solicitud pendiente para esta propiedad
        boolean existeSolicitudPendiente = repository.existsByUsuarioIdAndPropiedadIdAndEstado(
                solicitudDTO.getUsuarioId(),
                solicitudDTO.getPropiedadId(),
                EstadoSolicitud.PENDIENTE
        );

        if (existeSolicitudPendiente) {
            log.warn("Usuario {} ya tiene solicitud pendiente para propiedad {}",
                    usuario.getId(), solicitudDTO.getPropiedadId());
            throw new BusinessValidationException(Mensajes.SOLICITUD_DUPLICADA);
        }

        // 5. Validar que la propiedad existe
        if (!propertyServiceClient.existsProperty(solicitudDTO.getPropiedadId())) {
            throw new BusinessValidationException(
                    String.format(Mensajes.PROPIEDAD_NO_EXISTE, solicitudDTO.getPropiedadId())
            );
        }

        // 6. Validar que la propiedad está disponible
        if (!propertyServiceClient.isPropertyAvailable(solicitudDTO.getPropiedadId())) {
            log.warn("Propiedad {} no está disponible", solicitudDTO.getPropiedadId());
            throw new BusinessValidationException(Mensajes.PROPIEDAD_NO_DISPONIBLE);
        }

        // 7. Validar que el usuario tenga documentos aprobados
        // NOTA: Esta validación puede ser comentada durante desarrollo si Document Service no está listo
        if (!documentServiceClient.hasApprovedDocuments(solicitudDTO.getUsuarioId())) {
            log.warn("Usuario {} no tiene documentos aprobados", usuario.getId());
            throw new BusinessValidationException(Mensajes.DOCUMENTOS_NO_APROBADOS);
        }

        // 8. Crear la solicitud
        SolicitudArriendo solicitud = new SolicitudArriendo();
        solicitud.setUsuarioId(solicitudDTO.getUsuarioId());
        solicitud.setPropiedadId(solicitudDTO.getPropiedadId());
        solicitud.setEstado(EstadoSolicitud.PENDIENTE);
        solicitud.setFechaSolicitud(new Date());

        SolicitudArriendo saved = repository.save(solicitud);
        log.info("Solicitud creada exitosamente con ID: {}", saved.getId());

        return convertToDTO(saved, true);
    }

    /**
     * Lista todas las solicitudes con opción de incluir detalles
     */
    @Transactional(readOnly = true)
    public List<SolicitudArriendoDTO> listarTodas(boolean includeDetails) {
        log.debug("Listando todas las solicitudes (includeDetails: {})", Boolean.valueOf(includeDetails));
        return repository.findAll().stream()
                .map(s -> convertToDTO(s, includeDetails))
                .collect(Collectors.toList());
    }

    /**
     * Obtiene una solicitud por su ID
     */
    @Transactional(readOnly = true)
    public SolicitudArriendoDTO obtenerPorId(Long id, boolean includeDetails) {
        log.debug("Obteniendo solicitud con ID: {}", id);
        SolicitudArriendo solicitud = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(Mensajes.SOLICITUD_NO_ENCONTRADA, id)
                ));
        return convertToDTO(solicitud, includeDetails);
    }

    /**
     * Obtiene todas las solicitudes de un usuario
     */
    @Transactional(readOnly = true)
    public List<SolicitudArriendoDTO> obtenerPorUsuario(Long usuarioId) {
        log.debug("Obteniendo solicitudes del usuario: {}", usuarioId);
        return repository.findByUsuarioId(usuarioId).stream()
                .map(s -> convertToDTO(s, false))
                .collect(Collectors.toList());
    }

    /**
     * Obtiene todas las solicitudes para una propiedad
     */
    @Transactional(readOnly = true)
    public List<SolicitudArriendoDTO> obtenerPorPropiedad(Long propiedadId) {
        log.debug("Obteniendo solicitudes de la propiedad: {}", propiedadId);
        return repository.findByPropiedadId(propiedadId).stream()
                .map(s -> convertToDTO(s, false))
                .collect(Collectors.toList());
    }

    /**
     * Actualiza el estado de una solicitud
     */
    @Transactional
    public SolicitudArriendoDTO actualizarEstado(Long id, String nuevoEstado) {
        log.info("Actualizando estado de solicitud {} a {}", id, nuevoEstado);

        SolicitudArriendo solicitud = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(Mensajes.SOLICITUD_NO_ENCONTRADA, id)
                ));

        String estadoUpper = nuevoEstado.toUpperCase();

        if (!EstadoSolicitud.esValido(estadoUpper)) {
            throw new BusinessValidationException(
                    String.format(Mensajes.ESTADO_INVALIDO, nuevoEstado)
            );
        }

        solicitud.setEstado(estadoUpper);
        SolicitudArriendo updated = repository.save(solicitud);
        log.info("Estado de solicitud {} actualizado exitosamente a: {}", id, estadoUpper);

        return convertToDTO(updated, true);
    }

    /**
     * Convierte una entidad SolicitudArriendo a DTO
     * Opcionalmente incluye información detallada de usuario y propiedad
     */
    private SolicitudArriendoDTO convertToDTO(SolicitudArriendo solicitud, boolean includeDetails) {
        SolicitudArriendoDTO dto = modelMapper.map(solicitud, SolicitudArriendoDTO.class);

        if (includeDetails) {
            try {
                UsuarioDTO usuario = userServiceClient.getUserById(solicitud.getUsuarioId());
                dto.setUsuario(usuario);
            } catch (Exception e) {
                log.warn("No se pudo obtener información del usuario {}: {}",
                        solicitud.getUsuarioId(), e.getMessage());
            }

            try {
                PropiedadDTO propiedad = propertyServiceClient.getPropertyById(solicitud.getPropiedadId());
                dto.setPropiedad(propiedad);
            } catch (Exception e) {
                log.warn("No se pudo obtener información de la propiedad {}: {}",
                        solicitud.getPropiedadId(), e.getMessage());
            }
        }

        return dto;
    }
}