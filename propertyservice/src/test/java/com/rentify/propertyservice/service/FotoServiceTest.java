package com.rentify.propertyservice.service;

import com.rentify.propertyservice.constants.PropertyConstants;
import com.rentify.propertyservice.dto.FotoDTO;
import com.rentify.propertyservice.exception.FileStorageException;
import com.rentify.propertyservice.exception.ResourceNotFoundException;
import com.rentify.propertyservice.model.Foto;
import com.rentify.propertyservice.model.Property;
import com.rentify.propertyservice.repository.FotoRepository;
import com.rentify.propertyservice.repository.PropertyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para FotoService.
 * Utiliza Mockito para moclear dependencias de archivo y BD.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests de FotoService")
class FotoServiceTest {

    @Mock
    private FotoRepository fotoRepository;

    @Mock
    private PropertyRepository propertyRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private MultipartFile multipartFile;

    @InjectMocks
    private FotoService fotoService;

    private Property property;
    private Foto foto;
    private FotoDTO fotoDTO;

    @BeforeEach
    void setUp() {
        // Inyectar uploadDir usando reflexión
        ReflectionTestUtils.setField(fotoService, "uploadDir", "test-uploads");

        property = Property.builder()
                .id(1L)
                .codigo("DP001")
                .titulo("Dpto Test")
                .build();

        foto = Foto.builder()
                .id(1L)
                .nombre("test.jpg")
                .url("test-uploads/properties/1/1234567890_test.jpg")
                .sortOrder(0)
                .property(property)
                .build();

        fotoDTO = FotoDTO.builder()
                .id(1L)
                .nombre("test.jpg")
                .url("test-uploads/properties/1/1234567890_test.jpg")
                .sortOrder(0)
                .propiedadId(1L)
                .build();
    }

    // ==================== Tests de Validación de Archivo ====================

    @Test
    @DisplayName("guardarFoto - Debe lanzar excepción si propiedad no existe")
    void guardarFoto_PropiedadNoExiste_ThrowsException() {
        // Arrange
        when(propertyRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> fotoService.guardarFoto(999L, multipartFile))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("propiedad");

        verify(fotoRepository, never()).save(any());
    }

    @Test
    @DisplayName("guardarFoto - Debe lanzar excepción si archivo está vacío")
    void guardarFoto_ArchivoVacio_ThrowsException() {
        // Arrange
        when(propertyRepository.findById(1L)).thenReturn(Optional.of(property));
        when(multipartFile.isEmpty()).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> fotoService.guardarFoto(1L, multipartFile))
                .isInstanceOf(FileStorageException.class)
                .hasMessageContaining("vacío");

        verify(fotoRepository, never()).save(any());
    }

    @Test
    @DisplayName("guardarFoto - Debe lanzar excepción si formato de archivo es inválido")
    void guardarFoto_FormatoInvalido_ThrowsException() {
        // Arrange
        when(propertyRepository.findById(1L)).thenReturn(Optional.of(property));
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getContentType()).thenReturn("application/pdf"); // Inválido

        // Act & Assert
        assertThatThrownBy(() -> fotoService.guardarFoto(1L, multipartFile))
                .isInstanceOf(FileStorageException.class)
                .hasMessageContaining("formato");

        verify(fotoRepository, never()).save(any());
    }

    @Test
    @DisplayName("guardarFoto - Debe lanzar excepción si archivo es demasiado grande")
    void guardarFoto_ArchivoDemasiandoGrande_ThrowsException() {
        // Arrange
        when(propertyRepository.findById(1L)).thenReturn(Optional.of(property));
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getContentType()).thenReturn("image/jpeg");
        // Archivo de 15 MB (máximo es 10 MB)
        when(multipartFile.getSize()).thenReturn(15 * 1024 * 1024L);

        // Act & Assert
        // ✅ CORREGIDO: Cambiar "demasiado grande" por el mensaje real del servicio
        assertThatThrownBy(() -> fotoService.guardarFoto(1L, multipartFile))
                .isInstanceOf(FileStorageException.class)
                .hasMessageContaining("tamaño máximo");

        verify(fotoRepository, never()).save(any());
    }

    @Test
    @DisplayName("guardarFoto - Debe lanzar excepción si se alcanza límite de fotos")
    void guardarFoto_LimiteFotosAlcanzado_ThrowsException() {
        // Arrange
        when(propertyRepository.findById(1L)).thenReturn(Optional.of(property));
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getContentType()).thenReturn("image/jpeg");
        when(multipartFile.getSize()).thenReturn(1 * 1024 * 1024L); // 1 MB válido
        // Se alcanzó el límite de 20 fotos
        when(fotoRepository.countByPropertyId(1L))
                .thenReturn((long) PropertyConstants.Limites.MAX_FOTOS_POR_PROPIEDAD);

        // Act & Assert
        assertThatThrownBy(() -> fotoService.guardarFoto(1L, multipartFile))
                .isInstanceOf(FileStorageException.class)
                .hasMessageContaining("límite");

        verify(fotoRepository, never()).save(any());
    }

    @Test
    @DisplayName("guardarFoto - Debe guardar foto exitosamente con datos válidos")
    void guardarFoto_DatosValidos_Success() throws IOException {
        // Arrange
        when(propertyRepository.findById(1L)).thenReturn(Optional.of(property));
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getContentType()).thenReturn("image/jpeg");
        when(multipartFile.getSize()).thenReturn(2 * 1024 * 1024L); // 2 MB
        when(multipartFile.getOriginalFilename()).thenReturn("test.jpg");
        when(multipartFile.getInputStream())
                .thenReturn(new ByteArrayInputStream("fake image data".getBytes()));
        when(fotoRepository.countByPropertyId(1L)).thenReturn(0L);
        when(fotoRepository.findMaxSortOrderByPropertyId(1L)).thenReturn(null);
        when(fotoRepository.save(any(Foto.class))).thenReturn(foto);
        when(modelMapper.map(any(Foto.class), eq(FotoDTO.class))).thenReturn(fotoDTO);

        // Act
        FotoDTO resultado = fotoService.guardarFoto(1L, multipartFile);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.getNombre()).isEqualTo("test.jpg");
        verify(fotoRepository, times(1)).save(any(Foto.class));
    }

    // ==================== Tests de Listado ====================

    @Test
    @DisplayName("listarFotos - Debe retornar lista de fotos de una propiedad")
    void listarFotos_PropiedadExiste_ReturnsDto() {
        // Arrange
        when(propertyRepository.existsById(1L)).thenReturn(true);
        when(fotoRepository.findByPropertyIdOrderBySortOrderAsc(1L))
                .thenReturn(List.of(foto));
        when(modelMapper.map(any(Foto.class), eq(FotoDTO.class)))
                .thenReturn(fotoDTO);

        // Act
        List<FotoDTO> resultado = fotoService.listarFotos(1L);

        // Assert
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getNombre()).isEqualTo("test.jpg");
    }

    @Test
    @DisplayName("listarFotos - Debe lanzar excepción si propiedad no existe")
    void listarFotos_PropiedadNoExiste_ThrowsException() {
        // Arrange
        when(propertyRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> fotoService.listarFotos(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("propiedad");
    }

    // ==================== Tests de Obtención ====================

    @Test
    @DisplayName("obtenerPorId - Debe retornar foto cuando existe")
    void obtenerPorId_FotoExiste_ReturnsDto() {
        // Arrange
        when(fotoRepository.findById(1L)).thenReturn(Optional.of(foto));
        when(modelMapper.map(any(Foto.class), eq(FotoDTO.class)))
                .thenReturn(fotoDTO);

        // Act
        FotoDTO resultado = fotoService.obtenerPorId(1L);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.getNombre()).isEqualTo("test.jpg");
    }

    @Test
    @DisplayName("obtenerPorId - Debe lanzar excepción si foto no existe")
    void obtenerPorId_FotoNoExiste_ThrowsException() {
        // Arrange
        when(fotoRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> fotoService.obtenerPorId(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("foto");
    }

    // ==================== Tests de Eliminación ====================

    @Test
    @DisplayName("eliminarFoto - Debe eliminar foto exitosamente")
    void eliminarFoto_FotoExiste_Success() {
        // Arrange
        when(fotoRepository.findById(1L)).thenReturn(Optional.of(foto));

        // Act
        fotoService.eliminarFoto(1L);

        // Assert
        verify(fotoRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("eliminarFoto - Debe lanzar excepción si foto no existe")
    void eliminarFoto_FotoNoExiste_ThrowsException() {
        // Arrange
        when(fotoRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> fotoService.eliminarFoto(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("foto");

        verify(fotoRepository, never()).deleteById(any());
    }

    // ==================== Tests de Reordenamiento ====================

    @Test
    @DisplayName("reordenarFotos - Debe reordenar fotos exitosamente")
    void reordenarFotos_FotosValidas_Success() {
        // Arrange
        Foto foto2 = Foto.builder().id(2L).sortOrder(1).property(property).build();

        when(propertyRepository.existsById(1L)).thenReturn(true);
        when(fotoRepository.findById(2L)).thenReturn(Optional.of(foto2));
        when(fotoRepository.findById(1L)).thenReturn(Optional.of(foto));
        when(fotoRepository.save(any(Foto.class))).thenReturn(foto);

        // Act
        fotoService.reordenarFotos(1L, List.of(2L, 1L));

        // Assert
        verify(fotoRepository, times(2)).save(any(Foto.class));
    }

    @Test
    @DisplayName("reordenarFotos - Debe lanzar excepción si propiedad no existe")
    void reordenarFotos_PropiedadNoExiste_ThrowsException() {
        // Arrange
        when(propertyRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> fotoService.reordenarFotos(999L, List.of(1L)))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(fotoRepository, never()).save(any());
    }
}