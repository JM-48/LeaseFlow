package com.rentify.reviewService.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rentify.reviewService.client.PropertyServiceClient;
import com.rentify.reviewService.client.UserServiceClient;
import com.rentify.reviewService.dto.ReviewDTO;
import com.rentify.reviewService.dto.external.PropiedadDTO;
import com.rentify.reviewService.dto.external.UsuarioDTO;
import com.rentify.reviewService.model.TipoResena;
import com.rentify.reviewService.repository.ReviewRepository;
import com.rentify.reviewService.repository.TipoResenaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReviewControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TipoResenaRepository tipoResenaRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @MockBean
    private PropertyServiceClient propertyServiceClient;

    @MockBean
    private UserServiceClient userServiceClient;

    private TipoResena tipoResenaGuardado;

    @BeforeEach
    void setUp() {
        // 1. Limpieza absoluta de la Base de Datos para evitar contaminación entre ejecuciones
        reviewRepository.deleteAll();
        tipoResenaRepository.deleteAll();

        // 2. Insertar la Foreign Key obligatoria en la base de datos en memoria (H2)
        TipoResena tipo = new TipoResena();
        tipo.setNombre("RESENA_PROPIEDAD");
        tipoResenaGuardado = tipoResenaRepository.save(tipo);

        // 3. Moldear comportamiento de UserServiceClient
        UsuarioDTO creadorMock = UsuarioDTO.builder()
                .id(1L)
                .pnombre("Juan")
                .papellido("Pérez")
                .email("juan.perez@email.com")
                .rol("ARRIENDATARIO") // Supera la validación Roles.puedeCrearResena()
                .estado("ACTIVO")
                .build();

        when(userServiceClient.getUserById(anyLong())).thenReturn(creadorMock);
        when(userServiceClient.existsUser(anyLong())).thenReturn(true);

        // 4. Moldear comportamiento de PropertyServiceClient
        PropiedadDTO propiedadMock = PropiedadDTO.builder()
                .id(10L)
                .codigo("PROP-001")
                .titulo("Departamento en Providencia")
                .precioMensual(650000L)
                .direccion("Av. Providencia 1234")
                .propietarioId(2L) // El dueño es el ID 2, por ende el creador (ID 1) no es el dueño
                .estado("DISPONIBLE")
                .build();

        when(propertyServiceClient.existsProperty(anyLong())).thenReturn(true);
        when(propertyServiceClient.isPropertyOwner(anyLong(), anyLong())).thenReturn(false);
        when(propertyServiceClient.getPropertyById(anyLong())).thenReturn(propiedadMock);
    }

    @Test
    @DisplayName("Debería crear una reseña exitosamente cuando los DTOs y mocks externos son válidos")
    void crearResena_DeberiaRetornar201_CuandoDatosYDependenciasSonValidos() throws Exception {
        // Arrange
        ReviewDTO nuevaResena = ReviewDTO.builder()
                .usuarioId(1L)
                .propiedadId(10L)
                .puntaje(8)
                .comentario("Excelente propiedad, muy bien ubicada y en perfectas condiciones.")
                .tipoResenaId(tipoResenaGuardado.getId())
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nuevaResena)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.puntaje").value(8))
                .andExpect(jsonPath("$.estado").value("ACTIVA"))
                .andExpect(jsonPath("$.comentario").value("Excelente propiedad, muy bien ubicada y en perfectas condiciones."))
                .andExpect(jsonPath("$.usuario.id").value(1))
                .andExpect(jsonPath("$.propiedad.id").value(10));
    }

    @Test
    @DisplayName("Debería retornar 400 cuando el comentario no cumple con el tamaño mínimo")
    void crearResena_DeberiaRetornar400_CuandoComentarioEsMuyCorto() throws Exception {
        // Arrange: Comentario con menos de 10 caracteres (rompe la regla de @Size en ReviewDTO)
        ReviewDTO resenaInvalida = ReviewDTO.builder()
                .usuarioId(1L)
                .propiedadId(10L)
                .puntaje(8)
                .comentario("Corto")
                .tipoResenaId(tipoResenaGuardado.getId())
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resenaInvalida)))
                .andExpect(status().isBadRequest());
    }
}