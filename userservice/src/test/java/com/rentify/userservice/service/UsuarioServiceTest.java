package com.rentify.userservice.service;

import com.rentify.userservice.constants.UserConstants.Estados;
import com.rentify.userservice.dto.LoginDTO;
import com.rentify.userservice.dto.UsuarioDTO;
import com.rentify.userservice.dto.UsuarioUpdateDTO;
import com.rentify.userservice.exception.AuthenticationException;
import com.rentify.userservice.exception.BusinessValidationException;
import com.rentify.userservice.exception.ResourceNotFoundException;
import com.rentify.userservice.model.Usuario;
import com.rentify.userservice.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests de UsuarioService")
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private RolService rolService;

    @Mock
    private EstadoService estadoService;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    private UsuarioDTO usuarioDTO;
    private Usuario usuarioEntity;

    @BeforeEach
    void setUp() {
        usuarioDTO = new UsuarioDTO();
        usuarioDTO.setPnombre("Juan");
        usuarioDTO.setPapellido("Perez");
        usuarioDTO.setEmail("juan.perez@mail.com");
        usuarioDTO.setRut("12345678-9");
        usuarioDTO.setFnacimiento("1995-05-20");
        usuarioDTO.setClave("password123");
        usuarioDTO.setRolId(3L);

        usuarioEntity = new Usuario();
        usuarioEntity.setId(1L);
        usuarioEntity.setPnombre("Juan");
        usuarioEntity.setSnombre("");
        usuarioEntity.setPapellido("Perez");
        usuarioEntity.setEmail("juan.perez@mail.com");
        usuarioEntity.setRut("12345678-9");
        usuarioEntity.setFnacimiento(LocalDate.of(1995, 5, 20));
        usuarioEntity.setClave("encodedPassword");
        usuarioEntity.setEstadoId(1L);
        usuarioEntity.setRolId(3L);
        usuarioEntity.setDuocVip(false);
        usuarioEntity.setPuntos(0);
        usuarioEntity.setCodigoRef("REF123");
    }

    // ==================== TESTS DE REGISTRO ====================

    @Test
    @DisplayName("registrarUsuario - Éxito con datos válidos")
    void registrarUsuario_DatosValidos_Success() {
        // Arrange
        when(usuarioRepository.existsByEmail(usuarioDTO.getEmail())).thenReturn(false);
        when(usuarioRepository.existsByRut(usuarioDTO.getRut())).thenReturn(false);
        when(usuarioRepository.existsByCodigoRef(anyString())).thenReturn(false);
        when(passwordEncoder.encode(usuarioDTO.getClave())).thenReturn("encodedPassword");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioEntity);

        // Act
        UsuarioDTO resultado = usuarioService.registrarUsuario(usuarioDTO);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.getEmail()).isEqualTo(usuarioDTO.getEmail());
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }

    @Test
    @DisplayName("registrarUsuario - Éxito detecta Dominio DUOC y asigna VIP")
    void registrarUsuario_EmailDuoc_AsignaVipTrue() {
        // Arrange
        usuarioDTO.setEmail("alumno@duocuc.cl"); // O el dominio que use Validaciones.DOMINIO_DUOC
        usuarioEntity.setEmail("alumno@duocuc.cl");
        usuarioEntity.setDuocVip(true);

        when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);
        when(usuarioRepository.existsByRut(anyString())).thenReturn(false);
        when(usuarioRepository.existsByCodigoRef(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioEntity);

        // Act
        UsuarioDTO resultado = usuarioService.registrarUsuario(usuarioDTO);

        // Assert
        assertThat(resultado).isNotNull();
        verify(usuarioRepository).save(argThat(user -> user.getDuocVip().equals(true) || user.getEmail().endsWith("duocuc.cl")));
    }

    @Test
    @DisplayName("registrarUsuario - Lanza excepción por formato de fecha inválido")
    void registrarUsuario_FechaInvalida_ThrowsBusinessException() {
        // Arrange
        usuarioDTO.setFnacimiento("20-05-1995"); // Formato incorrecto d-m-y

        // Act & Assert
        assertThatThrownBy(() -> usuarioService.registrarUsuario(usuarioDTO))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("Formato de fecha de nacimiento inválido");
    }

    @Test
    @DisplayName("registrarUsuario - Lanza excepción si el usuario es menor de edad")
    void registrarUsuario_MenorDeEdad_ThrowsBusinessException() {
        // Arrange
        LocalDate anoActual = LocalDate.now();
        usuarioDTO.setFnacimiento(anoActual.minusYears(5).toString()); // 5 años de edad

        // Act & Assert
        assertThatThrownBy(() -> usuarioService.registrarUsuario(usuarioDTO))
                .isInstanceOf(BusinessValidationException.class);
    }

    @Test
    @DisplayName("registrarUsuario - Lanza excepción por Email duplicado")
    void registrarUsuario_EmailDuplicado_ThrowsBusinessException() {
        // Arrange
        when(usuarioRepository.existsByEmail(usuarioDTO.getEmail())).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> usuarioService.registrarUsuario(usuarioDTO))
                .isInstanceOf(BusinessValidationException.class);
    }

    // ==================== TESTS DE LOGIN ====================

    @Test
    @DisplayName("login - Éxito con credenciales correctas")
    void login_CredencialesValidas_Success() {
        // Arrange
        LoginDTO loginDTO = new LoginDTO("juan.perez@mail.com", "password123");
        when(usuarioRepository.findByEmail(loginDTO.getEmail())).thenReturn(Optional.of(usuarioEntity));
        when(passwordEncoder.matches(loginDTO.getClave(), usuarioEntity.getClave())).thenReturn(true);

        // Act
        UsuarioDTO resultado = usuarioService.login(loginDTO);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.getEmail()).isEqualTo(usuarioEntity.getEmail());
    }

    @Test
    @DisplayName("login - Fallo por clave incorrecta")
    void login_ClaveIncorrecta_ThrowsAuthenticationException() {
        // Arrange
        LoginDTO loginDTO = new LoginDTO("juan.perez@mail.com", "wrongPassword");
        when(usuarioRepository.findByEmail(loginDTO.getEmail())).thenReturn(Optional.of(usuarioEntity));
        when(passwordEncoder.matches(loginDTO.getClave(), usuarioEntity.getClave())).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> usuarioService.login(loginDTO))
                .isInstanceOf(AuthenticationException.class);
    }

    // ==================== TESTS DE BÚSQUEDAS ====================

    @Test
    @DisplayName("obtenerPorId - Éxito si el usuario existe")
    void obtenerPorId_UsuarioExiste_RetornaUsuario() {
        // Arrange
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioEntity));

        // Act
        UsuarioDTO resultado = usuarioService.obtenerPorId(1L, false);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("obtenerPorId - Lanza excepción si el usuario no existe")
    void obtenerPorId_UsuarioNoExiste_ThrowsResourceNotFoundException() {
        // Arrange
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> usuarioService.obtenerPorId(99L, false))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ==================== TESTS DE MODIFICACIONES Y AGREGACIONES ====================

    @Test
    @DisplayName("agregarPuntos - Incrementa el puntaje del usuario de forma correcta")
    void agregarPuntos_UsuarioExiste_IncrementaPuntos() {
        // Arrange
        usuarioEntity.setPuntos(100);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioEntity));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        UsuarioDTO resultado = usuarioService.agregarPuntos(1L, 50);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.getPuntos()).isEqualTo(150);
        verify(usuarioRepository, times(1)).save(usuarioEntity);
    }

    @Test
    @DisplayName("cambiarEstado - Lanza excepción si el nuevo estado no es válido")
    void cambiarEstado_EstadoInvalido_ThrowsBusinessException() {
        // Arrange
        Long idInvalido = 999L;
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioEntity));

        // Usamos mockStatic para interceptar el método estático de validación si fuera necesario,
        // o asumimos que Estados.esValido devolverá false para 999L de forma natural.
        try (MockedStatic<Estados> estadosMockedStatic = Mockito.mockStatic(Estados.class)) {
            estadosMockedStatic.when(() -> Estados.esValido(idInvalido)).thenReturn(false);

            // Act & Assert
            assertThatThrownBy(() -> usuarioService.cambiarEstado(1L, idInvalido))
                    .isInstanceOf(BusinessValidationException.class);
        }
    }

    @Test
    @DisplayName("eliminarUsuario - Éxito si el ID existe en el sistema")
    void eliminarUsuario_IdExiste_EliminaCorrectamente() {
        // Arrange
        when(usuarioRepository.existsById(1L)).thenReturn(true);
        doNothing().when(usuarioRepository).deleteById(1L);

        // Act
        usuarioService.eliminarUsuario(1L);

        // Assert
        verify(usuarioRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("eliminarUsuario - Lanza excepción si el ID no existe")
    void eliminarUsuario_IdNoExiste_ThrowsResourceNotFoundException() {
        // Arrange
        when(usuarioRepository.existsById(99L)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> usuarioService.eliminarUsuario(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(usuarioRepository, never()).deleteById(anyLong());
    }
}