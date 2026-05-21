package com.rentify.userservice.service;

import com.rentify.userservice.constants.UserConstants.*;
import com.rentify.userservice.dto.UsuarioDTO;
import com.rentify.userservice.dto.UsuarioUpdateDTO;
import com.rentify.userservice.dto.LoginDTO;
import com.rentify.userservice.dto.RolDTO;
import com.rentify.userservice.dto.EstadoDTO;
import com.rentify.userservice.exception.AuthenticationException;
import com.rentify.userservice.exception.BusinessValidationException;
import com.rentify.userservice.exception.ResourceNotFoundException;
import com.rentify.userservice.model.Usuario;
import com.rentify.userservice.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolService rolService;
    private final EstadoService estadoService;
    private final ModelMapper modelMapper;
    private final BCryptPasswordEncoder passwordEncoder;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Transactional
    public UsuarioDTO registrarUsuario(UsuarioDTO usuarioDTO) {
        log.info("Registrando nuevo usuario: {}", usuarioDTO.getEmail());

        LocalDate fechaNacimiento = parseDate(usuarioDTO.getFnacimiento());
        if (fechaNacimiento == null) {
            throw new BusinessValidationException("Formato de fecha de nacimiento inválido. Use yyyy-MM-dd");
        }
        validarEdadMinima(fechaNacimiento);

        if (usuarioRepository.existsByEmail(usuarioDTO.getEmail())) {
            throw new BusinessValidationException(
                    String.format(Mensajes.EMAIL_DUPLICADO, usuarioDTO.getEmail())
            );
        }

        if (usuarioRepository.existsByRut(usuarioDTO.getRut())) {
            throw new BusinessValidationException(
                    String.format(Mensajes.RUT_DUPLICADO, usuarioDTO.getRut())
            );
        }

        String codigoRef = generarCodigoReferido();
        boolean isDuocEmail = usuarioDTO.getEmail().toLowerCase().endsWith(Validaciones.DOMINIO_DUOC);
        LocalDate now = LocalDate.now();
        Long rolId = usuarioDTO.getRolId() != null ? usuarioDTO.getRolId() : 3L;

        rolService.obtenerPorId(rolId);

        String snombre = usuarioDTO.getSnombre();
        if (snombre == null) {
            snombre = "";
        }

        Usuario usuario = new Usuario();
        usuario.setPnombre(usuarioDTO.getPnombre());
        usuario.setSnombre(snombre);
        usuario.setPapellido(usuarioDTO.getPapellido());
        usuario.setFnacimiento(fechaNacimiento);
        usuario.setEmail(usuarioDTO.getEmail());
        usuario.setRut(usuarioDTO.getRut());
        usuario.setNtelefono(usuarioDTO.getNtelefono());
        usuario.setClave(passwordEncoder.encode(usuarioDTO.getClave()));
        usuario.setPuntos(Validaciones.PUNTOS_INICIALES);
        usuario.setDuocVip(isDuocEmail);
        usuario.setCodigoRef(codigoRef);
        usuario.setFcreacion(now);
        usuario.setFactualizacion(now);
        usuario.setEstadoId(1L);
        usuario.setRolId(rolId);

        Usuario saved = usuarioRepository.save(usuario);

        log.info("Usuario registrado exitosamente con ID: {} - DUOC VIP: {}",
                saved.getId(), Boolean.valueOf(saved.getDuocVip()));

        return convertToDTO(saved, true);
    }

    @Transactional(readOnly = true)
    public UsuarioDTO login(LoginDTO loginDTO) {
        log.info("Intento de login para email: {}", loginDTO.getEmail());

        Usuario usuario = usuarioRepository.findByEmail(loginDTO.getEmail())
                .orElseThrow(() -> new AuthenticationException(Mensajes.CREDENCIALES_INVALIDAS));

        if (!passwordEncoder.matches(loginDTO.getClave(), usuario.getClave())) {
            log.warn("Intento de login fallido para email: {}", loginDTO.getEmail());
            throw new AuthenticationException(Mensajes.CREDENCIALES_INVALIDAS);
        }

        if (usuario.getEstadoId().equals(Estados.INACTIVO)) {
            throw new AuthenticationException(Mensajes.CUENTA_INACTIVA);
        }

        if (usuario.getEstadoId().equals(Estados.SUSPENDIDO)) {
            throw new AuthenticationException(Mensajes.CUENTA_SUSPENDIDA);
        }

        log.info("Login exitoso para usuario: {} (ID: {})", usuario.getEmail(), usuario.getId());
        return convertToDTO(usuario, true);
    }

    @Transactional(readOnly = true)
    public List<UsuarioDTO> obtenerTodos(boolean includeDetails) {
        log.debug("Obteniendo todos los usuarios (includeDetails: {})", Boolean.valueOf(includeDetails));
        return usuarioRepository.findAll().stream()
                .map(u -> convertToDTO(u, includeDetails))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UsuarioDTO obtenerPorId(Long id, boolean includeDetails) {
        log.debug("Obteniendo usuario con ID: {}", id);
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(Mensajes.USUARIO_NO_ENCONTRADO, id)
                ));
        return convertToDTO(usuario, includeDetails);
    }

    @Transactional(readOnly = true)
    public UsuarioDTO obtenerPorEmail(String email, boolean includeDetails) {
        log.debug("Obteniendo usuario con email: {}", email);
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(Mensajes.USUARIO_EMAIL_NO_ENCONTRADO, email)
                ));
        return convertToDTO(usuario, includeDetails);
    }

    @Transactional(readOnly = true)
    public List<UsuarioDTO> obtenerPorRol(Long rolId, boolean includeDetails) {
        log.debug("Obteniendo usuarios con rol ID: {}", rolId);
        rolService.obtenerPorId(rolId);
        return usuarioRepository.findByRolId(rolId).stream()
                .map(u -> convertToDTO(u, includeDetails))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UsuarioDTO> obtenerUsuariosVIP(boolean includeDetails) {
        log.debug("Obteniendo usuarios DUOC VIP");
        return usuarioRepository.findByDuocVip(true).stream()
                .map(u -> convertToDTO(u, includeDetails))
                .collect(Collectors.toList());
    }

    @Transactional
    public UsuarioDTO actualizarUsuarioAdmin(Long id, UsuarioUpdateDTO updateDTO) {
        log.info("Actualizando usuario con ID: {} (admin)", id);

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(Mensajes.USUARIO_NO_ENCONTRADO, id)
                ));

        usuario.setPnombre(updateDTO.getPnombre());
        usuario.setSnombre(updateDTO.getSnombre() != null ? updateDTO.getSnombre() : "");
        usuario.setPapellido(updateDTO.getPapellido());
        usuario.setNtelefono(updateDTO.getNtelefono());
        usuario.setFactualizacion(LocalDate.now());

        if (!usuario.getEmail().equals(updateDTO.getEmail())) {
            if (usuarioRepository.existsByEmail(updateDTO.getEmail())) {
                throw new BusinessValidationException(
                        String.format(Mensajes.EMAIL_DUPLICADO, updateDTO.getEmail())
                );
            }
            usuario.setEmail(updateDTO.getEmail());
            boolean isDuocEmail = updateDTO.getEmail().toLowerCase().endsWith(Validaciones.DOMINIO_DUOC);
            usuario.setDuocVip(isDuocEmail);
            log.info("Email actualizado a: {} - DUOC VIP: {}", updateDTO.getEmail(), isDuocEmail);
        }

        if (updateDTO.getRolId() != null) {
            rolService.obtenerPorId(updateDTO.getRolId());
            usuario.setRolId(updateDTO.getRolId());
            log.info("Rol actualizado a: {}", updateDTO.getRolId());
        }

        if (updateDTO.getEstadoId() != null) {
            if (!Estados.esValido(updateDTO.getEstadoId())) {
                throw new BusinessValidationException(
                        String.format(Mensajes.ESTADO_INVALIDO, updateDTO.getEstadoId())
                );
            }
            usuario.setEstadoId(updateDTO.getEstadoId());
            log.info("Estado actualizado a: {}", updateDTO.getEstadoId());
        }

        Usuario updated = usuarioRepository.save(usuario);
        log.info("Usuario actualizado exitosamente: {}", updated.getId());

        return convertToDTO(updated, true);
    }

    @Deprecated
    @Transactional
    public UsuarioDTO actualizarUsuario(Long id, UsuarioDTO usuarioDTO) {
        log.info("Actualizando usuario con ID: {}", id);

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(Mensajes.USUARIO_NO_ENCONTRADO, id)
                ));

        usuario.setPnombre(usuarioDTO.getPnombre());
        usuario.setSnombre(usuarioDTO.getSnombre() != null ? usuarioDTO.getSnombre() : "");
        usuario.setPapellido(usuarioDTO.getPapellido());
        usuario.setNtelefono(usuarioDTO.getNtelefono());
        usuario.setFactualizacion(LocalDate.now());

        if (!usuario.getEmail().equals(usuarioDTO.getEmail())) {
            if (usuarioRepository.existsByEmail(usuarioDTO.getEmail())) {
                throw new BusinessValidationException(
                        String.format(Mensajes.EMAIL_DUPLICADO, usuarioDTO.getEmail())
                );
            }
            usuario.setEmail(usuarioDTO.getEmail());
            boolean isDuocEmail = usuarioDTO.getEmail().toLowerCase().endsWith(Validaciones.DOMINIO_DUOC);
            usuario.setDuocVip(isDuocEmail);
        }

        Usuario updated = usuarioRepository.save(usuario);
        log.info("Usuario actualizado exitosamente: {}", updated.getId());

        return convertToDTO(updated, true);
    }

    @Transactional
    public UsuarioDTO cambiarRol(Long usuarioId, Long nuevoRolId) {
        log.info("Cambiando rol del usuario {} a rol {}", usuarioId, nuevoRolId);

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(Mensajes.USUARIO_NO_ENCONTRADO, usuarioId)
                ));

        rolService.obtenerPorId(nuevoRolId);

        usuario.setRolId(nuevoRolId);
        usuario.setFactualizacion(LocalDate.now());

        Usuario updated = usuarioRepository.save(usuario);
        log.info("Rol cambiado exitosamente para usuario: {}", updated.getId());

        return convertToDTO(updated, true);
    }

    @Transactional
    public UsuarioDTO cambiarEstado(Long usuarioId, Long nuevoEstadoId) {
        log.info("Cambiando estado del usuario {} a estado {}", usuarioId, nuevoEstadoId);

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(Mensajes.USUARIO_NO_ENCONTRADO, usuarioId)
                ));

        if (!Estados.esValido(nuevoEstadoId)) {
            throw new BusinessValidationException(
                    String.format(Mensajes.ESTADO_INVALIDO, nuevoEstadoId)
            );
        }

        usuario.setEstadoId(nuevoEstadoId);
        usuario.setFactualizacion(LocalDate.now());

        Usuario updated = usuarioRepository.save(usuario);
        log.info("Estado cambiado exitosamente para usuario: {}", updated.getId());

        return convertToDTO(updated, true);
    }

    @Transactional
    public UsuarioDTO agregarPuntos(Long usuarioId, Integer puntos) {
        log.info("Agregando {} puntos al usuario {}", puntos, usuarioId);

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(Mensajes.USUARIO_NO_ENCONTRADO, usuarioId)
                ));

        usuario.setPuntos(usuario.getPuntos() + puntos);
        usuario.setFactualizacion(LocalDate.now());

        Usuario updated = usuarioRepository.save(usuario);
        log.info("Puntos agregados exitosamente. Total: {}", updated.getPuntos());

        return convertToDTO(updated, false);
    }

    @Transactional(readOnly = true)
    public boolean existeUsuario(Long id) {
        return usuarioRepository.existsById(id);
    }

    // ==================== METODOS PRIVADOS ====================

    private UsuarioDTO convertToDTO(Usuario usuario, boolean includeDetails) {
        UsuarioDTO dto = new UsuarioDTO();

        dto.setId(usuario.getId());
        dto.setPnombre(usuario.getPnombre());
        dto.setSnombre(usuario.getSnombre());
        dto.setPapellido(usuario.getPapellido());
        dto.setFnacimiento(formatDate(usuario.getFnacimiento()));
        dto.setEmail(usuario.getEmail());
        dto.setRut(usuario.getRut());
        dto.setNtelefono(usuario.getNtelefono());
        dto.setClave(null); // nunca exponer el hash
        dto.setDuocVip(usuario.getDuocVip());
        dto.setPuntos(usuario.getPuntos());
        dto.setCodigoRef(usuario.getCodigoRef());
        dto.setFcreacion(formatDate(usuario.getFcreacion()));
        dto.setFactualizacion(formatDate(usuario.getFactualizacion()));
        dto.setEstadoId(usuario.getEstadoId());
        dto.setRolId(usuario.getRolId());

        if (includeDetails) {
            try {
                if (usuario.getRolId() != null) {
                    RolDTO rol = rolService.obtenerPorId(usuario.getRolId());
                    dto.setRol(rol);
                }
            } catch (Exception e) {
                log.warn("No se pudo obtener informacion del rol {} para usuario {}",
                        usuario.getRolId(), usuario.getId());
            }

            try {
                EstadoDTO estado = estadoService.obtenerPorId(usuario.getEstadoId());
                dto.setEstado(estado);
            } catch (Exception e) {
                log.warn("No se pudo obtener informacion del estado {} para usuario {}",
                        usuario.getEstadoId(), usuario.getId());
            }
        }

        return dto;
    }

    private void validarEdadMinima(LocalDate fechaNacimiento) {
        Period edad = Period.between(fechaNacimiento, LocalDate.now());
        if (edad.getYears() < Validaciones.EDAD_MINIMA) {
            throw new BusinessValidationException(
                    String.format(Mensajes.EDAD_INSUFICIENTE, Integer.valueOf(Validaciones.EDAD_MINIMA))
            );
        }
    }

    private String generarCodigoReferido() {
        String caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuilder codigo = new StringBuilder();

        int intentos = 0;
        int maxIntentos = 10;

        do {
            codigo.setLength(0);
            for (int i = 0; i < Validaciones.LONGITUD_CODIGO_REF; i++) {
                codigo.append(caracteres.charAt(random.nextInt(caracteres.length())));
            }
            intentos++;

            if (intentos >= maxIntentos) {
                log.error("No se pudo generar un codigo de referido unico despues de {} intentos", maxIntentos);
                throw new BusinessValidationException(Mensajes.CODIGO_REF_DUPLICADO);
            }

        } while (usuarioRepository.existsByCodigoRef(codigo.toString()));

        return codigo.toString();
    }

    private LocalDate parseDate(String dateString) {
        if (dateString == null || dateString.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(dateString, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            log.warn("Error al parsear fecha: {}", dateString);
            return null;
        }
    }

    private String formatDate(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.format(DATE_FORMATTER);
    }
    /**
     * Elimina físicamente a un usuario del sistema.
     * @param id ID del usuario a eliminar
     */
    @Transactional
    public void eliminarUsuario(Long id) {
        log.info("Solicitud para eliminar físicamente al usuario con ID: {}", id);

        // 1. Verificar si el usuario existe
        if (!usuarioRepository.existsById(id)) {
            log.error("Error al eliminar: No se encontró el usuario con ID {}", id);
            throw new ResourceNotFoundException("Usuario no encontrado con ID: " + id);
        }

        // 2. Ejecutar el borrado físico
        usuarioRepository.deleteById(id);

        log.info("Usuario con ID: {} eliminado exitosamente de la base de datos", id);
    }
}