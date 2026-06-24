package com.rentify.propertyservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rentify.propertyservice.dto.ComunaDTO;
import com.rentify.propertyservice.dto.PropertyDTO;
import com.rentify.propertyservice.dto.RegionDTO;
import com.rentify.propertyservice.dto.TipoDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false) // Desactiva filtros básicos por defecto de Spring Security
@ActiveProfiles("test")
@Transactional
@DisplayName("Tests de Integración - PropertyService (Robustos)")
class PropertyServiceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // Headers de seguridad inyectados por el Gateway
    private static final String HEADER_USER = "X-Usuario-Id";
    private static final String HEADER_ROLE = "X-Rol-Id";

    // Roles y Usuarios simulados para la integración
    private static final String ROL_ADMIN = "1";
    private static final String ROL_USER = "2";
    private static final String ADMIN_ID = "999";
    private static final String PROPIETARIO_ID = "99";

    // ==========================================
    // 🛠️ MÉTODOS AUXILIARES (DRY) - Ejecutados por ADMIN
    // ==========================================

    private Long crearRegion(String nombre) throws Exception {
        RegionDTO regionDTO = new RegionDTO();
        regionDTO.setNombre(nombre);

        MvcResult result = mockMvc.perform(post("/api/regiones")
                        .header(HEADER_USER, ADMIN_ID)
                        .header(HEADER_ROLE, ROL_ADMIN) // BLINDADO: Solo Admin puede crear
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(regionDTO)))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readValue(result.getResponse().getContentAsString(), RegionDTO.class).getId();
    }

    private Long crearComuna(String nombre, Long regionId) throws Exception {
        ComunaDTO comunaDTO = new ComunaDTO();
        comunaDTO.setNombre(nombre);
        comunaDTO.setRegionId(regionId);

        MvcResult result = mockMvc.perform(post("/api/comunas")
                        .header(HEADER_USER, ADMIN_ID)
                        .header(HEADER_ROLE, ROL_ADMIN) // BLINDADO: Solo Admin puede crear
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(comunaDTO)))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readValue(result.getResponse().getContentAsString(), ComunaDTO.class).getId();
    }

    private Long crearTipo(String nombre) throws Exception {
        TipoDTO tipoDTO = new TipoDTO();
        tipoDTO.setNombre(nombre);

        MvcResult result = mockMvc.perform(post("/api/tipos")
                        .header(HEADER_USER, ADMIN_ID)
                        .header(HEADER_ROLE, ROL_ADMIN) // BLINDADO: Solo Admin puede crear
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tipoDTO)))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readValue(result.getResponse().getContentAsString(), TipoDTO.class).getId();
    }

    // ==========================================
    // 🟢 TESTS DE CAMINO FELIZ
    // ==========================================

    @Test
    @DisplayName("Flujo Maestro: Crear Región -> Comuna -> Tipo -> Propiedad -> Búsqueda")
    void flujoCompleto_CrearPropiedadYBuscar() throws Exception {
        // Los helpers ya llevan los headers de ADMIN incorporados
        Long regionId = crearRegion("Región Metropolitana");
        Long comunaId = crearComuna("Providencia", regionId);
        Long tipoId = crearTipo("Departamento");

        // 4. Crear la Propiedad (Lo hace el usuario Propietario)
        PropertyDTO propiedadDTO = PropertyDTO.builder()
                .codigo("DP001")
                .titulo("Hermoso depto en arriendo")
                .precioMensual(new BigDecimal("650000"))
                .divisa("CLP")
                .m2(new BigDecimal("65.5"))
                .nHabit(2)
                .nBanos(2)
                .petFriendly(true)
                .direccion("Av. Providencia 1234")
                .tipoId(tipoId)
                .comunaId(comunaId)
                .propietarioId(Long.valueOf(PROPIETARIO_ID)) // Match con el header
                .build();

        MvcResult resultPropiedad = mockMvc.perform(post("/api/propiedades")
                        .header(HEADER_USER, PROPIETARIO_ID)
                        .header(HEADER_ROLE, ROL_USER) // Un usuario normal crea su propiedad
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(propiedadDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigo").value("DP001"))
                .andExpect(jsonPath("$.nHabit").value(2))
                .andReturn();

        Long propiedadId = objectMapper.readValue(resultPropiedad.getResponse().getContentAsString(), PropertyDTO.class).getId();

        // 5. Probar el endpoint de Búsqueda (Cualquier usuario puede buscar)
        mockMvc.perform(get("/api/propiedades/buscar")
                        .header(HEADER_USER, "123") // Usuario random buscando
                        .header(HEADER_ROLE, ROL_USER)
                        .param("comunaId", comunaId.toString())
                        .param("petFriendly", "true")
                        .param("minPrecio", "600000")
                        .param("maxPrecio", "700000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(propiedadId))
                .andExpect(jsonPath("$[0].titulo").value("Hermoso depto en arriendo"));
    }

    // ==========================================
    // 🔥 TESTS DE REGLAS DE NEGOCIO Y ERRORES
    // ==========================================

    @Test
    @DisplayName("POST /api/propiedades - Falla (400) si las validaciones del DTO no se cumplen")
    void crearPropiedad_ValidacionesFallan_Retorna400() throws Exception {
        PropertyDTO propiedadInvalida = PropertyDTO.builder()
                .codigo("CODIGO_DEMASIADO_LARGO_PARA_LA_VALIDACION")
                .titulo("Test")
                .precioMensual(new BigDecimal("-100"))
                .divisa("ARS")
                .m2(new BigDecimal("0.5"))
                .nHabit(-1)
                .nBanos(30)
                .propietarioId(Long.valueOf(PROPIETARIO_ID))
                .build();

        mockMvc.perform(post("/api/propiedades")
                        .header(HEADER_USER, PROPIETARIO_ID)
                        .header(HEADER_ROLE, ROL_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(propiedadInvalida)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Error"));
    }

    @Test
    @DisplayName("POST /api/propiedades - Falla si la Comuna no existe en BD")
    void crearPropiedad_ComunaInexistente_RetornaError() throws Exception {
        Long tipoId = crearTipo("Casa"); // Esto lo hace el ADMIN internamente

        PropertyDTO propiedadInvalida = PropertyDTO.builder()
                .codigo("CS001")
                .titulo("Casa en comuna fantasma")
                .precioMensual(new BigDecimal("500000"))
                .divisa("CLP")
                .m2(new BigDecimal("100.0"))
                .nHabit(3)
                .nBanos(2)
                .petFriendly(false)
                .direccion("Calle Falsa 123")
                .tipoId(tipoId)
                .comunaId(99999L) // Comuna inexistente
                .propietarioId(Long.valueOf(PROPIETARIO_ID))
                .build();

        mockMvc.perform(post("/api/propiedades")
                        .header(HEADER_USER, PROPIETARIO_ID)
                        .header(HEADER_ROLE, ROL_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(propiedadInvalida)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("POST /api/propiedades - Falla si el Tipo de Propiedad no existe en BD")
    void crearPropiedad_TipoInexistente_RetornaError() throws Exception {
        Long regionId = crearRegion("Valparaíso");
        Long comunaId = crearComuna("Viña del Mar", regionId);

        PropertyDTO propiedadInvalida = PropertyDTO.builder()
                .codigo("XX001")
                .titulo("Propiedad de tipo OVNI")
                .precioMensual(new BigDecimal("500000"))
                .divisa("CLP")
                .m2(new BigDecimal("100.0"))
                .nHabit(3)
                .nBanos(2)
                .petFriendly(false)
                .direccion("Av. Libertad 456")
                .tipoId(99999L) // Tipo inexistente
                .comunaId(comunaId)
                .propietarioId(Long.valueOf(PROPIETARIO_ID))
                .build();

        mockMvc.perform(post("/api/propiedades")
                        .header(HEADER_USER, PROPIETARIO_ID)
                        .header(HEADER_ROLE, ROL_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(propiedadInvalida)))
                .andExpect(status().is4xxClientError());
    }
}