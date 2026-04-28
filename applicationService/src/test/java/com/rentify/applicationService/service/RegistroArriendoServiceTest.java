package com.rentify.applicationService.service;

import com.rentify.applicationService.dto.RegistroArriendoDTO;
import com.rentify.applicationService.dto.SolicitudArriendoDTO;
import com.rentify.applicationService.exception.BusinessValidationException;
import com.rentify.applicationService.exception.ResourceNotFoundException;
import com.rentify.applicationService.model.RegistroArriendo;
import com.rentify.applicationService.model.SolicitudArriendo;
import com.rentify.applicationService.repository.RegistroArriendoRepository;
import com.rentify.applicationService.repository.SolicitudArriendoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para RegistroArriendoService
 * Valida toda la lógica de negocio del servicio
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests de RegistroArriendoService")
class RegistroArriendoServiceTest {

    @Mock
    private RegistroArriendoRepository repository;

    @Mock
    private SolicitudArriendoRepository solicitudRepository;

    @Mock
    private SolicitudArriendoService solicitudService;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private RegistroArriendoService service;

    private SolicitudArriendo solicitudAceptada;
    private RegistroArriendoDTO registroDTO;
    private RegistroArriendo registroEntity;

    @BeforeEach
    void setUp() {
        // Solicitud aceptada
        solicitudAceptada = new SolicitudArriendo();
        solicitudAceptada.setId(1L);
        solicitudAceptada.setUsuarioId(1L);
        solicitudAceptada.setPropiedadId(1L);
        solicitudAceptada.setEstado("ACEPTADA");
        solicitudAceptada.setFechaSolicitud(new Date());

        // DTO de registro
        Calendar cal = Calendar.getInstance();
        Date fechaInicio = cal.getTime();
        cal.add(Calendar.YEAR, 1);
        Date fechaFin = cal.getTime();

        registroDTO = RegistroArriendoDTO.builder()
                .solicitudId(1L)
                .fechaInicio(fechaInicio)
                .fechaFin(fechaFin)
                .montoMensual(500000.0)
                .build();

        // Entidad de registro
        registroEntity = new RegistroArriendo();
        registroEntity.setId(1L);
        registroEntity.setSolicitudId(1L);
        registroEntity.setFechaInicio(fechaInicio);
        registroEntity.setFechaFin(fechaFin);
        registroEntity.setMontoMensual(500000.0);
        registroEntity.setActivo(true);
    }

    @Test
    @DisplayName("Debe crear registro exitosamente cuando la solicitud está aceptada")
    void crearRegistro_SolicitudAceptada_Success() {
        // Arrange
        when(solicitudRepository.findById(1L)).thenReturn(Optional.of(solicitudAceptada));
        when(repository.findBySolicitudId(1L)).thenReturn(Collections.emptyList());
        when(modelMapper.map(any(RegistroArriendoDTO.class), eq(RegistroArriendo.class)))
                .thenReturn(registroEntity);
        when(repository.save(any(RegistroArriendo.class))).thenReturn(registroEntity);
        when(modelMapper.map(any(RegistroArriendo.class), eq(RegistroArriendoDTO.class)))
                .thenReturn(registroDTO);

        // Act
        RegistroArriendoDTO resultado = service.crearRegistro(registroDTO);

        // Assert
        assertThat(resultado).isNotNull();
        verify(repository, times(1)).save(any(RegistroArriendo.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando la solicitud no existe")
    void crearRegistro_SolicitudNoExiste_ThrowsException() {
        // Arrange
        when(solicitudRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> service.crearRegistro(registroDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Solicitud no encontrada con ID: 1");

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando la solicitud no está aceptada")
    void crearRegistro_SolicitudNoAceptada_ThrowsException() {
        // Arrange
        solicitudAceptada.setEstado("PENDIENTE");
        when(solicitudRepository.findById(1L)).thenReturn(Optional.of(solicitudAceptada));

        // Act & Assert
        assertThatThrownBy(() -> service.crearRegistro(registroDTO))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("Solo se pueden crear registros para solicitudes aceptadas");

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando ya existe un registro activo")
    void crearRegistro_RegistroActivoExiste_ThrowsException() {
        // Arrange
        RegistroArriendo registroActivo = new RegistroArriendo();
        registroActivo.setId(2L);
        registroActivo.setActivo(true);

        when(solicitudRepository.findById(1L)).thenReturn(Optional.of(solicitudAceptada));
        when(repository.findBySolicitudId(1L)).thenReturn(Arrays.asList(registroActivo));

        // Act & Assert
        assertThatThrownBy(() -> service.crearRegistro(registroDTO))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("Ya existe un registro activo");

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando las fechas son inválidas")
    void crearRegistro_FechasInvalidas_ThrowsException() {
        // Arrange
        Calendar cal = Calendar.getInstance();
        registroDTO.setFechaInicio(cal.getTime());
        cal.add(Calendar.YEAR, -1); // Fecha fin antes de fecha inicio
        registroDTO.setFechaFin(cal.getTime());

        when(solicitudRepository.findById(1L)).thenReturn(Optional.of(solicitudAceptada));
        when(repository.findBySolicitudId(1L)).thenReturn(Collections.emptyList());

        // Act & Assert
        assertThatThrownBy(() -> service.crearRegistro(registroDTO))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("fecha de inicio no puede ser posterior a la fecha de fin");

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Debe crear registro sin fecha fin")
    void crearRegistro_SinFechaFin_Success() {
        // Arrange
        registroDTO.setFechaFin(null);
        when(solicitudRepository.findById(1L)).thenReturn(Optional.of(solicitudAceptada));
        when(repository.findBySolicitudId(1L)).thenReturn(Collections.emptyList());
        when(modelMapper.map(any(RegistroArriendoDTO.class), eq(RegistroArriendo.class)))
                .thenReturn(registroEntity);
        when(repository.save(any(RegistroArriendo.class))).thenReturn(registroEntity);
        when(modelMapper.map(any(RegistroArriendo.class), eq(RegistroArriendoDTO.class)))
                .thenReturn(registroDTO);

        // Act
        RegistroArriendoDTO resultado = service.crearRegistro(registroDTO);

        // Assert
        assertThat(resultado).isNotNull();
        verify(repository, times(1)).save(any(RegistroArriendo.class));
    }

    @Test
    @DisplayName("Debe listar todos los registros")
    void listarTodos_RetornaListado() {
        // Arrange
        RegistroArriendo registro2 = new RegistroArriendo();
        registro2.setId(2L);
        when(repository.findAll()).thenReturn(Arrays.asList(registroEntity, registro2));
        when(modelMapper.map(any(RegistroArriendo.class), eq(RegistroArriendoDTO.class)))
                .thenReturn(registroDTO);

        // Act
        List<RegistroArriendoDTO> resultado = service.listarTodos(false);

        // Assert
        assertThat(resultado).hasSize(2);
        verify(repository, times(1)).findAll();
    }

    @Test
    @DisplayName("Debe obtener registro por ID")
    void obtenerPorId_RegistroExiste_RetornaRegistro() {
        // Arrange
        when(repository.findById(1L)).thenReturn(Optional.of(registroEntity));
        when(modelMapper.map(any(RegistroArriendo.class), eq(RegistroArriendoDTO.class)))
                .thenReturn(registroDTO);

        // Act
        RegistroArriendoDTO resultado = service.obtenerPorId(1L, false);

        // Assert
        assertThat(resultado).isNotNull();
        verify(repository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando registro no existe")
    void obtenerPorId_RegistroNoExiste_ThrowsException() {
        // Arrange
        when(repository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> service.obtenerPorId(1L, false))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Registro no encontrado con ID: 1");
    }

    @Test
    @DisplayName("Debe obtener registros por solicitud")
    void obtenerPorSolicitud_RetornaListado() {
        // Arrange
        when(repository.findBySolicitudId(1L)).thenReturn(Arrays.asList(registroEntity));
        when(modelMapper.map(any(RegistroArriendo.class), eq(RegistroArriendoDTO.class)))
                .thenReturn(registroDTO);

        // Act
        List<RegistroArriendoDTO> resultado = service.obtenerPorSolicitud(1L);

        // Assert
        assertThat(resultado).hasSize(1);
        verify(repository, times(1)).findBySolicitudId(1L);
    }

    @Test
    @DisplayName("Debe finalizar registro exitosamente")
    void finalizarRegistro_RegistroActivo_Success() {
        // Arrange
        when(repository.findById(1L)).thenReturn(Optional.of(registroEntity));
        when(repository.save(any(RegistroArriendo.class))).thenReturn(registroEntity);
        when(modelMapper.map(any(RegistroArriendo.class), eq(RegistroArriendoDTO.class)))
                .thenReturn(registroDTO);

        // Act
        RegistroArriendoDTO resultado = service.finalizarRegistro(1L);

        // Assert
        assertThat(resultado).isNotNull();
        verify(repository, times(1)).save(any(RegistroArriendo.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando registro ya está inactivo")
    void finalizarRegistro_RegistroInactivo_ThrowsException() {
        // Arrange
        registroEntity.setActivo(false);
        when(repository.findById(1L)).thenReturn(Optional.of(registroEntity));

        // Act & Assert
        assertThatThrownBy(() -> service.finalizarRegistro(1L))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("El registro ya está inactivo");

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción al finalizar registro que no existe")
    void finalizarRegistro_RegistroNoExiste_ThrowsException() {
        // Arrange
        when(repository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> service.finalizarRegistro(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Registro no encontrado con ID: 1");
    }

    @Test
    @DisplayName("Debe permitir crear múltiples registros si los anteriores están inactivos")
    void crearRegistro_RegistrosInactivosExisten_Success() {
        // Arrange
        RegistroArriendo registroInactivo = new RegistroArriendo();
        registroInactivo.setId(2L);
        registroInactivo.setActivo(false);

        when(solicitudRepository.findById(1L)).thenReturn(Optional.of(solicitudAceptada));
        when(repository.findBySolicitudId(1L)).thenReturn(Arrays.asList(registroInactivo));
        when(modelMapper.map(any(RegistroArriendoDTO.class), eq(RegistroArriendo.class)))
                .thenReturn(registroEntity);
        when(repository.save(any(RegistroArriendo.class))).thenReturn(registroEntity);
        when(modelMapper.map(any(RegistroArriendo.class), eq(RegistroArriendoDTO.class)))
                .thenReturn(registroDTO);

        // Act
        RegistroArriendoDTO resultado = service.crearRegistro(registroDTO);

        // Assert
        assertThat(resultado).isNotNull();
        verify(repository, times(1)).save(any(RegistroArriendo.class));
    }

    @Test
    @DisplayName("Debe incluir detalles de solicitud cuando includeDetails es true")
    void obtenerPorId_ConDetalles_IncluyeSolicitud() {
        // Arrange
        SolicitudArriendoDTO solicitudDTO = new SolicitudArriendoDTO();
        solicitudDTO.setId(1L);

        when(repository.findById(1L)).thenReturn(Optional.of(registroEntity));
        when(modelMapper.map(any(RegistroArriendo.class), eq(RegistroArriendoDTO.class)))
                .thenReturn(registroDTO);
        when(solicitudService.obtenerPorId(1L, true)).thenReturn(solicitudDTO);

        // Act
        RegistroArriendoDTO resultado = service.obtenerPorId(1L, true);

        // Assert
        assertThat(resultado).isNotNull();
        verify(solicitudService, times(1)).obtenerPorId(1L, true);
    }
}