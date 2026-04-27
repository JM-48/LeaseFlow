package com.rentify.reviewService.controller;

import com.rentify.reviewService.dto.TipoResenaDTO;
import com.rentify.reviewService.model.TipoResena;
import com.rentify.reviewService.repository.TipoResenaRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controlador REST para la gestión de tipos de reseña.
 */
@RestController
@RequestMapping("/api/tipo-resenas")
@RequiredArgsConstructor
@Tag(name = "Tipos de Reseña", description = "Gestión de tipos de reseña del sistema")
public class TipoResenaController {

    private final TipoResenaRepository tipoResenaRepository;
    private final ModelMapper modelMapper;

    @GetMapping
    @Operation(summary = "Listar todos los tipos de reseña",
            description = "Obtiene todos los tipos de reseña disponibles en el sistema")
    public ResponseEntity<List<TipoResenaDTO>> getAll() {
        List<TipoResenaDTO> tipos = tipoResenaRepository.findAll().stream()
                .map(tipo -> modelMapper.map(tipo, TipoResenaDTO.class))
                .collect(Collectors.toList());
        return ResponseEntity.ok(tipos);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener tipo de reseña por ID",
            description = "Obtiene los detalles de un tipo de reseña específico")
    public ResponseEntity<TipoResenaDTO> getById(@PathVariable Long id) {
        return tipoResenaRepository.findById(id)
                .map(tipo -> modelMapper.map(tipo, TipoResenaDTO.class))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Crear nuevo tipo de reseña",
            description = "Crea un nuevo tipo de reseña en el sistema")
    public ResponseEntity<TipoResenaDTO> create(@Valid @RequestBody TipoResenaDTO tipoResenaDTO) {
        TipoResena tipoResena = modelMapper.map(tipoResenaDTO, TipoResena.class);
        TipoResena saved = tipoResenaRepository.save(tipoResena);
        TipoResenaDTO dto = modelMapper.map(saved, TipoResenaDTO.class);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar tipo de reseña",
            description = "Actualiza la información de un tipo de reseña existente")
    public ResponseEntity<TipoResenaDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody TipoResenaDTO tipoResenaDTO) {
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
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (tipoResenaRepository.existsById(id)) {
            tipoResenaRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}