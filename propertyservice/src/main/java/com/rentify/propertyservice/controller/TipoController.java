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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller REST para gestión de tipos de propiedades.
 * Endpoints de lectura públicos. Modificaciones protegidas solo para Administradores.
 */
@RestController
@RequestMapping("/api/tipos")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Tipos", description = "Gestión de tipos de propiedades")
public class TipoController {

    private final TipoRepository tipoRepository;
    private final ModelMapper modelMapper;

    private static final Long ROL_ADMIN = 1L;

    /**
     * Crea un nuevo tipo.
     * 🔴 BLINDADO: Solo Administradores.
     */
    @PostMapping
    @Operation(summary = "Crear tipo", description = "Crea un nuevo tipo de propiedad (Solo Administradores). Requiere cabeceras.")
    public ResponseEntity<?> crear(
            @RequestHeader(value = "X-Usuario-Id", required = false) Long usuarioIdHeader,
            @RequestHeader(value = "X-Rol-Id", required = false) Long rolIdHeader,
            @Valid @RequestBody TipoDTO tipoDTO) {

        if (usuarioIdHeader == null || rolIdHeader == null) {
            log.warn("Intento de acceso no autorizado a POST /api/tipos");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No autorizado: Faltan cabeceras de identidad.");
        }

        if (!ROL_ADMIN.equals(rolIdHeader)) {
            log.warn("Usuario {} intentó crear un tipo sin ser administrador", usuarioIdHeader);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Acceso denegado: Solo los administradores pueden crear tipos de propiedades.");
        }

        log.info("Creando nuevo tipo: {}", tipoDTO.getNombre());

        Tipo tipo = modelMapper.map(tipoDTO, Tipo.class);
        Tipo saved = tipoRepository.save(tipo);

        return ResponseEntity.created(URI.create("/api/tipos/" + saved.getId()))
                .body(modelMapper.map(saved, TipoDTO.class));
    }

    /**
     * Lista todos los tipos.
     * 🟢 PÚBLICO: Para cargar los selectores y filtros en la web.
     */
    @GetMapping
    @Operation(summary = "Listar tipos", description = "Obtiene todos los tipos disponibles. Endpoint público.")
    public ResponseEntity<?> listar() {

        log.debug("Listando todos los tipos");

        List<TipoDTO> tipos = tipoRepository.findAll().stream()
                .map(t -> modelMapper.map(t, TipoDTO.class))
                .collect(Collectors.toList());

        return ResponseEntity.ok(tipos);
    }

    /**
     * Obtiene tipo por ID.
     * 🟢 PÚBLICO: Para detalles en la web.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtener tipo por ID", description = "Obtiene los detalles de un tipo. Endpoint público.")
    public ResponseEntity<?> obtenerPorId(
            @Parameter(description = "ID del tipo", example = "1")
            @PathVariable Long id) {

        log.debug("Obteniendo tipo con ID: {}", id);

        return tipoRepository.findById(id)
                .map(t -> ResponseEntity.ok(modelMapper.map(t, TipoDTO.class)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Actualiza un tipo.
     * 🔴 BLINDADO: Solo Administradores.
     */
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar tipo", description = "Actualiza el nombre de un tipo (Solo Administradores). Requiere cabeceras.")
    public ResponseEntity<?> actualizar(
            @RequestHeader(value = "X-Usuario-Id", required = false) Long usuarioIdHeader,
            @RequestHeader(value = "X-Rol-Id", required = false) Long rolIdHeader,
            @Parameter(description = "ID del tipo", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody TipoDTO tipoDTO) {

        if (usuarioIdHeader == null || rolIdHeader == null) {
            log.warn("Intento de acceso no autorizado a PUT /api/tipos/{}", id);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (!ROL_ADMIN.equals(rolIdHeader)) {
            log.warn("Usuario {} intentó modificar el tipo {} sin ser administrador", usuarioIdHeader, id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Acceso denegado: Solo los administradores pueden modificar tipos de propiedades.");
        }

        log.info("Actualizando tipo con ID: {}", id);

        return tipoRepository.findById(id)
                .map(t -> {
                    t.setNombre(tipoDTO.getNombre());
                    Tipo updated = tipoRepository.save(t);
                    return ResponseEntity.ok(modelMapper.map(updated, TipoDTO.class));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Elimina un tipo.
     * 🔴 BLINDADO: Solo Administradores.
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar tipo", description = "Elimina un tipo del sistema (Solo Administradores). Requiere cabeceras.")
    public ResponseEntity<?> eliminar(
            @RequestHeader(value = "X-Usuario-Id", required = false) Long usuarioIdHeader,
            @RequestHeader(value = "X-Rol-Id", required = false) Long rolIdHeader,
            @Parameter(description = "ID del tipo", example = "1")
            @PathVariable Long id) {

        if (usuarioIdHeader == null || rolIdHeader == null) {
            log.warn("Intento de acceso no autorizado a DELETE /api/tipos/{}", id);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (!ROL_ADMIN.equals(rolIdHeader)) {
            log.warn("Usuario {} intentó eliminar el tipo {} sin ser administrador", usuarioIdHeader, id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Acceso denegado: Solo los administradores pueden eliminar tipos de propiedades.");
        }

        log.info("Eliminando tipo con ID: {}", id);

        if (!tipoRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        tipoRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}