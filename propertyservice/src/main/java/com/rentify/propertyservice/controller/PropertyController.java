package com.rentify.propertyservice.controller;

import com.rentify.propertyservice.dto.PropertyDTO;
import com.rentify.propertyservice.service.PropertyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;

/**
 * Controller REST para gestión de propiedades.
 * Expone endpoints CRUD y búsqueda avanzada de propiedades.
 */
@RestController
@RequestMapping("/api/propiedades")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Propiedades", description = "Gestión de propiedades inmobiliarias")
public class PropertyController {

    private final PropertyService propertyService;
    private static final Long ROL_ADMIN = 1L;

    /**
     * Crea una nueva propiedad.
     * 🔴 BLINDADO: Requiere identidad obligatoria.
     */
    @PostMapping
    @Operation(summary = "Crear nueva propiedad", description = "Crea una nueva propiedad con validaciones de negocio. Requiere cabeceras.")
    public ResponseEntity<?> crear(
            @RequestHeader(value = "X-Usuario-Id", required = false) Long usuarioIdHeader,
            @RequestHeader(value = "X-Rol-Id", required = false) Long rolIdHeader,
            @Valid @RequestBody PropertyDTO propertyDTO) {

        if (usuarioIdHeader == null || rolIdHeader == null) {
            log.warn("Intento de acceso no autorizado a POST /api/propiedades");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No autorizado: Faltan cabeceras de identidad.");
        }

        if (!ROL_ADMIN.equals(rolIdHeader)) {
            propertyDTO.setPropietarioId(usuarioIdHeader);
        }

        log.info("Endpoint POST /api/propiedades - Crear propiedad con código: {}", propertyDTO.getCodigo());
        PropertyDTO created = propertyService.crearProperty(propertyDTO);

        return ResponseEntity
                .created(URI.create("/api/propiedades/" + created.getId()))
                .body(created);
    }

    /**
     * Obtiene todas las propiedades (CON PAGINACIÓN).
     * 🟢 PÚBLICO: Para que la web muestre el catálogo inicial.
     */
    @GetMapping
    @Operation(summary = "Listar todas las propiedades", description = "Retorna las propiedades registradas divididas en páginas. Endpoint público.")
    public ResponseEntity<?> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "false") boolean includeDetails) {

        log.debug("Endpoint GET /api/propiedades - Listar todas paginadas (includeDetails: {})", includeDetails);
        org.springframework.data.domain.Pageable paginacion = org.springframework.data.domain.PageRequest.of(page, size);
        org.springframework.data.domain.Page<PropertyDTO> propiedades = propertyService.listarTodas(paginacion, includeDetails);

        return ResponseEntity.ok(propiedades);
    }

    /**
     * Obtiene una propiedad por su ID.
     * 🟢 PÚBLICO: Para ver la ficha de una casa.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtener propiedad por ID", description = "Retorna los detalles de una propiedad específica. Endpoint público.")
    public ResponseEntity<?> obtenerPorId(
            @PathVariable Long id,
            @RequestParam(defaultValue = "true") boolean includeDetails) {

        log.debug("Endpoint GET /api/propiedades/{} - Obtener por ID (includeDetails: {})", id, includeDetails);
        PropertyDTO propiedad = propertyService.obtenerPorId(id, includeDetails);

        return ResponseEntity.ok(propiedad);
    }

    /**
     * Obtiene todas las propiedades publicadas por un usuario/propietario específico.
     * 🟢 PÚBLICO: Para ver el perfil público de un arrendador y sus casas.
     */
    @GetMapping("/usuario/{usuarioId}")
    @Operation(summary = "Listar propiedades por ID de usuario", description = "Retorna todas las propiedades que pertenecen a un propietario. Endpoint público.")
    public ResponseEntity<?> listarPorUsuario(
            @PathVariable Long usuarioId,
            @RequestParam(defaultValue = "true") boolean includeDetails) {

        log.info("Endpoint GET /api/propiedades/usuario/{} - Listar por usuario", usuarioId);
        List<PropertyDTO> propiedades = propertyService.listarPorUsuario(usuarioId, includeDetails);

        return ResponseEntity.ok(propiedades);
    }

    /**
     * Obtiene una propiedad por su código único.
     * 🟢 PÚBLICO: Usado para búsquedas directas o URLs limpias.
     */
    @GetMapping("/codigo/{codigo}")
    @Operation(summary = "Obtener propiedad por código", description = "Retorna una propiedad específica usando su código único. Endpoint público.")
    public ResponseEntity<?> obtenerPorCodigo(
            @PathVariable String codigo,
            @RequestParam(defaultValue = "true") boolean includeDetails) {

        log.debug("Endpoint GET /api/propiedades/codigo/{} - Obtener por código", codigo);
        PropertyDTO propiedad = propertyService.obtenerPorCodigo(codigo, includeDetails);

        return ResponseEntity.ok(propiedad);
    }

    /**
     * Actualiza una propiedad existente.
     * 🔴 BLINDADO: Solo el admin o el dueño pueden editar.
     */
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar propiedad", description = "Actualiza los datos de una propiedad existente. Requiere cabeceras.")
    public ResponseEntity<?> actualizar(
            @RequestHeader(value = "X-Usuario-Id", required = false) Long usuarioIdHeader,
            @RequestHeader(value = "X-Rol-Id", required = false) Long rolIdHeader,
            @PathVariable Long id,
            @Valid @RequestBody PropertyDTO propertyDTO) {

        if (usuarioIdHeader == null || rolIdHeader == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        PropertyDTO propiedadActual = propertyService.obtenerPorId(id, false);

        if (!ROL_ADMIN.equals(rolIdHeader) && !propiedadActual.getPropietarioId().equals(usuarioIdHeader)) {
            log.warn("Usuario {} intentó modificar propiedad {} que no le pertenece", usuarioIdHeader, id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Acceso denegado: No puedes modificar propiedades que no te pertenecen.");
        }

        if (!ROL_ADMIN.equals(rolIdHeader)) {
            propertyDTO.setPropietarioId(usuarioIdHeader);
        }

        log.info("Endpoint PUT /api/propiedades/{} - Actualizar propiedad", id);
        PropertyDTO actualizado = propertyService.actualizar(id, propertyDTO);

        return ResponseEntity.ok(actualizado);
    }

    /**
     * Elimina una propiedad.
     * 🔴 BLINDADO: Solo admin o dueño eliminan.
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar propiedad", description = "Elimina una propiedad del sistema. Requiere cabeceras.")
    public ResponseEntity<?> eliminar(
            @RequestHeader(value = "X-Usuario-Id", required = false) Long usuarioIdHeader,
            @RequestHeader(value = "X-Rol-Id", required = false) Long rolIdHeader,
            @PathVariable Long id) {

        if (usuarioIdHeader == null || rolIdHeader == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        PropertyDTO propiedadActual = propertyService.obtenerPorId(id, false);

        if (!ROL_ADMIN.equals(rolIdHeader) && !propiedadActual.getPropietarioId().equals(usuarioIdHeader)) {
            log.warn("Usuario {} intentó eliminar propiedad {} que no le pertenece", usuarioIdHeader, id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Acceso denegado: No puedes eliminar propiedades que no te pertenecen.");
        }

        log.info("Endpoint DELETE /api/propiedades/{} - Eliminar propiedad", id);
        propertyService.eliminar(id);

        return ResponseEntity.noContent().build();
    }

    /**
     * Busca propiedades con filtros avanzados.
     * 🟢 PÚBLICO: Es el motor de búsqueda que usa cualquier visitante en la web.
     */
    @GetMapping("/buscar")
    @Operation(summary = "Buscar propiedades con filtros", description = "Busca propiedades aplicando múltiples filtros opcionales. Endpoint público.")
    public ResponseEntity<?> buscarConFiltros(
            @RequestParam(required = false) Long comunaId,
            @RequestParam(required = false) Long tipoId,
            @RequestParam(required = false) BigDecimal minPrecio,
            @RequestParam(required = false) BigDecimal maxPrecio,
            @RequestParam(required = false) Integer nHabit,
            @RequestParam(required = false) Integer nBanos,
            @RequestParam(required = false) Boolean petFriendly,
            @RequestParam(defaultValue = "false") boolean includeDetails) {

        log.debug("Endpoint GET /api/propiedades/buscar - Búsqueda con filtros");
        List<PropertyDTO> propiedades = propertyService.buscarConFiltros(
                tipoId, comunaId, minPrecio, maxPrecio, nHabit, nBanos, petFriendly, includeDetails
        );

        return ResponseEntity.ok(propiedades);
    }

    /**
     * Verifica si existe una propiedad.
     * 🟢 PÚBLICO: Comunicación rápida o validaciones de interfaz.
     */
    @GetMapping("/{id}/existe")
    @Operation(summary = "Verificar existencia de propiedad", description = "Verifica si una propiedad existe. Endpoint público.")
    public ResponseEntity<?> existe(@PathVariable Long id) {
        log.debug("Endpoint GET /api/propiedades/{}/existe - Verificar existencia", id);
        boolean existe = propertyService.existsProperty(id);
        return ResponseEntity.ok(existe);
    }
}