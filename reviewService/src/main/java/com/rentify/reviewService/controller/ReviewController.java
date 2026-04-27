package com.rentify.reviewService.controller;

import com.rentify.reviewService.dto.ReviewDTO;
import com.rentify.reviewService.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la gestión de reseñas y valoraciones.
 */
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Tag(name = "Reseñas", description = "Gestión de reseñas y valoraciones de propiedades y usuarios")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    @Operation(summary = "Crear nueva reseña",
            description = "Crea una nueva reseña con validaciones de negocio. Puede ser para una propiedad o para un usuario.")
    public ResponseEntity<ReviewDTO> crearResena(@Valid @RequestBody ReviewDTO reviewDTO) {
        ReviewDTO created = reviewService.crearResena(reviewDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    @Operation(summary = "Listar todas las reseñas",
            description = "Obtiene todas las reseñas del sistema")
    public ResponseEntity<List<ReviewDTO>> listarTodas(
            @Parameter(description = "Incluir detalles de usuario y propiedad")
            @RequestParam(defaultValue = "false") boolean includeDetails) {
        return ResponseEntity.ok(reviewService.listarTodas(includeDetails));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener reseña por ID",
            description = "Obtiene los detalles de una reseña específica")
    public ResponseEntity<ReviewDTO> obtenerPorId(
            @PathVariable Long id,
            @Parameter(description = "Incluir detalles de usuario y propiedad")
            @RequestParam(defaultValue = "true") boolean includeDetails) {
        return ResponseEntity.ok(reviewService.obtenerPorId(id, includeDetails));
    }

    @GetMapping("/usuario/{usuarioId}")
    @Operation(summary = "Obtener reseñas por usuario",
            description = "Obtiene todas las reseñas creadas por un usuario específico")
    public ResponseEntity<List<ReviewDTO>> obtenerPorUsuario(
            @PathVariable Long usuarioId,
            @Parameter(description = "Incluir detalles de usuario y propiedad")
            @RequestParam(defaultValue = "false") boolean includeDetails) {
        return ResponseEntity.ok(reviewService.obtenerPorUsuario(usuarioId, includeDetails));
    }

    @GetMapping("/propiedad/{propiedadId}")
    @Operation(summary = "Obtener reseñas por propiedad",
            description = "Obtiene todas las reseñas de una propiedad específica")
    public ResponseEntity<List<ReviewDTO>> obtenerPorPropiedad(
            @PathVariable Long propiedadId,
            @Parameter(description = "Incluir detalles de usuario y propiedad")
            @RequestParam(defaultValue = "false") boolean includeDetails) {
        return ResponseEntity.ok(reviewService.obtenerPorPropiedad(propiedadId, includeDetails));
    }

    @GetMapping("/usuario-resenado/{usuarioResenadoId}")
    @Operation(summary = "Obtener reseñas sobre un usuario",
            description = "Obtiene todas las reseñas que han escrito sobre un usuario específico")
    public ResponseEntity<List<ReviewDTO>> obtenerPorUsuarioResenado(
            @PathVariable Long usuarioResenadoId,
            @Parameter(description = "Incluir detalles de usuario y propiedad")
            @RequestParam(defaultValue = "false") boolean includeDetails) {
        return ResponseEntity.ok(reviewService.obtenerPorUsuarioResenado(usuarioResenadoId, includeDetails));
    }

    @GetMapping("/propiedad/{propiedadId}/promedio")
    @Operation(summary = "Calcular promedio de reseñas de propiedad",
            description = "Calcula el promedio de puntaje de todas las reseñas de una propiedad")
    public ResponseEntity<Double> calcularPromedioPorPropiedad(@PathVariable Long propiedadId) {
        return ResponseEntity.ok(reviewService.calcularPromedioPorPropiedad(propiedadId));
    }

    @GetMapping("/usuario-resenado/{usuarioResenadoId}/promedio")
    @Operation(summary = "Calcular promedio de reseñas de usuario",
            description = "Calcula el promedio de puntaje de todas las reseñas sobre un usuario")
    public ResponseEntity<Double> calcularPromedioPorUsuario(@PathVariable Long usuarioResenadoId) {
        return ResponseEntity.ok(reviewService.calcularPromedioPorUsuario(usuarioResenadoId));
    }

    @PatchMapping("/{id}/estado")
    @Operation(summary = "Actualizar estado de reseña",
            description = "Actualiza el estado de una reseña (ACTIVA, BANEADA, OCULTA)")
    public ResponseEntity<ReviewDTO> actualizarEstado(
            @PathVariable Long id,
            @Parameter(description = "Nuevo estado (ACTIVA, BANEADA, OCULTA)")
            @RequestParam String estado) {
        return ResponseEntity.ok(reviewService.actualizarEstado(id, estado));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar reseña",
            description = "Elimina una reseña del sistema de forma permanente")
    public ResponseEntity<Void> eliminarResena(@PathVariable Long id) {
        reviewService.eliminarResena(id);
        return ResponseEntity.noContent().build();
    }
}