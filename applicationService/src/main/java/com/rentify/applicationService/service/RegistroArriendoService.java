package com.rentify.applicationService.service;

import com.rentify.applicationService.dto.RegistroArriendoDTO;
import com.rentify.applicationService.dto.SolicitudArriendoDTO;
import com.rentify.applicationService.exception.BusinessValidationException;
import com.rentify.applicationService.exception.ResourceNotFoundException;
import com.rentify.applicationService.model.RegistroArriendo;
import com.rentify.applicationService.model.SolicitudArriendo;
import com.rentify.applicationService.repository.RegistroArriendoRepository;
import com.rentify.applicationService.repository.SolicitudArriendoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static com.rentify.applicationService.constants.ApplicationConstants.*;

/**
 * Servicio para gestión de registros de arriendos activos
 * Implementa toda la lógica de negocio relacionada con registros
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RegistroArriendoService {

    private final RegistroArriendoRepository repository;
    private final SolicitudArriendoRepository solicitudRepository;
    private final SolicitudArriendoService solicitudService;
    private final ModelMapper modelMapper;

    /**
     * Crea un nuevo registro de arriendo con todas las validaciones
     */
    @Transactional
    public RegistroArriendoDTO crearRegistro(RegistroArriendoDTO registroDTO) {
        log.info("Creando nuevo registro para solicitud {}", registroDTO.getSolicitudId());

        // 1. Validar que la solicitud existe
        SolicitudArriendo solicitud = solicitudRepository.findById(registroDTO.getSolicitudId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(Mensajes.SOLICITUD_NO_ENCONTRADA, registroDTO.getSolicitudId())
                ));

        // 2. Validar que la solicitud está ACEPTADA
        if (!EstadoSolicitud.ACEPTADA.equals(solicitud.getEstado())) {
            log.warn("Intento de crear registro para solicitud {} con estado {}",
                    solicitud.getId(), solicitud.getEstado());
            throw new BusinessValidationException(
                    String.format(Mensajes.REGISTRO_SOLO_ACEPTADA, solicitud.getEstado())
            );
        }

        // 3. Validar que no existe ya un registro activo para esta solicitud
        List<RegistroArriendo> registrosActivos = repository.findBySolicitudId(registroDTO.getSolicitudId())
                .stream()
                .filter(RegistroArriendo::getActivo)
                .toList();

        if (!registrosActivos.isEmpty()) {
            log.warn("Ya existe registro activo para solicitud {}", solicitud.getId());
            throw new BusinessValidationException(Mensajes.REGISTRO_YA_EXISTE);
        }

        // 4. Validar fechas si se proporciona fecha de fin
        if (registroDTO.getFechaFin() != null &&
                registroDTO.getFechaInicio().after(registroDTO.getFechaFin())) {
            log.warn("Fechas inválidas: inicio {} después de fin {}",
                    registroDTO.getFechaInicio(), registroDTO.getFechaFin());
            throw new BusinessValidationException(Mensajes.FECHAS_INVALIDAS);
        }

        // 5. Crear el registro
        RegistroArriendo registro = modelMapper.map(registroDTO, RegistroArriendo.class);
        registro.setActivo(Boolean.TRUE);  // CORREGIDO

        RegistroArriendo saved = repository.save(registro);
        log.info("Registro creado exitosamente con ID: {}", saved.getId());

        return convertToDTO(saved, true);
    }

    /**
     * Lista todos los registros con opción de incluir detalles
     */
    @Transactional(readOnly = true)
    public List<RegistroArriendoDTO> listarTodos(boolean includeDetails) {
        log.debug("Listando todos los registros (includeDetails: {})", Boolean.valueOf(includeDetails));  // CORREGIDO
        return repository.findAll().stream()
                .map(r -> convertToDTO(r, includeDetails))
                .collect(Collectors.toList());
    }

    /**
     * Obtiene un registro por su ID
     */
    @Transactional(readOnly = true)
    public RegistroArriendoDTO obtenerPorId(Long id, boolean includeDetails) {
        log.debug("Obteniendo registro con ID: {}", id);
        RegistroArriendo registro = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(Mensajes.REGISTRO_NO_ENCONTRADO, id)
                ));
        return convertToDTO(registro, includeDetails);
    }

    /**
     * Obtiene todos los registros de una solicitud
     */
    @Transactional(readOnly = true)
    public List<RegistroArriendoDTO> obtenerPorSolicitud(Long solicitudId) {
        log.debug("Obteniendo registros de la solicitud: {}", solicitudId);
        return repository.findBySolicitudId(solicitudId).stream()
                .map(r -> convertToDTO(r, false))
                .collect(Collectors.toList());
    }

    /**
     * Finaliza un registro marcándolo como inactivo
     */
    @Transactional
    public RegistroArriendoDTO finalizarRegistro(Long id) {
        log.info("Finalizando registro {}", id);

        RegistroArriendo registro = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(Mensajes.REGISTRO_NO_ENCONTRADO, id)
                ));

        if (!registro.getActivo()) {
            log.warn("Intento de finalizar registro {} que ya está inactivo", id);
            throw new BusinessValidationException(Mensajes.REGISTRO_YA_INACTIVO);
        }

        registro.setActivo(Boolean.FALSE);  // CORREGIDO
        registro.setFechaFin(new java.util.Date());

        RegistroArriendo updated = repository.save(registro);
        log.info("Registro {} finalizado exitosamente", id);

        return convertToDTO(updated, true);
    }

    /**
     * Convierte una entidad RegistroArriendo a DTO
     * Opcionalmente incluye información detallada de la solicitud
     */
    private RegistroArriendoDTO convertToDTO(RegistroArriendo registro, boolean includeDetails) {
        RegistroArriendoDTO dto = modelMapper.map(registro, RegistroArriendoDTO.class);

        if (includeDetails) {
            try {
                SolicitudArriendoDTO solicitud = solicitudService.obtenerPorId(
                        registro.getSolicitudId(),
                        true  // Aquí es OK porque el parámetro es primitivo boolean
                );
                dto.setSolicitud(solicitud);
            } catch (Exception e) {
                log.warn("No se pudo obtener información de la solicitud {}: {}",
                        registro.getSolicitudId(), e.getMessage());
            }
        }

        return dto;
    }
}