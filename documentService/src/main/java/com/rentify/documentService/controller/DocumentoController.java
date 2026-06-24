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
 * Completamente protegido con validaciones de cabeceras de seguridad.
 */
@RestController
@RequestMapping("/api/documentos")
@RequiredArgsConstructor
@Tag(name = "Documentos", description = "Gestión de documentos de usuarios")
public class DocumentoController {

    private final DocumentoService documentoService;

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
     * Crea/sube un nuevo documento.
     * ? PROTEGIDO: Cualquier usuario (el documento queda a su nombre).
     */
    @PostMapping
    @Operation(summary = "Subir nuevo documento",
            description = "? PROTEGIDO (Usuarios Autenticados): Crea un nuevo documento. El usuario se asigna automáticamente desde el token.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Documento creado exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o validación de negocio fallida")
    })
    public ResponseEntity<?> crearDocumento(
            @Valid @RequestBody DocumentoDTO documentoDTO,
            @RequestHeader(value = HEADER_USER, required = false) Long usuarioIdHeader,
            @RequestHeader(value = HEADER_ROLE, required = false) Long rolIdHeader) {

        if (isNoAutorizado(usuarioIdHeader, rolIdHeader)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Acceso denegado: Cabeceras de identidad ausentes.");
        }

        // Medida Anti-Fraude: Forzamos el ID del usuario del Header en el DTO,
        // ignorando cualquier ID que el usuario intente falsificar en el JSON.
        documentoDTO.setUsuarioId(usuarioIdHeader);

        DocumentoDTO created = documentoService.crearDocumento(documentoDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Lista todos los documentos del sistema.
     * ? BLINDADO: Solo Administradores.
     */
    @GetMapping
    @Operation(summary = "Listar todos los documentos",
            description = "? BLINDADO (Solo Admin): Obtiene el listado completo de documentos. Requiere permisos de administrador.")
    public ResponseEntity<?> listarTodos(
            @RequestHeader(value = HEADER_USER, required = false) Long usuarioIdHeader,
            @RequestHeader(value = HEADER_ROLE, required = false) Long rolIdHeader,
            @RequestParam(defaultValue = "false") boolean includeDetails) {

        if (isNoAutorizado(usuarioIdHeader, rolIdHeader)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (!ROL_ADMIN.equals(rolIdHeader)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Acceso denegado: Solo los administradores pueden ver el listado global de documentos.");
        }

        return ResponseEntity.ok(documentoService.listarTodos(includeDetails));
    }

    /**
     * Obtiene un documento específico por ID.
     * ? PROTEGIDO: Administrador o Dueño del documento.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtener documento por ID",
            description = "? PROTEGIDO (Admin o Dueño): Consulta un documento específico. Solo accesible por el dueño o un administrador.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Documento encontrado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado a documento ajeno"),
            @ApiResponse(responseCode = "404", description = "Documento no encontrado")
    })
    public ResponseEntity<?> obtenerPorId(
            @PathVariable Long id,
            @RequestHeader(value = HEADER_USER, required = false) Long usuarioIdHeader,
            @RequestHeader(value = HEADER_ROLE, required = false) Long rolIdHeader,
            @Parameter(description = "Incluir detalles expandidos")
            @RequestParam(defaultValue = "true") boolean includeDetails) {

        if (isNoAutorizado(usuarioIdHeader, rolIdHeader)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        DocumentoDTO documento = documentoService.obtenerPorId(id, includeDetails);

        // Si no es Admin, validamos que el documento le pertenezca
        if (!ROL_ADMIN.equals(rolIdHeader) && !documento.getUsuarioId().equals(usuarioIdHeader)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Acceso denegado: No tienes permiso para ver este documento.");
        }

        return ResponseEntity.ok(documento);
    }

    /**
     * Obtiene todos los documentos de un usuario específico.
     * ? PROTEGIDO: Administrador o Dueño del recurso.
     */
    @GetMapping("/usuario/{usuarioId}")
    @Operation(summary = "Obtener documentos por usuario",
            description = "? PROTEGIDO (Admin o Dueño): Lista todos los documentos de un usuario. Solo accesible por el propio usuario o admin.")
    public ResponseEntity<?> obtenerPorUsuario(
            @PathVariable Long usuarioId,
            @RequestHeader(value = HEADER_USER, required = false) Long usuarioIdHeader,
            @RequestHeader(value = HEADER_ROLE, required = false) Long rolIdHeader,
            @RequestParam(defaultValue = "true") boolean includeDetails) {

        if (isNoAutorizado(usuarioIdHeader, rolIdHeader)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Prevención de Fuga de Datos
        if (!ROL_ADMIN.equals(rolIdHeader) && !usuarioId.equals(usuarioIdHeader)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Acceso denegado: No puedes ver los documentos de otros usuarios.");
        }

        return ResponseEntity.ok(documentoService.obtenerPorUsuario(usuarioId, includeDetails));
    }

    /**
     * Verifica si un usuario tiene documentos aprobados.
     * ? PROTEGIDO: Administrador o Dueño del recurso.
     */
    @GetMapping("/usuario/{usuarioId}/verificar-aprobados")
    @Operation(summary = "Verificar si usuario tiene documentos aprobados",
            description = "? PROTEGIDO (Admin o Dueño): Verifica si un usuario tiene al menos un documento con estado ACEPTADO.")
    public ResponseEntity<?> verificarDocumentosAprobados(
            @Parameter(description = "ID del usuario a verificar")
            @PathVariable Long usuarioId,
            @RequestHeader(value = HEADER_USER, required = false) Long usuarioIdHeader,
            @RequestHeader(value = HEADER_ROLE, required = false) Long rolIdHeader) {

        if (isNoAutorizado(usuarioIdHeader, rolIdHeader)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (!ROL_ADMIN.equals(rolIdHeader) && !usuarioId.equals(usuarioIdHeader)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Acceso denegado: No puedes consultar el estado de otros usuarios.");
        }

        boolean hasApproved = documentoService.hasApprovedDocuments(usuarioId);
        return ResponseEntity.ok(hasApproved);
    }

    /**
     * Actualiza el estado de un documento (sin observaciones).
     * ? BLINDADO: Solo Administradores.
     */
    @PatchMapping("/{id}/estado/{estadoId}")
    @Operation(summary = "Actualizar estado de documento",
            description = "? BLINDADO (Solo Admin): Cambia el estado de un documento. Requiere permisos de administrador.")
    public ResponseEntity<?> actualizarEstado(
            @PathVariable Long id,
            @PathVariable Long estadoId,
            @RequestHeader(value = HEADER_USER, required = false) Long usuarioIdHeader,
            @RequestHeader(value = HEADER_ROLE, required = false) Long rolIdHeader) {

        if (isNoAutorizado(usuarioIdHeader, rolIdHeader)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (!ROL_ADMIN.equals(rolIdHeader)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Acceso denegado: Solo los administradores pueden cambiar el estado de los documentos.");
        }

        return ResponseEntity.ok(documentoService.actualizarEstado(id, estadoId));
    }

    /**
     * Actualiza el estado de un documento CON observaciones.
     * ? BLINDADO: Solo Administradores.
     */
    @PatchMapping("/{id}/estado")
    @Operation(summary = "Actualizar estado con observaciones",
            description = "? BLINDADO (Solo Admin): Cambia el estado de un documento incluyendo motivo. Requiere permisos de administrador.")
    public ResponseEntity<?> actualizarEstadoConObservaciones(
            @Parameter(description = "ID del documento")
            @PathVariable Long id,
            @RequestHeader(value = HEADER_USER, required = false) Long usuarioIdHeader,
            @RequestHeader(value = HEADER_ROLE, required = false) Long rolIdHeader,
            @Valid @RequestBody ActualizarEstadoRequest request) {

        if (isNoAutorizado(usuarioIdHeader, rolIdHeader)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (!ROL_ADMIN.equals(rolIdHeader)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Acceso denegado: Solo los administradores pueden evaluar documentos.");
        }

        // Medida Anti-Fraude: Forzamos que el "revisadoPor" sea el ID del Admin que hace la petición
        request.setRevisadoPor(usuarioIdHeader);

        return ResponseEntity.ok(documentoService.actualizarEstadoConObservaciones(id, request));
    }

    /**
     * Endpoint alternativo (DEPRECATED).
     * ? PROTEGIDO: Administrador o Dueño del recurso.
     */
    @GetMapping("/usuario/{usuarioId}/aprobados")
    @Operation(summary = "Verificar documentos aprobados (DEPRECATED)",
            description = "? PROTEGIDO (Admin o Dueño): DEPRECATED: Usar /usuario/{usuarioId}/verificar-aprobados en su lugar")
    @Deprecated
    public ResponseEntity<?> hasApprovedDocuments(
            @PathVariable Long usuarioId,
            @RequestHeader(value = HEADER_USER, required = false) Long usuarioIdHeader,
            @RequestHeader(value = HEADER_ROLE, required = false) Long rolIdHeader) {

        if (isNoAutorizado(usuarioIdHeader, rolIdHeader)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (!ROL_ADMIN.equals(rolIdHeader) && !usuarioId.equals(usuarioIdHeader)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Acceso denegado.");
        }
        return ResponseEntity.ok(documentoService.hasApprovedDocuments(usuarioId));
    }

    /**
     * Elimina un documento.
     * ? PROTEGIDO: Administrador o Dueño del documento.
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar documento",
            description = "? PROTEGIDO (Admin o Dueño): Elimina permanentemente un documento. Solo accesible por el dueño o un administrador.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Documento eliminado exitosamente"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado a documento ajeno"),
            @ApiResponse(responseCode = "404", description = "Documento no encontrado")
    })
    public ResponseEntity<?> eliminarDocumento(
            @PathVariable Long id,
            @RequestHeader(value = HEADER_USER, required = false) Long usuarioIdHeader,
            @RequestHeader(value = HEADER_ROLE, required = false) Long rolIdHeader) {

        if (isNoAutorizado(usuarioIdHeader, rolIdHeader)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Buscamos el documento primero para saber de quién es
        DocumentoDTO documento = documentoService.obtenerPorId(id, false);

        // Si no es Admin, validamos que sea el dueño
        if (!ROL_ADMIN.equals(rolIdHeader) && !documento.getUsuarioId().equals(usuarioIdHeader)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Acceso denegado: No puedes eliminar documentos que no te pertenecen.");
        }

        documentoService.eliminarDocumento(id);
        return ResponseEntity.noContent().build();
    }
}