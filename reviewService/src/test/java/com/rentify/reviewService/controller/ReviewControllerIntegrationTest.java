package com.rentify.reviewService.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rentify.reviewService.client.PropertyServiceClient;
import com.rentify.reviewService.client.UserServiceClient;
import com.rentify.reviewService.dto.ReviewDTO;
import com.rentify.reviewService.dto.external.PropiedadDTO;
import com.rentify.reviewService.dto.external.UsuarioDTO;
import com.rentify.reviewService.model.TipoResena;
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
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Tests de Integración - ReviewController")
class ReviewControllerIntegrationTest {

    private static final String APP_CLIENT_HEADER = "X-App-Client";
    private static final String APP_CLIENT_KEY = "test-key-123";
    private static final String HEADER_USER = "X-Usuario-Id";
    private static final String HEADER_ROLE = "X-Rol-Id";
    private static final String ROL_USER = "2";
    private static final String USUARIO_AUTENTICADO_ID = "1";

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

    private MockHttpServletRequestBuilder withAppKey(MockHttpServletRequestBuilder builder) {
        return builder.header(APP_CLIENT_HEADER, APP_CLIENT_KEY);
    }

    @BeforeEach
    void setUp() {
        TipoResena tipo = new TipoResena();
        tipo.setNombre("RESENA_PROPIEDAD");
        tipoResenaGuardado = tipoResenaRepository.save(tipo);

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
        when(propertyServiceClient.isPropertyOwner(10L, 1L)).thenReturn(false);
        when(propertyServiceClient.getPropertyById(10L)).thenReturn(propiedadMock);
    }

    private ReviewDTO crearReviewBase() {
        return ReviewDTO.builder()
                .usuarioId(Long.valueOf(USUARIO_AUTENTICADO_ID))
                .propiedadId(10L)
                .puntaje(8)
                .comentario("Excelente propiedad, muy bien ubicada y en perfectas condiciones.")
                .tipoResenaId(tipoResenaGuardado.getId())
                .build();
    }

    @Test
    @DisplayName("POST /api/reviews - Crea reseña exitosamente (Datos Válidos)")
    void crearResena_DeberiaRetornar201_CuandoDatosYDependenciasSonValidos() throws Exception {
        ReviewDTO nuevaResena = crearReviewBase();

        mockMvc.perform(withAppKey(post("/api/reviews"))
                        .header(HEADER_USER, USUARIO_AUTENTICADO_ID)
                        .header(HEADER_ROLE, ROL_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nuevaResena)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.puntaje").value(8))
                .andExpect(jsonPath("$.estado").value("ACTIVA"));
    }

    @Test
    @DisplayName("POST /api/reviews - Falla (400) cuando el comentario es muy corto")
    void crearResena_DeberiaRetornar400_CuandoComentarioEsMuyCorto() throws Exception {
        ReviewDTO resenaInvalida = crearReviewBase();
        resenaInvalida.setComentario("Corto");

        mockMvc.perform(withAppKey(post("/api/reviews"))
                        .header(HEADER_USER, USUARIO_AUTENTICADO_ID)
                        .header(HEADER_ROLE, ROL_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resenaInvalida)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/reviews - Falla (400) si el puntaje está fuera de rango")
    void crearResena_PuntajeFueraDeRango_Retorna400() throws Exception {
        ReviewDTO resenaInvalida = crearReviewBase();
        resenaInvalida.setPuntaje(15);

        mockMvc.perform(withAppKey(post("/api/reviews"))
                        .header(HEADER_USER, USUARIO_AUTENTICADO_ID)
                        .header(HEADER_ROLE, ROL_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resenaInvalida)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/reviews - Falla (4xx) si el usuario intenta reseñar su propia propiedad")
    void crearResena_UsuarioEsDueno_RetornaError() throws Exception {
        ReviewDTO resenaInvalida = crearReviewBase();
        when(propertyServiceClient.isPropertyOwner(10L, 1L)).thenReturn(true);

        mockMvc.perform(withAppKey(post("/api/reviews"))
                        .header(HEADER_USER, USUARIO_AUTENTICADO_ID)
                        .header(HEADER_ROLE, ROL_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resenaInvalida)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("POST /api/reviews - Falla (4xx) si la propiedad no existe en PropertyService")
    void crearResena_PropiedadNoExiste_RetornaError() throws Exception {
        ReviewDTO resenaInvalida = crearReviewBase();
        resenaInvalida.setPropiedadId(99L);
        when(propertyServiceClient.existsProperty(99L)).thenReturn(false);

        mockMvc.perform(withAppKey(post("/api/reviews"))
                        .header(HEADER_USER, USUARIO_AUTENTICADO_ID)
                        .header(HEADER_ROLE, ROL_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resenaInvalida)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("POST /api/reviews - Falla (403) si falta X-App-Client")
    void crearResena_SinApiKey_Retorna403() throws Exception {
        ReviewDTO nuevaResena = crearReviewBase();

        mockMvc.perform(post("/api/reviews")
                        .header(HEADER_USER, USUARIO_AUTENTICADO_ID)
                        .header(HEADER_ROLE, ROL_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nuevaResena)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/reviews - Falla (401) si faltan cabeceras de identidad")
    void crearResena_SinCabeceras_Retorna401() throws Exception {
        ReviewDTO nuevaResena = crearReviewBase();

        mockMvc.perform(withAppKey(post("/api/reviews"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nuevaResena)))
                .andExpect(status().isUnauthorized());
    }
}