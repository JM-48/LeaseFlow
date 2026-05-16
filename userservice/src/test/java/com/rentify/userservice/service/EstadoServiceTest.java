package com.rentify.userservice.service;

import com.rentify.userservice.dto.EstadoDTO;
import com.rentify.userservice.exception.BusinessValidationException;
import com.rentify.userservice.exception.ResourceNotFoundException;
import com.rentify.userservice.model.Estado;
import com.rentify.userservice.repository.EstadoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests de EstadoService")
class EstadoServiceTest {

    @Mock
    private EstadoRepository estadoRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private EstadoService estadoService;

    private EstadoDTO estadoDTO;
    private Estado estadoEntity;

    @BeforeEach
    void setUp() {
        estadoDTO = EstadoDTO.builder()
                .id(1L)
                .nombre("ACTIVO")
                .build();

        estadoEntity = Estado.builder()
                .id(1L)
                .nombre("ACTIVO")
                .build();
    }

    @Test
    @DisplayName("Debe crear estado exitosamente cuando el nombre no existe")
    void crearEstado_NombreNoExiste_Success() {
        // Arrange
        when(estadoRepository.existsByNombre("ACTIVO")).thenReturn(false);
        when(modelMapper.map(estadoDTO, Estado.class)).thenReturn(estadoEntity);
        when(estadoRepository.save(any(Estado.class))).thenReturn(estadoEntity);
        when(modelMapper.map(estadoEntity, EstadoDTO.class)).thenReturn(estadoDTO);

        // Act
        EstadoDTO resultado = estadoService.crearEstado(estadoDTO);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.getNombre()).isEqualTo("ACTIVO");
        verify(estadoRepository, times(1)).save(any(Estado.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el estado ya existe")
    void crearEstado_EstadoDuplicado_ThrowsException() {
        // Arrange
        when(estadoRepository.existsByNombre("ACTIVO")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> estadoService.crearEstado(estadoDTO))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("Ya existe un estado con el nombre ACTIVO");

        verify(estadoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe obtener todos los estados")
    void obtenerTodos_DeberiaRetornarListaDeEstados() {
        // Arrange
        Estado estado2 = Estado.builder().id(2L).nombre("INACTIVO").build();
        List<Estado> estados = Arrays.asList(estadoEntity, estado2);

        EstadoDTO estadoDTO2 = EstadoDTO.builder().id(2L).nombre("INACTIVO").build();

        when(estadoRepository.findAll()).thenReturn(estados);
        when(modelMapper.map(estadoEntity, EstadoDTO.class)).thenReturn(estadoDTO);
        when(modelMapper.map(estado2, EstadoDTO.class)).thenReturn(estadoDTO2);

        // Act
        List<EstadoDTO> resultado = estadoService.obtenerTodos();

        // Assert
        assertThat(resultado).hasSize(2);
        assertThat(resultado).extracting(EstadoDTO::getNombre)
                .containsExactly("ACTIVO", "INACTIVO");
        verify(estadoRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Debe obtener estado por ID cuando existe")
    void obtenerPorId_EstadoExiste_RetornaEstado() {
        // Arrange
        when(estadoRepository.findById(1L)).thenReturn(Optional.of(estadoEntity));
        when(modelMapper.map(estadoEntity, EstadoDTO.class)).thenReturn(estadoDTO);

        // Act
        EstadoDTO resultado = estadoService.obtenerPorId(1L);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getNombre()).isEqualTo("ACTIVO");
        verify(estadoRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el estado no existe por ID")
    void obtenerPorId_EstadoNoExiste_ThrowsException() {
        // Arrange
        when(estadoRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> estadoService.obtenerPorId(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Estado con ID 999 no encontrado");

        verify(estadoRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Debe obtener estado por nombre cuando existe")
    void obtenerPorNombre_EstadoExiste_RetornaEstado() {
        // Arrange
        when(estadoRepository.findByNombre("ACTIVO")).thenReturn(Optional.of(estadoEntity));
        when(modelMapper.map(estadoEntity, EstadoDTO.class)).thenReturn(estadoDTO);

        // Act
        EstadoDTO resultado = estadoService.obtenerPorNombre("ACTIVO");

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.getNombre()).isEqualTo("ACTIVO");
        verify(estadoRepository, times(1)).findByNombre("ACTIVO");
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el estado no existe por nombre")
    void obtenerPorNombre_EstadoNoExiste_ThrowsException() {
        // Arrange
        when(estadoRepository.findByNombre("NO_EXISTE")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> estadoService.obtenerPorNombre("NO_EXISTE"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Estado NO_EXISTE no encontrado");

        verify(estadoRepository, times(1)).findByNombre("NO_EXISTE");
    }
}