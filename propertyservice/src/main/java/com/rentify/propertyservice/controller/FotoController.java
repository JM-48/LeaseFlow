package com.rentify.propertyservice.controller;

import com.rentify.propertyservice.dto.FotoDTO;
import com.rentify.propertyservice.service.FotoService;
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
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Fotos", description = "Gestión de fotos de propiedades")
public class FotoController {

    private final FotoService fotoService;

    /**
     * Valida de forma sencilla si el request trae un token Bearer.
     */
    private boolean isNoAutorizado(String token) {
        return token == null || token.trim().isEmpty() || !token.startsWith("Bearer ");
    }

    /**
     * Sube una nueva foto para una propiedad.
     *
     * @param token Header de autorización
     * @param propertyId ID de la propiedad
     * @param file Archivo de imagen a subir (JPG, PNG, WEBP)
     * @return FotoDTO con los datos de la foto guardada
     */
    @PostMapping("/propiedades/{id}/fotos")
    @Operation(
            summary = "Subir foto a propiedad",
            description = "Sube una nueva foto a una propiedad. Máximo 20 fotos por propiedad, máximo 10 MB por archivo"
    )
    public ResponseEntity<?> uploadFoto(
            @RequestHeader(value = "Authorization", required = false) String token,

            @Parameter(description = "ID de la propiedad", example = "1")
            @PathVariable(name = "id") Long propertyId,

            @Parameter(description = "Archivo de imagen (JPG, PNG, WEBP)",
                    content = @Content(mediaType = "multipart/form-data"))
            @RequestParam(name = "file") MultipartFile file) {

        if (isNoAutorizado(token)) {
            log.warn("Intento de acceso no autorizado a POST /api/propiedades/{}/fotos", propertyId);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        log.info("Endpoint POST /api/propiedades/{}/fotos - Subir foto", propertyId);

        FotoDTO fotoDTO = fotoService.guardarFoto(propertyId, file);

        return ResponseEntity.status(HttpStatus.CREATED).body(fotoDTO);
    }

    /**
     * Lista todas las fotos de una propiedad.
     *
     * @param token Header de autorización
     * @param propertyId ID de la propiedad
     * @return Lista de FotoDTO ordenadas por orden de visualización
     */
    @GetMapping("/propiedades/{id}/fotos")
    @Operation(
            summary = "Listar fotos de propiedad",
            description = "Obtiene todas las fotos de una propiedad ordenadas por orden de visualización"
    )
    public ResponseEntity<?> listarFotos(
            @RequestHeader(value = "Authorization", required = false) String token,

            @Parameter(description = "ID de la propiedad", example = "1")
            @PathVariable(name = "id") Long propertyId) {

        if (isNoAutorizado(token)) {
            log.warn("Intento de acceso no autorizado a GET /api/propiedades/{}/fotos", propertyId);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        log.debug("Endpoint GET /api/propiedades/{}/fotos - Listar fotos", propertyId);

        List<FotoDTO> fotos = fotoService.listarFotos(propertyId);

        return ResponseEntity.ok(fotos);
    }

    /**
     * Obtiene una foto específica por su ID.
     *
     * @param token Header de autorización
     * @param fotoId ID de la foto
     * @return FotoDTO
     */
    @GetMapping("/fotos/{fotoId}")
    @Operation(
            summary = "Obtener foto por ID",
            description = "Obtiene los detalles de una foto específica"
    )
    public ResponseEntity<?> obtenerFoto(
            @RequestHeader(value = "Authorization", required = false) String token,

            @Parameter(description = "ID de la foto", example = "1")
            @PathVariable Long fotoId) {

        if (isNoAutorizado(token)) {
            log.warn("Intento de acceso no autorizado a GET /api/fotos/{}", fotoId);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        log.debug("Endpoint GET /api/fotos/{} - Obtener foto", fotoId);

        FotoDTO fotoDTO = fotoService.obtenerPorId(fotoId);

        return ResponseEntity.ok(fotoDTO);
    }

    /**
     * Elimina una foto.
     *
     * @param token Header de autorización
     * @param fotoId ID de la foto a eliminar
     * @return Respuesta vacía con código 204 NO_CONTENT
     */
    @DeleteMapping("/fotos/{fotoId}")
    @Operation(
            summary = "Eliminar foto",
            description = "Elimina una foto de la propiedad"
    )
    public ResponseEntity<?> eliminarFoto(
            @RequestHeader(value = "Authorization", required = false) String token,

            @Parameter(description = "ID de la foto a eliminar", example = "1")
            @PathVariable Long fotoId) {

        if (isNoAutorizado(token)) {
            log.warn("Intento de acceso no autorizado a DELETE /api/fotos/{}", fotoId);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        log.info("Endpoint DELETE /api/fotos/{} - Eliminar foto", fotoId);

        fotoService.eliminarFoto(fotoId);

        return ResponseEntity.noContent().build();
    }

    /**
     * Reordena las fotos de una propiedad.
     *
     * @param token Header de autorización
     * @param propertyId ID de la propiedad
     * @param fotosIds Lista de IDs de fotos en el nuevo orden deseado
     * @return Respuesta vacía con código 204 NO_CONTENT
     */
    @PutMapping("/propiedades/{id}/fotos/reordenar")
    @Operation(
            summary = "Reordenar fotos",
            description = "Cambia el orden de visualización de las fotos de una propiedad"
    )
    public ResponseEntity<?> reordenarFotos(
            @RequestHeader(value = "Authorization", required = false) String token,

            @Parameter(description = "ID de la propiedad", example = "1")
            @PathVariable(name = "id") Long propertyId,

            @Parameter(description = "Lista de IDs de fotos en el nuevo orden",
                    example = "[3, 1, 2]")
            @RequestBody List<Long> fotosIds) {

        if (isNoAutorizado(token)) {
            log.warn("Intento de acceso no autorizado a PUT /api/propiedades/{}/fotos/reordenar", propertyId);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        log.info("Endpoint PUT /api/propiedades/{}/fotos/reordenar - Reordenar fotos", propertyId);

        fotoService.reordenarFotos(propertyId, fotosIds);

        return ResponseEntity.noContent().build();
    }
}