package com.rentify.propertyservice.controller;

import com.rentify.propertyservice.dto.RegionDTO;
import com.rentify.propertyservice.model.Region;
import com.rentify.propertyservice.repository.RegionRepository;
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
 * Controller REST para gestión de regiones.
 */
@RestController
@RequestMapping("/api/regiones")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Regiones", description = "Gestión de regiones administrativas")
public class RegionController {

    private final RegionRepository regionRepository;
    private final ModelMapper modelMapper;

    @PostMapping
    @Operation(summary = "Crear región", description = "Crea una nueva región")
    public ResponseEntity<RegionDTO> crear(@Valid @RequestBody RegionDTO regionDTO) {
        log.info("Creando nueva región: {}", regionDTO.getNombre());

        Region region = modelMapper.map(regionDTO, Region.class);
        Region saved = regionRepository.save(region);

        return ResponseEntity.created(URI.create("/api/regiones/" + saved.getId()))
                .body(modelMapper.map(saved, RegionDTO.class));
    }

    @GetMapping
    @Operation(summary = "Listar regiones", description = "Obtiene todas las regiones disponibles")
    public ResponseEntity<List<RegionDTO>> listar() {
        log.debug("Listando todas las regiones");

        List<RegionDTO> regiones = regionRepository.findAll().stream()
                .map(r -> modelMapper.map(r, RegionDTO.class))
                .collect(Collectors.toList());

        return ResponseEntity.ok(regiones);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener región por ID")
    public ResponseEntity<RegionDTO> obtenerPorId(
            @Parameter(description = "ID de la región", example = "1")
            @PathVariable Long id) {
        log.debug("Obteniendo región con ID: {}", id);

        return regionRepository.findById(id)
                .map(r -> ResponseEntity.ok(modelMapper.map(r, RegionDTO.class)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar región")
    public ResponseEntity<RegionDTO> actualizar(
            @Parameter(description = "ID de la región", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody RegionDTO regionDTO) {
        log.info("Actualizando región con ID: {}", id);

        return regionRepository.findById(id)
                .map(r -> {
                    r.setNombre(regionDTO.getNombre());
                    Region updated = regionRepository.save(r);
                    return ResponseEntity.ok(modelMapper.map(updated, RegionDTO.class));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar región")
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "ID de la región", example = "1")
            @PathVariable Long id) {
        log.info("Eliminando región con ID: {}", id);

        if (!regionRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        regionRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}