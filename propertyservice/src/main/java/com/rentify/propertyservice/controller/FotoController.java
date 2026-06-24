package com.rentify.propertyservice.controller;

import com.rentify.propertyservice.dto.FotoDTO;
import com.rentify.propertyservice.dto.PropertyDTO;
import com.rentify.propertyservice.service.FotoService;
import com.rentify.propertyservice.service.PropertyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Controller REST para gestión de fotos de propiedades.
 * Maneja subida, listado y eliminación de fotos.
 * Protegido mediante validación de cabeceras inyectadas por el API Gateway.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Fotos", description = "Gestión de fotos de propiedades")
public class FotoController {

    private final FotoService fotoService;
    private final PropertyService propertyService; // Inyectado para validar propiedad (autoría)

    private static final Long ROL_ADMIN = 1L;

    /**
     * Sube una nueva foto para una propiedad.
     * BLINDADO: Solo Administradores o el dueño de la propiedad pueden subir fotos.
     */
    @PostMapping("/propiedades/{id}/fotos")
    @Operation(
            summary = "Subir foto a propiedad",
            description = "Sube una nueva foto a una propiedad. Máximo 20 fotos por propiedad, máximo 10 MB por archivo"
    )
    public ResponseEntity<?> uploadFoto(
            @RequestHeader(value = "X-Usuario-Id", required = false) Long usuarioIdHeader,
            @RequestHeader(value = "X-Rol-Id", required = false) Long rolIdHeader,
            @Parameter(description = "ID de la propiedad", example = "1")
            @PathVariable(name = "id") Long propertyId,
            @Parameter(description = "Archivo de imagen (JPG, PNG, WEBP)",
                    content = @Content(mediaType = "multipart/form-data"))
            @RequestParam(name = "file") MultipartFile file) {

        if (usuarioIdHeader == null || rolIdHeader == null) {
            log.warn("Intento de acceso no autorizado a POST /api/propiedades/{}/fotos", propertyId);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No autorizado: Faltan cabeceras de identidad.");
        }

        // Validación de autoría: Obtenemos la propiedad para saber quién es el dueño
        PropertyDTO propiedadActual = propertyService.obtenerPorId(propertyId, false);

        if (!ROL_ADMIN.equals(rolIdHeader) && !propiedadActual.getPropietarioId().equals(usuarioIdHeader)) {
            log.warn("Usuario {} intentó subir fotos a la propiedad {} que no le pertenece", usuarioIdHeader, propertyId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Acceso denegado: No puedes modificar propiedades que no te pertenecen.");
        }

        log.info("Endpoint POST /api/propiedades/{}/fotos - Subir foto por usuario {}", propertyId, usuarioIdHeader);
        FotoDTO fotoDTO = fotoService.guardarFoto(propertyId, file);

        return ResponseEntity.status(HttpStatus.CREATED).body(fotoDTO);
    }

    /**
     * Lista todas las fotos de una propiedad.
     * Acceso: Cualquier usuario autenticado puede verlas.
     */
    @GetMapping("/propiedades/{id}/fotos")
    @Operation(
            summary = "Listar fotos de propiedad",
            description = "Obtiene todas las fotos de una propiedad ordenadas por orden de visualización"
    )
    public ResponseEntity<?> listarFotos(
            @RequestHeader(value = "X-Usuario-Id", required = false) Long usuarioIdHeader,
            @RequestHeader(value = "X-Rol-Id", required = false) Long rolIdHeader,
            @Parameter(description = "ID de la propiedad", example = "1")
            @PathVariable(name = "id") Long propertyId) {

        if (usuarioIdHeader == null || rolIdHeader == null) {
            log.warn("Intento de acceso no autorizado a GET /api/propiedades/{}/fotos", propertyId);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        log.debug("Endpoint GET /api/propiedades/{}/fotos - Listar fotos", propertyId);
        List<FotoDTO> fotos = fotoService.listarFotos(propertyId);

        return ResponseEntity.ok(fotos);
    }

    /**
     * Obtiene una foto específica por su ID.
     * Acceso: Cualquier usuario autenticado puede verla.
     */
    @GetMapping("/fotos/{fotoId}")
    @Operation(
            summary = "Obtener foto por ID",
            description = "Obtiene los detalles de una foto específica"
    )
    public ResponseEntity<?> obtenerFoto(
            @RequestHeader(value = "X-Usuario-Id", required = false) Long usuarioIdHeader,
            @RequestHeader(value = "X-Rol-Id", required = false) Long rolIdHeader,
            @Parameter(description = "ID de la foto", example = "1")
            @PathVariable Long fotoId) {

        if (usuarioIdHeader == null || rolIdHeader == null) {
            log.warn("Intento de acceso no autorizado a GET /api/fotos/{}", fotoId);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        log.debug("Endpoint GET /api/fotos/{} - Obtener foto", fotoId);
        FotoDTO fotoDTO = fotoService.obtenerPorId(fotoId);

        return ResponseEntity.ok(fotoDTO);
    }

    /**
     * Elimina una foto.
     * BLINDADO: Solo Administradores o el dueño de la propiedad pueden eliminar fotos.
     */
    @DeleteMapping("/fotos/{fotoId}")
    @Operation(
            summary = "Eliminar foto",
            description = "Elimina una foto de la propiedad"
    )
    public ResponseEntity<?> eliminarFoto(
            @RequestHeader(value = "X-Usuario-Id", required = false) Long usuarioIdHeader,
            @RequestHeader(value = "X-Rol-Id", required = false) Long rolIdHeader,
            @Parameter(description = "ID de la foto a eliminar", example = "1")
            @PathVariable Long fotoId) {

        if (usuarioIdHeader == null || rolIdHeader == null) {
            log.warn("Intento de acceso no autorizado a DELETE /api/fotos/{}", fotoId);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Recuperamos la foto para saber a qué propiedad pertenece
        FotoDTO fotoActual = fotoService.obtenerPorId(fotoId);

        // Asumiendo que FotoDTO tiene la referencia a la propiedad (ajusta getPropertyId si se llama distinto)
        // NOTA: Si tu FotoDTO no tiene este campo, podrías necesitar una consulta personalizada en el repositorio.
        PropertyDTO propiedadActual = propertyService.obtenerPorId(fotoActual.getPropiedadId(), false);

        if (!ROL_ADMIN.equals(rolIdHeader) && !propiedadActual.getPropietarioId().equals(usuarioIdHeader)) {
            log.warn("Usuario {} intentó eliminar foto {} de una propiedad que no le pertenece", usuarioIdHeader, fotoId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Acceso denegado: No puedes eliminar fotos de propiedades que no te pertenecen.");
        }

        log.info("Endpoint DELETE /api/fotos/{} - Eliminar foto", fotoId);
        fotoService.eliminarFoto(fotoId);

        return ResponseEntity.noContent().build();
    }

    /**
     * Reordena las fotos de una propiedad.
     * BLINDADO: Solo Administradores o el dueño de la propiedad pueden reordenar.
     */
    @PutMapping("/propiedades/{id}/fotos/reordenar")
    @Operation(
            summary = "Reordenar fotos",
            description = "Cambia el orden de visualización de las fotos de una propiedad"
    )
    public ResponseEntity<?> reordenarFotos(
            @RequestHeader(value = "X-Usuario-Id", required = false) Long usuarioIdHeader,
            @RequestHeader(value = "X-Rol-Id", required = false) Long rolIdHeader,
            @Parameter(description = "ID de la propiedad", example = "1")
            @PathVariable(name = "id") Long propertyId,
            @Parameter(description = "Lista de IDs de fotos en el nuevo orden", example = "[3, 1, 2]")
            @RequestBody List<Long> fotosIds) {

        if (usuarioIdHeader == null || rolIdHeader == null) {
            log.warn("Intento de acceso no autorizado a PUT /api/propiedades/{}/fotos/reordenar", propertyId);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Validación de autoría
        PropertyDTO propiedadActual = propertyService.obtenerPorId(propertyId, false);

        if (!ROL_ADMIN.equals(rolIdHeader) && !propiedadActual.getPropietarioId().equals(usuarioIdHeader)) {
            log.warn("Usuario {} intentó reordenar fotos de la propiedad {} que no le pertenece", usuarioIdHeader, propertyId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Acceso denegado: No puedes reordenar fotos de propiedades que no te pertenecen.");
        }

        log.info("Endpoint PUT /api/propiedades/{}/fotos/reordenar - Reordenar fotos", propertyId);
        fotoService.reordenarFotos(propertyId, fotosIds);

        return ResponseEntity.noContent().build();
    }
}