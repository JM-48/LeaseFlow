package com.rentify.userservice.controller;

import com.rentify.userservice.dto.RolDTO;
import com.rentify.userservice.service.RolService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller para gestión de roles
 * Endpoints: POST /api/roles, GET /api/roles, GET /api/roles/{id}, GET /api/roles/nombre/{nombre}
 */
@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
@Tag(name = "Roles", description = "Gestión de roles del sistema (ADMIN, PROPIETARIO, ARRIENDATARIO)")
public class RolController {

    private final RolService rolService;

    /**
     * Crea un nuevo rol
     * POST /api/roles
     */
    @PostMapping
    @Operation(summary = "Crear nuevo rol",
            description = "Crea un nuevo rol en el sistema. Roles válidos: ADMIN, PROPIETARIO, ARRIENDATARIO")
    public ResponseEntity<RolDTO> crearRol(@Valid @RequestBody RolDTO rolDTO) {
        RolDTO creado = rolService.crearRol(rolDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    /**
     * Obtiene todos los roles
     * GET /api/roles
     */
    @GetMapping
    @Operation(summary = "Listar todos los roles",
            description = "Obtiene la lista completa de roles del sistema")
    public ResponseEntity<List<RolDTO>> obtenerTodos() {
        return ResponseEntity.ok(rolService.obtenerTodos());
    }

    /**
     * Obtiene un rol por su ID
     * GET /api/roles/{id}
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtener rol por ID",
            description = "Obtiene un rol específico por su ID")
    public ResponseEntity<RolDTO> obtenerPorId(
            @Parameter(description = "ID del rol", example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(rolService.obtenerPorId(id));
    }

    /**
     * Obtiene un rol por su nombre
     * GET /api/roles/nombre/{nombre}
     */
    @GetMapping("/nombre/{nombre}")
    @Operation(summary = "Obtener rol por nombre",
            description = "Obtiene un rol específico por su nombre (ADMIN, PROPIETARIO, ARRIENDATARIO)")
    public ResponseEntity<RolDTO> obtenerPorNombre(
            @Parameter(description = "Nombre del rol", example = "ARRIENDATARIO")
            @PathVariable String nombre) {
        return ResponseEntity.ok(rolService.obtenerPorNombre(nombre));
    }
}