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
 * Endpoints de lectura públicos. Modificaciones protegidas solo para Administradores.
 */
@RestController
@RequestMapping("/api/categorias")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Categorías", description = "Gestión de categorías de propiedades")
public class CategoriaController {

    private final CategoriaRepository categoriaRepository;
    private final ModelMapper modelMapper;

    private static final Long ROL_ADMIN = 1L;

    /**
     * Crea una nueva categoría.
     * 🔴 BLINDADO: Solo Administradores.
     */
    @PostMapping
    @Operation(summary = "Crear categoría", description = "Crea una nueva categoría de propiedad (Solo Administradores). Requiere cabeceras.")
    public ResponseEntity<?> crear(
            @RequestHeader(value = "X-Usuario-Id", required = false) Long usuarioIdHeader,
            @RequestHeader(value = "X-Rol-Id", required = false) Long rolIdHeader,
            @Valid @RequestBody CategoriaDTO categoriaDTO) {

        if (usuarioIdHeader == null || rolIdHeader == null) {
            log.warn("Intento de acceso no autorizado a POST /api/categorias");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No autorizado: Faltan cabeceras de identidad.");
        }

        if (!ROL_ADMIN.equals(rolIdHeader)) {
            log.warn("Usuario {} intentó crear una categoría sin ser administrador", usuarioIdHeader);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Acceso denegado: Solo los administradores pueden crear categorías.");
        }

        log.info("Creando nueva categoría: {}", categoriaDTO.getNombre());

        Categoria categoria = modelMapper.map(categoriaDTO, Categoria.class);
        Categoria saved = categoriaRepository.save(categoria);

        return ResponseEntity.created(URI.create("/api/categorias/" + saved.getId()))
                .body(modelMapper.map(saved, CategoriaDTO.class));
    }

    /**
     * Lista todas las categorías.
     * 🟢 PÚBLICO: Para cargar los selectores y filtros en la web.
     */
    @GetMapping
    @Operation(summary = "Listar categorías", description = "Obtiene todas las categorías disponibles. Endpoint público.")
    public ResponseEntity<?> listar() {

        log.debug("Listando todas las categorías");

        List<CategoriaDTO> categorias = categoriaRepository.findAll().stream()
                .map(c -> modelMapper.map(c, CategoriaDTO.class))
                .collect(Collectors.toList());

        return ResponseEntity.ok(categorias);
    }

    /**
     * Obtiene categoría por ID.
     * 🟢 PÚBLICO: Para lectura de detalles en la web.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtener categoría por ID", description = "Obtiene los detalles de una categoría. Endpoint público.")
    public ResponseEntity<?> obtenerPorId(
            @Parameter(description = "ID de la categoría", example = "1")
            @PathVariable Long id) {

        log.debug("Obteniendo categoría con ID: {}", id);

        return categoriaRepository.findById(id)
                .map(c -> ResponseEntity.ok(modelMapper.map(c, CategoriaDTO.class)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Actualiza una categoría.
     * 🔴 BLINDADO: Solo Administradores.
     */
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar categoría", description = "Actualiza el nombre de una categoría (Solo Administradores). Requiere cabeceras.")
    public ResponseEntity<?> actualizar(
            @RequestHeader(value = "X-Usuario-Id", required = false) Long usuarioIdHeader,
            @RequestHeader(value = "X-Rol-Id", required = false) Long rolIdHeader,
            @Parameter(description = "ID de la categoría", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody CategoriaDTO categoriaDTO) {

        if (usuarioIdHeader == null || rolIdHeader == null) {
            log.warn("Intento de acceso no autorizado a PUT /api/categorias/{}", id);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (!ROL_ADMIN.equals(rolIdHeader)) {
            log.warn("Usuario {} intentó modificar la categoría {} sin ser administrador", usuarioIdHeader, id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Acceso denegado: Solo los administradores pueden modificar categorías.");
        }

        log.info("Actualizando categoría con ID: {}", id);

        return categoriaRepository.findById(id)
                .map(c -> {
                    c.setNombre(categoriaDTO.getNombre());
                    Categoria updated = categoriaRepository.save(c);
                    return ResponseEntity.ok(modelMapper.map(updated, CategoriaDTO.class));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Elimina una categoría.
     * 🔴 BLINDADO: Solo Administradores.
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar categoría", description = "Elimina una categoría del sistema (Solo Administradores). Requiere cabeceras.")
    public ResponseEntity<?> eliminar(
            @RequestHeader(value = "X-Usuario-Id", required = false) Long usuarioIdHeader,
            @RequestHeader(value = "X-Rol-Id", required = false) Long rolIdHeader,
            @Parameter(description = "ID de la categoría", example = "1")
            @PathVariable Long id) {

        if (usuarioIdHeader == null || rolIdHeader == null) {
            log.warn("Intento de acceso no autorizado a DELETE /api/categorias/{}", id);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (!ROL_ADMIN.equals(rolIdHeader)) {
            log.warn("Usuario {} intentó eliminar la categoría {} sin ser administrador", usuarioIdHeader, id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Acceso denegado: Solo los administradores pueden eliminar categorías.");
        }

        log.info("Eliminando categoría con ID: {}", id);

        if (!categoriaRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        categoriaRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}