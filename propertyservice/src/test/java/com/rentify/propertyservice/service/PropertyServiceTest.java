package com.rentify.propertyservice.service;

import com.rentify.propertyservice.constants.PropertyConstants;
import com.rentify.propertyservice.dto.*;
import com.rentify.propertyservice.exception.BusinessValidationException;
import com.rentify.propertyservice.exception.ResourceNotFoundException;
import com.rentify.propertyservice.model.Comuna;
import com.rentify.propertyservice.model.Property;
import com.rentify.propertyservice.model.Region;
import com.rentify.propertyservice.model.Tipo;
import com.rentify.propertyservice.repository.CategoriaRepository;
import com.rentify.propertyservice.repository.ComunaRepository;
import com.rentify.propertyservice.repository.PropertyRepository;
import com.rentify.propertyservice.repository.TipoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para PropertyService.
 * Utiliza Mockito para moclear dependencias.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests de PropertyService")
class PropertyServiceTest {

    @Mock
    private PropertyRepository propertyRepository;

    @Mock
    private TipoRepository tipoRepository;

    @Mock
    private ComunaRepository comunaRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private PropertyService propertyService;

    private PropertyDTO propertyDTO;
    private Property propertyEntity;
    private Tipo tipo;
    private TipoDTO tipoDTO;
    private Comuna comuna;
    private ComunaDTO comunaDTO;
    private Region region;
    private RegionDTO regionDTO;

    @BeforeEach
    void setUp() {
        region = Region.builder()
                .id(1L)
                .nombre("Región Metropolitana")
                .build();

        regionDTO = RegionDTO.builder()
                .id(1L)
                .nombre("Región Metropolitana")
                .build();

        comuna = Comuna.builder()
                .id(1L)
                .nombre("Providencia")
                .region(region)
                .build();

        comunaDTO = ComunaDTO.builder()
                .id(1L)
                .nombre("Providencia")
                .regionId(1L)
                .region(regionDTO)
                .build();

        tipo = Tipo.builder()
                .id(1L)
                .nombre("Departamento")
                .build();

        tipoDTO = TipoDTO.builder()
                .id(1L)
                .nombre("Departamento")
                .build();

        propertyDTO = PropertyDTO.builder()
                .codigo("DP001")
                .titulo("Dpto 2D/2B")
                .precioMensual(BigDecimal.valueOf(650000))
                .divisa("CLP")
                .m2(BigDecimal.valueOf(65.5))
                .nHabit(2)
                .nBanos(2)
                .petFriendly(true)
                .direccion("Av. Providencia 1234")
                .tipoId(1L)
                .comunaId(1L)
                .build();

        propertyEntity = Property.builder()
                .id(1L)
                .codigo("DP001")
                .titulo("Dpto 2D/2B")
                .precioMensual(BigDecimal.valueOf(650000))
                .divisa("CLP")
                .m2(BigDecimal.valueOf(65.5))
                .nHabit(2)
                .nBanos(2)
                .petFriendly(true)
                .direccion("Av. Providencia 1234")
                .fcreacion(LocalDate.now())
                .tipo(tipo)
                .comuna(comuna)
                .build();
    }

    // ==================== Tests de Creación ====================

    @Test
    @DisplayName("crearProperty - Debe crear propiedad exitosamente con datos válidos")
    void crearProperty_DatosValidos_Success() {
        // Arrange
        when(propertyRepository.existsByCodigo("DP001")).thenReturn(false);
        when(tipoRepository.findById(1L)).thenReturn(Optional.of(tipo));
        when(comunaRepository.findById(1L)).thenReturn(Optional.of(comuna));
        when(propertyRepository.save(any(Property.class))).thenReturn(propertyEntity);
        when(modelMapper.map(tipo, TipoDTO.class)).thenReturn(tipoDTO);
        when(modelMapper.map(region, RegionDTO.class)).thenReturn(regionDTO);

        // Act
        PropertyDTO resultado = propertyService.crearProperty(propertyDTO);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.getCodigo()).isEqualTo("DP001");
        verify(propertyRepository, times(1)).save(any(Property.class));
    }

    @Test
    @DisplayName("crearProperty - Debe lanzar excepción si código está duplicado")
    void crearProperty_CodigoDuplicado_ThrowsException() {
        // Arrange
        when(propertyRepository.existsByCodigo("DP001")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> propertyService.crearProperty(propertyDTO))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("código");

        verify(propertyRepository, never()).save(any());
    }

    @Test
    @DisplayName("crearProperty - Debe lanzar excepción si divisa es inválida")
    void crearProperty_DivisaInvalida_ThrowsException() {
        // Arrange
        propertyDTO.setDivisa("XYZ");
        when(propertyRepository.existsByCodigo("DP001")).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> propertyService.crearProperty(propertyDTO))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("divisa");

        verify(propertyRepository, never()).save(any());
    }

    @Test
    @DisplayName("crearProperty - Debe lanzar excepción si precio es inválido")
    void crearProperty_PrecioInvalido_ThrowsException() {
        // Arrange
        propertyDTO.setPrecioMensual(BigDecimal.ZERO);
        when(propertyRepository.existsByCodigo("DP001")).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> propertyService.crearProperty(propertyDTO))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("precio");

        verify(propertyRepository, never()).save(any());
    }

    @Test
    @DisplayName("crearProperty - Debe lanzar excepción si tipo no existe")
    void crearProperty_TipoNoExiste_ThrowsException() {
        // Arrange
        when(propertyRepository.existsByCodigo("DP001")).thenReturn(false);
        when(tipoRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> propertyService.crearProperty(propertyDTO))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("tipo");

        verify(propertyRepository, never()).save(any());
    }

    @Test
    @DisplayName("crearProperty - Debe lanzar excepción si comuna no existe")
    void crearProperty_ComunaNoExiste_ThrowsException() {
        // Arrange
        when(propertyRepository.existsByCodigo("DP001")).thenReturn(false);
        when(tipoRepository.findById(1L)).thenReturn(Optional.of(tipo));
        when(comunaRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> propertyService.crearProperty(propertyDTO))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("comuna");

        verify(propertyRepository, never()).save(any());
    }

    // ==================== Tests de Listado ====================

    @Test
    @DisplayName("listarTodas - Debe retornar lista de propiedades sin detalles")
    void listarTodas_SinDetalles_ReturnsDto() {
        // Arrange
        when(propertyRepository.findAll()).thenReturn(List.of(propertyEntity));
        // ✅ NO mockear nada más - convertToDTO lo maneja manualmente sin modelMapper

        // Act
        List<PropertyDTO> resultado = propertyService.listarTodas(false);

        // Assert
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getCodigo()).isEqualTo("DP001");
    }

    @Test
    @DisplayName("listarTodas - Debe retornar lista de propiedades con detalles")
    void listarTodas_ConDetalles_ReturnsDto() {
        // Arrange
        when(propertyRepository.findAll()).thenReturn(List.of(propertyEntity));
        // ✅ Solo mockear tipos relacionados, convertToDTO lo maneja manualmente
        when(modelMapper.map(tipo, TipoDTO.class)).thenReturn(tipoDTO);
        when(modelMapper.map(region, RegionDTO.class)).thenReturn(regionDTO);

        // Act
        List<PropertyDTO> resultado = propertyService.listarTodas(true);

        // Assert
        assertThat(resultado).hasSize(1);
    }

    // ==================== Tests de Obtención ====================

    @Test
    @DisplayName("obtenerPorId - Debe retornar propiedad cuando existe")
    void obtenerPorId_PropiedadExiste_ReturnsDto() {
        // Arrange
        when(propertyRepository.findById(1L)).thenReturn(Optional.of(propertyEntity));
        // ✅ NO mockear Property->PropertyDTO aquí, solo mockeamos cuando convertToDTO lo necesite
        when(modelMapper.map(tipo, TipoDTO.class)).thenReturn(tipoDTO);
        when(modelMapper.map(region, RegionDTO.class)).thenReturn(regionDTO);

        // Act
        PropertyDTO resultado = propertyService.obtenerPorId(1L, true);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.getCodigo()).isEqualTo("DP001");
    }

    @Test
    @DisplayName("obtenerPorId - Debe lanzar excepción si propiedad no existe")
    void obtenerPorId_PropiedadNoExiste_ThrowsException() {
        // Arrange
        when(propertyRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> propertyService.obtenerPorId(999L, false))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("no existe");
    }

    @Test
    @DisplayName("obtenerPorCodigo - Debe retornar propiedad cuando existe")
    void obtenerPorCodigo_PropiedadExiste_ReturnsDto() {
        // Arrange
        when(propertyRepository.findByCodigo("DP001")).thenReturn(Optional.of(propertyEntity));
        // ✅ NO mockear Property->PropertyDTO aquí, solo mockeamos tipos relacionados
        when(modelMapper.map(tipo, TipoDTO.class)).thenReturn(tipoDTO);
        when(modelMapper.map(region, RegionDTO.class)).thenReturn(regionDTO);

        // Act
        PropertyDTO resultado = propertyService.obtenerPorCodigo("DP001", true);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.getCodigo()).isEqualTo("DP001");
    }

    // ==================== Tests de Actualización ====================

    @Test
    @DisplayName("actualizar - Debe actualizar propiedad exitosamente")
    void actualizar_DatosValidos_Success() {
        // Arrange
        PropertyDTO updateDTO = PropertyDTO.builder()
                .titulo("Dpto Actualizado")
                .precioMensual(BigDecimal.valueOf(700000))
                .build();

        when(propertyRepository.findById(1L)).thenReturn(Optional.of(propertyEntity));
        when(propertyRepository.save(any(Property.class))).thenReturn(propertyEntity);
        // ✅ NO mockear Property->PropertyDTO aquí, solo mockeamos tipos relacionados
        when(modelMapper.map(tipo, TipoDTO.class)).thenReturn(tipoDTO);
        when(modelMapper.map(region, RegionDTO.class)).thenReturn(regionDTO);

        // Act
        PropertyDTO resultado = propertyService.actualizar(1L, updateDTO);

        // Assert
        assertThat(resultado).isNotNull();
        verify(propertyRepository, times(1)).save(any(Property.class));
    }

    @Test
    @DisplayName("actualizar - Debe lanzar excepción si propiedad no existe")
    void actualizar_PropiedadNoExiste_ThrowsException() {
        // Arrange
        when(propertyRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> propertyService.actualizar(999L, propertyDTO))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(propertyRepository, never()).save(any());
    }

    // ==================== Tests de Eliminación ====================

    @Test
    @DisplayName("eliminar - Debe eliminar propiedad exitosamente")
    void eliminar_PropiedadExiste_Success() {
        // Arrange
        when(propertyRepository.existsById(1L)).thenReturn(true);

        // Act
        propertyService.eliminar(1L);

        // Assert
        verify(propertyRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("eliminar - Debe lanzar excepción si propiedad no existe")
    void eliminar_PropiedadNoExiste_ThrowsException() {
        // Arrange
        when(propertyRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> propertyService.eliminar(999L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(propertyRepository, never()).deleteById(999L);
    }

    // ==================== Tests de Búsqueda ====================

    @Test
    @DisplayName("buscarConFiltros - Debe retornar propiedades que cumplen filtros")
    void buscarConFiltros_ConFiltros_ReturnsList() {
        // Arrange
        // ✅ CORREGIDO: Usar any() en lugar de anyLong(), anyInt(), etc.
        // porque el controller envía null para parámetros no especificados
        when(propertyRepository.findByFilters(
                any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of(propertyEntity));
        // ✅ NO mockear Property->PropertyDTO aquí porque includeDetails=false

        // Act
        List<PropertyDTO> resultado = propertyService.buscarConFiltros(
                1L, 1L, BigDecimal.valueOf(600000), BigDecimal.valueOf(700000),
                2, 2, true, false
        );

        // Assert
        assertThat(resultado).hasSize(1);
    }

    // ==================== Tests de Verificación ====================

    @Test
    @DisplayName("existsProperty - Debe retornar true si propiedad existe")
    void existsProperty_PropiedadExiste_ReturnsTrue() {
        // Arrange
        when(propertyRepository.existsById(1L)).thenReturn(true);

        // Act
        boolean existe = propertyService.existsProperty(1L);

        // Assert
        assertThat(existe).isTrue();
    }

    @Test
    @DisplayName("existsProperty - Debe retornar false si propiedad no existe")
    void existsProperty_PropiedadNoExiste_ReturnsFalse() {
        // Arrange
        when(propertyRepository.existsById(999L)).thenReturn(false);

        // Act
        boolean existe = propertyService.existsProperty(999L);

        // Assert
        assertThat(existe).isFalse();
    }
}