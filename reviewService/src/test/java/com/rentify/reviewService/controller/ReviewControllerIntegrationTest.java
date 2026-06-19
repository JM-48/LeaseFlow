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
import org.springframework.transaction.annotation.Transactional;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional // Agregado: Limpia la BD automáticamente tras cada test
class ReviewControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TipoResenaRepository tipoResenaRepository;

    @MockBean
    private PropertyServiceClient propertyServiceClient;

    @MockBean
    private UserServiceClient userServiceClient;

    private TipoResena tipoResenaGuardado;

    @BeforeEach
    void setUp() {
        // 1. Insertar la Foreign Key obligatoria en la base de datos en memoria (H2)
        TipoResena tipo = new TipoResena();
        tipo.setNombre("RESENA_PROPIEDAD");
        tipoResenaGuardado = tipoResenaRepository.save(tipo);

        // 2. Moldear comportamiento base de UserServiceClient
        UsuarioDTO creadorMock = UsuarioDTO.builder()
                .id(1L)
                .pnombre("Juan")
                .papellido("Pérez")
                .email("juan.perez@email.com")
                .rol("ARRIENDATARIO")
                .estado("ACTIVO")
                .build();

        when(userServiceClient.getUserById(1L)).thenReturn(creadorMock);
        when(userServiceClient.existsUser(1L)).thenReturn(true);

        // 3. Moldear comportamiento base de PropertyServiceClient
        PropiedadDTO propiedadMock = PropiedadDTO.builder()
                .id(10L)
                .codigo("PROP-001")
                .titulo("Departamento en Providencia")
                .precioMensual(650000L)
                .direccion("Av. Providencia 1234")
                .propietarioId(2L)
                .estado("DISPONIBLE")
                .build();

        when(propertyServiceClient.existsProperty(10L)).thenReturn(true);
        // Primero Propiedad (10L), luego Usuario (1L)
        when(propertyServiceClient.isPropertyOwner(10L, 1L)).thenReturn(false);
        when(propertyServiceClient.getPropertyById(10L)).thenReturn(propiedadMock);
    }

    // ==========================================
    // 🛠️ MÉTODOS AUXILIARES (DRY)
    // ==========================================

    private ReviewDTO crearReviewBase() {
        return ReviewDTO.builder()
                .usuarioId(1L)
                .propiedadId(10L)
                .puntaje(8)
                .comentario("Excelente propiedad, muy bien ubicada y en perfectas condiciones.")
                .tipoResenaId(tipoResenaGuardado.getId())
                .build();
    }

    // ==========================================
    // 🟢 TESTS DE CAMINO FELIZ
    // ==========================================

    @Test
    @DisplayName("POST /api/reviews - Crea reseña exitosamente (Datos Válidos)")
    void crearResena_DeberiaRetornar201_CuandoDatosYDependenciasSonValidos() throws Exception {
        ReviewDTO nuevaResena = crearReviewBase();

        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nuevaResena)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.puntaje").value(8))
                .andExpect(jsonPath("$.estado").value("ACTIVA"))
                .andExpect(jsonPath("$.usuario.id").value(1))
                .andExpect(jsonPath("$.propiedad.id").value(10));
    }

    // ==========================================
    // 🔥 TESTS DE REGLAS DE NEGOCIO Y ERRORES
    // ==========================================

    @Test
    @DisplayName("POST /api/reviews - Falla (400) cuando el comentario es muy corto")
    void crearResena_DeberiaRetornar400_CuandoComentarioEsMuyCorto() throws Exception {
        ReviewDTO resenaInvalida = crearReviewBase();
        resenaInvalida.setComentario("Corto"); // Falla @Size

        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resenaInvalida)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/reviews - Falla (400) si el puntaje está fuera de rango")
    void crearResena_PuntajeFueraDeRango_Retorna400() throws Exception {
        ReviewDTO resenaInvalida = crearReviewBase();
        resenaInvalida.setPuntaje(15); // Asumiendo que tu máximo es 5 o 10

        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resenaInvalida)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/reviews - Falla (400/403) si el usuario intenta reseñar su propia propiedad")
    void crearResena_UsuarioEsDueno_RetornaError() throws Exception {
        ReviewDTO resenaInvalida = crearReviewBase();

        // Sobrescribimos el mock SÓLO para este test: ahora el usuario 1 SÍ es el dueño
        // Primero Propiedad (10L), luego Usuario (1L)
        when(propertyServiceClient.isPropertyOwner(10L, 1L)).thenReturn(true);

        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resenaInvalida)))
                .andExpect(status().is4xxClientError()); // isBadRequest o isForbidden
    }

    @Test
    @DisplayName("POST /api/reviews - Falla (404/400) si la propiedad no existe en PropertyService")
    void crearResena_PropiedadNoExiste_RetornaError() throws Exception {
        ReviewDTO resenaInvalida = crearReviewBase();
        resenaInvalida.setPropiedadId(99L); // ID Inexistente

        // Configuramos el mock para la propiedad 99L
        when(propertyServiceClient.existsProperty(99L)).thenReturn(false);

        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resenaInvalida)))
                .andExpect(status().is4xxClientError());
    }
}