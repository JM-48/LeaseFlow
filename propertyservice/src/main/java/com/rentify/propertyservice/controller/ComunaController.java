package com.rentify.propertyservice.controller;

import com.rentify.propertyservice.dto.ComunaDTO;
import com.rentify.propertyservice.model.Comuna;
import com.rentify.propertyservice.model.Region;
import com.rentify.propertyservice.repository.ComunaRepository;
import com.rentify.propertyservice.repository.RegionRepository;
import com.rentify.propertyservice.service.PropertyService;
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
 * Controller REST para gestión de comunas.
 */
@RestController
@RequestMapping("/api/comunas")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Comunas", description = "Gestión de comunas administrativas")
public class ComunaController {

    private final ComunaRepository comunaRepository;
    private final RegionRepository regionRepository;
    private final ModelMapper modelMapper;
    private final PropertyService propertyService; // <-- INYECCIÓN DEL SERVICIO

    /**
     * Valida de forma sencilla si el request trae un token Bearer.
     */
    private boolean isNoAutorizado(String token) {
        return token == null || token.trim().isEmpty() || !token.startsWith("Bearer ");
    }

    @PostMapping
    @Operation(summary = "Crear comuna", description = "Crea una nueva comuna")
    public ResponseEntity<?> crear(
            @RequestHeader(value = "Authorization", required = false) String token,
            @Valid @RequestBody ComunaDTO comunaDTO) {

        if (isNoAutorizado(token)) {
            log.warn("Intento de acceso no autorizado a POST /api/comunas");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        log.info("Creando nueva comuna: {}", comunaDTO.getNombre());

        Region region = regionRepository.findById(comunaDTO.getRegionId())
                .orElseThrow(() -> new IllegalArgumentException("Región no encontrada"));

        Comuna comuna = Comuna.builder()
                .nombre(comunaDTO.getNombre())
                .region(region)
                .build();

        Comuna saved = comunaRepository.save(comuna);
        return ResponseEntity.created(URI.create("/api/comunas/" + saved.getId()))
                .body(convertToDTO(saved));
    }

    @GetMapping
    @Operation(summary = "Listar comunas", description = "Obtiene todas las comunas disponibles")
    public ResponseEntity<?> listar(
            @RequestHeader(value = "Authorization", required = false) String token) {

        if (isNoAutorizado(token)) {
            log.warn("Intento de acceso no autorizado a GET /api/comunas");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        log.debug("Listando todas las comunas");

        List<ComunaDTO> comunas = propertyService.listarTodasComunas();

        return ResponseEntity.ok(comunas);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener comuna por ID")
    public ResponseEntity<?> obtenerPorId(
            @RequestHeader(value = "Authorization", required = false) String token,
            @Parameter(description = "ID de la comuna", example = "1")
            @PathVariable Long id) {

        if (isNoAutorizado(token)) {
            log.warn("Intento de acceso no autorizado a GET /api/comunas/{}", id);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        log.debug("Obteniendo comuna con ID: {}", id);

        return comunaRepository.findById(id)
                .map(c -> ResponseEntity.ok(convertToDTO(c)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/region/{regionId}")
    @Operation(summary = "Obtener comunas por región")
    public ResponseEntity<?> obtenerPorRegion(
            @RequestHeader(value = "Authorization", required = false) String token,
            @Parameter(description = "ID de la región", example = "1")
            @PathVariable Long regionId) {

        if (isNoAutorizado(token)) {
            log.warn("Intento de acceso no autorizado a GET /api/comunas/region/{}", regionId);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        log.debug("Obteniendo comunas de la región: {}", regionId);

        List<ComunaDTO> comunas = comunaRepository.findByRegionId(regionId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(comunas);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar comuna")
    public ResponseEntity<?> actualizar(
            @RequestHeader(value = "Authorization", required = false) String token,
            @Parameter(description = "ID de la comuna", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody ComunaDTO comunaDTO) {

        if (isNoAutorizado(token)) {
            log.warn("Intento de acceso no autorizado a PUT /api/comunas/{}", id);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        log.info("Actualizando comuna con ID: {}", id);

        return comunaRepository.findById(id)
                .map(c -> {
                    c.setNombre(comunaDTO.getNombre());
                    if (comunaDTO.getRegionId() != null) {
                        Region region = regionRepository.findById(comunaDTO.getRegionId())
                                .orElseThrow(() -> new IllegalArgumentException("Región no encontrada"));
                        c.setRegion(region);
                    }
                    Comuna updated = comunaRepository.save(c);
                    return ResponseEntity.ok(convertToDTO(updated));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar comuna")
    public ResponseEntity<?> eliminar(
            @RequestHeader(value = "Authorization", required = false) String token,
            @Parameter(description = "ID de la comuna", example = "1")
            @PathVariable Long id) {

        if (isNoAutorizado(token)) {
            log.warn("Intento de acceso no autorizado a DELETE /api/comunas/{}", id);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        log.info("Eliminando comuna con ID: {}", id);

        if (!comunaRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        comunaRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private ComunaDTO convertToDTO(Comuna comuna) {
        ComunaDTO dto = modelMapper.map(comuna, ComunaDTO.class);
        dto.setRegionId(comuna.getRegion().getId());
        return dto;
    }
}