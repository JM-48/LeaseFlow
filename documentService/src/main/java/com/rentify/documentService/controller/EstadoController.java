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
     */
    @GetMapping
    @Operation(summary = "Listar todos los estados",
            description = "Obtiene listado completo de estados de documentos disponibles")
    public ResponseEntity<List<EstadoDTO>> listarTodos() {
        return ResponseEntity.ok(estadoService.listarTodos());
    }

    /**
     * Obtiene un estado específico por ID.
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
     */
    @PostMapping
    @Operation(summary = "Crear nuevo estado",
            description = "Crea un nuevo estado de documento en el sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Estado creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    public ResponseEntity<EstadoDTO> crear(@Valid @RequestBody EstadoDTO estadoDTO) {
        EstadoDTO created = estadoService.crear(estadoDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Actualiza un estado existente.
     */
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar estado",
            description = "Actualiza la información de un estado existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estado actualizado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Estado no encontrado")
    })
    public ResponseEntity<EstadoDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody EstadoDTO estadoDTO) {
        return ResponseEntity.ok(estadoService.actualizar(id, estadoDTO));
    }

    /**
     * Elimina un estado.
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar estado",
            description = "Elimina permanentemente un estado del sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Estado eliminado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Estado no encontrado")
    })
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        estadoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}