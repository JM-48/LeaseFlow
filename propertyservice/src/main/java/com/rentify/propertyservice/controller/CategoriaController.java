package com.rentify.propertyservice.controller;

import com.rentify.propertyservice.dto.CategoriaDTO;
import com.rentify.propertyservice.model.Categoria;
import com.rentify.propertyservice.repository.CategoriaRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller REST para gestión de categorías de propiedades.
 */
@RestController
@RequestMapping("/api/categorias")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Categorías", description = "Gestión de categorías de propiedades")
public class CategoriaController {

    private final CategoriaRepository categoriaRepository;
    private final ModelMapper modelMapper;

    @PostMapping
    @Operation(summary = "Crear categoría", description = "Crea una nueva categoría de propiedad")
    public ResponseEntity<CategoriaDTO> crear(@Valid @RequestBody CategoriaDTO categoriaDTO) {
        log.info("Creando nueva categoría: {}", categoriaDTO.getNombre());

        Categoria categoria = modelMapper.map(categoriaDTO, Categoria.class);
        Categoria saved = categoriaRepository.save(categoria);

        return ResponseEntity.created(URI.create("/api/categorias/" + saved.getId()))
                .body(modelMapper.map(saved, CategoriaDTO.class));
    }

    @GetMapping
    @Operation(summary = "Listar categorías", description = "Obtiene todas las categorías disponibles")
    public ResponseEntity<List<CategoriaDTO>> listar() {
        log.debug("Listando todas las categorías");

        List<CategoriaDTO> categorias = categoriaRepository.findAll().stream()
                .map(c -> modelMapper.map(c, CategoriaDTO.class))
                .collect(Collectors.toList());

        return ResponseEntity.ok(categorias);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener categoría por ID")
    public ResponseEntity<CategoriaDTO> obtenerPorId(
            @Parameter(description = "ID de la categoría", example = "1")
            @PathVariable Long id) {
        log.debug("Obteniendo categoría con ID: {}", id);

        return categoriaRepository.findById(id)
                .map(c -> ResponseEntity.ok(modelMapper.map(c, CategoriaDTO.class)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar categoría")
    public ResponseEntity<CategoriaDTO> actualizar(
            @Parameter(description = "ID de la categoría", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody CategoriaDTO categoriaDTO) {
        log.info("Actualizando categoría con ID: {}", id);

        return categoriaRepository.findById(id)
                .map(c -> {
                    c.setNombre(categoriaDTO.getNombre());
                    Categoria updated = categoriaRepository.save(c);
                    return ResponseEntity.ok(modelMapper.map(updated, CategoriaDTO.class));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar categoría")
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "ID de la categoría", example = "1")
            @PathVariable Long id) {
        log.info("Eliminando categoría con ID: {}", id);

        if (!categoriaRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        categoriaRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}