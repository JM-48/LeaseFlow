package com.rentify.propertyservice.controller;

import com.rentify.propertyservice.dto.PropertyDTO;
import com.rentify.propertyservice.service.PropertyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
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

    /**
     * Crea una nueva propiedad.
     *
     * @param propertyDTO Datos de la propiedad a crear
     * @return Propiedad creada con código 201 CREATED
     */
    @PostMapping
    @Operation(
            summary = "Crear nueva propiedad",
            description = "Crea una nueva propiedad con validaciones de negocio"
    )
    public ResponseEntity<PropertyDTO> crear(@Valid @RequestBody PropertyDTO propertyDTO) {
        log.info("Endpoint POST /api/propiedades - Crear propiedad con código: {}", propertyDTO.getCodigo());

        PropertyDTO created = propertyService.crearProperty(propertyDTO);

        return ResponseEntity
                .created(URI.create("/api/propiedades/" + created.getId()))
                .body(created);
    }

    /**
     * Obtiene todas las propiedades.
     *
     * @param includeDetails Incluir detalles de tipo, comuna, fotos y categorías
     * @return Lista de propiedades
     */
    @GetMapping
    @Operation(
            summary = "Listar todas las propiedades",
            description = "Retorna todas las propiedades registradas en el sistema"
    )
    public ResponseEntity<List<PropertyDTO>> listar(
            @Parameter(description = "Incluir detalles de relaciones (tipo, comuna, fotos, categorías)")
            @RequestParam(defaultValue = "false") boolean includeDetails) {

        log.debug("Endpoint GET /api/propiedades - Listar todas (includeDetails: {})", includeDetails);

        List<PropertyDTO> propiedades = propertyService.listarTodas(includeDetails);

        return ResponseEntity.ok(propiedades);
    }

    /**
     * Obtiene una propiedad por su ID.
     *
     * @param id ID de la propiedad
     * @param includeDetails Incluir detalles de relaciones
     * @return Propiedad encontrada
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "Obtener propiedad por ID",
            description = "Retorna los detalles de una propiedad específica"
    )
    public ResponseEntity<PropertyDTO> obtenerPorId(
            @Parameter(description = "ID de la propiedad", example = "1")
            @PathVariable Long id,
            @Parameter(description = "Incluir detalles de relaciones")
            @RequestParam(defaultValue = "true") boolean includeDetails) {

        log.debug("Endpoint GET /api/propiedades/{} - Obtener por ID (includeDetails: {})", id, includeDetails);

        PropertyDTO propiedad = propertyService.obtenerPorId(id, includeDetails);

        return ResponseEntity.ok(propiedad);
    }

    /**
     * Obtiene una propiedad por su código único.
     *
     * @param codigo Código de la propiedad
     * @param includeDetails Incluir detalles de relaciones
     * @return Propiedad encontrada
     */
    @GetMapping("/codigo/{codigo}")
    @Operation(
            summary = "Obtener propiedad por código",
            description = "Retorna una propiedad específica usando su código único"
    )
    public ResponseEntity<PropertyDTO> obtenerPorCodigo(
            @Parameter(description = "Código único de la propiedad", example = "DP001")
            @PathVariable String codigo,
            @Parameter(description = "Incluir detalles de relaciones")
            @RequestParam(defaultValue = "true") boolean includeDetails) {

        log.debug("Endpoint GET /api/propiedades/codigo/{} - Obtener por código (includeDetails: {})", codigo, includeDetails);

        PropertyDTO propiedad = propertyService.obtenerPorCodigo(codigo, includeDetails);

        return ResponseEntity.ok(propiedad);
    }

    /**
     * Actualiza una propiedad existente.
     *
     * @param id ID de la propiedad a actualizar
     * @param propertyDTO Datos actualizados
     * @return Propiedad actualizada
     */
    @PutMapping("/{id}")
    @Operation(
            summary = "Actualizar propiedad",
            description = "Actualiza los datos de una propiedad existente"
    )
    public ResponseEntity<PropertyDTO> actualizar(
            @Parameter(description = "ID de la propiedad", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody PropertyDTO propertyDTO) {

        log.info("Endpoint PUT /api/propiedades/{} - Actualizar propiedad", id);

        PropertyDTO actualizado = propertyService.actualizar(id, propertyDTO);

        return ResponseEntity.ok(actualizado);
    }

    /**
     * Elimina una propiedad.
     *
     * @param id ID de la propiedad a eliminar
     * @return Respuesta sin contenido (204 NO_CONTENT)
     */
    @DeleteMapping("/{id}")
    @Operation(
            summary = "Eliminar propiedad",
            description = "Elimina una propiedad del sistema"
    )
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "ID de la propiedad", example = "1")
            @PathVariable Long id) {

        log.info("Endpoint DELETE /api/propiedades/{} - Eliminar propiedad", id);

        propertyService.eliminar(id);

        return ResponseEntity.noContent().build();
    }

    /**
     * Busca propiedades con filtros avanzados.
     *
     * @param comunaId Filtro por ID de comuna
     * @param tipoId Filtro por ID de tipo
     * @param minPrecio Precio mínimo
     * @param maxPrecio Precio máximo
     * @param nHabit Número de habitaciones
     * @param nBanos Número de baños
     * @param petFriendly Acepta mascotas
     * @param includeDetails Incluir detalles
     * @return Lista de propiedades que cumplen los filtros
     */
    @GetMapping("/buscar")
    @Operation(
            summary = "Buscar propiedades con filtros",
            description = "Busca propiedades aplicando múltiples filtros opcionales"
    )
    public ResponseEntity<List<PropertyDTO>> buscarConFiltros(
            @Parameter(description = "ID de la comuna (opcional)")
            @RequestParam(required = false) Long comunaId,

            @Parameter(description = "ID del tipo de propiedad (opcional)")
            @RequestParam(required = false) Long tipoId,

            @Parameter(description = "Precio mínimo mensual (opcional)")
            @RequestParam(required = false) BigDecimal minPrecio,

            @Parameter(description = "Precio máximo mensual (opcional)")
            @RequestParam(required = false) BigDecimal maxPrecio,

            @Parameter(description = "Número de habitaciones (opcional)")
            @RequestParam(required = false) Integer nHabit,

            @Parameter(description = "Número de baños (opcional)")
            @RequestParam(required = false) Integer nBanos,

            @Parameter(description = "Acepta mascotas (opcional)")
            @RequestParam(required = false) Boolean petFriendly,

            @Parameter(description = "Incluir detalles de relaciones")
            @RequestParam(defaultValue = "false") boolean includeDetails) {

        log.debug("Endpoint GET /api/propiedades/buscar - Búsqueda con filtros");

        List<PropertyDTO> propiedades = propertyService.buscarConFiltros(
                tipoId, comunaId, minPrecio, maxPrecio, nHabit, nBanos, petFriendly, includeDetails
        );

        return ResponseEntity.ok(propiedades);
    }

    /**
     * Verifica si existe una propiedad.
     *
     * @param id ID de la propiedad
     * @return true si existe, false en caso contrario
     */
    @GetMapping("/{id}/existe")
    @Operation(
            summary = "Verificar existencia de propiedad",
            description = "Verifica si una propiedad existe en el sistema"
    )
    public ResponseEntity<Boolean> existe(
            @Parameter(description = "ID de la propiedad", example = "1")
            @PathVariable Long id) {

        log.debug("Endpoint GET /api/propiedades/{}/existe - Verificar existencia", id);

        boolean existe = propertyService.existsProperty(id);

        return ResponseEntity.ok(existe);
    }
}