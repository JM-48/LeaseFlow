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
import org.springframework.http.HttpStatus;
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

    /**
     * Valida de forma sencilla si el request trae un token Bearer.
     */
    private boolean isNoAutorizado(String token) {
        return token == null || token.trim().isEmpty() || !token.startsWith("Bearer ");
    }

    @PostMapping
    @Operation(summary = "Crear región", description = "Crea una nueva región")
    public ResponseEntity<?> crear(
            @RequestHeader(value = "Authorization", required = false) String token,
            @Valid @RequestBody RegionDTO regionDTO) {

        if (isNoAutorizado(token)) {
            log.warn("Intento de acceso no autorizado a POST /api/regiones");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        log.info("Creando nueva región: {}", regionDTO.getNombre());

        Region region = modelMapper.map(regionDTO, Region.class);
        Region saved = regionRepository.save(region);

        return ResponseEntity.created(URI.create("/api/regiones/" + saved.getId()))
                .body(modelMapper.map(saved, RegionDTO.class));
    }

    @GetMapping
    @Operation(summary = "Listar regiones", description = "Obtiene todas las regiones disponibles")
    public ResponseEntity<?> listar(
            @RequestHeader(value = "Authorization", required = false) String token) {

        if (isNoAutorizado(token)) {
            log.warn("Intento de acceso no autorizado a GET /api/regiones");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        log.debug("Listando todas las regiones");

        List<RegionDTO> regiones = regionRepository.findAll().stream()
                .map(r -> modelMapper.map(r, RegionDTO.class))
                .collect(Collectors.toList());

        return ResponseEntity.ok(regiones);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener región por ID")
    public ResponseEntity<?> obtenerPorId(
            @RequestHeader(value = "Authorization", required = false) String token,
            @Parameter(description = "ID de la región", example = "1")
            @PathVariable Long id) {

        if (isNoAutorizado(token)) {
            log.warn("Intento de acceso no autorizado a GET /api/regiones/{}", id);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        log.debug("Obteniendo región con ID: {}", id);

        return regionRepository.findById(id)
                .map(r -> ResponseEntity.ok(modelMapper.map(r, RegionDTO.class)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar región")
    public ResponseEntity<?> actualizar(
            @RequestHeader(value = "Authorization", required = false) String token,
            @Parameter(description = "ID de la región", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody RegionDTO regionDTO) {

        if (isNoAutorizado(token)) {
            log.warn("Intento de acceso no autorizado a PUT /api/regiones/{}", id);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

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
    public ResponseEntity<?> eliminar(
            @RequestHeader(value = "Authorization", required = false) String token,
            @Parameter(description = "ID de la región", example = "1")
            @PathVariable Long id) {

        if (isNoAutorizado(token)) {
            log.warn("Intento de acceso no autorizado a DELETE /api/regiones/{}", id);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        log.info("Eliminando región con ID: {}", id);

        if (!regionRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        regionRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}