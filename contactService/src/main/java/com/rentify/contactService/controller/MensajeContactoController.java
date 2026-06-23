package com.rentify.contactService.controller;

import com.rentify.contactService.dto.MensajeContactoDTO;
import com.rentify.contactService.dto.RespuestaMensajeDTO;
import com.rentify.contactService.service.MensajeContactoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/contacto")
@RequiredArgsConstructor
@Tag(name = "Mensajes de Contacto", description = "Gestión de mensajes de contacto de usuarios")
public class MensajeContactoController {

    private final MensajeContactoService service;

    @PostMapping
    @Operation(summary = "Crear nuevo mensaje de contacto",
            description = "Permite a cualquier usuario enviar un mensaje de contacto")
    public ResponseEntity<MensajeContactoDTO> crearMensaje(
            @Valid @RequestBody MensajeContactoDTO mensajeDTO) {
        MensajeContactoDTO created = service.crearMensaje(mensajeDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // --- ENDPOINT CORREGIDO: Seguridad de Listado ---
    @GetMapping
    @Operation(summary = "Listar mensajes (Protegido)",
            description = "Usuarios normales ven los suyos, ADMIN ve todos")
    public ResponseEntity<List<MensajeContactoDTO>> listarMensajesSeguros(
            @Parameter(description = "Incluir detalles del usuario")
            @RequestParam(defaultValue = "false") boolean includeDetails,
            @RequestHeader(value = "X-Usuario-Id", required = false) Long usuarioId,
            @RequestHeader(value = "X-Rol-Id", required = false) Long rolId) {

        // Si no envían credenciales en los Headers, devolvemos 401 Unauthorized
        if (usuarioId == null || rolId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Llamamos al nuevo método seguro en el Service
        return ResponseEntity.ok(service.listarMensajesSeguros(usuarioId, rolId, includeDetails));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener mensaje por ID",
            description = "Obtiene un mensaje específico por su ID")
    public ResponseEntity<MensajeContactoDTO> obtenerPorId(
            @PathVariable Long id,
            @RequestParam(defaultValue = "true") boolean includeDetails) {
        return ResponseEntity.ok(service.obtenerPorId(id, includeDetails));
    }

    @GetMapping("/email/{email}")
    @Operation(summary = "Listar mensajes por email",
            description = "Obtiene todos los mensajes enviados por un email específico")
    public ResponseEntity<List<MensajeContactoDTO>> listarPorEmail(
            @PathVariable String email) {
        return ResponseEntity.ok(service.listarPorEmail(email));
    }

    @GetMapping("/usuario/{usuarioId}")
    @Operation(summary = "Listar mensajes por usuario",
            description = "Obtiene todos los mensajes de un usuario autenticado")
    public ResponseEntity<List<MensajeContactoDTO>> listarPorUsuario(
            @PathVariable Long usuarioId) {
        return ResponseEntity.ok(service.listarPorUsuario(usuarioId));
    }

    @GetMapping("/estado/{estado}")
    @Operation(summary = "Listar mensajes por estado",
            description = "Filtra mensajes por estado: PENDIENTE, EN_PROCESO, RESUELTO")
    public ResponseEntity<List<MensajeContactoDTO>> listarPorEstado(
            @PathVariable String estado) {
        return ResponseEntity.ok(service.listarPorEstado(estado));
    }

    // --- ENDPOINT CORREGIDO: Solo Admin ---
    @GetMapping("/sin-responder")
    @Operation(summary = "Listar mensajes sin responder (Solo Admin)",
            description = "Obtiene todos los mensajes pendientes sin respuesta")
    public ResponseEntity<List<MensajeContactoDTO>> listarSinResponder(
            @RequestHeader(value = "X-Rol-Id", required = false) Long rolId) {

        // Bloqueo estricto: Si no hay rol o el rol no es 1 (Admin), lanzamos 403 Forbidden
        if (rolId == null || rolId != 1L) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(service.listarMensajesSinResponder());
    }

    @GetMapping("/buscar")
    @Operation(summary = "Buscar mensajes por palabra clave",
            description = "Busca mensajes que contengan la palabra clave en asunto o mensaje")
    public ResponseEntity<List<MensajeContactoDTO>> buscarPorPalabraClave(
            @RequestParam String keyword) {
        return ResponseEntity.ok(service.buscarPorPalabraClave(keyword));
    }

    @PatchMapping("/{id}/estado")
    @Operation(summary = "Actualizar estado del mensaje",
            description = "Cambia el estado de un mensaje (solo admin)")
    public ResponseEntity<MensajeContactoDTO> actualizarEstado(
            @PathVariable Long id,
            @RequestParam String estado) {
        return ResponseEntity.ok(service.actualizarEstado(id, estado));
    }

    @PostMapping("/{id}/responder")
    @Operation(summary = "Responder mensaje de contacto",
            description = "Permite a un admin responder un mensaje (solo admin)")
    public ResponseEntity<MensajeContactoDTO> responderMensaje(
            @PathVariable Long id,
            @Valid @RequestBody RespuestaMensajeDTO respuestaDTO) {
        return ResponseEntity.ok(service.responderMensaje(id, respuestaDTO));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar mensaje",
            description = "Elimina un mensaje de contacto (solo admin)")
    public ResponseEntity<Void> eliminarMensaje(
            @PathVariable Long id,
            @RequestParam Long adminId) {
        service.eliminarMensaje(id, adminId);
        return ResponseEntity.noContent().build();
    }

    // --- ENDPOINT CORREGIDO: Solo Admin ---
    @GetMapping("/estadisticas")
    @Operation(summary = "Obtener estadísticas (Solo Admin)",
            description = "Obtiene estadísticas de mensajes por estado")
    public ResponseEntity<Map<String, Long>> obtenerEstadisticas(
            @RequestHeader(value = "X-Rol-Id", required = false) Long rolId) {

        // Bloqueo estricto: Solo el Admin puede ver estadísticas globales
        if (rolId == null || rolId != 1L) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(service.obtenerEstadisticas());
    }
}