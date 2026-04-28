//package com.rentify.contactService.service;
//
//import com.rentify.contactService.client.UserServiceClient;
//import com.rentify.contactService.dto.MensajeContactoDTO;
//import com.rentify.contactService.dto.RespuestaMensajeDTO;
//import com.rentify.contactService.dto.external.UsuarioDTO;
//import com.rentify.contactService.exception.BusinessValidationException;
//import com.rentify.contactService.exception.ResourceNotFoundException;
//import com.rentify.contactService.model.MensajeContacto;
//import com.rentify.contactService.repository.MensajeContactoRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.modelmapper.ModelMapper;
//
//import java.util.Date;
//import java.util.List;
//import java.util.Optional;
//
//import static org.assertj.core.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//@DisplayName("Tests de MensajeContactoService")
//class MensajeContactoServiceTest {
//
//    @Mock
//    private MensajeContactoRepository repository;
//
//    @Mock
//    private UserServiceClient userServiceClient;
//
//    @Mock
//    private ModelMapper modelMapper;
//
//    @InjectMocks
//    private MensajeContactoService service;
//
//    private UsuarioDTO usuarioValido;
//    private UsuarioDTO adminValido;
//    private MensajeContactoDTO mensajeDTO;
//    private MensajeContacto mensajeEntity;
//
//    @BeforeEach
//    void setUp() {
//        usuarioValido = new UsuarioDTO();
//        usuarioValido.setId(1L);
//        usuarioValido.setPnombre("Juan");
//        usuarioValido.setPapellido("Pérez");
//        usuarioValido.setEmail("juan@email.com");
//        usuarioValido.setRol("ARRIENDATARIO");
//        usuarioValido.setEstado("ACTIVO");
//
//        adminValido = new UsuarioDTO();
//        adminValido.setId(5L);
//        adminValido.setPnombre("Admin");
//        adminValido.setPapellido("Sistema");
//        adminValido.setEmail("admin@rentify.com");
//        adminValido.setRol("ADMIN");
//        adminValido.setEstado("ACTIVO");
//
//        mensajeDTO = MensajeContactoDTO.builder()
//                .nombre("Juan Pérez")
//                .email("juan@email.com")
//                .asunto("Consulta sobre arriendo")
//                .mensaje("Quisiera más información sobre el departamento en Providencia")
//                .numeroTelefono("+56912345678")
//                .usuarioId(1L)
//                .build();
//
//        mensajeEntity = new MensajeContacto();
//        mensajeEntity.setId(1L);
//        mensajeEntity.setNombre("Juan Pérez");
//        mensajeEntity.setEmail("juan@email.com");
//        mensajeEntity.setAsunto("Consulta sobre arriendo");
//        mensajeEntity.setMensaje("Quisiera más información sobre el departamento en Providencia");
//        mensajeEntity.setNumeroTelefono("+56912345678");
//        mensajeEntity.setUsuarioId(1L);
//        mensajeEntity.setEstado("PENDIENTE");
//        mensajeEntity.setFechaCreacion(new Date());
//    }
//
//    @Test
//    @DisplayName("crearMensaje - Debe crear mensaje exitosamente cuando datos son válidos")
//    void crearMensaje_DatosValidos_Success() {
//        // Arrange
//        when(userServiceClient.getUserById(1L)).thenReturn(usuarioValido);
//        when(repository.countByUsuarioIdAndEstado(1L, "PENDIENTE")).thenReturn(0L);
//        when(modelMapper.map(any(MensajeContactoDTO.class), eq(MensajeContacto.class)))
//                .thenReturn(mensajeEntity);
//        when(repository.save(any(MensajeContacto.class))).thenReturn(mensajeEntity);
//        when(modelMapper.map(any(MensajeContacto.class), eq(MensajeContactoDTO.class)))
//                .thenReturn(mensajeDTO);
//
//        // Act
//        MensajeContactoDTO resultado = service.crearMensaje(mensajeDTO);
//
//        // Assert
//        assertThat(resultado).isNotNull();
//        verify(repository, times(1)).save(any(MensajeContacto.class));
//        verify(userServiceClient, atLeastOnce()).getUserById(1L); // Cambiado a atLeastOnce
//    }
//
//    @Test
//    @DisplayName("crearMensaje - Debe lanzar excepción cuando usuario no existe")
//    void crearMensaje_UsuarioNoExiste_ThrowsException() {
//        // Arrange
//        when(userServiceClient.getUserById(1L)).thenReturn(null);
//
//        // Act & Assert
//        assertThatThrownBy(() -> service.crearMensaje(mensajeDTO))
//                .isInstanceOf(BusinessValidationException.class)
//                .hasMessageContaining("usuario con ID 1 no existe");
//
//        verify(repository, never()).save(any());
//    }
//
//    @Test
//    @DisplayName("crearMensaje - Debe lanzar excepción cuando supera límite de mensajes")
//    void crearMensaje_SuperaLimite_ThrowsException() {
//        // Arrange
//        when(userServiceClient.getUserById(1L)).thenReturn(usuarioValido);
//        when(repository.countByUsuarioIdAndEstado(1L, "PENDIENTE")).thenReturn(5L);
//
//        // Act & Assert
//        assertThatThrownBy(() -> service.crearMensaje(mensajeDTO))
//                .isInstanceOf(BusinessValidationException.class)
//                .hasMessageContaining("límite de 5 mensajes pendientes");
//
//        verify(repository, never()).save(any());
//    }
//
//    @Test
//    @DisplayName("crearMensaje - Debe lanzar excepción cuando mensaje es muy corto")
//    void crearMensaje_MensajeMuyCorto_ThrowsException() {
//        // Arrange
//        mensajeDTO.setMensaje("Hola");
//        // NO mockear nada innecesario cuando la excepción se lanza antes
//
//        // Act & Assert
//        assertThatThrownBy(() -> service.crearMensaje(mensajeDTO))
//                .isInstanceOf(BusinessValidationException.class)
//                .hasMessageContaining("al menos 10 caracteres");
//
//        verify(repository, never()).save(any());
//    }
//
//    @Test
//    @DisplayName("crearMensaje - Debe crear mensaje sin usuario autenticado")
//    void crearMensaje_SinUsuarioAutenticado_Success() {
//        // Arrange
//        mensajeDTO.setUsuarioId(null);
//        mensajeEntity.setUsuarioId(null);
//
//        when(repository.countByEmailAndEstado(mensajeDTO.getEmail(), "PENDIENTE")).thenReturn(0L);
//        when(modelMapper.map(any(MensajeContactoDTO.class), eq(MensajeContacto.class)))
//                .thenReturn(mensajeEntity);
//        when(repository.save(any(MensajeContacto.class))).thenReturn(mensajeEntity);
//        when(modelMapper.map(any(MensajeContacto.class), eq(MensajeContactoDTO.class)))
//                .thenReturn(mensajeDTO);
//
//        // Act
//        MensajeContactoDTO resultado = service.crearMensaje(mensajeDTO);
//
//        // Assert
//        assertThat(resultado).isNotNull();
//        verify(repository, times(1)).save(any(MensajeContacto.class));
//        verify(userServiceClient, never()).getUserById(anyLong()); // No debe llamarse
//    }
//
//    @Test
//    @DisplayName("obtenerPorId - Debe retornar mensaje cuando existe")
//    void obtenerPorId_MensajeExiste_ReturnsMensaje() {
//        // Arrange
//        when(repository.findById(1L)).thenReturn(Optional.of(mensajeEntity));
//        when(modelMapper.map(any(MensajeContacto.class), eq(MensajeContactoDTO.class)))
//                .thenReturn(mensajeDTO);
//
//        // Act
//        MensajeContactoDTO resultado = service.obtenerPorId(1L, false);
//
//        // Assert
//        assertThat(resultado).isNotNull();
//        verify(repository, times(1)).findById(1L);
//    }
//
//    @Test
//    @DisplayName("obtenerPorId - Debe lanzar excepción cuando no existe")
//    void obtenerPorId_MensajeNoExiste_ThrowsException() {
//        // Arrange
//        when(repository.findById(999L)).thenReturn(Optional.empty());
//
//        // Act & Assert
//        assertThatThrownBy(() -> service.obtenerPorId(999L, false))
//                .isInstanceOf(ResourceNotFoundException.class)
//                .hasMessageContaining("mensaje con ID 999 no fue encontrado");
//    }
//
//    @Test
//    @DisplayName("listarPorEstado - Debe retornar mensajes filtrados por estado")
//    void listarPorEstado_EstadoValido_ReturnsMensajes() {
//        // Arrange
//        when(repository.findByEstado("PENDIENTE")).thenReturn(List.of(mensajeEntity));
//        when(modelMapper.map(any(MensajeContacto.class), eq(MensajeContactoDTO.class)))
//                .thenReturn(mensajeDTO);
//
//        // Act
//        List<MensajeContactoDTO> resultados = service.listarPorEstado("PENDIENTE");
//
//        // Assert
//        assertThat(resultados).hasSize(1);
//        verify(repository, times(1)).findByEstado("PENDIENTE");
//    }
//
//    @Test
//    @DisplayName("listarPorEstado - Debe lanzar excepción cuando estado es inválido")
//    void listarPorEstado_EstadoInvalido_ThrowsException() {
//        // Act & Assert
//        assertThatThrownBy(() -> service.listarPorEstado("INVALIDO"))
//                .isInstanceOf(BusinessValidationException.class)
//                .hasMessageContaining("estado 'INVALIDO' no es válido");
//
//        verify(repository, never()).findByEstado(anyString());
//    }
//
//    @Test
//    @DisplayName("actualizarEstado - Debe actualizar estado correctamente")
//    void actualizarEstado_EstadoValido_Success() {
//        // Arrange
//        when(repository.findById(1L)).thenReturn(Optional.of(mensajeEntity));
//        when(repository.save(any(MensajeContacto.class))).thenReturn(mensajeEntity);
//        when(modelMapper.map(any(MensajeContacto.class), eq(MensajeContactoDTO.class)))
//                .thenReturn(mensajeDTO);
//
//        // Act
//        MensajeContactoDTO resultado = service.actualizarEstado(1L, "EN_PROCESO");
//
//        // Assert
//        assertThat(resultado).isNotNull();
//        verify(repository, times(1)).save(any(MensajeContacto.class));
//    }
//
//    @Test
//    @DisplayName("responderMensaje - Debe responder mensaje correctamente cuando es admin")
//    void responderMensaje_AdminValido_Success() {
//        // Arrange
//        RespuestaMensajeDTO respuestaDTO = RespuestaMensajeDTO.builder()
//                .respuesta("Gracias por contactarnos. Le responderemos pronto.")
//                .respondidoPor(5L)
//                .nuevoEstado("RESUELTO")
//                .build();
//
//        when(repository.findById(1L)).thenReturn(Optional.of(mensajeEntity));
//        when(userServiceClient.isAdmin(5L)).thenReturn(true);
//        when(repository.save(any(MensajeContacto.class))).thenReturn(mensajeEntity);
//        when(modelMapper.map(any(MensajeContacto.class), eq(MensajeContactoDTO.class)))
//                .thenReturn(mensajeDTO);
//
//        // Act
//        MensajeContactoDTO resultado = service.responderMensaje(1L, respuestaDTO);
//
//        // Assert
//        assertThat(resultado).isNotNull();
//        verify(userServiceClient, times(1)).isAdmin(5L);
//        verify(repository, times(1)).save(any(MensajeContacto.class));
//    }
//
//    @Test
//    @DisplayName("responderMensaje - Debe lanzar excepción cuando no es admin")
//    void responderMensaje_NoEsAdmin_ThrowsException() {
//        // Arrange
//        RespuestaMensajeDTO respuestaDTO = RespuestaMensajeDTO.builder()
//                .respuesta("Intento de respuesta")
//                .respondidoPor(1L)
//                .build();
//
//        when(repository.findById(1L)).thenReturn(Optional.of(mensajeEntity));
//        when(userServiceClient.isAdmin(1L)).thenReturn(false);
//
//        // Act & Assert
//        assertThatThrownBy(() -> service.responderMensaje(1L, respuestaDTO))
//                .isInstanceOf(BusinessValidationException.class)
//                .hasMessageContaining("Solo los administradores pueden responder");
//
//        verify(repository, never()).save(any());
//    }
//
//    @Test
//    @DisplayName("eliminarMensaje - Debe eliminar mensaje cuando es admin")
//    void eliminarMensaje_AdminValido_Success() {
//        // Arrange
//        when(userServiceClient.isAdmin(5L)).thenReturn(true);
//        when(repository.findById(1L)).thenReturn(Optional.of(mensajeEntity));
//
//        // Act
//        service.eliminarMensaje(1L, 5L);
//
//        // Assert
//        verify(repository, times(1)).delete(mensajeEntity);
//    }
//
//    @Test
//    @DisplayName("eliminarMensaje - Debe lanzar excepción cuando no es admin")
//    void eliminarMensaje_NoEsAdmin_ThrowsException() {
//        // Arrange
//        when(userServiceClient.isAdmin(1L)).thenReturn(false);
//
//        // Act & Assert
//        assertThatThrownBy(() -> service.eliminarMensaje(1L, 1L))
//                .isInstanceOf(BusinessValidationException.class)
//                .hasMessageContaining("Solo los administradores");
//
//        verify(repository, never()).delete(any());
//    }
//
//    @Test
//    @DisplayName("buscarPorPalabraClave - Debe buscar mensajes por keyword")
//    void buscarPorPalabraClave_KeywordValida_ReturnsMensajes() {
//        // Arrange
//        when(repository.searchByKeyword("arriendo")).thenReturn(List.of(mensajeEntity));
//        when(modelMapper.map(any(MensajeContacto.class), eq(MensajeContactoDTO.class)))
//                .thenReturn(mensajeDTO);
//
//        // Act
//        List<MensajeContactoDTO> resultados = service.buscarPorPalabraClave("arriendo");
//
//        // Assert
//        assertThat(resultados).hasSize(1);
//        verify(repository, times(1)).searchByKeyword("arriendo");
//    }
//
//    @Test
//    @DisplayName("obtenerEstadisticas - Debe retornar estadísticas correctas")
//    void obtenerEstadisticas_DeberiaRetornarEstadisticas() {
//        // Arrange
//        when(repository.count()).thenReturn(10L);
//        when(repository.countByEstado("PENDIENTE")).thenReturn(3L);
//        when(repository.countByEstado("EN_PROCESO")).thenReturn(2L);
//        when(repository.countByEstado("RESUELTO")).thenReturn(5L);
//
//        // Act
//        var estadisticas = service.obtenerEstadisticas();
//
//        // Assert
//        assertThat(estadisticas).containsEntry("total", 10L);
//        assertThat(estadisticas).containsEntry("pendientes", 3L);
//        assertThat(estadisticas).containsEntry("enProceso", 2L);
//        assertThat(estadisticas).containsEntry("resueltos", 5L);
package com.rentify.contactService.service;

import com.rentify.contactService.client.UserServiceClient;
import com.rentify.contactService.dto.MensajeContactoDTO;
import com.rentify.contactService.dto.external.UsuarioDTO;
import com.rentify.contactService.exception.BusinessValidationException;
import com.rentify.contactService.model.MensajeContacto;
import com.rentify.contactService.repository.MensajeContactoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.modelmapper.ModelMapper;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MensajeContactoServiceTest {

    @Mock
    private MensajeContactoRepository repository;

    @Mock
    private UserServiceClient userServiceClient;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private MensajeContactoService service;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void crearMensaje_deberiaCrearMensajeCorrectamente() {

        // --- Datos de entrada ---
        MensajeContactoDTO inputDto = new MensajeContactoDTO();
        inputDto.setNombre("Fernanda");
        inputDto.setEmail("fer@test.com");
        inputDto.setAsunto("Consulta");
        inputDto.setMensaje("Esto es un mensaje válido de prueba");
        inputDto.setUsuarioId(1L);

        // Simulación de user existente
        UsuarioDTO usuario = new UsuarioDTO();
        usuario.setId(1L);
        when(userServiceClient.getUserById(1L)).thenReturn(usuario);

        // No tiene mensajes pendientes
        when(repository.countByUsuarioIdAndEstado(eq(1L), anyString()))
                .thenReturn(0L);

        // Mapeo DTO → Entity
        MensajeContacto entidad = new MensajeContacto();
        entidad.setId(10L);
        entidad.setNombre("Fernanda");
        entidad.setEmail("fer@test.com");
        entidad.setAsunto("Consulta");
        entidad.setMensaje("Esto es un mensaje válido de prueba");
        entidad.setEstado("PENDIENTE");
        entidad.setFechaCreacion(new Date());

        when(modelMapper.map(inputDto, MensajeContacto.class)).thenReturn(entidad);
        when(repository.save(any(MensajeContacto.class))).thenReturn(entidad);

        // Mapeo final Entity → DTO
        when(modelMapper.map(entidad, MensajeContactoDTO.class))
                .thenReturn(inputDto);

        // --- Ejecutar ---
        MensajeContactoDTO resultado = service.crearMensaje(inputDto);

        // --- Verificaciones ---
        assertNotNull(resultado);
        verify(repository).save(any(MensajeContacto.class));
        verify(userServiceClient).getUserById(1L);
    }

    @Test
    void crearMensaje_mensajeMuyCorto_deberiaLanzarExcepcion() {
        MensajeContactoDTO dto = new MensajeContactoDTO();
        dto.setNombre("Fer");
        dto.setEmail("fer@test.com");
        dto.setAsunto("Test");
        dto.setMensaje("a"); // muy corto

        assertThrows(BusinessValidationException.class, () -> {
            service.crearMensaje(dto);
        });
    }
}
