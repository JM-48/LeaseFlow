package com.rentify.userservice.service;

import com.rentify.userservice.dto.RolDTO;
import com.rentify.userservice.exception.BusinessValidationException;
import com.rentify.userservice.exception.ResourceNotFoundException;
import com.rentify.userservice.model.Rol;
import com.rentify.userservice.repository.RolRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests de RolService")
class RolServiceTest {

    @Mock
    private RolRepository rolRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private RolService rolService;

    private RolDTO rolDTO;
    private Rol rolEntity;

    @BeforeEach
    void setUp() {
        rolDTO = RolDTO.builder()
                .id(1L)
                .nombre("ADMIN")
                .build();

        rolEntity = Rol.builder()
                .id(1L)
                .nombre("ADMIN")
                .build();
    }

    @Test
    @DisplayName("Debe crear rol exitosamente cuando es válido y no existe")
    void crearRol_RolValido_Success() {
        // Arrange
        when(rolRepository.existsByNombre("ADMIN")).thenReturn(false);
        when(modelMapper.map(rolDTO, Rol.class)).thenReturn(rolEntity);
        when(rolRepository.save(any(Rol.class))).thenReturn(rolEntity);
        when(modelMapper.map(rolEntity, RolDTO.class)).thenReturn(rolDTO);

        // Act
        RolDTO resultado = rolService.crearRol(rolDTO);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.getNombre()).isEqualTo("ADMIN");
        verify(rolRepository, times(1)).save(any(Rol.class));
    }

    @Test
    @DisplayName("Debe convertir nombre a mayúsculas al crear rol")
    void crearRol_ConvierteAMayusculas_Success() {
        // Arrange
        RolDTO rolMinuscula = RolDTO.builder().nombre("admin").build();
        Rol rolEntityUpper = Rol.builder().id(1L).nombre("ADMIN").build();

        when(rolRepository.existsByNombre("ADMIN")).thenReturn(false);
        when(modelMapper.map(rolMinuscula, Rol.class)).thenReturn(rolEntityUpper);
        when(rolRepository.save(any(Rol.class))).thenReturn(rolEntityUpper);
        when(modelMapper.map(rolEntityUpper, RolDTO.class)).thenReturn(rolDTO);

        // Act
        RolDTO resultado = rolService.crearRol(rolMinuscula);

        // Assert
        assertThat(resultado).isNotNull();
        verify(rolRepository, times(1)).save(argThat(rol -> rol.getNombre().equals("ADMIN")));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el rol no es válido")
    void crearRol_RolInvalido_ThrowsException() {
        // Arrange
        RolDTO rolInvalido = RolDTO.builder().nombre("ROL_INVALIDO").build();

        // Act & Assert
        assertThatThrownBy(() -> rolService.crearRol(rolInvalido))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("no es válido");

        verify(rolRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el rol ya existe")
    void crearRol_RolDuplicado_ThrowsException() {
        // Arrange
        when(rolRepository.existsByNombre("ADMIN")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> rolService.crearRol(rolDTO))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("Ya existe un rol con el nombre");

        verify(rolRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe obtener todos los roles")
    void obtenerTodos_DeberiaRetornarListaDeRoles() {
        // Arrange
        Rol rol2 = Rol.builder().id(2L).nombre("PROPIETARIO").build();
        Rol rol3 = Rol.builder().id(3L).nombre("ARRIENDATARIO").build();
        List<Rol> roles = Arrays.asList(rolEntity, rol2, rol3);

        RolDTO rolDTO2 = RolDTO.builder().id(2L).nombre("PROPIETARIO").build();
        RolDTO rolDTO3 = RolDTO.builder().id(3L).nombre("ARRIENDATARIO").build();

        when(rolRepository.findAll()).thenReturn(roles);
        when(modelMapper.map(rolEntity, RolDTO.class)).thenReturn(rolDTO);
        when(modelMapper.map(rol2, RolDTO.class)).thenReturn(rolDTO2);
        when(modelMapper.map(rol3, RolDTO.class)).thenReturn(rolDTO3);

        // Act
        List<RolDTO> resultado = rolService.obtenerTodos();

        // Assert
        assertThat(resultado).hasSize(3);
        assertThat(resultado).extracting(RolDTO::getNombre)
                .containsExactly("ADMIN", "PROPIETARIO", "ARRIENDATARIO");
        verify(rolRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Debe obtener rol por ID cuando existe")
    void obtenerPorId_RolExiste_RetornaRol() {
        // Arrange
        when(rolRepository.findById(1L)).thenReturn(Optional.of(rolEntity));
        when(modelMapper.map(rolEntity, RolDTO.class)).thenReturn(rolDTO);

        // Act
        RolDTO resultado = rolService.obtenerPorId(1L);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getNombre()).isEqualTo("ADMIN");
        verify(rolRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el rol no existe por ID")
    void obtenerPorId_RolNoExiste_ThrowsException() {
        // Arrange
        when(rolRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> rolService.obtenerPorId(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Rol con ID 999 no encontrado");

        verify(rolRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Debe obtener rol por nombre cuando existe")
    void obtenerPorNombre_RolExiste_RetornaRol() {
        // Arrange
        when(rolRepository.findByNombre("ADMIN")).thenReturn(Optional.of(rolEntity));
        when(modelMapper.map(rolEntity, RolDTO.class)).thenReturn(rolDTO);

        // Act
        RolDTO resultado = rolService.obtenerPorNombre("admin"); // lowercase

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.getNombre()).isEqualTo("ADMIN");
        verify(rolRepository, times(1)).findByNombre("ADMIN");
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el rol no existe por nombre")
    void obtenerPorNombre_RolNoExiste_ThrowsException() {
        // Arrange
        when(rolRepository.findByNombre("NO_EXISTE")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> rolService.obtenerPorNombre("NO_EXISTE"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Rol NO_EXISTE no encontrado");

        verify(rolRepository, times(1)).findByNombre("NO_EXISTE");
    }
}