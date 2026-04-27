package com.rentify.propertyservice.service;

import com.rentify.propertyservice.constants.PropertyConstants;
import com.rentify.propertyservice.dto.FotoDTO;
import com.rentify.propertyservice.exception.FileStorageException;
import com.rentify.propertyservice.exception.ResourceNotFoundException;
import com.rentify.propertyservice.model.Foto;
import com.rentify.propertyservice.model.Property;
import com.rentify.propertyservice.repository.FotoRepository;
import com.rentify.propertyservice.repository.PropertyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para gestión de fotos de propiedades.
 * Maneja subida, validación y eliminación de archivos.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FotoService {

    private final FotoRepository fotoRepository;
    private final PropertyRepository propertyRepository;
    private final ModelMapper modelMapper;

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    /**
     * Guarda una nueva foto para una propiedad.
     * Realiza validaciones de:
     * - Existencia de propiedad
     * - Límite de 20 fotos por propiedad
     * - Formato de archivo válido
     * - Tamaño de archivo
     *
     * @param propertyId ID de la propiedad
     * @param file Archivo a guardar
     * @return FotoDTO con los datos de la foto guardada
     * @throws ResourceNotFoundException si la propiedad no existe
     * @throws FileStorageException si hay errores en la validación o almacenamiento
     */
    @Transactional
    public FotoDTO guardarFoto(Long propertyId, MultipartFile file) {
        log.info("Guardando foto para propiedad ID: {}", propertyId);

        // 1. Validar que la propiedad existe
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> {
                    log.error("Propiedad no encontrada: {}", propertyId);
                    return new ResourceNotFoundException(
                            String.format(PropertyConstants.Mensajes.PROPIEDAD_NO_ENCONTRADA, propertyId)
                    );
                });

        // 2. Validar que el archivo no está vacío
        if (file.isEmpty()) {
            log.error("Archivo vacío para propiedad: {}", propertyId);
            throw new FileStorageException(PropertyConstants.Mensajes.ARCHIVO_VACIO);
        }

        // 3. Validar formato de archivo
        String contentType = file.getContentType();
        if (!PropertyConstants.FormatosArchivo.esFormatoValido(contentType)) {
            log.error("Formato de archivo inválido: {} para propiedad: {}", contentType, propertyId);
            throw new FileStorageException(PropertyConstants.Mensajes.FORMATO_ARCHIVO_INVALIDO);
        }

        // 4. Validar tamaño de archivo
        long fileSizeInMB = file.getSize() / (1024 * 1024);
        if (fileSizeInMB > PropertyConstants.Limites.MAX_FILE_SIZE_MB) {
            log.error("Archivo demasiado grande: {} MB (máximo: {} MB)",
                    fileSizeInMB, PropertyConstants.Limites.MAX_FILE_SIZE_MB);
            throw new FileStorageException(
                    String.format(PropertyConstants.Mensajes.ARCHIVO_MUY_GRANDE,
                            PropertyConstants.Limites.MAX_FILE_SIZE_MB)
            );
        }

        // 5. Validar límite de fotos (máximo 20 por propiedad)
        long countFotos = fotoRepository.countByPropertyId(propertyId);
        if (countFotos >= PropertyConstants.Limites.MAX_FOTOS_POR_PROPIEDAD) {
            log.error("Se alcanzó el límite de fotos para propiedad: {}", propertyId);
            throw new FileStorageException(
                    String.format(PropertyConstants.Mensajes.MAX_FOTOS_ALCANZADO,
                            PropertyConstants.Limites.MAX_FOTOS_POR_PROPIEDAD)
            );
        }

        // 6. Crear directorio para la propiedad
        Path propertyFolder = Paths.get(uploadDir, "properties", String.valueOf(propertyId));
        try {
            Files.createDirectories(propertyFolder);
            log.debug("Directorio creado/verificado: {}", propertyFolder);
        } catch (IOException e) {
            log.error("Error al crear directorio de subida: {}", e.getMessage());
            throw new FileStorageException("No se pudo crear el directorio de almacenamiento", e);
        }

        // 7. Generar nombre de archivo único
        String originalFilename = file.getOriginalFilename();
        String filename = System.currentTimeMillis() + "_" +
                (originalFilename != null ? originalFilename.replaceAll("[^a-zA-Z0-9._-]", "_") : "image");

        Path targetPath = propertyFolder.resolve(filename);

        // 8. Guardar archivo en disco
        try {
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            log.info("Archivo guardado exitosamente: {}", targetPath);
        } catch (IOException e) {
            log.error("Error al guardar archivo: {}", e.getMessage());
            throw new FileStorageException("No se pudo guardar el archivo de imagen", e);
        }

        // 9. Determinar sortOrder
        Integer maxSortOrder = fotoRepository.findMaxSortOrderByPropertyId(propertyId);
        Integer newSortOrder = (maxSortOrder != null) ? maxSortOrder + 1 : 0;

        // 10. Crear y guardar entidad Foto en BD
        String url = targetPath.toString(); // En producción, esto sería una URL pública

        Foto foto = Foto.builder()
                .nombre(originalFilename != null ? originalFilename : filename)
                .url(url)
                .sortOrder(newSortOrder)
                .property(property)
                .build();

        Foto savedFoto = fotoRepository.save(foto);
        log.info("Foto guardada en BD con ID: {} para propiedad: {}", savedFoto.getId(), propertyId);

        return modelMapper.map(savedFoto, FotoDTO.class);
    }

    /**
     * Lista todas las fotos de una propiedad.
     *
     * @param propertyId ID de la propiedad
     * @return Lista de FotoDTO ordenadas por sortOrder
     */
    @Transactional(readOnly = true)
    public List<FotoDTO> listarFotos(Long propertyId) {
        log.debug("Listando fotos para propiedad ID: {}", propertyId);

        // Validar que la propiedad existe
        if (!propertyRepository.existsById(propertyId)) {
            log.error("Propiedad no encontrada: {}", propertyId);
            throw new ResourceNotFoundException(
                    String.format(PropertyConstants.Mensajes.PROPIEDAD_NO_ENCONTRADA, propertyId)
            );
        }

        return fotoRepository.findByPropertyIdOrderBySortOrderAsc(propertyId).stream()
                .map(f -> modelMapper.map(f, FotoDTO.class))
                .collect(Collectors.toList());
    }

    /**
     * Obtiene una foto específica por ID.
     *
     * @param fotoId ID de la foto
     * @return FotoDTO
     */
    @Transactional(readOnly = true)
    public FotoDTO obtenerPorId(Long fotoId) {
        log.debug("Obteniendo foto con ID: {}", fotoId);

        Foto foto = fotoRepository.findById(fotoId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(PropertyConstants.Mensajes.FOTO_NO_ENCONTRADA, fotoId)
                ));

        return modelMapper.map(foto, FotoDTO.class);
    }

    /**
     * Elimina una foto.
     * Elimina tanto el archivo del disco como el registro de BD.
     *
     * @param fotoId ID de la foto a eliminar
     */
    @Transactional
    public void eliminarFoto(Long fotoId) {
        log.info("Eliminando foto con ID: {}", fotoId);

        Foto foto = fotoRepository.findById(fotoId)
                .orElseThrow(() -> {
                    log.error("Foto no encontrada: {}", fotoId);
                    return new ResourceNotFoundException(
                            String.format(PropertyConstants.Mensajes.FOTO_NO_ENCONTRADA, fotoId)
                    );
                });

        // Intentar eliminar archivo del disco
        try {
            Path filePath = Paths.get(foto.getUrl());
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.debug("Archivo eliminado del disco: {}", filePath);
            }
        } catch (IOException e) {
            log.warn("No se pudo eliminar el archivo del disco: {}. Se procede a eliminar registro en BD.",
                    foto.getUrl());
            // No lanzamos excepción, continuamos con la eliminación de BD
        }

        // Eliminar registro de BD
        fotoRepository.deleteById(fotoId);
        log.info("Foto eliminada exitosamente de BD: {}", fotoId);
    }

    /**
     * Elimina todas las fotos de una propiedad.
     * Se ejecuta típicamente cuando se elimina una propiedad.
     *
     * @param propertyId ID de la propiedad
     */
    @Transactional
    public void eliminarFotosPorPropiedad(Long propertyId) {
        log.info("Eliminando todas las fotos de la propiedad: {}", propertyId);

        List<Foto> fotos = fotoRepository.findByPropertyId(propertyId);

        for (Foto foto : fotos) {
            try {
                Path filePath = Paths.get(foto.getUrl());
                if (Files.exists(filePath)) {
                    Files.delete(filePath);
                    log.debug("Archivo eliminado: {}", filePath);
                }
            } catch (IOException e) {
                log.warn("Error al eliminar archivo: {}", foto.getUrl());
            }
        }

        fotoRepository.deleteByPropertyId(propertyId);
        log.info("Todas las fotos eliminadas para propiedad: {}", propertyId);
    }

    /**
     * Reordena las fotos de una propiedad.
     *
     * @param propertyId ID de la propiedad
     * @param fotosIds Lista de IDs de fotos en el nuevo orden
     */
    @Transactional
    public void reordenarFotos(Long propertyId, List<Long> fotosIds) {
        log.info("Reordenando fotos para propiedad: {}", propertyId);

        if (!propertyRepository.existsById(propertyId)) {
            throw new ResourceNotFoundException(
                    String.format(PropertyConstants.Mensajes.PROPIEDAD_NO_ENCONTRADA, propertyId)
            );
        }

        // Usar variable final para usar en forEach
        int[] sortOrderArray = {0};

        for (Long fotoId : fotosIds) {
            Foto foto = fotoRepository.findById(fotoId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            String.format(PropertyConstants.Mensajes.FOTO_NO_ENCONTRADA, fotoId)
                    ));

            foto.setSortOrder(sortOrderArray[0]);
            fotoRepository.save(foto);
            sortOrderArray[0]++;
        }

        log.info("Fotos reordenadas exitosamente para propiedad: {}", propertyId);
    }
}
