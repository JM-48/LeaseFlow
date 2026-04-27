package com.rentify.documentService.service;

import com.rentify.documentService.dto.EstadoDTO;
import com.rentify.documentService.exception.ResourceNotFoundException;
import com.rentify.documentService.model.Estado;
import com.rentify.documentService.repository.EstadoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static com.rentify.documentService.constants.DocumentConstants.Mensajes;

/**
 * Servicio para gestión de estados de documentos.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EstadoService {

    private final EstadoRepository estadoRepository;
    private final ModelMapper modelMapper;

    @Transactional(readOnly = true)
    public List<EstadoDTO> listarTodos() {
        log.debug("Listando todos los estados");
        return estadoRepository.findAll().stream()
                .map(e -> modelMapper.map(e, EstadoDTO.class))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EstadoDTO obtenerPorId(Long id) {
        log.debug("Obteniendo estado con ID: {}", id);
        Estado estado = estadoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(Mensajes.ESTADO_NO_ENCONTRADO, id)));
        return modelMapper.map(estado, EstadoDTO.class);
    }

    @Transactional
    public EstadoDTO crear(EstadoDTO estadoDTO) {
        log.info("Creando nuevo estado: {}", estadoDTO.getNombre());
        Estado estado = modelMapper.map(estadoDTO, Estado.class);
        Estado saved = estadoRepository.save(estado);
        return modelMapper.map(saved, EstadoDTO.class);
    }

    @Transactional
    public EstadoDTO actualizar(Long id, EstadoDTO estadoDTO) {
        log.info("Actualizando estado con ID: {}", id);
        Estado estado = estadoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(Mensajes.ESTADO_NO_ENCONTRADO, id)));

        estado.setNombre(estadoDTO.getNombre());
        Estado updated = estadoRepository.save(estado);
        return modelMapper.map(updated, EstadoDTO.class);
    }

    @Transactional
    public void eliminar(Long id) {
        log.info("Eliminando estado con ID: {}", id);
        if (!estadoRepository.existsById(id)) {
            throw new ResourceNotFoundException(
                    String.format(Mensajes.ESTADO_NO_ENCONTRADO, id));
        }
        estadoRepository.deleteById(id);
    }
}