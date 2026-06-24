package com.rentify.userservice.controller;

import com.rentify.userservice.dto.RolDTO;
import com.rentify.userservice.service.RolService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller para gestión de roles.
 * Protegido mediante control de flujo perimetral por cabeceras de identidad del API Gateway.
 */
@Slf4j
@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
@Tag(name = "Roles", description = "Gestión de roles del sistema (ADMIN, PROPIETARIO, ARRIENDATARIO)")
public class RolController {

    private final RolService rolService;

    private static final Long ROL_ADMIN = 1L;
    private static final String HEADER_USER = "X-Usuario-Id";
    private static final String HEADER_ROLE = "X-Rol-Id";

    /**
     * Valida si las cabeceras de identidad están ausentes (Intento de bypass del Gateway).
     */
    private boolean isNoAutorizado(Long usuarioId, Long rolId) {
        return usuarioId == null || rolId == null;
    }

    /**
     * Crea un nuevo rol.
     * 🔴 BLINDADO: Solo Administradores.
     */
    @PostMapping
    @Operation(summary = "Crear nuevo rol",
            description = "🔴 BLINDADO (Solo Admin): Crea un nuevo rol en el sistema.")
    public ResponseEntity<?> crearRol(
            @RequestHeader(value = HEADER_USER, required = false) Long usuarioIdHeader,
            @RequestHeader(value = HEADER_ROLE, required = false) Long rolIdHeader,
            @Valid @RequestBody RolDTO rolDTO) {

        if (isNoAutorizado(usuarioIdHeader, rolIdHeader)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (!ROL_ADMIN.equals(rolIdHeader)) {
            log.warn("Usuario {} intentó crear el rol {} sin ser ADMIN", usuarioIdHeader, rolDTO.getNombre());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acceso denegado: Operación reservada para administradores.");
        }

        RolDTO creado = rolService.crearRol(rolDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    /**
     * Obtiene todos los roles.
     * 🟡 PROTEGIDO: Cualquier usuario autenticado.
     */
    @GetMapping
    @Operation(summary = "Listar todos los roles",
            description = "🟡 PROTEGIDO (Usuarios Autenticados): Obtiene la lista completa de roles del sistema.")
    public ResponseEntity<?> obtenerTodos(
            @RequestHeader(value = HEADER_USER, required = false) Long usuarioIdHeader,
            @RequestHeader(value = HEADER_ROLE, required = false) Long rolIdHeader) {

        if (isNoAutorizado(usuarioIdHeader, rolIdHeader)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(rolService.obtenerTodos());
    }

    /**
     * Obtiene un rol por su ID.
     * 🟡 PROTEGIDO: Cualquier usuario autenticado.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtener rol por ID",
            description = "🟡 PROTEGIDO (Usuarios Autenticados): Obtiene un rol específico por su ID.")
    public ResponseEntity<?> obtenerPorId(
            @RequestHeader(value = HEADER_USER, required = false) Long usuarioIdHeader,
            @RequestHeader(value = HEADER_ROLE, required = false) Long rolIdHeader,
            @Parameter(description = "ID del rol", example = "1")
            @PathVariable Long id) {

        if (isNoAutorizado(usuarioIdHeader, rolIdHeader)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(rolService.obtenerPorId(id));
    }

    /**
     * Obtiene un rol por su nombre.
     * 🟡 PROTEGIDO: Cualquier usuario autenticado.
     */
    @GetMapping("/nombre/{nombre}")
    @Operation(summary = "Obtener rol por nombre",
            description = "🟡 PROTEGIDO (Usuarios Autenticados): Obtiene un rol específico por su nombre (ADMIN, PROPIETARIO, ARRIENDATARIO)")
    public ResponseEntity<?> obtenerPorNombre(
            @RequestHeader(value = HEADER_USER, required = false) Long usuarioIdHeader,
            @RequestHeader(value = HEADER_ROLE, required = false) Long rolIdHeader,
            @Parameter(description = "Nombre del rol", example = "ARRIENDATARIO")
            @PathVariable String nombre) {

        if (isNoAutorizado(usuarioIdHeader, rolIdHeader)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(rolService.obtenerPorNombre(nombre));
    }
}