package com.rentify.userservice.controller;

import com.rentify.userservice.dto.UsuarioDTO;
import com.rentify.userservice.dto.UsuarioUpdateDTO;
import com.rentify.userservice.dto.LoginDTO;
import com.rentify.userservice.dto.LoginResponseDTO;
import com.rentify.userservice.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller para gestión de usuarios.
 * Endpoints protegidos mediante las cabeceras de identidad inyectadas por el API Gateway.
 */
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

    /**
     * Valida si las cabeceras de identidad están ausentes (Intento de bypass del Gateway).
     */
    private boolean isNoAutorizado(Long usuarioId, Long rolId) {
        return usuarioId == null || rolId == null;
    }

    // ==========================================
    // ? ENDPOINTS PÚBLICOS (Sin restricciones)
    // ==========================================

    @PostMapping
    @Operation(summary = "Registrar nuevo usuario",
            description = "Endpoint Público. Registra un nuevo usuario en el sistema. Solo mayores de 18 años.")
    public ResponseEntity<UsuarioDTO> registrarUsuario(
            @Valid @RequestBody UsuarioDTO usuarioDTO) {
        UsuarioDTO creado = usuarioService.registrarUsuario(usuarioDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PostMapping("/login")
    @Operation(summary = "Login de usuario",
            description = "Endpoint Público. Autentica un usuario con email y contraseña utilizando hash BCrypt.")
    public ResponseEntity<LoginResponseDTO> login(
            @Valid @RequestBody LoginDTO loginDTO) {
        UsuarioDTO usuario = usuarioService.login(loginDTO);
        LoginResponseDTO response = LoginResponseDTO.builder()
                .mensaje("Login exitoso")
                .usuario(usuario)
                .build();
        return ResponseEntity.ok(response);
    }

    // ==========================================
    // ? ENDPOINTS PROTEGIDOS POR IDENTITY HEADERS
    // ==========================================

    @GetMapping
    @Operation(summary = "Listar todos los usuarios (Solo Admin)",
            description = "Obtiene la lista completa de usuarios registrados. Requiere privilegios de Administrador.")
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
    @Operation(summary = "Obtener usuario por ID (Dueño o Admin)",
            description = "Obtiene un usuario específico por su ID. Solo permitido para el propio usuario o un Administrador.")
    public ResponseEntity<?> obtenerPorId(
            @RequestHeader(value = HEADER_USER, required = false) Long usuarioIdHeader,
            @RequestHeader(value = HEADER_ROLE, required = false) Long rolIdHeader,
            @PathVariable Long id,
            @RequestParam(defaultValue = "true") boolean includeDetails) {

        if (isNoAutorizado(usuarioIdHeader, rolIdHeader)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Regla: Si no es Admin Y tampoco es el dueño de la cuenta que está consultando -> Bloquear
        if (!ROL_ADMIN.equals(rolIdHeader) && !usuarioIdHeader.equals(id)) {
            log.warn("Usuario {} intentó consultar información privada del usuario {}", usuarioIdHeader, id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acceso denegado: No tienes permisos para ver este perfil.");
        }

        return ResponseEntity.ok(usuarioService.obtenerPorId(id, includeDetails));
    }

    @GetMapping("/email/{email}")
    @Operation(summary = "Obtener usuario por email (Dueño o Admin)",
            description = "Obtiene un usuario específico por su correo electrónico. Protegido contra fuga de información.")
    public ResponseEntity<?> obtenerPorEmail(
            @RequestHeader(value = HEADER_USER, required = false) Long usuarioIdHeader,
            @RequestHeader(value = HEADER_ROLE, required = false) Long rolIdHeader,
            @PathVariable String email,
            @RequestParam(defaultValue = "true") boolean includeDetails) {

        if (isNoAutorizado(usuarioIdHeader, rolIdHeader)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UsuarioDTO usuario = usuarioService.obtenerPorEmail(email, includeDetails);

        // Seguridad post-consulta: Si no es Admin y el ID del registro encontrado no coincide con el suyo, denegar.
        if (!ROL_ADMIN.equals(rolIdHeader) && !usuario.getId().equals(usuarioIdHeader)) {
            log.warn("Usuario {} intentó buscar por email una cuenta ajena ({})", usuarioIdHeader, email);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acceso denegado: No tienes permiso para buscar este perfil.");
        }

        return ResponseEntity.ok(usuario);
    }

    @GetMapping("/rol/{rolId}")
    @Operation(summary = "Obtener usuarios por rol (Solo Admin)",
            description = "Obtiene todos los usuarios con un rol específico.")
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
    @Operation(summary = "Obtener usuarios DUOC VIP (Solo Admin)",
            description = "Obtiene todos los usuarios con beneficio DUOC.")
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
    @Operation(summary = "Actualizar usuario (Solo Admin)",
            description = "Actualiza los datos de un usuario. Permite cambiar nombre, email, teléfono, rol y estado.")
    public ResponseEntity<?> actualizarUsuario(
            @RequestHeader(value = HEADER_USER, required = false) Long usuarioIdHeader,
            @RequestHeader(value = HEADER_ROLE, required = false) Long rolIdHeader,
            @PathVariable Long id,
            @Valid @RequestBody UsuarioUpdateDTO updateDTO) {

        if (isNoAutorizado(usuarioIdHeader, rolIdHeader)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (!ROL_ADMIN.equals(rolIdHeader)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acceso denegado: Solo administradores pueden realizar esta actualización.");
        }

        return ResponseEntity.ok(usuarioService.actualizarUsuarioAdmin(id, updateDTO));
    }

    @PatchMapping("/{id}/rol")
    @Operation(summary = "Cambiar rol de usuario (Solo Admin)",
            description = "Cambia el rol asignado a un usuario.")
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
    @Operation(summary = "Cambiar estado de usuario (Solo Admin)",
            description = "Cambia el estado de un usuario (1=ACTIVO, 2=INACTIVO, 3=SUSPENDIDO)")
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
    @Operation(summary = "Agregar puntos RentifyPoints (Solo Admin / Interno)",
            description = "Agrega puntos al programa de fidelización del usuario.")
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
    @Operation(summary = "Verificar si usuario existe (Cualquier usuario autenticado)",
            description = "Permite a los demás microservicios de la red interna comprobar la existencia de un usuario mediante Feign.")
    public ResponseEntity<?> existeUsuario(
            @RequestHeader(value = HEADER_USER, required = false) Long usuarioIdHeader,
            @RequestHeader(value = HEADER_ROLE, required = false) Long rolIdHeader,
            @PathVariable Long id) {

        if (isNoAutorizado(usuarioIdHeader, rolIdHeader)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Cualquier usuario autenticado en el ecosistema (pasó por el gateway) puede gatillar esta verificación
        return ResponseEntity.ok(Boolean.valueOf(usuarioService.existeUsuario(id)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar usuario físicamente (Solo Admin)",
            description = "Elimina de forma permanente a un usuario del sistema utilizando su ID.")
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
}