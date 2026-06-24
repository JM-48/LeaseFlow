package com.rentify.reviewService.controller;

import com.rentify.reviewService.dto.ReviewDTO;
import com.rentify.reviewService.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la gestión de reseñas y valoraciones.
 * Incluye validación manual de tokens para el ecosistema Rentify.
 */
@Slf4j
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Tag(name = "Reseñas", description = "Gestión de reseñas y valoraciones de propiedades y usuarios")
public class ReviewController {

    private final ReviewService reviewService;

    /**
     * Valida si el token es nulo, vacío o no cumple con el formato Bearer.
     */
    private boolean isNoAutorizado(String token) {
        return token == null || token.isBlank() || !token.startsWith("Bearer ");
    }

    @PostMapping
    @Operation(summary = "Crear nueva reseña",
            description = "Crea una nueva reseña con validaciones de negocio. Puede ser para una propiedad o para un usuario.")
    public ResponseEntity<ReviewDTO> crearResena(
            @RequestHeader(value = "Authorization", required = false) String token,
            @Valid @RequestBody ReviewDTO reviewDTO) {

        if (isNoAutorizado(token)) {
            log.warn("Intento de acceso no autorizado a POST /api/reviews");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        ReviewDTO created = reviewService.crearResena(reviewDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    @Operation(summary = "Listar todas las reseñas",
            description = "Obtiene todas las reseñas del sistema")
    public ResponseEntity<List<ReviewDTO>> listarTodas(
            @RequestHeader(value = "Authorization", required = false) String token,
            @Parameter(description = "Incluir detalles de usuario y propiedad")
            @RequestParam(defaultValue = "false") boolean includeDetails) {

        if (isNoAutorizado(token)) {
            log.warn("Intento de acceso no autorizado a GET /api/reviews");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(reviewService.listarTodas(includeDetails));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener reseña por ID",
            description = "Obtiene los detalles de una reseña específica")
    public ResponseEntity<ReviewDTO> obtenerPorId(
            @RequestHeader(value = "Authorization", required = false) String token,
            @PathVariable Long id,
            @Parameter(description = "Incluir detalles de usuario y propiedad")
            @RequestParam(defaultValue = "true") boolean includeDetails) {

        if (isNoAutorizado(token)) {
            log.warn("Intento de acceso no autorizado a GET /api/reviews/{}", id);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(reviewService.obtenerPorId(id, includeDetails));
    }

    @GetMapping("/usuario/{usuarioId}")
    @Operation(summary = "Obtener reseñas por usuario",
            description = "Obtiene todas las reseñas creadas por un usuario específico")
    public ResponseEntity<List<ReviewDTO>> obtenerPorUsuario(
            @RequestHeader(value = "Authorization", required = false) String token,
            @PathVariable Long usuarioId,
            @Parameter(description = "Incluir detalles de usuario y propiedad")
            @RequestParam(defaultValue = "false") boolean includeDetails) {

        if (isNoAutorizado(token)) {
            log.warn("Intento de acceso no autorizado a GET /api/reviews/usuario/{}", usuarioId);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(reviewService.obtenerPorUsuario(usuarioId, includeDetails));
    }

    @GetMapping("/propiedad/{propiedadId}")
    @Operation(summary = "Obtener reseñas por propiedad",
            description = "Obtiene todas las reseñas de una propiedad específica")
    public ResponseEntity<List<ReviewDTO>> obtenerPorPropiedad(
            @RequestHeader(value = "Authorization", required = false) String token,
            @PathVariable Long propiedadId,
            @Parameter(description = "Incluir detalles de usuario y propiedad")
            @RequestParam(defaultValue = "false") boolean includeDetails) {

        if (isNoAutorizado(token)) {
            log.warn("Intento de acceso no autorizado a GET /api/reviews/propiedad/{}", propiedadId);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(reviewService.obtenerPorPropiedad(propiedadId, includeDetails));
    }

    @GetMapping("/usuario-resenado/{usuarioResenadoId}")
    @Operation(summary = "Obtener reseñas sobre un usuario",
            description = "Obtiene todas las reseñas que han escrito sobre un usuario específico")
    public ResponseEntity<List<ReviewDTO>> obtenerPorUsuarioResenado(
            @RequestHeader(value = "Authorization", required = false) String token,
            @PathVariable Long usuarioResenadoId,
            @Parameter(description = "Incluir detalles de usuario y propiedad")
            @RequestParam(defaultValue = "false") boolean includeDetails) {

        if (isNoAutorizado(token)) {
            log.warn("Intento de acceso no autorizado a GET /api/reviews/usuario-resenado/{}", usuarioResenadoId);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(reviewService.obtenerPorUsuarioResenado(usuarioResenadoId, includeDetails));
    }

    @GetMapping("/propiedad/{propiedadId}/promedio")
    @Operation(summary = "Calcular promedio de reseñas de propiedad",
            description = "Calcula el promedio de puntaje de todas las reseñas de una propiedad")
    public ResponseEntity<Double> calcularPromedioPorPropiedad(
            @RequestHeader(value = "Authorization", required = false) String token,
            @PathVariable Long propiedadId) {

        if (isNoAutorizado(token)) {
            log.warn("Intento de acceso no autorizado a GET /api/reviews/propiedad/{}/promedio", propiedadId);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(reviewService.calcularPromedioPorPropiedad(propiedadId));
    }

    @GetMapping("/usuario-resenado/{usuarioResenadoId}/promedio")
    @Operation(summary = "Calcular promedio de reseñas de usuario",
            description = "Calcula el promedio de puntaje de todas las reseñas sobre un usuario")
    public ResponseEntity<Double> calcularPromedioPorUsuario(
            @RequestHeader(value = "Authorization", required = false) String token,
            @PathVariable Long usuarioResenadoId) {

        if (isNoAutorizado(token)) {
            log.warn("Intento de acceso no autorizado a GET /api/reviews/usuario-resenado/{}/promedio", usuarioResenadoId);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(reviewService.calcularPromedioPorUsuario(usuarioResenadoId));
    }

    @PatchMapping("/{id}/estado")
    @Operation(summary = "Actualizar estado de reseña",
            description = "Actualiza el estado de una reseña (ACTIVA, BANEADA, OCULTA)")
    public ResponseEntity<ReviewDTO> actualizarEstado(
            @RequestHeader(value = "Authorization", required = false) String token,
            @PathVariable Long id,
            @Parameter(description = "Nuevo estado (ACTIVA, BANEADA, OCULTA)")
            @RequestParam String estado) {

        if (isNoAutorizado(token)) {
            log.warn("Intento de acceso no autorizado a PATCH /api/reviews/{}/estado", id);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(reviewService.actualizarEstado(id, estado));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar reseña",
            description = "Elimina una reseña del sistema de forma permanente")
    public ResponseEntity<Void> eliminarResena(
            @RequestHeader(value = "Authorization", required = false) String token,
            @PathVariable Long id) {

        if (isNoAutorizado(token)) {
            log.warn("Intento de acceso no autorizado a DELETE /api/reviews/{}", id);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        reviewService.eliminarResena(id);
        return ResponseEntity.noContent().build();
    }
}