package com.rentify.propertyservice.service;

import com.rentify.propertyservice.constants.PropertyConstants;
import com.rentify.propertyservice.dto.*;
import com.rentify.propertyservice.exception.BusinessValidationException;
import com.rentify.propertyservice.exception.ResourceNotFoundException;
import com.rentify.propertyservice.model.*;
import com.rentify.propertyservice.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PropertyService {

    private final PropertyRepository propertyRepository;
    private final TipoRepository tipoRepository;
    private final ComunaRepository comunaRepository;
    private final RegionRepository regionRepository;
    private final CategoriaRepository categoriaRepository;
    private final ModelMapper modelMapper;

    @Transactional
    public PropertyDTO crearProperty(PropertyDTO propertyDTO) {
        log.info("Creando nueva propiedad con código: {}", propertyDTO.getCodigo());

        if (propertyRepository.existsByCodigo(propertyDTO.getCodigo())) {
            throw new BusinessValidationException(
                    String.format(PropertyConstants.Mensajes.CODIGO_DUPLICADO, propertyDTO.getCodigo())
            );
        }

        if (!PropertyConstants.Divisas.esValida(propertyDTO.getDivisa())) {
            throw new BusinessValidationException(
                    String.format(PropertyConstants.Mensajes.DIVISA_INVALIDA, propertyDTO.getDivisa())
            );
        }

        if (propertyDTO.getPrecioMensual().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessValidationException(PropertyConstants.Mensajes.PRECIO_INVALIDO);
        }

        if (propertyDTO.getM2().compareTo(BigDecimal.valueOf(PropertyConstants.Limites.MIN_M2)) < 0 ||
                propertyDTO.getM2().compareTo(BigDecimal.valueOf(PropertyConstants.Limites.MAX_M2)) > 0) {
            throw new BusinessValidationException(
                    String.format(PropertyConstants.Mensajes.M2_INVALIDO,
                            PropertyConstants.Limites.MIN_M2,
                            PropertyConstants.Limites.MAX_M2)
            );
        }

        if (propertyDTO.getNHabit() < PropertyConstants.Limites.MIN_HABITACIONES ||
                propertyDTO.getNHabit() > PropertyConstants.Limites.MAX_HABITACIONES) {
            throw new BusinessValidationException(
                    String.format(PropertyConstants.Mensajes.HABITACIONES_INVALIDAS,
                            PropertyConstants.Limites.MIN_HABITACIONES,
                            PropertyConstants.Limites.MAX_HABITACIONES)
            );
        }

        if (propertyDTO.getNBanos() < PropertyConstants.Limites.MIN_BANOS ||
                propertyDTO.getNBanos() > PropertyConstants.Limites.MAX_BANOS) {
            throw new BusinessValidationException(
                    String.format(PropertyConstants.Mensajes.BANOS_INVALIDOS,
                            PropertyConstants.Limites.MIN_BANOS,
                            PropertyConstants.Limites.MAX_BANOS)
            );
        }

        Tipo tipo = tipoRepository.findById(propertyDTO.getTipoId())
                .orElseThrow(() -> new BusinessValidationException(
                        String.format(PropertyConstants.Mensajes.TIPO_NO_ENCONTRADO, propertyDTO.getTipoId())
                ));

        Comuna comuna = comunaRepository.findById(propertyDTO.getComunaId())
                .orElseThrow(() -> new BusinessValidationException(
                        String.format(PropertyConstants.Mensajes.COMUNA_NO_ENCONTRADA, propertyDTO.getComunaId())
                ));

        Property property = Property.builder()
                .codigo(propertyDTO.getCodigo())
                .titulo(propertyDTO.getTitulo())
                .precioMensual(propertyDTO.getPrecioMensual())
                .divisa(propertyDTO.getDivisa().toUpperCase())
                .m2(propertyDTO.getM2())
                .nHabit(propertyDTO.getNHabit())
                .nBanos(propertyDTO.getNBanos())
                .petFriendly(propertyDTO.getPetFriendly() != null ? propertyDTO.getPetFriendly() : false)
                .direccion(propertyDTO.getDireccion())
                .fcreacion(propertyDTO.getFcreacion() != null ? propertyDTO.getFcreacion() : LocalDate.now())
                .tipo(tipo)
                .comuna(comuna)
                .build();

        Property saved = propertyRepository.save(property);
        log.info("Propiedad creada exitosamente con ID: {}", saved.getId());

        return convertToDTO(saved, true);
    }

    @Transactional(readOnly = true)
    public List<PropertyDTO> listarTodas() {
        return listarTodas(false);
    }

    @Transactional(readOnly = true)
    public List<PropertyDTO> listarTodas(boolean includeDetails) {
        log.debug("Listando todas las propiedades (includeDetails: {})", Boolean.valueOf(includeDetails));

        return propertyRepository.findAll().stream()
                .map(p -> convertToDTO(p, includeDetails))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PropertyDTO obtenerPorId(Long id) {
        return obtenerPorId(id, false);
    }

    @Transactional(readOnly = true)
    public PropertyDTO obtenerPorId(Long id, boolean includeDetails) {
        log.debug("Obteniendo propiedad con ID: {} (includeDetails: {})", id, Boolean.valueOf(includeDetails));

        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(PropertyConstants.Mensajes.PROPIEDAD_NO_ENCONTRADA, id)
                ));

        return convertToDTO(property, includeDetails);
    }

    @Transactional(readOnly = true)
    public PropertyDTO obtenerPorCodigo(String codigo) {
        return obtenerPorCodigo(codigo, false);
    }

    @Transactional(readOnly = true)
    public PropertyDTO obtenerPorCodigo(String codigo, boolean includeDetails) {
        log.debug("Obteniendo propiedad con código: {} (includeDetails: {})", codigo, Boolean.valueOf(includeDetails));

        Property property = propertyRepository.findByCodigo(codigo)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "La propiedad con código " + codigo + " no existe"
                ));

        return convertToDTO(property, includeDetails);
    }

    @Transactional
    public PropertyDTO actualizar(Long id, PropertyDTO propertyDTO) {
        log.info("Actualizando propiedad con ID: {}", id);

        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(PropertyConstants.Mensajes.PROPIEDAD_NO_ENCONTRADA, id)
                ));

        if (propertyDTO.getCodigo() != null &&
                !propertyDTO.getCodigo().equals(property.getCodigo()) &&
                propertyRepository.existsByCodigo(propertyDTO.getCodigo())) {
            throw new BusinessValidationException(
                    String.format(PropertyConstants.Mensajes.CODIGO_DUPLICADO, propertyDTO.getCodigo())
            );
        }

        if (propertyDTO.getCodigo() != null) {
            property.setCodigo(propertyDTO.getCodigo());
        }
        if (propertyDTO.getTitulo() != null) {
            property.setTitulo(propertyDTO.getTitulo());
        }
        if (propertyDTO.getPrecioMensual() != null) {
            if (propertyDTO.getPrecioMensual().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessValidationException(PropertyConstants.Mensajes.PRECIO_INVALIDO);
            }
            property.setPrecioMensual(propertyDTO.getPrecioMensual());
        }
        if (propertyDTO.getDivisa() != null) {
            if (!PropertyConstants.Divisas.esValida(propertyDTO.getDivisa())) {
                throw new BusinessValidationException(
                        String.format(PropertyConstants.Mensajes.DIVISA_INVALIDA, propertyDTO.getDivisa())
                );
            }
            property.setDivisa(propertyDTO.getDivisa().toUpperCase());
        }
        if (propertyDTO.getM2() != null) {
            property.setM2(propertyDTO.getM2());
        }
        property.setNHabit(propertyDTO.getNHabit());
        property.setNBanos(propertyDTO.getNBanos());
        if (propertyDTO.getPetFriendly() != null) {
            property.setPetFriendly(propertyDTO.getPetFriendly());
        }
        if (propertyDTO.getDireccion() != null) {
            property.setDireccion(propertyDTO.getDireccion());
        }
        if (propertyDTO.getTipoId() != null) {
            Tipo tipo = tipoRepository.findById(propertyDTO.getTipoId())
                    .orElseThrow(() -> new BusinessValidationException(
                            String.format(PropertyConstants.Mensajes.TIPO_NO_ENCONTRADO, propertyDTO.getTipoId())
                    ));
            property.setTipo(tipo);
        }
        if (propertyDTO.getComunaId() != null) {
            Comuna comuna = comunaRepository.findById(propertyDTO.getComunaId())
                    .orElseThrow(() -> new BusinessValidationException(
                            String.format(PropertyConstants.Mensajes.COMUNA_NO_ENCONTRADA, propertyDTO.getComunaId())
                    ));
            property.setComuna(comuna);
        }

        Property updated = propertyRepository.save(property);
        log.info("Propiedad actualizada exitosamente con ID: {}", updated.getId());

        return convertToDTO(updated, true);
    }

    @Transactional
    public void eliminar(Long id) {
        log.info("Eliminando propiedad con ID: {}", id);

        if (!propertyRepository.existsById(id)) {
            throw new ResourceNotFoundException(
                    String.format(PropertyConstants.Mensajes.PROPIEDAD_NO_ENCONTRADA, id)
            );
        }

        propertyRepository.deleteById(id);
        log.info("Propiedad eliminada exitosamente con ID: {}", id);
    }

    @Transactional(readOnly = true)
    public List<PropertyDTO> buscarConFiltros(
            Long tipoId,
            Long comunaId,
            BigDecimal minPrecio,
            BigDecimal maxPrecio,
            Integer nHabit,
            Integer nBanos,
            Boolean petFriendly,
            boolean includeDetails) {

        log.debug("Buscando propiedades con filtros - tipo: {}, comuna: {}, minPrecio: {}, maxPrecio: {}",
                tipoId, comunaId, minPrecio, maxPrecio);

        List<Property> properties = propertyRepository.findByFilters(
                comunaId, tipoId, minPrecio, maxPrecio, nHabit, nBanos, petFriendly
        );

        return properties.stream()
                .map(p -> convertToDTO(p, includeDetails))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public boolean existsProperty(Long id) {
        return propertyRepository.existsById(id);
    }

    // ==================== LISTADO DE CATÁLOGOS (SOLUCIÓN AL ERROR 500) ====================

    @Transactional(readOnly = true)
    public List<TipoDTO> listarTodosTipos() {
        log.debug("Listando todos los tipos.");

        return tipoRepository.findAll().stream()
                .map(tipo -> modelMapper.map(tipo, TipoDTO.class))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RegionDTO> listarTodasRegiones() {
        log.debug("Listando todas las regiones.");

        return regionRepository.findAll().stream()
                .map(region -> modelMapper.map(region, RegionDTO.class))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ComunaDTO> listarTodasComunas() {
        log.debug("Listando todas las comunas.");

        List<Comuna> comunas = comunaRepository.findAll();

        // Mapeamos a DTO, forzando la inicialización del proxy de Region dentro de la transacción
        return comunas.stream()
                .map(comuna -> {
                    // Forzar el acceso a la propiedad LAZY de Region dentro de la sesión activa
                    comuna.getRegion().getNombre();

                    return modelMapper.map(comuna, ComunaDTO.class);
                })
                .collect(Collectors.toList());
    }

    // ==================== FIN LISTADO DE CATÁLOGOS ====================

    private PropertyDTO convertToDTO(Property property, boolean includeDetails) {
        PropertyDTO dto = new PropertyDTO();

        dto.setId(property.getId());
        dto.setCodigo(property.getCodigo());
        dto.setTitulo(property.getTitulo());
        dto.setPrecioMensual(property.getPrecioMensual());
        dto.setDivisa(property.getDivisa());
        dto.setM2(property.getM2());
        dto.setNHabit(property.getNHabit());
        dto.setNBanos(property.getNBanos());
        dto.setPetFriendly(property.getPetFriendly());
        dto.setDireccion(property.getDireccion());
        dto.setFcreacion(property.getFcreacion());


        dto.setTipoId(property.getTipo().getId());
        dto.setComunaId(property.getComuna().getId());

        if (includeDetails) {


            // 1. Inicializar colecciones (Fotos y Categorias)
            property.getFotos().size();
            property.getCategorias().size();

            // 2. Inicializar relaciones de entidad simple (Tipo, Comuna y Región)
            property.getTipo().getNombre();
            property.getComuna().getNombre();
            property.getComuna().getRegion().getNombre();
            // ---------------------------------------------------------


            TipoDTO tipoDTO = modelMapper.map(property.getTipo(), TipoDTO.class);
            dto.setTipo(tipoDTO);

            ComunaDTO comunaDTO = new ComunaDTO();
            comunaDTO.setId(property.getComuna().getId());
            comunaDTO.setNombre(property.getComuna().getNombre());
            comunaDTO.setRegionId(property.getComuna().getRegion().getId());

            RegionDTO regionDTO = modelMapper.map(property.getComuna().getRegion(), RegionDTO.class);
            comunaDTO.setRegion(regionDTO);
            dto.setComuna(comunaDTO);

            List<FotoDTO> fotosDTO = property.getFotos().stream()
                    .map(f -> modelMapper.map(f, FotoDTO.class))
                    .collect(Collectors.toList());
            dto.setFotos(fotosDTO);

            List<CategoriaDTO> categoriasDTO = property.getCategorias().stream()
                    .map(c -> modelMapper.map(c, CategoriaDTO.class))
                    .collect(Collectors.toList());
            dto.setCategorias(categoriasDTO);
        }

        return dto;
    }
}