package com.rentify.reviewService.controller;

import com.rentify.reviewService.dto.TipoResenaDTO;
import com.rentify.reviewService.model.TipoResena;
import com.rentify.reviewService.repository.TipoResenaRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controlador REST para la gestión de tipos de reseña.
 * Protegido mediante las cabeceras de identidad inyectadas por el API Gateway.
 */
@Slf4j
@RestController
@RequestMapping("/api/tipo-resenas")
@RequiredArgsConstructor
@Tag(name = "Tipos de Reseña", description = "Gestión de tipos de reseña del sistema")
public class TipoResenaController {

    private final TipoResenaRepository tipoResenaRepository;
    private final ModelMapper modelMapper;

    private static final Long ROL_ADMIN = 1L;

    @GetMapping
    @Operation(summary = "Listar todos los tipos de reseña",
            description = "Obtiene todos los tipos de reseña disponibles en el sistema")
    public ResponseEntity<?> getAll(
            @RequestHeader(value = "X-Usuario-Id", required = false) Long usuarioIdHeader,
            @RequestHeader(value = "X-Rol-Id", required = false) Long rolIdHeader) {

        if (usuarioIdHeader == null || rolIdHeader == null) {
            log.warn("Intento de acceso no autorizado a GET /api/tipo-resenas");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<TipoResenaDTO> tipos = tipoResenaRepository.findAll().stream()
                .map(tipo -> modelMapper.map(tipo, TipoResenaDTO.class))
                .collect(Collectors.toList());
        return ResponseEntity.ok(tipos);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener tipo de reseña por ID",
            description = "Obtiene los detalles de un tipo de reseña específico")
    public ResponseEntity<?> getById(
            @RequestHeader(value = "X-Usuario-Id", required = false) Long usuarioIdHeader,
            @RequestHeader(value = "X-Rol-Id", required = false) Long rolIdHeader,
            @PathVariable Long id) {

        if (usuarioIdHeader == null || rolIdHeader == null) {
            log.warn("Intento de acceso no autorizado a GET /api/tipo-resenas/{}", id);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return tipoResenaRepository.findById(id)
                .map(tipo -> modelMapper.map(tipo, TipoResenaDTO.class))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Crear nuevo tipo de reseña (Solo Admin)",
            description = "Crea un nuevo tipo de reseña en el sistema")
    public ResponseEntity<?> create(
            @RequestHeader(value = "X-Usuario-Id", required = false) Long usuarioIdHeader,
            @RequestHeader(value = "X-Rol-Id", required = false) Long rolIdHeader,
            @Valid @RequestBody TipoResenaDTO tipoResenaDTO) {

        if (usuarioIdHeader == null || rolIdHeader == null) {
            log.warn("Intento de acceso no autorizado a POST /api/tipo-resenas");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (!ROL_ADMIN.equals(rolIdHeader)) {
            log.warn("Usuario {} intentó crear un TipoResena sin privilegios de administrador", usuarioIdHeader);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Acceso denegado: Solo los administradores pueden crear nuevos tipos de reseña.");
        }

        TipoResena tipoResena = modelMapper.map(tipoResenaDTO, TipoResena.class);
        TipoResena saved = tipoResenaRepository.save(tipoResena);
        TipoResenaDTO dto = modelMapper.map(saved, TipoResenaDTO.class);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar tipo de reseña (Solo Admin)",
            description = "Actualiza la información de un tipo de reseña existente")
    public ResponseEntity<?> update(
            @RequestHeader(value = "X-Usuario-Id", required = false) Long usuarioIdHeader,
            @RequestHeader(value = "X-Rol-Id", required = false) Long rolIdHeader,
            @PathVariable Long id,
            @Valid @RequestBody TipoResenaDTO tipoResenaDTO) {

        if (usuarioIdHeader == null || rolIdHeader == null) {
            log.warn("Intento de acceso no autorizado a PUT /api/tipo-resenas/{}", id);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (!ROL_ADMIN.equals(rolIdHeader)) {
            log.warn("Usuario {} intentó editar el TipoResena {} sin privilegios de administrador", usuarioIdHeader, id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Acceso denegado: Solo los administradores pueden actualizar tipos de reseña.");
        }

        return tipoResenaRepository.findById(id)
                .map(existing -> {
                    existing.setNombre(tipoResenaDTO.getNombre());
                    TipoResena updated = tipoResenaRepository.save(existing);
                    TipoResenaDTO dto = modelMapper.map(updated, TipoResenaDTO.class);
                    return ResponseEntity.ok(dto);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar tipo de reseña (Solo Admin)",
            description = "Elimina un tipo de reseña del sistema")
    public ResponseEntity<?> delete(
            @RequestHeader(value = "X-Usuario-Id", required = false) Long usuarioIdHeader,
            @RequestHeader(value = "X-Rol-Id", required = false) Long rolIdHeader,
            @PathVariable Long id) {

        if (usuarioIdHeader == null || rolIdHeader == null) {
            log.warn("Intento de acceso no autorizado a DELETE /api/tipo-resenas/{}", id);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (!ROL_ADMIN.equals(rolIdHeader)) {
            log.warn("Usuario {} intentó eliminar el TipoResena {} sin privilegios de administrador", usuarioIdHeader, id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Acceso denegado: Solo los administradores pueden eliminar tipos de reseña.");
        }

        if (tipoResenaRepository.existsById(id)) {
            tipoResenaRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}