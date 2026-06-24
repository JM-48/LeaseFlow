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

/**
 * Controlador REST para gestión de tipos de documentos.
 * Tipos: DNI, PASAPORTE, LIQUIDACION_SUELDO, CERTIFICADO_ANTECEDENTES, etc.
 * Protegido mediante control de flujo perimetral por cabeceras de identidad del API Gateway.
 */
@RestController
@RequestMapping("/api/tipos-documentos")
@RequiredArgsConstructor
@Tag(name = "Tipos de Documentos", description = "Gestión de tipos de documentos")
public class TipoDocumentoController {

    private final TipoDocumentoService tipoDocumentoService;

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
     * Lista todos los tipos de documentos disponibles.
     * 🟡 PROTEGIDO: Cualquier usuario autenticado.
     */
    @GetMapping
    @Operation(summary = "Listar todos los tipos de documentos",
            description = "🟡 PROTEGIDO (Usuarios Autenticados): Obtiene listado completo de tipos de documentos disponibles en el sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Listado obtenido exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    public ResponseEntity<?> listarTodos(
            @RequestHeader(value = HEADER_USER, required = false) Long usuarioIdHeader,
            @RequestHeader(value = HEADER_ROLE, required = false) Long rolIdHeader) {

        if (isNoAutorizado(usuarioIdHeader, rolIdHeader)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(tipoDocumentoService.listarTodos());
    }

    /**
     * Obtiene un tipo de documento específico por ID.
     * 🟡 PROTEGIDO: Cualquier usuario autenticado.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtener tipo de documento por ID",
            description = "🟡 PROTEGIDO (Usuarios Autenticados): Consulta un tipo de documento específico por su identificador")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tipo de documento encontrado"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "404", description = "Tipo de documento no encontrado")
    })
    public ResponseEntity<?> obtenerPorId(
            @PathVariable Long id,
            @RequestHeader(value = HEADER_USER, required = false) Long usuarioIdHeader,
            @RequestHeader(value = HEADER_ROLE, required = false) Long rolIdHeader) {

        if (isNoAutorizado(usuarioIdHeader, rolIdHeader)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(tipoDocumentoService.obtenerPorId(id));
    }

    /**
     * Crea un nuevo tipo de documento.
     * 🔴 BLINDADO: SOLO ADMIN.
     */
    @PostMapping
    @Operation(summary = "Crear nuevo tipo de documento",
            description = "🔴 BLINDADO (Solo Admin): Registra un nuevo tipo de documento en el sistema. Requiere permisos de administrador.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Tipo de documento creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado: Solo administradores")
    })
    public ResponseEntity<?> crear(
            @Valid @RequestBody TipoDocumentoDTO tipoDocDTO,
            @RequestHeader(value = HEADER_USER, required = false) Long usuarioIdHeader,
            @RequestHeader(value = HEADER_ROLE, required = false) Long rolIdHeader) {

        if (isNoAutorizado(usuarioIdHeader, rolIdHeader)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (!ROL_ADMIN.equals(rolIdHeader)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Acceso denegado: Solo los administradores pueden crear nuevos tipos de documentos.");
        }

        TipoDocumentoDTO created = tipoDocumentoService.crear(tipoDocDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Actualiza un tipo de documento existente.
     * 🔴 BLINDADO: SOLO ADMIN.
     */
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar tipo de documento",
            description = "🔴 BLINDADO (Solo Admin): Actualiza la información de un tipo de documento existente. Requiere permisos de administrador.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tipo de documento actualizado exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado: Solo administradores"),
            @ApiResponse(responseCode = "404", description = "Tipo de documento no encontrado")
    })
    public ResponseEntity<?> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody TipoDocumentoDTO tipoDocDTO,
            @RequestHeader(value = HEADER_USER, required = false) Long usuarioIdHeader,
            @RequestHeader(value = HEADER_ROLE, required = false) Long rolIdHeader) {

        if (isNoAutorizado(usuarioIdHeader, rolIdHeader)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (!ROL_ADMIN.equals(rolIdHeader)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Acceso denegado: Solo los administradores pueden actualizar tipos de documentos.");
        }

        return ResponseEntity.ok(tipoDocumentoService.actualizar(id, tipoDocDTO));
    }

    /**
     * Elimina un tipo de documento.
     * 🔴 BLINDADO: SOLO ADMIN.
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar tipo de documento",
            description = "🔴 BLINDADO (Solo Admin): Elimina permanentemente un tipo de documento del sistema. Requiere permisos de administrador.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Tipo de documento eliminado exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado: Solo administradores"),
            @ApiResponse(responseCode = "404", description = "Tipo de documento no encontrado")
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
                    .body("Acceso denegado: Solo los administradores pueden eliminar tipos de documentos.");
        }

        tipoDocumentoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}