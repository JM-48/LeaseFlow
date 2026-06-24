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
 * Incluye validación manual de tokens para asegurar el ecosistema.
 */
@Slf4j
@RestController
@RequestMapping("/api/tipo-resenas")
@RequiredArgsConstructor
@Tag(name = "Tipos de Reseña", description = "Gestión de tipos de reseña del sistema")
public class TipoResenaController {

    private final TipoResenaRepository tipoResenaRepository;
    private final ModelMapper modelMapper;

    /**
     * Valida si el token es nulo, vacío o no cumple con el formato Bearer.
     */
    private boolean isNoAutorizado(String token) {
        return token == null || token.isBlank() || !token.startsWith("Bearer ");
    }

    @GetMapping
    @Operation(summary = "Listar todos los tipos de reseña",
            description = "Obtiene todos los tipos de reseña disponibles en el sistema")
    public ResponseEntity<List<TipoResenaDTO>> getAll(
            @RequestHeader(value = "Authorization", required = false) String token) {

        if (isNoAutorizado(token)) {
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
    public ResponseEntity<TipoResenaDTO> getById(
            @RequestHeader(value = "Authorization", required = false) String token,
            @PathVariable Long id) {

        if (isNoAutorizado(token)) {
            log.warn("Intento de acceso no autorizado a GET /api/tipo-resenas/{}", id);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return tipoResenaRepository.findById(id)
                .map(tipo -> modelMapper.map(tipo, TipoResenaDTO.class))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Crear nuevo tipo de reseña",
            description = "Crea un nuevo tipo de reseña en el sistema")
    public ResponseEntity<TipoResenaDTO> create(
            @RequestHeader(value = "Authorization", required = false) String token,
            @Valid @RequestBody TipoResenaDTO tipoResenaDTO) {

        if (isNoAutorizado(token)) {
            log.warn("Intento de acceso no autorizado a POST /api/tipo-resenas");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        TipoResena tipoResena = modelMapper.map(tipoResenaDTO, TipoResena.class);
        TipoResena saved = tipoResenaRepository.save(tipoResena);
        TipoResenaDTO dto = modelMapper.map(saved, TipoResenaDTO.class);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar tipo de reseña",
            description = "Actualiza la información de un tipo de reseña existente")
    public ResponseEntity<TipoResenaDTO> update(
            @RequestHeader(value = "Authorization", required = false) String token,
            @PathVariable Long id,
            @Valid @RequestBody TipoResenaDTO tipoResenaDTO) {

        if (isNoAutorizado(token)) {
            log.warn("Intento de acceso no autorizado a PUT /api/tipo-resenas/{}", id);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
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
    @Operation(summary = "Eliminar tipo de reseña",
            description = "Elimina un tipo de reseña del sistema")
    public ResponseEntity<Void> delete(
            @RequestHeader(value = "Authorization", required = false) String token,
            @PathVariable Long id) {

        if (isNoAutorizado(token)) {
            log.warn("Intento de acceso no autorizado a DELETE /api/tipo-resenas/{}", id);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (tipoResenaRepository.existsById(id)) {
            tipoResenaRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}