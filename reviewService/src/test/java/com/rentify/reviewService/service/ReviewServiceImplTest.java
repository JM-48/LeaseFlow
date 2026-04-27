package com.rentify.reviewService.service;

import com.rentify.reviewService.client.PropertyServiceClient;
import com.rentify.reviewService.client.UserServiceClient;
import com.rentify.reviewService.dto.ReviewDTO;
import com.rentify.reviewService.dto.external.UsuarioDTO;
import com.rentify.reviewService.exception.BusinessValidationException;
import com.rentify.reviewService.exception.ResourceNotFoundException;
import com.rentify.reviewService.model.Review;
import com.rentify.reviewService.model.TipoResena;
import com.rentify.reviewService.repository.ReviewRepository;
import com.rentify.reviewService.repository.TipoResenaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para ReviewServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests de ReviewServiceImpl")
class ReviewServiceImplTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private TipoResenaRepository tipoResenaRepository;

    @Mock
    private UserServiceClient userServiceClient;

    @Mock
    private PropertyServiceClient propertyServiceClient;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private ReviewServiceImpl service;

    private UsuarioDTO usuarioValido;
    private ReviewDTO reviewDTO;
    private Review reviewEntity;
    private TipoResena tipoResena;

    @BeforeEach
    void setUp() {
        usuarioValido = UsuarioDTO.builder()
                .id(1L)
                .pnombre("Juan")
                .papellido("Pérez")
                .rol("ARRIENDATARIO")
                .estado("ACTIVO")
                .build();

        reviewDTO = ReviewDTO.builder()
                .usuarioId(1L)
                .propiedadId(1L)
                .puntaje(8)
                .comentario("Excelente propiedad, muy bien ubicada")
                .tipoResenaId(1L)
                .build();

        reviewEntity = Review.builder()
                .id(1L)
                .usuarioId(1L)
                .propiedadId(1L)
                .puntaje(8)
                .comentario("Excelente propiedad, muy bien ubicada")
                .tipoResenaId(1L)
                .estado("ACTIVA")
                .build();

        tipoResena = TipoResena.builder()
                .id(1L)
                .nombre("RESENA_PROPIEDAD")
                .build();
    }

    @Test
    @DisplayName("Debe crear reseña exitosamente cuando todos los datos son válidos")
    void crearResena_DatosValidos_Success() {
        // Arrange
        when(userServiceClient.getUserById(1L)).thenReturn(usuarioValido);
        when(tipoResenaRepository.findById(1L)).thenReturn(Optional.of(tipoResena));
        when(propertyServiceClient.existsProperty(1L)).thenReturn(true);
        when(reviewRepository.existsByUsuarioIdAndPropiedadId(1L, 1L)).thenReturn(false);
        when(propertyServiceClient.isPropertyOwner(1L, 1L)).thenReturn(false);
        when(modelMapper.map(any(ReviewDTO.class), eq(Review.class))).thenReturn(reviewEntity);
        when(reviewRepository.save(any(Review.class))).thenReturn(reviewEntity);
        when(modelMapper.map(any(Review.class), eq(ReviewDTO.class))).thenReturn(reviewDTO);

        // Act
        ReviewDTO resultado = service.crearResena(reviewDTO);

        // Assert
        assertThat(resultado).isNotNull();
        verify(reviewRepository, times(1)).save(any(Review.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el usuario no existe")
    void crearResena_UsuarioNoExiste_ThrowsException() {
        // Arrange
        when(userServiceClient.getUserById(1L)).thenReturn(null);

        // Act & Assert
        assertThatThrownBy(() -> service.crearResena(reviewDTO))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("usuario con ID 1 no existe");

        verify(reviewRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el usuario no tiene permisos")
    void crearResena_RolInvalido_ThrowsException() {
        // Arrange
        usuarioValido.setRol("INVALIDO");
        when(userServiceClient.getUserById(1L)).thenReturn(usuarioValido);

        // Act & Assert
        assertThatThrownBy(() -> service.crearResena(reviewDTO))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("no tiene permisos");

        verify(reviewRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el puntaje es inválido")
    void crearResena_PuntajeInvalido_ThrowsException() {
        // Arrange
        reviewDTO.setPuntaje(15); // Puntaje fuera de rango
        when(userServiceClient.getUserById(1L)).thenReturn(usuarioValido);

        // Act & Assert
        assertThatThrownBy(() -> service.crearResena(reviewDTO))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("puntaje debe estar entre");

        verify(reviewRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando la propiedad no existe")
    void crearResena_PropiedadNoExiste_ThrowsException() {
        // Arrange
        when(userServiceClient.getUserById(1L)).thenReturn(usuarioValido);
        when(tipoResenaRepository.findById(1L)).thenReturn(Optional.of(tipoResena));
        when(propertyServiceClient.existsProperty(1L)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> service.crearResena(reviewDTO))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("propiedad con ID 1 no existe");

        verify(reviewRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando ya existe una reseña duplicada")
    void crearResena_ResenaDuplicada_ThrowsException() {
        // Arrange
        when(userServiceClient.getUserById(1L)).thenReturn(usuarioValido);
        when(tipoResenaRepository.findById(1L)).thenReturn(Optional.of(tipoResena));
        when(propertyServiceClient.existsProperty(1L)).thenReturn(true);
        when(reviewRepository.existsByUsuarioIdAndPropiedadId(1L, 1L)).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> service.crearResena(reviewDTO))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("ya ha creado una reseña");

        verify(reviewRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el propietario intenta reseñar su propia propiedad")
    void crearResena_PropietarioResenaPropia_ThrowsException() {
        // Arrange
        when(userServiceClient.getUserById(1L)).thenReturn(usuarioValido);
        when(tipoResenaRepository.findById(1L)).thenReturn(Optional.of(tipoResena));
        when(propertyServiceClient.existsProperty(1L)).thenReturn(true);
        when(reviewRepository.existsByUsuarioIdAndPropiedadId(1L, 1L)).thenReturn(false);
        when(propertyServiceClient.isPropertyOwner(1L, 1L)).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> service.crearResena(reviewDTO))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("no puede reseñar su propia propiedad");

        verify(reviewRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe obtener reseña por ID exitosamente")
    void obtenerPorId_ResenaExiste_Success() {
        // Arrange
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(reviewEntity));
        when(modelMapper.map(any(Review.class), eq(ReviewDTO.class))).thenReturn(reviewDTO);

        // Act
        ReviewDTO resultado = service.obtenerPorId(1L, false);

        // Assert
        assertThat(resultado).isNotNull();
        verify(reviewRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando la reseña no existe")
    void obtenerPorId_ResenaNoExiste_ThrowsException() {
        // Arrange
        when(reviewRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> service.obtenerPorId(999L, false))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("reseña con ID 999 no existe");
    }

    @Test
    @DisplayName("Debe actualizar estado de reseña exitosamente")
    void actualizarEstado_EstadoValido_Success() {
        // Arrange
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(reviewEntity));
        when(reviewRepository.save(any(Review.class))).thenReturn(reviewEntity);
        when(modelMapper.map(any(Review.class), eq(ReviewDTO.class))).thenReturn(reviewDTO);

        // Act
        ReviewDTO resultado = service.actualizarEstado(1L, "BANEADA");

        // Assert
        assertThat(resultado).isNotNull();
        verify(reviewRepository, times(1)).save(any(Review.class));
    }

    @Test
    @DisplayName("Debe eliminar reseña exitosamente")
    void eliminarResena_ResenaExiste_Success() {
        // Arrange
        when(reviewRepository.existsById(1L)).thenReturn(true);

        // Act
        service.eliminarResena(1L);

        // Assert
        verify(reviewRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Debe lanzar excepción al eliminar reseña que no existe")
    void eliminarResena_ResenaNoExiste_ThrowsException() {
        // Arrange
        when(reviewRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> service.eliminarResena(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("reseña con ID 999 no existe");

        verify(reviewRepository, never()).deleteById(any());
    }
}