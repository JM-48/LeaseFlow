package com.rentify.applicationService.controller;

import com.rentify.applicationService.dto.RegistroArriendoDTO;
import com.rentify.applicationService.service.RegistroArriendoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/registros")
@RequiredArgsConstructor
@Tag(name = "Registros de Arriendo", description = "Gestión de registros de arriendos activos")
public class RegistroController {

    private final RegistroArriendoService service;

    @PostMapping
    @Operation(summary = "Crear nuevo registro", description = "Crea un registro de arriendo para una solicitud aceptada")
    public ResponseEntity<RegistroArriendoDTO> crearRegistro(
            @Valid @RequestBody RegistroArriendoDTO registroDTO) {
        RegistroArriendoDTO created = service.crearRegistro(registroDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    @Operation(summary = "Listar todos los registros", description = "Requiere headers de identificación")
    public ResponseEntity<List<RegistroArriendoDTO>> listarTodos(
            @RequestParam(defaultValue = "false") boolean includeDetails,
            @RequestHeader(value = "X-Usuario-Id", required = false) Long usuarioId,
            @RequestHeader(value = "X-Rol-Id", required = false) Long rolId) {

        if (usuarioId == null || rolId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(service.listarTodos(includeDetails));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener registro por ID", description = "Requiere headers de identificación")
    public ResponseEntity<RegistroArriendoDTO> obtenerPorId(
            @PathVariable Long id,
            @RequestParam(defaultValue = "true") boolean includeDetails,
            @RequestHeader(value = "X-Usuario-Id", required = false) Long usuarioId,
            @RequestHeader(value = "X-Rol-Id", required = false) Long rolId) {

        if (usuarioId == null || rolId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(service.obtenerPorId(id, includeDetails));
    }

    @GetMapping("/solicitud/{solicitudId}")
    @Operation(summary = "Obtener registros por solicitud", description = "Requiere headers de identificación")
    public ResponseEntity<List<RegistroArriendoDTO>> obtenerPorSolicitud(
            @PathVariable Long solicitudId,
            @RequestHeader(value = "X-Usuario-Id", required = false) Long usuarioId,
            @RequestHeader(value = "X-Rol-Id", required = false) Long rolId) {

        if (usuarioId == null || rolId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(service.obtenerPorSolicitud(solicitudId));
    }

    @PatchMapping("/{id}/finalizar")
    @Operation(summary = "Finalizar registro", description = "Marca un registro como inactivo. Requiere headers de identificación")
    public ResponseEntity<RegistroArriendoDTO> finalizarRegistro(
            @PathVariable Long id,
            @RequestHeader(value = "X-Usuario-Id", required = false) Long usuarioId,
            @RequestHeader(value = "X-Rol-Id", required = false) Long rolId) {

        if (usuarioId == null || rolId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(service.finalizarRegistro(id));
    }
}