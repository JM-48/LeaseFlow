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

import java.util.List;

/**
 * Controlador REST para gestión de estados de documentos.
 * Estados: PENDIENTE, ACEPTADO, RECHAZADO, EN_REVISION
 */
@RestController
@RequestMapping("/api/estados")
@RequiredArgsConstructor
@Tag(name = "Estados", description = "Gestión de estados de documentos")
public class EstadoController {

    private final EstadoService estadoService;

    /**
     * Lista todos los estados disponibles.
     * (Público/Acceso general para rellenar selectores en el Front-End)
     */
    @GetMapping
    @Operation(summary = "Listar todos los estados",
            description = "Obtiene listado completo de estados de documentos disponibles")
    public ResponseEntity<List<EstadoDTO>> listarTodos() {
        return ResponseEntity.ok(estadoService.listarTodos());
    }

    /**
     * Obtiene un estado específico por ID.
     * (Público/Acceso general)
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtener estado por ID",
            description = "Consulta un estado específico por su identificador")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estado encontrado"),
            @ApiResponse(responseCode = "404", description = "Estado no encontrado")
    })
    public ResponseEntity<EstadoDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(estadoService.obtenerPorId(id));
    }

    /**
     * Crea un nuevo estado.
     * (Blindado: SOLO ADMIN)
     */
    @PostMapping
    @Operation(summary = "Crear nuevo estado",
            description = "Crea un nuevo estado de documento en el sistema. Requiere permisos de administrador.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Estado creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado: Solo administradores")
    })
    public ResponseEntity<?> crear(
            @Valid @RequestBody EstadoDTO estadoDTO,
            @RequestHeader(value = "X-Rol-Id", required = false) Long rolId) {

        if (rolId == null || rolId != 1L) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Acceso denegado: Solo los administradores pueden crear nuevos estados.");
        }

        EstadoDTO created = estadoService.crear(estadoDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Actualiza un estado existente.
     * (Blindado: SOLO ADMIN)
     */
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar estado",
            description = "Actualiza la información de un estado existente. Requiere permisos de administrador.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estado actualizado exitosamente"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado: Solo administradores"),
            @ApiResponse(responseCode = "404", description = "Estado no encontrado")
    })
    public ResponseEntity<?> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody EstadoDTO estadoDTO,
            @RequestHeader(value = "X-Rol-Id", required = false) Long rolId) {

        if (rolId == null || rolId != 1L) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Acceso denegado: Solo los administradores pueden actualizar estados.");
        }

        return ResponseEntity.ok(estadoService.actualizar(id, estadoDTO));
    }

    /**
     * Elimina un estado.
     * (Blindado: SOLO ADMIN)
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar estado",
            description = "Elimina permanentemente un estado del sistema. Requiere permisos de administrador.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Estado eliminado exitosamente"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado: Solo administradores"),
            @ApiResponse(responseCode = "404", description = "Estado no encontrado")
    })
    public ResponseEntity<?> eliminar(
            @PathVariable Long id,
            @RequestHeader(value = "X-Rol-Id", required = false) Long rolId) {

        if (rolId == null || rolId != 1L) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Acceso denegado: Solo los administradores pueden eliminar estados.");
        }

        estadoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}