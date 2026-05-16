package com.rentify.userservice.service;

import com.rentify.userservice.constants.UserConstants.Mensajes;
import com.rentify.userservice.dto.EstadoDTO;
import com.rentify.userservice.exception.BusinessValidationException;
import com.rentify.userservice.exception.ResourceNotFoundException;
import com.rentify.userservice.model.Estado;
import com.rentify.userservice.repository.EstadoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para gestión de estados
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EstadoService {

    private final EstadoRepository estadoRepository;
    private final ModelMapper modelMapper;

    /**
     * Crea un nuevo estado
     */
    @Transactional
    public EstadoDTO crearEstado(EstadoDTO estadoDTO) {
        log.info("Creando nuevo estado: {}", estadoDTO.getNombre());

        // Validar que no exista un estado con el mismo nombre
        if (estadoRepository.existsByNombre(estadoDTO.getNombre())) {
            throw new BusinessValidationException(
                    String.format("Ya existe un estado con el nombre %s", estadoDTO.getNombre())
            );
        }

        Estado estado = modelMapper.map(estadoDTO, Estado.class);
        Estado saved = estadoRepository.save(estado);

        log.info("Estado creado exitosamente con ID: {}", saved.getId());
        return modelMapper.map(saved, EstadoDTO.class);
    }

    /**
     * Obtiene todos los estados
     */
    @Transactional(readOnly = true)
    public List<EstadoDTO> obtenerTodos() {
        log.debug("Obteniendo todos los estados");
        return estadoRepository.findAll().stream()
                .map(estado -> modelMapper.map(estado, EstadoDTO.class))
                .collect(Collectors.toList());
    }

    /**
     * Obtiene un estado por su ID
     */
    @Transactional(readOnly = true)
    public EstadoDTO obtenerPorId(Long id) {
        log.debug("Obteniendo estado con ID: {}", id);
        Estado estado = estadoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Estado con ID %d no encontrado", id)
                ));
        return modelMapper.map(estado, EstadoDTO.class);
    }

    /**
     * Obtiene un estado por su nombre
     */
    @Transactional(readOnly = true)
    public EstadoDTO obtenerPorNombre(String nombre) {
        log.debug("Obteniendo estado con nombre: {}", nombre);
        Estado estado = estadoRepository.findByNombre(nombre)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Estado %s no encontrado", nombre)
                ));
        return modelMapper.map(estado, EstadoDTO.class);
    }
}