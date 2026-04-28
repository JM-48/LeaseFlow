package com.rentify.applicationService.service;

import com.rentify.applicationService.client.DocumentServiceClient;
import com.rentify.applicationService.client.PropertyServiceClient;
import com.rentify.applicationService.client.UserServiceClient;
import com.rentify.applicationService.dto.SolicitudArriendoDTO;
import com.rentify.applicationService.dto.UsuarioDTO;
import com.rentify.applicationService.exception.BusinessValidationException;
import com.rentify.applicationService.model.SolicitudArriendo;
import com.rentify.applicationService.repository.SolicitudArriendoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;

import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class SolicitudArriendoServiceTest {

    private SolicitudArriendoRepository repository;
    private UserServiceClient userClient;
    private PropertyServiceClient propertyClient;
    private DocumentServiceClient documentClient;
    private ModelMapper mapper;
    private SolicitudArriendoService service;

    @BeforeEach
    void setUp() {
        repository = mock(SolicitudArriendoRepository.class);
        userClient = mock(UserServiceClient.class);
        propertyClient = mock(PropertyServiceClient.class);
        documentClient = mock(DocumentServiceClient.class);

        // Crear un ModelMapper real
        mapper = new ModelMapper();

        service = new SolicitudArriendoService(
                repository, userClient, propertyClient, documentClient, mapper
        );
    }

    @Test
    void crearSolicitud_exito() {
        SolicitudArriendoDTO dto = new SolicitudArriendoDTO();
        dto.setUsuarioId(1L);
        dto.setPropiedadId(10L);

        // Mock de usuario
        UsuarioDTO usuario = new UsuarioDTO();
        usuario.setId(1L);
        usuario.setRolId(3); // ARRIENDATARIO
        when(userClient.getUserById(1L)).thenReturn(usuario);

        // 🔑 MOCK AÑADIDO (Solución del error "Propiedad no existe")
        // Simula que la propiedad SÍ existe. Esto es VITAL para que el servicio no lance la excepción.
        when(propertyClient.existsProperty(10L)).thenReturn(true);

        // Mock de propiedad disponible (Validación 6)
        when(propertyClient.isPropertyAvailable(10L)).thenReturn(true);

        // Mock de documentos aprobados
        when(documentClient.hasApprovedDocuments(1L)).thenReturn(true);

        // Mock del repositorio
        when(repository.save(any(SolicitudArriendo.class)))
                .thenAnswer(invocation -> {
                    SolicitudArriendo s = invocation.getArgument(0);
                    s.setId(100L); // asignar id simulado
                    return s;
                });

        SolicitudArriendoDTO result = service.crearSolicitud(dto);

        assertEquals(100L, result.getId());
        assertEquals(1L, result.getUsuarioId());
        assertEquals(10L, result.getPropiedadId());
        assertEquals("PENDIENTE", result.getEstado());

        verify(repository, times(1)).save(any());
    }

    @Test
    void crearSolicitud_usuarioNoExiste() {
        SolicitudArriendoDTO dto = new SolicitudArriendoDTO();
        dto.setUsuarioId(1L);
        dto.setPropiedadId(10L);

        when(userClient.getUserById(1L)).thenReturn(null);

        assertThrows(BusinessValidationException.class,
                () -> service.crearSolicitud(dto));
    }

    @Test
    void actualizarEstado_exito() {
        SolicitudArriendo solicitud = new SolicitudArriendo();
        solicitud.setId(1L);
        solicitud.setUsuarioId(1L);
        solicitud.setPropiedadId(10L);
        solicitud.setEstado("PENDIENTE");

        when(repository.findById(1L)).thenReturn(Optional.of(solicitud));
        when(repository.save(any())).thenReturn(solicitud);

        SolicitudArriendoDTO dto = service.actualizarEstado(1L, "ACEPTADA");

        assertEquals("ACEPTADA", dto.getEstado());
        verify(repository, times(1)).save(any());
    }
}
