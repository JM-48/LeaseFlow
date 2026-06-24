package com.rentify.documentService.controller;

import com.rentify.documentService.dto.EstadoDTO;
import com.rentify.documentService.service.EstadoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para gestión de estados de documentos.
 * Estados: PENDIENTE, ACEPTADO, RECHAZADO, EN_REVISION.
 * Protegido mediante control de flujo perimetral por cabeceras de identidad del API Gateway.
 */
@RestController
@RequestMapping("/api/estados")
@RequiredArgsConstructor
@Tag(name = "Estados", description = "Gestión de estados de documentos")
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
     * Lista todos los estados disponibles.
     * 🟡 PROTEGIDO: Cualquier usuario autenticado.
     */
    @GetMapping
    @Operation(summary = "Listar todos los estados",
            description = "🟡 PROTEGIDO (Usuarios Autenticados): Obtiene listado completo de estados de documentos disponibles")
    public ResponseEntity<?> listarTodos(
            @RequestHeader(value = HEADER_USER, required = false) Long usuarioIdHeader,
            @RequestHeader(value = HEADER_ROLE, required = false) Long rolIdHeader) {

        if (isNoAutorizado(usuarioIdHeader, rolIdHeader)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(estadoService.listarTodos());
    }

    /**
     * Obtiene un estado específico por ID.
     * 🟡 PROTEGIDO: Cualquier usuario autenticado.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtener estado por ID",
            description = "🟡 PROTEGIDO (Usuarios Autenticados): Consulta un estado específico por su identificador")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estado encontrado"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "404", description = "Estado no encontrado")
    })
    public ResponseEntity<?> obtenerPorId(
            @PathVariable Long id,
            @RequestHeader(value = HEADER_USER, required = false) Long usuarioIdHeader,
            @RequestHeader(value = HEADER_ROLE, required = false) Long rolIdHeader) {

        if (isNoAutorizado(usuarioIdHeader, rolIdHeader)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(estadoService.obtenerPorId(id));
    }

    /**
     * Crea un nuevo estado.
     * 🔴 BLINDADO: SOLO ADMIN.
     */
    @PostMapping
    @Operation(summary = "Crear nuevo estado",
            description = "🔴 BLINDADO (Solo Admin): Crea un nuevo estado de documento en el sistema. Requiere permisos de administrador.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Estado creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado: Solo administradores")
    })
    public ResponseEntity<?> crear(
            @Valid @RequestBody EstadoDTO estadoDTO,
            @RequestHeader(value = HEADER_USER, required = false) Long usuarioIdHeader,
            @RequestHeader(value = HEADER_ROLE, required = false) Long rolIdHeader) {

        if (isNoAutorizado(usuarioIdHeader, rolIdHeader)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (!ROL_ADMIN.equals(rolIdHeader)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Acceso denegado: Solo los administradores pueden crear nuevos estados.");
        }

        EstadoDTO created = estadoService.crear(estadoDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Actualiza un estado existente.
     * 🔴 BLINDADO: SOLO ADMIN.
     */
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar estado",
            description = "🔴 BLINDADO (Solo Admin): Actualiza la información de un estado existente. Requiere permisos de administrador.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estado actualizado exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado: Solo administradores"),
            @ApiResponse(responseCode = "404", description = "Estado no encontrado")
    })
    public ResponseEntity<?> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody EstadoDTO estadoDTO,
            @RequestHeader(value = HEADER_USER, required = false) Long usuarioIdHeader,
            @RequestHeader(value = HEADER_ROLE, required = false) Long rolIdHeader) {

        if (isNoAutorizado(usuarioIdHeader, rolIdHeader)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (!ROL_ADMIN.equals(rolIdHeader)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Acceso denegado: Solo los administradores pueden actualizar estados.");
        }

        return ResponseEntity.ok(estadoService.actualizar(id, estadoDTO));
    }

    /**
     * Elimina un estado.
     * 🔴 BLINDADO: SOLO ADMIN.
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar estado",
            description = "🔴 BLINDADO (Solo Admin): Elimina permanentemente un estado del sistema. Requiere permisos de administrador.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Estado eliminado exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado: Solo administradores"),
            @ApiResponse(responseCode = "404", description = "Estado no encontrado")
    })
    public ResponseEntity<?> eliminar(
            @PathVariable Long id,
            @RequestHeader(value = HEADER_USER, required = false) Long usuarioIdHeader,
            @RequestHeader(value = HEADER_ROLE, required = false) Long rolIdHeader) {

        if (isNoAutorizado(usuarioIdHeader, rolIdHeader)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (!ROL_ADMIN.equals(rolIdHeader)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Acceso denegado: Solo los administradores pueden eliminar estados.");
        }

        estadoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}