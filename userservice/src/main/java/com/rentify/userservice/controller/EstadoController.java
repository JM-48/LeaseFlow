package com.rentify.userservice.controller;

import com.rentify.userservice.dto.EstadoDTO;
import com.rentify.userservice.service.EstadoService;
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
 * Controller para gestión de estados.
 * Protegido mediante control de flujo por cabeceras de identidad del API Gateway.
 */
@Slf4j
@RestController
@RequestMapping("/api/estados")
@RequiredArgsConstructor
@Tag(name = "Estados", description = "Gestión de estados de usuario (ACTIVO, INACTIVO, SUSPENDIDO)")
public class EstadoController {

    private final EstadoService estadoService;

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
     * Crea un nuevo estado
     * POST /api/estados
     */
    @PostMapping
    @Operation(summary = "Crear nuevo estado (Solo Admin)", description = "Crea un nuevo estado en el sistema. Requiere privilegios de Administrador.")
    public ResponseEntity<?> crearEstado(
            @RequestHeader(value = HEADER_USER, required = false) Long usuarioIdHeader,
            @RequestHeader(value = HEADER_ROLE, required = false) Long rolIdHeader,
            @Valid @RequestBody EstadoDTO estadoDTO) {

        if (isNoAutorizado(usuarioIdHeader, rolIdHeader)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (!ROL_ADMIN.equals(rolIdHeader)) {
            log.warn("Usuario {} intentó crear el estado {} sin ser ADMIN", usuarioIdHeader, estadoDTO.getNombre());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acceso denegado: Operación reservada para administradores.");
        }

        EstadoDTO creado = estadoService.crearEstado(estadoDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    /**
     * Obtiene todos los estados
     * GET /api/estados
     */
    @GetMapping
    @Operation(summary = "Listar todos los estados (Usuarios Autenticados)", description = "Obtiene la lista completa de estados para uso de la plataforma.")
    public ResponseEntity<?> obtenerTodos(
            @RequestHeader(value = HEADER_USER, required = false) Long usuarioIdHeader,
            @RequestHeader(value = HEADER_ROLE, required = false) Long rolIdHeader) {

        if (isNoAutorizado(usuarioIdHeader, rolIdHeader)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(estadoService.obtenerTodos());
    }

    /**
     * Obtiene un estado por su ID
     * GET /api/estados/{id}
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtener estado por ID (Usuarios Autenticados)", description = "Obtiene un estado específico por su ID.")
    public ResponseEntity<?> obtenerPorId(
            @RequestHeader(value = HEADER_USER, required = false) Long usuarioIdHeader,
            @RequestHeader(value = HEADER_ROLE, required = false) Long rolIdHeader,
            @Parameter(description = "ID del estado", example = "1")
            @PathVariable Long id) {

        if (isNoAutorizado(usuarioIdHeader, rolIdHeader)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(estadoService.obtenerPorId(id));
    }

    /**
     * Obtiene un estado por su nombre
     * GET /api/estados/nombre/{nombre}
     */
    @GetMapping("/nombre/{nombre}")
    @Operation(summary = "Obtener estado por nombre (Usuarios Autenticados)",
            description = "Obtiene un estado específico por su nombre (ACTIVO, INACTIVO, SUSPENDIDO)")
    public ResponseEntity<?> obtenerPorNombre(
            @RequestHeader(value = HEADER_USER, required = false) Long usuarioIdHeader,
            @RequestHeader(value = HEADER_ROLE, required = false) Long rolIdHeader,
            @Parameter(description = "Nombre del estado", example = "ACTIVO")
            @PathVariable String nombre) {

        if (isNoAutorizado(usuarioIdHeader, rolIdHeader)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(estadoService.obtenerPorNombre(nombre));
    }
}