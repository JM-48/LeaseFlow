package com.rentify.propertyservice.controller;

import com.rentify.propertyservice.dto.PropertyDTO;
import com.rentify.propertyservice.service.PropertyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
 * Protegido mediante validación de cabeceras inyectadas por el API Gateway.
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
     * BLINDADO: Si no es admin, se fuerza a que el creador sea el usuario logueado.
     */
    @PostMapping
    @Operation(summary = "Crear nueva propiedad", description = "Crea una nueva propiedad con validaciones de negocio")
    public ResponseEntity<?> crear(
            @RequestHeader(value = "X-Usuario-Id", required = false) Long usuarioIdHeader,
            @RequestHeader(value = "X-Rol-Id", required = false) Long rolIdHeader,
            @Valid @RequestBody PropertyDTO propertyDTO) {

        if (usuarioIdHeader == null || rolIdHeader == null) {
            log.warn("Intento de acceso no autorizado a POST /api/propiedades");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No autorizado: Faltan cabeceras de identidad.");
        }

        // Medida Anti-Fraude: Si el usuario no es Admin (1L), forzamos que el propietario
        // sea él mismo, evitando que un usuario común cree propiedades a nombre de otros.
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
     */
    @GetMapping
    @Operation(summary = "Listar todas las propiedades", description = "Retorna las propiedades registradas en el sistema divididas en páginas")
    public ResponseEntity<?> listar(
            @RequestHeader(value = "X-Usuario-Id", required = false) Long usuarioIdHeader,
            @RequestHeader(value = "X-Rol-Id", required = false) Long rolIdHeader,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "false") boolean includeDetails) {

        if (usuarioIdHeader == null || rolIdHeader == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        log.debug("Endpoint GET /api/propiedades - Listar todas paginadas (includeDetails: {})", includeDetails);
        org.springframework.data.domain.Pageable paginacion = org.springframework.data.domain.PageRequest.of(page, size);
        org.springframework.data.domain.Page<PropertyDTO> propiedades = propertyService.listarTodas(paginacion, includeDetails);

        return ResponseEntity.ok(propiedades);
    }

    /**
     * Obtiene una propiedad por su ID.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtener propiedad por ID", description = "Retorna los detalles de una propiedad específica")
    public ResponseEntity<?> obtenerPorId(
            @RequestHeader(value = "X-Usuario-Id", required = false) Long usuarioIdHeader,
            @RequestHeader(value = "X-Rol-Id", required = false) Long rolIdHeader,
            @PathVariable Long id,
            @RequestParam(defaultValue = "true") boolean includeDetails) {

        if (usuarioIdHeader == null || rolIdHeader == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        log.debug("Endpoint GET /api/propiedades/{} - Obtener por ID (includeDetails: {})", id, includeDetails);
        PropertyDTO propiedad = propertyService.obtenerPorId(id, includeDetails);

        return ResponseEntity.ok(propiedad);
    }

    /**
     * Obtiene todas las propiedades publicadas por un usuario/propietario específico.
     */
    @GetMapping("/usuario/{usuarioId}")
    @Operation(summary = "Listar propiedades por ID de usuario", description = "Retorna todas las propiedades que pertenecen a un propietario específico")
    public ResponseEntity<?> listarPorUsuario(
            @RequestHeader(value = "X-Usuario-Id", required = false) Long usuarioIdHeader,
            @RequestHeader(value = "X-Rol-Id", required = false) Long rolIdHeader,
            @PathVariable Long usuarioId,
            @RequestParam(defaultValue = "true") boolean includeDetails) {

        if (usuarioIdHeader == null || rolIdHeader == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        log.info("Endpoint GET /api/propiedades/usuario/{} - Listar por usuario", usuarioId);
        List<PropertyDTO> propiedades = propertyService.listarPorUsuario(usuarioId, includeDetails);

        return ResponseEntity.ok(propiedades);
    }

    /**
     * Obtiene una propiedad por su código único.
     */
    @GetMapping("/codigo/{codigo}")
    @Operation(summary = "Obtener propiedad por código", description = "Retorna una propiedad específica usando su código único")
    public ResponseEntity<?> obtenerPorCodigo(
            @RequestHeader(value = "X-Usuario-Id", required = false) Long usuarioIdHeader,
            @RequestHeader(value = "X-Rol-Id", required = false) Long rolIdHeader,
            @PathVariable String codigo,
            @RequestParam(defaultValue = "true") boolean includeDetails) {

        if (usuarioIdHeader == null || rolIdHeader == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        log.debug("Endpoint GET /api/propiedades/codigo/{} - Obtener por código", codigo);
        PropertyDTO propiedad = propertyService.obtenerPorCodigo(codigo, includeDetails);

        return ResponseEntity.ok(propiedad);
    }

    /**
     * Actualiza una propiedad existente.
     * BLINDADO: Solo el administrador o el dueño de la propiedad pueden actualizarla.
     */
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar propiedad", description = "Actualiza los datos de una propiedad existente")
    public ResponseEntity<?> actualizar(
            @RequestHeader(value = "X-Usuario-Id", required = false) Long usuarioIdHeader,
            @RequestHeader(value = "X-Rol-Id", required = false) Long rolIdHeader,
            @PathVariable Long id,
            @Valid @RequestBody PropertyDTO propertyDTO) {

        if (usuarioIdHeader == null || rolIdHeader == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Buscamos la propiedad actual para saber a quién le pertenece
        PropertyDTO propiedadActual = propertyService.obtenerPorId(id, false);

        // Seguridad estricta: Si no es Admin, validamos que la propiedad le pertenezca
        if (!ROL_ADMIN.equals(rolIdHeader) && !propiedadActual.getPropietarioId().equals(usuarioIdHeader)) {
            log.warn("Usuario {} intentó modificar propiedad {} que no le pertenece", usuarioIdHeader, id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Acceso denegado: No puedes modificar propiedades que no te pertenecen.");
        }

        // Evitar que un usuario cambie de dueño la propiedad durante la actualización
        if (!ROL_ADMIN.equals(rolIdHeader)) {
            propertyDTO.setPropietarioId(usuarioIdHeader);
        }

        log.info("Endpoint PUT /api/propiedades/{} - Actualizar propiedad", id);
        PropertyDTO actualizado = propertyService.actualizar(id, propertyDTO);

        return ResponseEntity.ok(actualizado);
    }

    /**
     * Elimina una propiedad.
     * BLINDADO: Solo el administrador o el dueño de la propiedad pueden eliminarla.
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar propiedad", description = "Elimina una propiedad del sistema")
    public ResponseEntity<?> eliminar(
            @RequestHeader(value = "X-Usuario-Id", required = false) Long usuarioIdHeader,
            @RequestHeader(value = "X-Rol-Id", required = false) Long rolIdHeader,
            @PathVariable Long id) {

        if (usuarioIdHeader == null || rolIdHeader == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Buscamos la propiedad actual para saber a quién le pertenece
        PropertyDTO propiedadActual = propertyService.obtenerPorId(id, false);

        // Seguridad estricta: Si no es Admin, validamos que la propiedad le pertenezca
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
     */
    @GetMapping("/buscar")
    @Operation(summary = "Buscar propiedades con filtros", description = "Busca propiedades aplicando múltiples filtros opcionales")
    public ResponseEntity<?> buscarConFiltros(
            @RequestHeader(value = "X-Usuario-Id", required = false) Long usuarioIdHeader,
            @RequestHeader(value = "X-Rol-Id", required = false) Long rolIdHeader,
            @RequestParam(required = false) Long comunaId,
            @RequestParam(required = false) Long tipoId,
            @RequestParam(required = false) BigDecimal minPrecio,
            @RequestParam(required = false) BigDecimal maxPrecio,
            @RequestParam(required = false) Integer nHabit,
            @RequestParam(required = false) Integer nBanos,
            @RequestParam(required = false) Boolean petFriendly,
            @RequestParam(defaultValue = "false") boolean includeDetails) {

        if (usuarioIdHeader == null || rolIdHeader == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        log.debug("Endpoint GET /api/propiedades/buscar - Búsqueda con filtros");
        List<PropertyDTO> propiedades = propertyService.buscarConFiltros(
                tipoId, comunaId, minPrecio, maxPrecio, nHabit, nBanos, petFriendly, includeDetails
        );

        return ResponseEntity.ok(propiedades);
    }

    /**
     * Verifica si existe una propiedad.
     */
    @GetMapping("/{id}/existe")
    @Operation(summary = "Verificar existencia de propiedad", description = "Verifica si una propiedad existe en el sistema")
    public ResponseEntity<?> existe(
            @RequestHeader(value = "X-Usuario-Id", required = false) Long usuarioIdHeader,
            @RequestHeader(value = "X-Rol-Id", required = false) Long rolIdHeader,
            @PathVariable Long id) {

        if (usuarioIdHeader == null || rolIdHeader == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        log.debug("Endpoint GET /api/propiedades/{}/existe - Verificar existencia", id);
        boolean existe = propertyService.existsProperty(id);

        return ResponseEntity.ok(existe);
    }
}