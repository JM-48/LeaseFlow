package com.rentify.documentService.controller;

import com.rentify.documentService.dto.TipoDocumentoDTO;
import com.rentify.documentService.service.TipoDocumentoService;
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
 * Controlador REST para gestión de tipos de documentos.
 * Tipos: DNI, PASAPORTE, LIQUIDACION_SUELDO, CERTIFICADO_ANTECEDENTES, etc.
 */
@RestController
@RequestMapping("/api/tipos-documentos")
@RequiredArgsConstructor
@Tag(name = "Tipos de Documentos", description = "Gestión de tipos de documentos")
public class TipoDocumentoController {

    private final TipoDocumentoService tipoDocumentoService;

    /**
     * Lista todos los tipos de documentos disponibles.
     * (Público/Acceso general para rellenar selectores en el Front-End)
     */
    @GetMapping
    @Operation(summary = "Listar todos los tipos de documentos",
            description = "Obtiene listado completo de tipos de documentos disponibles en el sistema")
    public ResponseEntity<List<TipoDocumentoDTO>> listarTodos() {
        return ResponseEntity.ok(tipoDocumentoService.listarTodos());
    }

    /**
     * Obtiene un tipo de documento específico por ID.
     * (Público/Acceso general)
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtener tipo de documento por ID",
            description = "Consulta un tipo de documento específico por su identificador")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tipo de documento encontrado"),
            @ApiResponse(responseCode = "404", description = "Tipo de documento no encontrado")
    })
    public ResponseEntity<TipoDocumentoDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(tipoDocumentoService.obtenerPorId(id));
    }

    /**
     * Crea un nuevo tipo de documento.
     * (Blindado: SOLO ADMIN)
     */
    @PostMapping
    @Operation(summary = "Crear nuevo tipo de documento",
            description = "Registra un nuevo tipo de documento en el sistema. Requiere permisos de administrador.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Tipo de documento creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado: Solo administradores")
    })
    public ResponseEntity<?> crear(
            @Valid @RequestBody TipoDocumentoDTO tipoDocDTO,
            @RequestHeader(value = "X-Rol-Id", required = false) Long rolId) {

        if (rolId == null || rolId != 1L) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Acceso denegado: Solo los administradores pueden crear nuevos tipos de documentos.");
        }

        TipoDocumentoDTO created = tipoDocumentoService.crear(tipoDocDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Actualiza un tipo de documento existente.
     * (Blindado: SOLO ADMIN)
     */
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar tipo de documento",
            description = "Actualiza la información de un tipo de documento existente. Requiere permisos de administrador.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tipo de documento actualizado exitosamente"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado: Solo administradores"),
            @ApiResponse(responseCode = "404", description = "Tipo de documento no encontrado")
    })
    public ResponseEntity<?> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody TipoDocumentoDTO tipoDocDTO,
            @RequestHeader(value = "X-Rol-Id", required = false) Long rolId) {

        if (rolId == null || rolId != 1L) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Acceso denegado: Solo los administradores pueden actualizar tipos de documentos.");
        }

        return ResponseEntity.ok(tipoDocumentoService.actualizar(id, tipoDocDTO));
    }

    /**
     * Elimina un tipo de documento.
     * (Blindado: SOLO ADMIN)
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar tipo de documento",
            description = "Elimina permanentemente un tipo de documento del sistema. Requiere permisos de administrador.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Tipo de documento eliminado exitosamente"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado: Solo administradores"),
            @ApiResponse(responseCode = "404", description = "Tipo de documento no encontrado")
    })
    public ResponseEntity<?> eliminar(
            @PathVariable Long id,
            @RequestHeader(value = "X-Rol-Id", required = false) Long rolId) {

        if (rolId == null || rolId != 1L) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Acceso denegado: Solo los administradores pueden eliminar tipos de documentos.");
        }

        tipoDocumentoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}