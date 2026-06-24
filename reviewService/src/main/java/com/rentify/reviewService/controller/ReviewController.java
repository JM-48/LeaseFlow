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
 * Protegido mediante validación de cabeceras inyectadas por el API Gateway.
 */
@Slf4j
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Tag(name = "Reseñas", description = "Gestión de reseñas y valoraciones de propiedades y usuarios")
public class ReviewController {

    private final ReviewService reviewService;

    private static final Long ROL_ADMIN = 1L;

    @PostMapping
    @Operation(summary = "Crear nueva reseña",
            description = "Crea una nueva reseña. El usuario debe coincidir con el emisor de la cabecera.")
    public ResponseEntity<?> crearResena(
            @RequestHeader(value = "X-Usuario-Id", required = false) Long usuarioIdHeader,
            @RequestHeader(value = "X-Rol-Id", required = false) Long rolIdHeader,
            @Valid @RequestBody ReviewDTO reviewDTO) {

        if (usuarioIdHeader == null || rolIdHeader == null) {
            log.warn("Intento de acceso no autorizado a POST /api/reviews");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No autorizado: Faltan cabeceras de identidad.");
        }

        // Evitar suplantación de identidad (Impersonation)
        if (!ROL_ADMIN.equals(rolIdHeader) && !usuarioIdHeader.equals(reviewDTO.getUsuarioId())) {
            log.warn("Usuario {} intentó crear una reseña a nombre del usuario {}", usuarioIdHeader, reviewDTO.getUsuarioId());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Acceso denegado: No puedes crear una reseña usando la identidad de otro usuario.");
        }

        ReviewDTO created = reviewService.crearResena(reviewDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    @Operation(summary = "Listar todas las reseñas (Solo Admin)",
            description = "Obtiene todas las reseñas del sistema de manera global.")
    public ResponseEntity<?> listarTodas(
            @RequestHeader(value = "X-Usuario-Id", required = false) Long usuarioIdHeader,
            @RequestHeader(value = "X-Rol-Id", required = false) Long rolIdHeader,
            @Parameter(description = "Incluir detalles de usuario y propiedad")
            @RequestParam(defaultValue = "false") boolean includeDetails) {

        if (usuarioIdHeader == null || rolIdHeader == null) {
            log.warn("Intento de acceso no autorizado a GET /api/reviews");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (!ROL_ADMIN.equals(rolIdHeader)) {
            log.warn("Usuario {} intentó listar todas las reseñas sin ser administrador", usuarioIdHeader);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Acceso denegado: Solo los administradores pueden listar la totalidad de las reseñas.");
        }

        return ResponseEntity.ok(reviewService.listarTodas(includeDetails));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener reseña por ID",
            description = "Obtiene los detalles de una reseña específica")
    public ResponseEntity<?> obtenerPorId(
            @RequestHeader(value = "X-Usuario-Id", required = false) Long usuarioIdHeader,
            @RequestHeader(value = "X-Rol-Id", required = false) Long rolIdHeader,
            @PathVariable Long id,
            @Parameter(description = "Incluir detalles de usuario y propiedad")
            @RequestParam(defaultValue = "true") boolean includeDetails) {

        if (usuarioIdHeader == null || rolIdHeader == null) {
            log.warn("Intento de acceso no autorizado a GET /api/reviews/{}", id);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(reviewService.obtenerPorId(id, includeDetails));
    }

    @GetMapping("/usuario/{usuarioId}")
    @Operation(summary = "Obtener reseñas por usuario",
            description = "Obtiene todas las reseñas creadas por un usuario específico")
    public ResponseEntity<?> obtenerPorUsuario(
            @RequestHeader(value = "X-Usuario-Id", required = false) Long usuarioIdHeader,
            @RequestHeader(value = "X-Rol-Id", required = false) Long rolIdHeader,
            @PathVariable Long usuarioId,
            @Parameter(description = "Incluir detalles de usuario y propiedad")
            @RequestParam(defaultValue = "false") boolean includeDetails) {

        if (usuarioIdHeader == null || rolIdHeader == null) {
            log.warn("Intento de acceso no autorizado a GET /api/reviews/usuario/{}", usuarioId);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(reviewService.obtenerPorUsuario(usuarioId, includeDetails));
    }

    @GetMapping("/propiedad/{propiedadId}")
    @Operation(summary = "Obtener reseñas por propiedad",
            description = "Obtiene todas las reseñas de una propiedad específica")
    public ResponseEntity<?> obtenerPorPropiedad(
            @RequestHeader(value = "X-Usuario-Id", required = false) Long usuarioIdHeader,
            @RequestHeader(value = "X-Rol-Id", required = false) Long rolIdHeader,
            @PathVariable Long propiedadId,
            @Parameter(description = "Incluir detalles de usuario y propiedad")
            @RequestParam(defaultValue = "false") boolean includeDetails) {

        if (usuarioIdHeader == null || rolIdHeader == null) {
            log.warn("Intento de acceso no autorizado a GET /api/reviews/propiedad/{}", propiedadId);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(reviewService.obtenerPorPropiedad(propiedadId, includeDetails));
    }

    @GetMapping("/usuario-resenado/{usuarioResenadoId}")
    @Operation(summary = "Obtener reseñas sobre un usuario",
            description = "Obtiene todas las reseñas que han escrito sobre un usuario específico")
    public ResponseEntity<?> obtenerPorUsuarioResenado(
            @RequestHeader(value = "X-Usuario-Id", required = false) Long usuarioIdHeader,
            @RequestHeader(value = "X-Rol-Id", required = false) Long rolIdHeader,
            @PathVariable Long usuarioResenadoId,
            @Parameter(description = "Incluir detalles de usuario y propiedad")
            @RequestParam(defaultValue = "false") boolean includeDetails) {

        if (usuarioIdHeader == null || rolIdHeader == null) {
            log.warn("Intento de acceso no autorizado a GET /api/reviews/usuario-resenado/{}", usuarioResenadoId);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(reviewService.obtenerPorUsuarioResenado(usuarioResenadoId, includeDetails));
    }

    @GetMapping("/propiedad/{propiedadId}/promedio")
    @Operation(summary = "Calcular promedio de reseñas de propiedad",
            description = "Calcula el promedio de puntaje de todas las reseñas de una propiedad")
    public ResponseEntity<?> calcularPromedioPorPropiedad(
            @RequestHeader(value = "X-Usuario-Id", required = false) Long usuarioIdHeader,
            @RequestHeader(value = "X-Rol-Id", required = false) Long rolIdHeader,
            @PathVariable Long propiedadId) {

        if (usuarioIdHeader == null || rolIdHeader == null) {
            log.warn("Intento de acceso no autorizado a GET /api/reviews/propiedad/{}/promedio", propiedadId);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(reviewService.calcularPromedioPorPropiedad(propiedadId));
    }

    @GetMapping("/usuario-resenado/{usuarioResenadoId}/promedio")
    @Operation(summary = "Calcular promedio de reseñas de usuario",
            description = "Calcula el promedio de puntaje de todas las reseñas sobre un usuario")
    public ResponseEntity<?> calcularPromedioPorUsuario(
            @RequestHeader(value = "X-Usuario-Id", required = false) Long usuarioIdHeader,
            @RequestHeader(value = "X-Rol-Id", required = false) Long rolIdHeader,
            @PathVariable Long usuarioResenadoId) {

        if (usuarioIdHeader == null || rolIdHeader == null) {
            log.warn("Intento de acceso no autorizado a GET /api/reviews/usuario-resenado/{}/promedio", usuarioResenadoId);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(reviewService.calcularPromedioPorUsuario(usuarioResenadoId));
    }

    @PatchMapping("/{id}/estado")
    @Operation(summary = "Actualizar estado de reseña (Solo Admin)",
            description = "Actualiza el estado de una reseña (ACTIVA, BANEADA, OCULTA)")
    public ResponseEntity<?> actualizarEstado(
            @RequestHeader(value = "X-Usuario-Id", required = false) Long usuarioIdHeader,
            @RequestHeader(value = "X-Rol-Id", required = false) Long rolIdHeader,
            @PathVariable Long id,
            @Parameter(description = "Nuevo estado (ACTIVA, BANEADA, OCULTA)")
            @RequestParam String estado) {

        if (usuarioIdHeader == null || rolIdHeader == null) {
            log.warn("Intento de acceso no autorizado a PATCH /api/reviews/{}/estado", id);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (!ROL_ADMIN.equals(rolIdHeader)) {
            log.warn("Usuario {} intentó moderar la reseña {} sin privilegios de administrador", usuarioIdHeader, id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Acceso denegado: Solo los administradores pueden moderar estados de reseñas.");
        }

        return ResponseEntity.ok(reviewService.actualizarEstado(id, estado));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar reseña (Dueño o Admin)",
            description = "Elimina una reseña del sistema de forma permanente")
    public ResponseEntity<?> eliminarResena(
            @RequestHeader(value = "X-Usuario-Id", required = false) Long usuarioIdHeader,
            @RequestHeader(value = "X-Rol-Id", required = false) Long rolIdHeader,
            @PathVariable Long id) {

        if (usuarioIdHeader == null || rolIdHeader == null) {
            log.warn("Intento de acceso no autorizado a DELETE /api/reviews/{}", id);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Recuperamos la reseña para verificar la autoría
        ReviewDTO review = reviewService.obtenerPorId(id, false);

        if (!ROL_ADMIN.equals(rolIdHeader) && !review.getUsuarioId().equals(usuarioIdHeader)) {
            log.warn("Usuario {} intentó eliminar la reseña {} perteneciente al usuario {}", usuarioIdHeader, id, review.getUsuarioId());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Acceso denegado: No tienes permisos para eliminar esta reseña.");
        }

        reviewService.eliminarResena(id);
        return ResponseEntity.noContent().build();
    }
}