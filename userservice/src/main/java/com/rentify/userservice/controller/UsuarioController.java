package com.rentify.userservice.controller;

import com.rentify.userservice.dto.UsuarioDTO;
import com.rentify.userservice.dto.UsuarioUpdateDTO;
import com.rentify.userservice.dto.LoginDTO;
import com.rentify.userservice.dto.LoginResponseDTO;
import com.rentify.userservice.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@Tag(name = "Usuarios", description = "Gestión de usuarios del sistema Rentify")
public class UsuarioController {

    private final UsuarioService usuarioService;

    private static final Long ROL_ADMIN = 1L;
    private static final String HEADER_USER = "X-Usuario-Id";
    private static final String HEADER_ROLE = "X-Rol-Id";

    private boolean isNoAutorizado(Long usuarioId, Long rolId) {
        return usuarioId == null || rolId == null;
    }

    // ==========================================
    // ENDPOINTS PÚBLICOS
    // ==========================================

    @PostMapping
    @Operation(summary = "Registrar nuevo usuario", description = "PÚBLICO: Registra un nuevo usuario.")
    public ResponseEntity<UsuarioDTO> registrarUsuario(@Valid @RequestBody UsuarioDTO usuarioDTO) {
        UsuarioDTO creado = usuarioService.registrarUsuario(usuarioDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PostMapping("/login")
    @Operation(summary = "Login de usuario", description = "PÚBLICO: Autentica un usuario con email y contraseña.")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginDTO loginDTO) {
        UsuarioDTO usuario = usuarioService.login(loginDTO);
        LoginResponseDTO response = LoginResponseDTO.builder()
                .mensaje("Login exitoso")
                .usuario(usuario)
                .build();
        return ResponseEntity.ok(response);
    }

    // ==========================================
    // ENDPOINTS PROTEGIDOS
    // ==========================================

    @GetMapping
    @Operation(summary = "Listar todos los usuarios", description = "BLINDADO (Solo Admin)")
    public ResponseEntity<?> obtenerTodos(
            @RequestHeader(value = HEADER_USER, required = false) Long usuarioIdHeader,
            @RequestHeader(value = HEADER_ROLE, required = false) Long rolIdHeader,
            @RequestParam(defaultValue = "false") boolean includeDetails) {

        if (isNoAutorizado(usuarioIdHeader, rolIdHeader)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (!ROL_ADMIN.equals(rolIdHeader)) {
            log.warn("Usuario {} intentó listar todos los usuarios sin ser ADMIN", usuarioIdHeader);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acceso denegado: Solo administradores.");
        }
        return ResponseEntity.ok(usuarioService.obtenerTodos(includeDetails));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener usuario por ID", description = "PROTEGIDO (Dueño o Admin)")
    public ResponseEntity<?> obtenerPorId(
            @RequestHeader(value = HEADER_USER, required = false) Long usuarioIdHeader,
            @RequestHeader(value = HEADER_ROLE, required = false) Long rolIdHeader,
            @PathVariable Long id,
            @RequestParam(defaultValue = "true") boolean includeDetails) {

        if (isNoAutorizado(usuarioIdHeader, rolIdHeader)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (!ROL_ADMIN.equals(rolIdHeader) && !usuarioIdHeader.equals(id)) {
            log.warn("Usuario {} intentó consultar información privada del usuario {}", usuarioIdHeader, id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acceso denegado: No tienes permisos para ver este perfil.");
        }
        return ResponseEntity.ok(usuarioService.obtenerPorId(id, includeDetails));
    }

    @GetMapping("/email/{email}")
    @Operation(summary = "Obtener usuario por email", description = "PROTEGIDO (Dueño o Admin)")
    public ResponseEntity<?> obtenerPorEmail(
            @RequestHeader(value = HEADER_USER, required = false) Long usuarioIdHeader,
            @RequestHeader(value = HEADER_ROLE, required = false) Long rolIdHeader,
            @PathVariable String email,
            @RequestParam(defaultValue = "true") boolean includeDetails) {

        if (isNoAutorizado(usuarioIdHeader, rolIdHeader)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        UsuarioDTO usuario = usuarioService.obtenerPorEmail(email, includeDetails);
        if (!ROL_ADMIN.equals(rolIdHeader) && !usuario.getId().equals(usuarioIdHeader)) {
            log.warn("Usuario {} intentó buscar por email una cuenta ajena ({})", usuarioIdHeader, email);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acceso denegado: No tienes permiso para buscar este perfil.");
        }
        return ResponseEntity.ok(usuario);
    }

    @GetMapping("/rol/{rolId}")
    @Operation(summary = "Obtener usuarios por rol", description = "BLINDADO (Solo Admin)")
    public ResponseEntity<?> obtenerPorRol(
            @RequestHeader(value = HEADER_USER, required = false) Long usuarioIdHeader,
            @RequestHeader(value = HEADER_ROLE, required = false) Long rolIdHeader,
            @PathVariable Long rolId,
            @RequestParam(defaultValue = "false") boolean includeDetails) {

        if (isNoAutorizado(usuarioIdHeader, rolIdHeader)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (!ROL_ADMIN.equals(rolIdHeader)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acceso denegado: Operación administrativa.");
        }
        return ResponseEntity.ok(usuarioService.obtenerPorRol(rolId, includeDetails));
    }

    @GetMapping("/vip")
    @Operation(summary = "Obtener usuarios DUOC VIP", description = "BLINDADO (Solo Admin)")
    public ResponseEntity<?> obtenerUsuariosVIP(
            @RequestHeader(value = HEADER_USER, required = false) Long usuarioIdHeader,
            @RequestHeader(value = HEADER_ROLE, required = false) Long rolIdHeader,
            @RequestParam(defaultValue = "false") boolean includeDetails) {

        if (isNoAutorizado(usuarioIdHeader, rolIdHeader)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (!ROL_ADMIN.equals(rolIdHeader)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acceso denegado: Operación administrativa.");
        }
        return ResponseEntity.ok(usuarioService.obtenerUsuariosVIP(includeDetails));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar usuario",
            description = "ADMIN puede actualizar todo. El dueño puede actualizar sus propios datos básicos (no puede cambiar su propio rol ni estado).")
    public ResponseEntity<?> actualizarUsuario(
            @RequestHeader(value = HEADER_USER, required = false) Long usuarioIdHeader,
            @RequestHeader(value = HEADER_ROLE, required = false) Long rolIdHeader,
            @PathVariable Long id,
            @Valid @RequestBody UsuarioUpdateDTO updateDTO) {

        if (isNoAutorizado(usuarioIdHeader, rolIdHeader)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        boolean esAdmin = ROL_ADMIN.equals(rolIdHeader);
        boolean esDueno = usuarioIdHeader.equals(id);

        if (!esAdmin && !esDueno) {
            log.warn("Usuario {} intentó actualizar el perfil del usuario {} sin permisos", usuarioIdHeader, id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acceso denegado: Solo puedes editar tu propio perfil.");
        }

        // Si no es admin, bloquear cambio de rol y estado (esos son solo-admin)
        if (!esAdmin) {
            updateDTO.setRolId(null);
            updateDTO.setEstadoId(null);
        }

        return ResponseEntity.ok(usuarioService.actualizarUsuarioAdmin(id, updateDTO));
    }

    @PatchMapping("/{id}/rol")
    @Operation(summary = "Cambiar rol de usuario", description = "BLINDADO (Solo Admin)")
    public ResponseEntity<?> cambiarRol(
            @RequestHeader(value = HEADER_USER, required = false) Long usuarioIdHeader,
            @RequestHeader(value = HEADER_ROLE, required = false) Long rolIdHeader,
            @PathVariable Long id,
            @RequestParam Long rolId) {

        if (isNoAutorizado(usuarioIdHeader, rolIdHeader)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (!ROL_ADMIN.equals(rolIdHeader)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acceso denegado.");
        }
        return ResponseEntity.ok(usuarioService.cambiarRol(id, rolId));
    }

    @PatchMapping("/{id}/estado")
    @Operation(summary = "Cambiar estado de usuario", description = "BLINDADO (Solo Admin)")
    public ResponseEntity<?> cambiarEstado(
            @RequestHeader(value = HEADER_USER, required = false) Long usuarioIdHeader,
            @RequestHeader(value = HEADER_ROLE, required = false) Long rolIdHeader,
            @PathVariable Long id,
            @RequestParam Long estadoId) {

        if (isNoAutorizado(usuarioIdHeader, rolIdHeader)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (!ROL_ADMIN.equals(rolIdHeader)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acceso denegado.");
        }
        return ResponseEntity.ok(usuarioService.cambiarEstado(id, estadoId));
    }

    @PatchMapping("/{id}/puntos")
    @Operation(summary = "Agregar puntos RentifyPoints", description = "BLINDADO (Solo Admin)")
    public ResponseEntity<?> agregarPuntos(
            @RequestHeader(value = HEADER_USER, required = false) Long usuarioIdHeader,
            @RequestHeader(value = HEADER_ROLE, required = false) Long rolIdHeader,
            @PathVariable Long id,
            @RequestParam Integer puntos) {

        if (isNoAutorizado(usuarioIdHeader, rolIdHeader)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (!ROL_ADMIN.equals(rolIdHeader)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acceso denegado.");
        }
        return ResponseEntity.ok(usuarioService.agregarPuntos(id, puntos));
    }

    @GetMapping("/{id}/exists")
    @Operation(summary = "Verificar si usuario existe", description = "PROTEGIDO (Cualquier usuario autenticado)")
    public ResponseEntity<?> existeUsuario(
            @RequestHeader(value = HEADER_USER, required = false) Long usuarioIdHeader,
            @RequestHeader(value = HEADER_ROLE, required = false) Long rolIdHeader,
            @PathVariable Long id) {

        if (isNoAutorizado(usuarioIdHeader, rolIdHeader)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(Boolean.valueOf(usuarioService.existeUsuario(id)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar usuario físicamente", description = "BLINDADO (Solo Admin)")
    public ResponseEntity<?> eliminarUsuario(
            @RequestHeader(value = HEADER_USER, required = false) Long usuarioIdHeader,
            @RequestHeader(value = HEADER_ROLE, required = false) Long rolIdHeader,
            @PathVariable Long id) {

        if (isNoAutorizado(usuarioIdHeader, rolIdHeader)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (!ROL_ADMIN.equals(rolIdHeader)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acceso denegado: Solo administradores.");
        }
        usuarioService.eliminarUsuario(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/clave")
    @Operation(summary = "Cambiar contraseña", description = "Requiere autenticación")
    public ResponseEntity<Void> cambiarClave(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            @RequestHeader(value = "X-Usuario-Id", required = false) Long usuarioId,
            @RequestHeader(value = "X-Rol-Id", required = false) Long rolId) {

        if (usuarioId == null || rolId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Solo el propio usuario o un admin puede cambiar la clave
        boolean esAdmin = Long.valueOf(1L).equals(rolId);
        if (!esAdmin && !usuarioId.equals(id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        String claveActual = body.get("claveActual");
        String claveNueva = body.get("claveNueva");

        if (claveActual == null || claveNueva == null || claveNueva.length() < 6) {
            return ResponseEntity.badRequest().build();
        }

        usuarioService.cambiarClave(id, claveActual, claveNueva);
        return ResponseEntity.noContent().build();
    }
}