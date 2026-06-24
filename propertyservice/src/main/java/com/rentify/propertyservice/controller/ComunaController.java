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
 * Endpoints de lectura públicos. Modificaciones protegidas solo para Administradores.
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
    private final PropertyService propertyService;

    private static final Long ROL_ADMIN = 1L;

    /**
     * Crea una nueva comuna.
     * 🔴 BLINDADO: Solo Administradores.
     */
    @PostMapping
    @Operation(summary = "Crear comuna", description = "Crea una nueva comuna (Solo Administradores). Requiere cabeceras.")
    public ResponseEntity<?> crear(
            @RequestHeader(value = "X-Usuario-Id", required = false) Long usuarioIdHeader,
            @RequestHeader(value = "X-Rol-Id", required = false) Long rolIdHeader,
            @Valid @RequestBody ComunaDTO comunaDTO) {

        if (usuarioIdHeader == null || rolIdHeader == null) {
            log.warn("Intento de acceso no autorizado a POST /api/comunas");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No autorizado: Faltan cabeceras de identidad.");
        }

        if (!ROL_ADMIN.equals(rolIdHeader)) {
            log.warn("Usuario {} intentó crear una comuna sin ser administrador", usuarioIdHeader);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Acceso denegado: Solo los administradores pueden crear comunas.");
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

    /**
     * Lista todas las comunas.
     * 🟢 PÚBLICO: Para cargar los selectores en la web.
     */
    @GetMapping
    @Operation(summary = "Listar comunas", description = "Obtiene todas las comunas disponibles. Endpoint público.")
    public ResponseEntity<?> listar() {

        log.debug("Listando todas las comunas");

        List<ComunaDTO> comunas = propertyService.listarTodasComunas();

        return ResponseEntity.ok(comunas);
    }

    /**
     * Obtiene una comuna por ID.
     * 🟢 PÚBLICO: Para detalles y lecturas en la web.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtener comuna por ID", description = "Obtiene los detalles de una comuna. Endpoint público.")
    public ResponseEntity<?> obtenerPorId(
            @Parameter(description = "ID de la comuna", example = "1")
            @PathVariable Long id) {

        log.debug("Obteniendo comuna con ID: {}", id);

        return comunaRepository.findById(id)
                .map(c -> ResponseEntity.ok(convertToDTO(c)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Obtiene comunas por ID de Región.
     * 🟢 PÚBLICO: Esencial para los selectores en cascada (eliges región -> se cargan sus comunas).
     */
    @GetMapping("/region/{regionId}")
    @Operation(summary = "Obtener comunas por región", description = "Obtiene las comunas asociadas a una región específica. Endpoint público.")
    public ResponseEntity<?> obtenerPorRegion(
            @Parameter(description = "ID de la región", example = "1")
            @PathVariable Long regionId) {

        log.debug("Obteniendo comunas de la región: {}", regionId);

        List<ComunaDTO> comunas = comunaRepository.findByRegionId(regionId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(comunas);
    }

    /**
     * Actualiza una comuna.
     * 🔴 BLINDADO: Solo Administradores.
     */
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar comuna", description = "Actualiza los datos de una comuna (Solo Administradores). Requiere cabeceras.")
    public ResponseEntity<?> actualizar(
            @RequestHeader(value = "X-Usuario-Id", required = false) Long usuarioIdHeader,
            @RequestHeader(value = "X-Rol-Id", required = false) Long rolIdHeader,
            @Parameter(description = "ID de la comuna", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody ComunaDTO comunaDTO) {

        if (usuarioIdHeader == null || rolIdHeader == null) {
            log.warn("Intento de acceso no autorizado a PUT /api/comunas/{}", id);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (!ROL_ADMIN.equals(rolIdHeader)) {
            log.warn("Usuario {} intentó modificar la comuna {} sin ser administrador", usuarioIdHeader, id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Acceso denegado: Solo los administradores pueden modificar comunas.");
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

    /**
     * Elimina una comuna.
     * 🔴 BLINDADO: Solo Administradores.
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar comuna", description = "Elimina una comuna del sistema (Solo Administradores). Requiere cabeceras.")
    public ResponseEntity<?> eliminar(
            @RequestHeader(value = "X-Usuario-Id", required = false) Long usuarioIdHeader,
            @RequestHeader(value = "X-Rol-Id", required = false) Long rolIdHeader,
            @Parameter(description = "ID de la comuna", example = "1")
            @PathVariable Long id) {

        if (usuarioIdHeader == null || rolIdHeader == null) {
            log.warn("Intento de acceso no autorizado a DELETE /api/comunas/{}", id);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (!ROL_ADMIN.equals(rolIdHeader)) {
            log.warn("Usuario {} intentó eliminar la comuna {} sin ser administrador", usuarioIdHeader, id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Acceso denegado: Solo los administradores pueden eliminar comunas.");
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