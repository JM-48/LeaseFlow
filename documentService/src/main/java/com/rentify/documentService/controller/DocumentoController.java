package com.rentify.documentService.controller;

import com.rentify.documentService.dto.ActualizarEstadoRequest;
import com.rentify.documentService.dto.DocumentoDTO;
import com.rentify.documentService.service.DocumentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para gestion de documentos de usuarios.
 * Provee endpoints para subir, consultar, actualizar y eliminar documentos.
 * Completamente blindado con validaciones de cabeceras de seguridad.
 */
@RestController
@RequestMapping("/api/documentos")
@RequiredArgsConstructor
@Tag(name = "Documentos", description = "Gestion de documentos de usuarios")
public class DocumentoController {

    private final DocumentoService documentoService;

    /**
     * Crea/sube un nuevo documento.
     * (Forzamos a que el documento quede a nombre del usuario logueado)
     */
    @PostMapping
    @Operation(summary = "Subir nuevo documento",
            description = "Crea un nuevo documento. El usuario se asigna automáticamente desde el token.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Documento creado exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos o validacion de negocio fallida")
    })
    public ResponseEntity<?> crearDocumento(
            @Valid @RequestBody DocumentoDTO documentoDTO,
            @RequestHeader(value = "X-Usuario-Id", required = false) Long headerUsuarioId) {

        if (headerUsuarioId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no identificado.");
        }

        // Medida Anti-Fraude: Forzamos el ID del usuario del Header en el DTO,
        // ignorando cualquier ID que el usuario intente falsificar en el JSON.
        documentoDTO.setUsuarioId(headerUsuarioId);

        DocumentoDTO created = documentoService.crearDocumento(documentoDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Lista todos los documentos del sistema (SOLO ADMIN)
     */
    @GetMapping
    @Operation(summary = "Listar todos los documentos",
            description = "Obtiene listado completo de documentos. Requiere permisos de administrador.")
    public ResponseEntity<?> listarTodos(
            @RequestHeader(value = "X-Rol-Id", required = false) Long rolId,
            @RequestParam(defaultValue = "false") boolean includeDetails) {

        // Bloqueo estricto: Si no es Admin (1), se va rechazado
        if (rolId == null || rolId != 1L) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Acceso denegado: Solo los administradores pueden ver el listado global de documentos.");
        }

        return ResponseEntity.ok(documentoService.listarTodos(includeDetails));
    }

    /**
     * Obtiene un documento especifico por ID.
     * (Blindado: El Admin ve cualquiera, el usuario solo si es el dueño)
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtener documento por ID",
            description = "Consulta un documento especifico. Solo accesible por el dueño o un administrador.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Documento encontrado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado a documento ajeno"),
            @ApiResponse(responseCode = "404", description = "Documento no encontrado")
    })
    public ResponseEntity<?> obtenerPorId(
            @PathVariable Long id,
            @RequestHeader(value = "X-Usuario-Id", required = false) Long headerUsuarioId,
            @RequestHeader(value = "X-Rol-Id", required = false) Long rolId,
            @Parameter(description = "Incluir detalles expandidos")
            @RequestParam(defaultValue = "true") boolean includeDetails) {

        DocumentoDTO documento = documentoService.obtenerPorId(id, includeDetails);

        // Si no es Admin, validamos que el documento le pertenezca
        if (rolId != null && rolId != 1L) {
            if (!documento.getUsuarioId().equals(headerUsuarioId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Acceso denegado: No tienes permiso para ver este documento.");
            }
        }

        return ResponseEntity.ok(documento);
    }

    /**
     * Obtiene todos los documentos de un usuario especifico.
     * (ADMIN ve cualquiera, USUARIO solo ve los suyos)
     */
    @GetMapping("/usuario/{usuarioId}")
    @Operation(summary = "Obtener documentos por usuario",
            description = "Lista todos los documentos de un usuario. Solo accesible por el propio usuario o admin.")
    public ResponseEntity<?> obtenerPorUsuario(
            @PathVariable Long usuarioId,
            @RequestHeader(value = "X-Usuario-Id", required = false) Long headerUsuarioId,
            @RequestHeader(value = "X-Rol-Id", required = false) Long rolId,
            @RequestParam(defaultValue = "true") boolean includeDetails) {

        // Prevención de Fuga de Datos:
        // Si no es Admin, verificamos que el ID de la URL coincida con el ID de su token
        if (rolId != null && rolId != 1L) {
            if (!usuarioId.equals(headerUsuarioId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Acceso denegado: No puedes ver los documentos de otros usuarios.");
            }
        }

        return ResponseEntity.ok(documentoService.obtenerPorUsuario(usuarioId, includeDetails));
    }

    /**
     * Verifica si un usuario tiene documentos aprobados.
     * (Blindado: Solo Admin o el propio dueño pueden consultar esto)
     */
    @GetMapping("/usuario/{usuarioId}/verificar-aprobados")
    @Operation(summary = "Verificar si usuario tiene documentos aprobados",
            description = "Verifica si un usuario tiene al menos un documento con estado ACEPTADO.")
    public ResponseEntity<?> verificarDocumentosAprobados(
            @Parameter(description = "ID del usuario a verificar")
            @PathVariable Long usuarioId,
            @RequestHeader(value = "X-Usuario-Id", required = false) Long headerUsuarioId,
            @RequestHeader(value = "X-Rol-Id", required = false) Long rolId) {

        if (rolId != null && rolId != 1L) {
            if (!usuarioId.equals(headerUsuarioId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Acceso denegado: No puedes consultar el estado de otros usuarios.");
            }
        }

        boolean hasApproved = documentoService.hasApprovedDocuments(usuarioId);
        return ResponseEntity.ok(hasApproved);
    }

    /**
     * Actualiza el estado de un documento (sin observaciones).
     * (Blindado: ESTRICTAMENTE SOLO ADMIN)
     */
    @PatchMapping("/{id}/estado/{estadoId}")
    @Operation(summary = "Actualizar estado de documento",
            description = "Cambia el estado de un documento. Requiere permisos de administrador.")
    public ResponseEntity<?> actualizarEstado(
            @PathVariable Long id,
            @PathVariable Long estadoId,
            @RequestHeader(value = "X-Rol-Id", required = false) Long rolId) {

        if (rolId == null || rolId != 1L) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Acceso denegado: Solo los administradores pueden cambiar el estado de los documentos.");
        }

        return ResponseEntity.ok(documentoService.actualizarEstado(id, estadoId));
    }

    /**
     * NUEVO: Actualiza el estado de un documento CON observaciones.
     * (Blindado: ESTRICTAMENTE SOLO ADMIN. Inyectamos quién lo revisó automáticamente)
     */
    @PatchMapping("/{id}/estado")
    @Operation(summary = "Actualizar estado con observaciones",
            description = "Cambia el estado de un documento incluyendo motivo. Requiere permisos de administrador.")
    public ResponseEntity<?> actualizarEstadoConObservaciones(
            @Parameter(description = "ID del documento")
            @PathVariable Long id,
            @RequestHeader(value = "X-Rol-Id", required = false) Long rolId,
            @RequestHeader(value = "X-Usuario-Id", required = false) Long adminId,
            @Valid @RequestBody ActualizarEstadoRequest request) {

        if (rolId == null || rolId != 1L) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Acceso denegado: Solo los administradores pueden evaluar documentos.");
        }

        // Medida Anti-Fraude: Forzamos que el "revisadoPor" sea el ID del Admin que hace la petición
        request.setRevisadoPor(adminId);

        return ResponseEntity.ok(documentoService.actualizarEstadoConObservaciones(id, request));
    }

    /**
     * Endpoint alternativo (DEPRECATED).
     * Mantenemos el blindaje por si aún se consume.
     */
    @GetMapping("/usuario/{usuarioId}/aprobados")
    @Operation(summary = "Verificar documentos aprobados (DEPRECATED)",
            description = "DEPRECATED: Usar /usuario/{usuarioId}/verificar-aprobados en su lugar")
    @Deprecated
    public ResponseEntity<?> hasApprovedDocuments(
            @PathVariable Long usuarioId,
            @RequestHeader(value = "X-Usuario-Id", required = false) Long headerUsuarioId,
            @RequestHeader(value = "X-Rol-Id", required = false) Long rolId) {

        if (rolId != null && rolId != 1L) {
            if (!usuarioId.equals(headerUsuarioId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Acceso denegado.");
            }
        }
        return ResponseEntity.ok(documentoService.hasApprovedDocuments(usuarioId));
    }

    /**
     * Elimina un documento.
     * (Blindado: Admin puede borrar cualquiera. Usuario solo puede borrar los suyos)
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar documento",
            description = "Elimina permanentemente un documento. Solo accesible por el dueño o un administrador.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Documento eliminado exitosamente"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado a documento ajeno"),
            @ApiResponse(responseCode = "404", description = "Documento no encontrado")
    })
    public ResponseEntity<?> eliminarDocumento(
            @PathVariable Long id,
            @RequestHeader(value = "X-Usuario-Id", required = false) Long headerUsuarioId,
            @RequestHeader(value = "X-Rol-Id", required = false) Long rolId) {

        // Buscamos el documento primero para saber de quién es
        DocumentoDTO documento = documentoService.obtenerPorId(id, false);

        // Si no es Admin, validamos que sea el dueño
        if (rolId != null && rolId != 1L) {
            if (!documento.getUsuarioId().equals(headerUsuarioId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Acceso denegado: No puedes eliminar documentos que no te pertenecen.");
            }
        }

        documentoService.eliminarDocumento(id);
        return ResponseEntity.noContent().build();
    }
}