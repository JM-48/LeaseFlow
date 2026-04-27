package com.rentify.propertyservice.controller;

import com.rentify.propertyservice.dto.TipoDTO;
import com.rentify.propertyservice.model.Tipo;
import com.rentify.propertyservice.repository.TipoRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller REST para gestión de tipos de propiedades.
 */
@RestController
@RequestMapping("/api/tipos")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Tipos", description = "Gestión de tipos de propiedades")
public class TipoController {

    private final TipoRepository tipoRepository;
    private final ModelMapper modelMapper;

    @PostMapping
    @Operation(summary = "Crear tipo", description = "Crea un nuevo tipo de propiedad")
    public ResponseEntity<TipoDTO> crear(@Valid @RequestBody TipoDTO tipoDTO) {
        log.info("Creando nuevo tipo: {}", tipoDTO.getNombre());

        Tipo tipo = modelMapper.map(tipoDTO, Tipo.class);
        Tipo saved = tipoRepository.save(tipo);

        return ResponseEntity.created(URI.create("/api/tipos/" + saved.getId()))
                .body(modelMapper.map(saved, TipoDTO.class));
    }

    @GetMapping
    @Operation(summary = "Listar tipos", description = "Obtiene todos los tipos disponibles")
    public ResponseEntity<List<TipoDTO>> listar() {
        log.debug("Listando todos los tipos");

        List<TipoDTO> tipos = tipoRepository.findAll().stream()
                .map(t -> modelMapper.map(t, TipoDTO.class))
                .collect(Collectors.toList());

        return ResponseEntity.ok(tipos);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener tipo por ID")
    public ResponseEntity<TipoDTO> obtenerPorId(
            @Parameter(description = "ID del tipo", example = "1")
            @PathVariable Long id) {
        log.debug("Obteniendo tipo con ID: {}", id);

        return tipoRepository.findById(id)
                .map(t -> ResponseEntity.ok(modelMapper.map(t, TipoDTO.class)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar tipo")
    public ResponseEntity<TipoDTO> actualizar(
            @Parameter(description = "ID del tipo", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody TipoDTO tipoDTO) {
        log.info("Actualizando tipo con ID: {}", id);

        return tipoRepository.findById(id)
                .map(t -> {
                    t.setNombre(tipoDTO.getNombre());
                    Tipo updated = tipoRepository.save(t);
                    return ResponseEntity.ok(modelMapper.map(updated, TipoDTO.class));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar tipo")
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "ID del tipo", example = "1")
            @PathVariable Long id) {
        log.info("Eliminando tipo con ID: {}", id);

        if (!tipoRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        tipoRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}