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
 * Protegido mediante validación de cabeceras inyectadas por el API Gateway.
 */
@RestController
@RequestMapping("/api/regiones")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Regiones", description = "Gestión de regiones administrativas")
public class RegionController {

    private final RegionRepository regionRepository;
    private final ModelMapper modelMapper;

    private static final Long ROL_ADMIN = 1L;

    /**
     * Crea una nueva región.
     * BLINDADO: Solo Administradores.
     */
    @PostMapping
    @Operation(summary = "Crear región", description = "Crea una nueva región (Solo Administradores)")
    public ResponseEntity<?> crear(
            @RequestHeader(value = "X-Usuario-Id", required = false) Long usuarioIdHeader,
            @RequestHeader(value = "X-Rol-Id", required = false) Long rolIdHeader,
            @Valid @RequestBody RegionDTO regionDTO) {

        if (usuarioIdHeader == null || rolIdHeader == null) {
            log.warn("Intento de acceso no autorizado a POST /api/regiones");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No autorizado: Faltan cabeceras de identidad.");
        }

        if (!ROL_ADMIN.equals(rolIdHeader)) {
            log.warn("Usuario {} intentó crear una región sin ser administrador", usuarioIdHeader);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Acceso denegado: Solo los administradores pueden crear regiones.");
        }

        log.info("Creando nueva región: {}", regionDTO.getNombre());

        Region region = modelMapper.map(regionDTO, Region.class);
        Region saved = regionRepository.save(region);

        return ResponseEntity.created(URI.create("/api/regiones/" + saved.getId()))
                .body(modelMapper.map(saved, RegionDTO.class));
    }

    /**
     * Lista todas las regiones.
     * Acceso: Cualquier usuario autenticado.
     */
    @GetMapping
    @Operation(summary = "Listar regiones", description = "Obtiene todas las regiones disponibles")
    public ResponseEntity<?> listar(
            @RequestHeader(value = "X-Usuario-Id", required = false) Long usuarioIdHeader,
            @RequestHeader(value = "X-Rol-Id", required = false) Long rolIdHeader) {

        if (usuarioIdHeader == null || rolIdHeader == null) {
            log.warn("Intento de acceso no autorizado a GET /api/regiones");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        log.debug("Listando todas las regiones");

        List<RegionDTO> regiones = regionRepository.findAll().stream()
                .map(r -> modelMapper.map(r, RegionDTO.class))
                .collect(Collectors.toList());

        return ResponseEntity.ok(regiones);
    }

    /**
     * Obtiene una región por ID.
     * Acceso: Cualquier usuario autenticado.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtener región por ID")
    public ResponseEntity<?> obtenerPorId(
            @RequestHeader(value = "X-Usuario-Id", required = false) Long usuarioIdHeader,
            @RequestHeader(value = "X-Rol-Id", required = false) Long rolIdHeader,
            @Parameter(description = "ID de la región", example = "1")
            @PathVariable Long id) {

        if (usuarioIdHeader == null || rolIdHeader == null) {
            log.warn("Intento de acceso no autorizado a GET /api/regiones/{}", id);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        log.debug("Obteniendo región con ID: {}", id);

        return regionRepository.findById(id)
                .map(r -> ResponseEntity.ok(modelMapper.map(r, RegionDTO.class)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Actualiza una región.
     * BLINDADO: Solo Administradores.
     */
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar región", description = "Actualiza el nombre de una región (Solo Administradores)")
    public ResponseEntity<?> actualizar(
            @RequestHeader(value = "X-Usuario-Id", required = false) Long usuarioIdHeader,
            @RequestHeader(value = "X-Rol-Id", required = false) Long rolIdHeader,
            @Parameter(description = "ID de la región", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody RegionDTO regionDTO) {

        if (usuarioIdHeader == null || rolIdHeader == null) {
            log.warn("Intento de acceso no autorizado a PUT /api/regiones/{}", id);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (!ROL_ADMIN.equals(rolIdHeader)) {
            log.warn("Usuario {} intentó modificar la región {} sin ser administrador", usuarioIdHeader, id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Acceso denegado: Solo los administradores pueden modificar regiones.");
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

    /**
     * Elimina una región.
     * BLINDADO: Solo Administradores.
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar región", description = "Elimina una región del sistema (Solo Administradores)")
    public ResponseEntity<?> eliminar(
            @RequestHeader(value = "X-Usuario-Id", required = false) Long usuarioIdHeader,
            @RequestHeader(value = "X-Rol-Id", required = false) Long rolIdHeader,
            @Parameter(description = "ID de la región", example = "1")
            @PathVariable Long id) {

        if (usuarioIdHeader == null || rolIdHeader == null) {
            log.warn("Intento de acceso no autorizado a DELETE /api/regiones/{}", id);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (!ROL_ADMIN.equals(rolIdHeader)) {
            log.warn("Usuario {} intentó eliminar la región {} sin ser administrador", usuarioIdHeader, id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Acceso denegado: Solo los administradores pueden eliminar regiones.");
        }

        log.info("Eliminando región con ID: {}", id);

        if (!regionRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        regionRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}