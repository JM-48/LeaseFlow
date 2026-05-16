package com.rentify.userservice.service;

import com.rentify.userservice.constants.UserConstants.Mensajes;
import com.rentify.userservice.constants.UserConstants.Roles;
import com.rentify.userservice.dto.RolDTO;
import com.rentify.userservice.exception.BusinessValidationException;
import com.rentify.userservice.exception.ResourceNotFoundException;
import com.rentify.userservice.model.Rol;
import com.rentify.userservice.repository.RolRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para gestión de roles
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RolService {

    private final RolRepository rolRepository;
    private final ModelMapper modelMapper;

    /**
     * Crea un nuevo rol
     */
    @Transactional
    public RolDTO crearRol(RolDTO rolDTO) {
        log.info("Creando nuevo rol: {}", rolDTO.getNombre());

        // Validar que el nombre del rol sea válido
        String nombreUpper = rolDTO.getNombre().toUpperCase();
        if (!Roles.esValido(nombreUpper)) {
            throw new BusinessValidationException(
                    String.format(Mensajes.ROL_INVALIDO, rolDTO.getNombre())
            );
        }

        // Validar que no exista un rol con el mismo nombre
        if (rolRepository.existsByNombre(nombreUpper)) {
            throw new BusinessValidationException(
                    String.format(Mensajes.ROL_DUPLICADO, nombreUpper)
            );
        }

        Rol rol = modelMapper.map(rolDTO, Rol.class);
        rol.setNombre(nombreUpper); // Guardar en mayúsculas
        Rol saved = rolRepository.save(rol);

        log.info("Rol creado exitosamente con ID: {}", saved.getId());
        return modelMapper.map(saved, RolDTO.class);
    }

    /**
     * Obtiene todos los roles
     */
    @Transactional(readOnly = true)
    public List<RolDTO> obtenerTodos() {
        log.debug("Obteniendo todos los roles");
        return rolRepository.findAll().stream()
                .map(rol -> modelMapper.map(rol, RolDTO.class))
                .collect(Collectors.toList());
    }

    /**
     * Obtiene un rol por su ID
     */
    @Transactional(readOnly = true)
    public RolDTO obtenerPorId(Long id) {
        log.debug("Obteniendo rol con ID: {}", id);
        Rol rol = rolRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(Mensajes.ROL_NO_ENCONTRADO, id)
                ));
        return modelMapper.map(rol, RolDTO.class);
    }

    /**
     * Obtiene un rol por su nombre
     */
    @Transactional(readOnly = true)
    public RolDTO obtenerPorNombre(String nombre) {
        log.debug("Obteniendo rol con nombre: {}", nombre);
        String nombreUpper = nombre.toUpperCase();
        Rol rol = rolRepository.findByNombre(nombreUpper)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(Mensajes.ROL_NOMBRE_NO_ENCONTRADO, nombreUpper)
                ));
        return modelMapper.map(rol, RolDTO.class);
    }
}