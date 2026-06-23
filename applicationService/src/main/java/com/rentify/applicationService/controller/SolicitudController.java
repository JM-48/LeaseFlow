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

            // Extraemos los datos de quién hace la petición desde los Headers
            @RequestHeader(value = "X-Usuario-Id", required = false) Long usuarioId,
            @RequestHeader(value = "X-Rol-Id", required = false) Long rolId) {

        // Si alguien intenta entrar por el navegador directamente sin identificarse -> 401 No Autorizado
        if (usuarioId == null || rolId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Pasamos los datos a la nueva lógica de negocio
        List<SolicitudArriendoDTO> solicitudes = service.listarSolicitudesSeguras(usuarioId, rolId, includeDetails);
        return ResponseEntity.ok(solicitudes);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener solicitud por ID")
    public ResponseEntity<SolicitudArriendoDTO> obtenerPorId(
            @PathVariable Long id,
            @RequestParam(defaultValue = "true") boolean includeDetails) {
        return ResponseEntity.ok(service.obtenerPorId(id, includeDetails));
    }

    @GetMapping("/usuario/{usuarioId}")
    @Operation(summary = "Obtener solicitudes por usuario")
    public ResponseEntity<List<SolicitudArriendoDTO>> obtenerPorUsuario(
            @PathVariable Long usuarioId) {
        return ResponseEntity.ok(service.obtenerPorUsuario(usuarioId));
    }

    @GetMapping("/propiedad/{propiedadId}")
    @Operation(summary = "Obtener solicitudes por propiedad")
    public ResponseEntity<List<SolicitudArriendoDTO>> obtenerPorPropiedad(
            @PathVariable Long propiedadId) {
        return ResponseEntity.ok(service.obtenerPorPropiedad(propiedadId));
    }

    @PatchMapping("/{id}/estado")
    @Operation(summary = "Actualizar estado de solicitud")
    public ResponseEntity<SolicitudArriendoDTO> actualizarEstado(
            @PathVariable Long id,
            @RequestParam String estado) {
        return ResponseEntity.ok(service.actualizarEstado(id, estado));
    }
}