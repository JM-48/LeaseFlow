package com.rentify.applicationService.controller;

import com.rentify.applicationService.dto.SolicitudArriendoDTO;
import com.rentify.applicationService.service.SolicitudArriendoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/solicitudes")
@RequiredArgsConstructor
@Tag(name = "Solicitudes de Arriendo", description = "Gestión de solicitudes de arriendo")
public class SolicitudController {

    private static final Long ROL_ADMIN = 1L;

    private final SolicitudArriendoService service;

    @PostMapping
    @Operation(summary = "Crear nueva solicitud", description = "Crea una nueva solicitud de arriendo")
    public ResponseEntity<SolicitudArriendoDTO> crearSolicitud(
            @Valid @RequestBody SolicitudArriendoDTO solicitudDTO) {
        SolicitudArriendoDTO created = service.crearSolicitud(solicitudDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    @Operation(summary = "Listar solicitudes (Protegido por Rol)",
            description = "Los usuarios normales solo ven las suyas, los ADMIN ven todas")
    public ResponseEntity<List<SolicitudArriendoDTO>> listarSolicitudesSeguras(
            @Parameter(description = "Incluir detalles de usuario y propiedad")
            @RequestParam(defaultValue = "false") boolean includeDetails,

            @RequestHeader(value = "X-Usuario-Id", required = false) Long usuarioId,
            @RequestHeader(value = "X-Rol-Id", required = false) Long rolId) {

        if (usuarioId == null || rolId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<SolicitudArriendoDTO> solicitudes = service.listarSolicitudesSeguras(usuarioId, rolId, includeDetails);
        return ResponseEntity.ok(solicitudes);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener solicitud por ID", description = "Requiere headers de identificación")
    public ResponseEntity<SolicitudArriendoDTO> obtenerPorId(
            @PathVariable Long id,
            @RequestParam(defaultValue = "true") boolean includeDetails,
            @RequestHeader(value = "X-Usuario-Id", required = false) Long usuarioId,
            @RequestHeader(value = "X-Rol-Id", required = false) Long rolId) {

        if (usuarioId == null || rolId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(service.obtenerPorId(id, includeDetails));
    }

    @GetMapping("/usuario/{usuarioId}")
    @Operation(summary = "Obtener solicitudes por usuario",
            description = "Requiere headers de identificación. Un usuario normal solo puede consultar sus propias solicitudes")
    public ResponseEntity<List<SolicitudArriendoDTO>> obtenerPorUsuario(
            @PathVariable Long usuarioId,
            @Parameter(description = "Incluir detalles de usuario y propiedad")
            @RequestParam(defaultValue = "false") boolean includeDetails,
            @RequestHeader(value = "X-Usuario-Id", required = false) Long usuarioIdHeader,
            @RequestHeader(value = "X-Rol-Id", required = false) Long rolId) {

        if (usuarioIdHeader == null || rolId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        boolean esAdmin = ROL_ADMIN.equals(rolId);
        if (!esAdmin && !usuarioIdHeader.equals(usuarioId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(service.obtenerPorUsuario(usuarioId, includeDetails));
    }

    @GetMapping("/propiedad/{propiedadId}")
    @Operation(summary = "Obtener solicitudes por propiedad", description = "Requiere headers de identificación")
    public ResponseEntity<List<SolicitudArriendoDTO>> obtenerPorPropiedad(
            @PathVariable Long propiedadId,
            @RequestHeader(value = "X-Usuario-Id", required = false) Long usuarioId,
            @RequestHeader(value = "X-Rol-Id", required = false) Long rolId) {

        if (usuarioId == null || rolId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(service.obtenerPorPropiedad(propiedadId));
    }

    @PatchMapping("/{id}/estado")
    @Operation(summary = "Actualizar estado de solicitud", description = "Requiere headers de identificación")
    public ResponseEntity<SolicitudArriendoDTO> actualizarEstado(
            @PathVariable Long id,
            @RequestParam String estado,
            @RequestHeader(value = "X-Usuario-Id", required = false) Long usuarioId,
            @RequestHeader(value = "X-Rol-Id", required = false) Long rolId) {

        if (usuarioId == null || rolId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(service.actualizarEstado(id, estado));
    }
}